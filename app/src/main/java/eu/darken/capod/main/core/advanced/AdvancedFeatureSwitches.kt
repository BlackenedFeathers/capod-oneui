package eu.darken.capod.main.core.advanced

import eu.darken.capod.profiles.core.ReactionConfig
import eu.darken.capod.reaction.core.conversation.ConversationAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Process-wide, in-memory view of the persisted [AdvancedFeaturesSettings].
 *
 * Deliberately a plain object instead of an injected dependency: the off-switches are read at a
 * handful of low-level call sites (reaction config mapping, popup window, widgets, tile) and this
 * keeps every existing constructor signature of the original CAPod classes untouched.
 *
 * Until [AdvancedFeaturesSettings] has loaded the stored preferences (a few milliseconds after
 * process start) every feature reports as enabled, which is also the default.
 */
object AdvancedFeatureSwitches {

    private val disabledState = MutableStateFlow<Set<AdvancedFeature>>(emptySet())

    /** The set of features the user switched off. Empty means everything is enabled. */
    val disabled: StateFlow<Set<AdvancedFeature>> = disabledState

    fun isEnabled(feature: AdvancedFeature): Boolean = feature !in disabledState.value

    fun enabledFlow(feature: AdvancedFeature): Flow<Boolean> = disabledState
        .map { feature !in it }
        .distinctUntilChanged()

    internal fun publish(disabledFeatures: Set<AdvancedFeature>) {
        disabledState.value = disabledFeatures
    }

    /**
     * Applies the off-switches to a profile's reaction config. A switched-off feature behaves as if
     * its per-device toggles were off; the stored profile itself is never modified, so switching
     * the feature back on restores the previous per-device configuration.
     */
    fun mask(config: ReactionConfig): ReactionConfig {
        val off = disabledState.value
        if (off.isEmpty()) return config
        var result = config
        if (AdvancedFeature.POPUP in off) {
            result = result.copy(showPopUpOnCaseOpen = false, showPopUpOnConnection = false)
        }
        if (AdvancedFeature.AUTO_PLAY_PAUSE in off) {
            result = result.copy(autoPlay = false, autoPause = false, startMusicOnWear = false)
        }
        if (AdvancedFeature.CONVERSATION in off) {
            result = result.copy(conversationAction = ConversationAction.NOTHING)
        }
        if (AdvancedFeature.BATTERY_ALERTS in off) {
            result = result.copy(notifyWhenCharged = false, notifyWhenCaseLow = false)
        }
        return result
    }
}
