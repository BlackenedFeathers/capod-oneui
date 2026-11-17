package eu.darken.capod.main.core.advanced

/**
 * CAPod One UI Edition: advanced functions that can be switched on and off individually from
 * Settings > Advanced features. All of them default to enabled.
 *
 * Only functions with a clean runtime off-switch are listed. The other formerly Pro-gated functions
 * (multiple device profiles, press controls, themes, device controls, widgets, the quick settings
 * tile) are always available in the FOSS build of this fork: they only do something once the user
 * sets them up, so a second switch would add nothing.
 */
enum class AdvancedFeature(val storageKey: String) {
    /** Overlay card shown on case open / connection. */
    POPUP("advanced.feature.popup.enabled"),

    /** Ear detection reactions: auto play, auto pause, start music on wear. */
    AUTO_PLAY_PAUSE("advanced.feature.autoplaypause.enabled"),

    /** Conversation awareness reaction (pause / lower volume while speaking). */
    CONVERSATION("advanced.feature.conversation.enabled"),

    /** "Fully charged" and "case battery low" notifications. */
    BATTERY_ALERTS("advanced.feature.batteryalerts.enabled"),
}
