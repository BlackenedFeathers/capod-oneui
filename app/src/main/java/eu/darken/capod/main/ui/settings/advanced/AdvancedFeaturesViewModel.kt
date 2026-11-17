package eu.darken.capod.main.ui.settings.advanced

import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.capod.common.coroutine.DispatcherProvider
import eu.darken.capod.common.debug.logging.Logging.Priority.INFO
import eu.darken.capod.common.debug.logging.log
import eu.darken.capod.common.debug.logging.logTag
import eu.darken.capod.common.uix.ViewModel4
import eu.darken.capod.main.core.advanced.AdvancedFeature
import eu.darken.capod.main.core.advanced.AdvancedFeaturesSettings
import eu.darken.capod.reaction.ui.popup.PopUpWindow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** CAPod One UI Edition: backs the "Advanced features" settings screen. */
@HiltViewModel
class AdvancedFeaturesViewModel @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val settings: AdvancedFeaturesSettings,
    private val popUpWindow: PopUpWindow,
) : ViewModel4(dispatcherProvider) {

    data class State(
        val disabled: Set<AdvancedFeature> = emptySet(),
    ) {
        fun isEnabled(feature: AdvancedFeature): Boolean = feature !in disabled
    }

    val state = settings.disabled
        .map { State(disabled = it) }
        .asLiveState()

    fun setEnabled(feature: AdvancedFeature, enabled: Boolean) = launch {
        log(TAG, INFO) { "setEnabled($feature, $enabled)" }
        settings.setEnabled(feature, enabled)
        if (feature == AdvancedFeature.POPUP && !enabled) {
            // A card that is on screen right now must not outlive its switch. The window is
            // main-thread only.
            withContext(dispatcherProvider.Main) { popUpWindow.close() }
        }
    }

    companion object {
        private val TAG = logTag("Settings", "Advanced", "VM")
    }
}
