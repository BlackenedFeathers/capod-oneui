package eu.darken.capod.reaction.ui.popup

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.BatteryChargingFull
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.darken.capod.R
import eu.darken.capod.common.SystemTimeSource
import eu.darken.capod.common.compose.Preview2
import eu.darken.capod.common.compose.PreviewWrapper
import eu.darken.capod.common.compose.preview.MockPodDataProvider
import eu.darken.capod.main.ui.overview.cards.components.SignalIndicator
import eu.darken.capod.monitor.core.PodDevice
import eu.darken.capod.monitor.core.cachedBatteryFormatted
import eu.darken.capod.pods.core.apple.PodModel
import eu.darken.capod.pods.core.apple.ble.batteryProgress
import eu.darken.capod.pods.core.apple.ble.formatBatteryPercent
import eu.darken.capod.pods.core.apple.ble.isKnownBattery

// CAPod One UI Edition: the card keeps one fixed dark palette, whatever theme the app is set to,
// so it looks the same over any app it is drawn on top of.
private val OneUiCard = Color(0xFF1C1C1E)
private val OneUiCardRaised = Color(0xFF2C2C2E)
private val OneUiBarTrack = Color(0xFF3A3A3C)
private val OneUiBlue = Color(0xFF8EC5FF)
private val OneUiTextPrimary = Color(0xFFFAFAFA)
private val OneUiTextSecondary = Color(0xFFA9A9AE)

private val OneUiPopUpColors = darkColorScheme(
    primary = OneUiBlue,
    onPrimary = Color(0xFF0B1B2B),
    surface = OneUiCard,
    onSurface = OneUiTextPrimary,
    surfaceVariant = OneUiCardRaised,
    onSurfaceVariant = OneUiTextSecondary,
)

/** Shown instead of a percentage when the level is not known. Never a made-up number. */
private const val UNKNOWN_LEVEL = "—"

/** Values that come from the cache instead of a live reading are drawn at this opacity. */
private const val STALE_ALPHA = 0.45f

@Composable
fun PopUpContent(
    device: PodDevice,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val cachedLabel = if (device.isBatteryCached) {
        val now = remember(device) { SystemTimeSource.now() }
        device.cachedBatteryFormatted(now).takeIf { it.isNotEmpty() }
    } else {
        null
    }

    MaterialTheme(
        colorScheme = OneUiPopUpColors,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
    ) {
        Box(modifier = modifier.padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 16.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = OneUiCard,
                    contentColor = OneUiTextPrimary,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 22.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Header: label + signal indicator (sits right after the text)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = device.getLabel(context),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = OneUiTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        SignalIndicator(
                            signalQuality = device.rssiQuality,
                            isLive = device.isLive,
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }

                    // Only present when at least one value is not a live reading.
                    if (cachedLabel != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.battery_cached_label, cachedLabel),
                            style = MaterialTheme.typography.bodySmall,
                            color = OneUiTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Device-specific content
                    when {
                        device.hasDualPods -> DualPodContent(device)
                        device.model != PodModel.UNKNOWN -> SinglePodContent(device)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Close button
                    Button(
                        onClick = onClose,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OneUiCardRaised,
                            contentColor = OneUiBlue,
                        ),
                    ) {
                        Text(
                            text = stringResource(R.string.general_close_action),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DualPodContent(device: PodDevice) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top,
    ) {
        // Left pod
        BatteryColumn(
            iconRes = device.leftPodIcon,
            label = stringResource(R.string.popup_content_left_label),
            batteryPercent = device.batteryLeft,
            isCharging = device.isLeftPodCharging ?: false,
            isStale = device.isLeftBatteryCached,
            modifier = Modifier.weight(1f),
        )

        // Case (only if device has one)
        if (device.hasCase) {
            BatteryColumn(
                iconRes = device.caseIcon,
                label = stringResource(R.string.popup_content_case_label),
                batteryPercent = device.batteryCase,
                isCharging = device.isCaseCharging ?: false,
                isStale = device.isCaseBatteryCached,
                modifier = Modifier.weight(1f),
            )
        }

        // Right pod
        BatteryColumn(
            iconRes = device.rightPodIcon,
            label = stringResource(R.string.popup_content_right_label),
            batteryPercent = device.batteryRight,
            isCharging = device.isRightPodCharging ?: false,
            isStale = device.isRightBatteryCached,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SinglePodContent(device: PodDevice) {
    BatteryColumn(
        iconRes = device.iconRes,
        label = null,
        batteryPercent = device.batteryHeadset,
        isCharging = device.isHeadsetBeingCharged ?: false,
        isStale = device.isHeadsetBatteryCached,
        modifier = Modifier.fillMaxWidth(0.5f),
    )
}

/**
 * One component of the device: the original artwork of the model, its name, the level as text and
 * as a bar. The artwork is a density-bucketed drawable, so Android picks the sharpest variant the
 * screen can use.
 */
@Composable
private fun BatteryColumn(
    iconRes: Int,
    label: String?,
    batteryPercent: Float,
    isCharging: Boolean,
    isStale: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val isKnown = isKnownBattery(batteryPercent)

    Column(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .alpha(if (isStale) STALE_ALPHA else 1f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            contentScale = ContentScale.Fit,
        )

        if (label != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = OneUiTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (isCharging) {
                Icon(
                    imageVector = Icons.TwoTone.BatteryChargingFull,
                    contentDescription = stringResource(R.string.pods_charging_label),
                    modifier = Modifier.size(16.dp),
                    tint = OneUiBlue,
                )
                Spacer(modifier = Modifier.width(2.dp))
            }
            Text(
                text = if (isKnown) formatBatteryPercent(context, batteryPercent) else UNKNOWN_LEVEL,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = OneUiTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        BatteryBar(progress = batteryProgress(batteryPercent))
    }
}

/** Light blue level bar. Fills up from empty when the card appears and glides to new values. */
@Composable
private fun BatteryBar(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val target = progress.coerceIn(0f, 1f)
    val fill = remember { Animatable(0f) }
    LaunchedEffect(target) {
        fill.animateTo(
            targetValue = target,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(CircleShape)
            .background(OneUiBarTrack),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fill.value.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(CircleShape)
                .background(OneUiBlue),
        )
    }
}

@Preview2
@Composable
private fun PopUpContentDualPodPreview() = PreviewWrapper {
    PopUpContent(device = MockPodDataProvider.dualPodMonitoredMixed(), onClose = {})
}

@Preview2
@Composable
private fun PopUpContentSinglePodPreview() = PreviewWrapper {
    PopUpContent(device = MockPodDataProvider.singlePodMonitored(), onClose = {})
}
