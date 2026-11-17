package eu.darken.capod.main.ui.settings.advanced

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.ArrowBack
import androidx.compose.material.icons.automirrored.twotone.Message
import androidx.compose.material.icons.twotone.BatteryAlert
import androidx.compose.material.icons.twotone.PlayCircle
import androidx.compose.material.icons.twotone.RecordVoiceOver
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.darken.capod.R
import eu.darken.capod.common.compose.Preview2
import eu.darken.capod.common.compose.PreviewWrapper
import eu.darken.capod.common.error.ErrorEventHandler
import eu.darken.capod.common.navigation.NavigationEventHandler
import eu.darken.capod.common.settings.SettingsCategoryHeader
import eu.darken.capod.common.settings.SettingsInfoBox
import eu.darken.capod.common.settings.SettingsSwitchItem
import eu.darken.capod.main.core.advanced.AdvancedFeature

@Composable
fun AdvancedFeaturesScreenHost(vm: AdvancedFeaturesViewModel = hiltViewModel()) {
    ErrorEventHandler(vm)
    NavigationEventHandler(vm)

    val state by vm.state.collectAsStateWithLifecycle(initialValue = null)
    state?.let {
        AdvancedFeaturesScreen(
            state = it,
            onNavigateUp = { vm.navUp() },
            onFeatureChanged = { feature, enabled -> vm.setEnabled(feature, enabled) },
        )
    }
}

private data class FeatureRow(
    val feature: AdvancedFeature,
    val icon: ImageVector,
    val titleRes: Int,
    val descriptionRes: Int,
)

private val windowRows = listOf(
    FeatureRow(
        feature = AdvancedFeature.POPUP,
        icon = Icons.AutoMirrored.TwoTone.Message,
        titleRes = R.string.settings_advanced_popup_label,
        descriptionRes = R.string.settings_advanced_popup_description,
    ),
)

private val reactionRows = listOf(
    FeatureRow(
        feature = AdvancedFeature.AUTO_PLAY_PAUSE,
        icon = Icons.TwoTone.PlayCircle,
        titleRes = R.string.settings_advanced_autoplaypause_label,
        descriptionRes = R.string.settings_advanced_autoplaypause_description,
    ),
    FeatureRow(
        feature = AdvancedFeature.CONVERSATION,
        icon = Icons.TwoTone.RecordVoiceOver,
        titleRes = R.string.settings_advanced_conversation_label,
        descriptionRes = R.string.settings_advanced_conversation_description,
    ),
    FeatureRow(
        feature = AdvancedFeature.BATTERY_ALERTS,
        icon = Icons.TwoTone.BatteryAlert,
        titleRes = R.string.settings_advanced_batteryalerts_label,
        descriptionRes = R.string.settings_advanced_batteryalerts_description,
    ),
)

@Composable
fun AdvancedFeaturesScreen(
    state: AdvancedFeaturesViewModel.State,
    onNavigateUp: () -> Unit,
    onFeatureChanged: (AdvancedFeature, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.settings_advanced_label))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.TwoTone.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding,
        ) {
            item {
                SettingsInfoBox(text = stringResource(R.string.settings_advanced_intro))
            }

            item {
                SettingsCategoryHeader(text = stringResource(R.string.settings_advanced_category_window_label))
            }
            windowRows.forEach { row ->
                item { FeatureSwitch(row = row, state = state, onFeatureChanged = onFeatureChanged) }
            }

            item {
                SettingsCategoryHeader(text = stringResource(R.string.settings_advanced_category_reactions_label))
            }
            reactionRows.forEach { row ->
                item { FeatureSwitch(row = row, state = state, onFeatureChanged = onFeatureChanged) }
            }

            item {
                SettingsInfoBox(text = stringResource(R.string.settings_advanced_always_on_note))
            }
        }
    }
}

@Composable
private fun FeatureSwitch(
    row: FeatureRow,
    state: AdvancedFeaturesViewModel.State,
    onFeatureChanged: (AdvancedFeature, Boolean) -> Unit,
) {
    SettingsSwitchItem(
        icon = row.icon,
        title = stringResource(row.titleRes),
        subtitle = stringResource(row.descriptionRes),
        checked = state.isEnabled(row.feature),
        onCheckedChange = { enabled -> onFeatureChanged(row.feature, enabled) },
    )
}

@Preview2
@Composable
private fun AdvancedFeaturesScreenPreview() = PreviewWrapper {
    AdvancedFeaturesScreen(
        state = AdvancedFeaturesViewModel.State(disabled = setOf(AdvancedFeature.CONVERSATION)),
        onNavigateUp = {},
        onFeatureChanged = { _, _ -> },
    )
}
