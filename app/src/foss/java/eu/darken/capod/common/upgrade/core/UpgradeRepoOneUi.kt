package eu.darken.capod.common.upgrade.core

import eu.darken.capod.common.upgrade.UpgradeRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CAPod One UI Edition (personal fork, FOSS flavor only).
 *
 * Decorates [UpgradeRepoFoss] so the app-wide [UpgradeRepo] always reports the advanced
 * functions as available. CAPod is GPLv3 and the FOSS flavor's upgrade state is a local,
 * honour-based supporter record (no purchase, no licence server), so this changes a default in
 * free software and does not fake or bypass any purchase. The Google Play flavor and its billing
 * code are not touched.
 *
 * [UpgradeRepoFoss] itself is unchanged and still backs the supporter screen
 * (Settings > supporter status), which keeps showing the real sponsor state and the link to
 * sponsor the original developer.
 *
 * Individual functions can still be switched off by the user: see
 * [eu.darken.capod.main.core.advanced.AdvancedFeaturesSettings].
 */
@Singleton
class UpgradeRepoOneUi @Inject constructor(
    private val base: UpgradeRepoFoss,
) : UpgradeRepo {

    override val storeSite: String get() = base.storeSite
    override val upgradeSite: String get() = base.upgradeSite
    override val betaSite: String get() = base.betaSite

    override val upgradeInfo: Flow<UpgradeRepo.Info> = base.upgradeInfo
        .map { supporter -> Info(upgradedAt = supporter.upgradedAt) }

    override suspend fun refresh() = base.refresh()

    data class Info(
        /** When the user recorded themselves as a supporter of the original project, if ever. */
        override val upgradedAt: Instant? = null,
    ) : UpgradeRepo.Info {
        override val type: UpgradeRepo.Type = UpgradeRepo.Type.FOSS
        override val isPro: Boolean = true
        override val isSettled: Boolean = true
        override val error: Throwable? = null
    }
}
