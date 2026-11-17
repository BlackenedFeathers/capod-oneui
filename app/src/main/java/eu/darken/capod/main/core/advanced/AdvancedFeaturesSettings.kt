package eu.darken.capod.main.core.advanced

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.capod.common.coroutine.AppScope
import eu.darken.capod.common.debug.logging.Logging.Priority.INFO
import eu.darken.capod.common.debug.logging.Logging.Priority.WARN
import eu.darken.capod.common.debug.logging.asLog
import eu.darken.capod.common.debug.logging.log
import eu.darken.capod.common.debug.logging.logTag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CAPod One UI Edition: persistent on/off preferences for the [AdvancedFeature]s.
 *
 * Stored in its own DataStore file. A missing key means "enabled", so every advanced feature is on
 * after a fresh install. The current values are mirrored into [AdvancedFeatureSwitches] for the
 * synchronous call sites.
 */
@Singleton
class AdvancedFeaturesSettings @Inject constructor(
    @ApplicationContext private val context: Context,
    @AppScope private val appScope: CoroutineScope,
) {

    private val Context.dataStore by preferencesDataStore(name = "settings_advanced_features")

    private val dataStore: DataStore<Preferences> get() = context.dataStore

    /** Features the user switched off. */
    val disabled: Flow<Set<AdvancedFeature>> = dataStore.data
        .map { prefs ->
            val off = mutableSetOf<AdvancedFeature>()
            for (feature in AdvancedFeature.values()) {
                if (prefs[booleanPreferencesKey(feature.storageKey)] == false) off.add(feature)
            }
            off.toSet()
        }
        .catch { e ->
            // A failed read must not take reactions or widgets down with it: fall back to defaults.
            log(TAG, WARN) { "Failed to read advanced feature settings: ${e.asLog()}" }
            emit(emptySet())
        }
        .distinctUntilChanged()

    init {
        disabled
            .onEach {
                log(TAG, INFO) { "Disabled advanced features: $it" }
                AdvancedFeatureSwitches.publish(it)
            }
            .launchIn(appScope)
    }

    suspend fun setEnabled(feature: AdvancedFeature, enabled: Boolean) {
        log(TAG, INFO) { "setEnabled($feature, $enabled)" }
        dataStore.edit { prefs ->
            prefs[booleanPreferencesKey(feature.storageKey)] = enabled
        }
    }

    companion object {
        private val TAG = logTag("Advanced", "Features", "Settings")
    }
}
