#!/usr/bin/env bash
# CAPod One UI Edition: runs a Gradle invocation and, if it fails, turns the compiler output into
# GitHub annotations and a readable job summary so the cause is visible without digging through
# the raw log.
#
# Usage: .github/scripts/run-gradle.sh "<step title>" <gradle arguments...>
set -uo pipefail

title="$1"
shift

log_dir="${RUNNER_TEMP:-/tmp}/gradle-logs"
mkdir -p "$log_dir"
log_file="$log_dir/$(echo "$title" | tr -c 'A-Za-z0-9' '_').log"

echo "::group::./gradlew $*"
./gradlew "$@" --stacktrace --console=plain 2>&1 | tee "$log_file"
status=${PIPESTATUS[0]}
echo "::endgroup::"

summary="${GITHUB_STEP_SUMMARY:-/dev/null}"

if [ "$status" -eq 0 ]; then
    echo "### ✅ $title" >> "$summary"
    exit 0
fi

workspace="${GITHUB_WORKSPACE:-$PWD}"
kotlin_error='^e: file://([^:]+):([0-9]+):([0-9]+) (.*)$'
errors_file="$log_dir/errors.txt"

# Kotlin/KSP errors start with "e: ", javac errors look like "File.java:12: error: ...".
grep -E '^e: |\.java:[0-9]+: error: ' "$log_file" | sort -u | head -n 60 > "$errors_file" || true

while IFS= read -r line; do
    if [[ "$line" =~ $kotlin_error ]]; then
        file="${BASH_REMATCH[1]#"$workspace"/}"
        echo "::error file=${file},line=${BASH_REMATCH[2]},col=${BASH_REMATCH[3]}::${BASH_REMATCH[4]}"
    else
        echo "::error::${line}"
    fi
done < "$errors_file"

{
    echo "### ❌ $title failed (exit code $status)"
    echo
    if [ -s "$errors_file" ]; then
        echo "**Compiler errors**"
        echo
        echo '```'
        sed "s#file://$workspace/##" "$errors_file"
        echo '```'
    fi
    echo
    echo "**What went wrong (Gradle)**"
    echo
    echo '```'
    # The block between "* What went wrong:" and "* Try:" is Gradle's own failure description.
    awk '/^\* What went wrong:/{p=1} /^\* Try:/{p=0} p' "$log_file" | head -n 60
    echo '```'
    echo
    echo "The full log is attached to this run as the \`build-logs\` artifact."
} >> "$summary"

echo "::error::$title failed. See the job summary for the extracted errors."
exit "$status"
