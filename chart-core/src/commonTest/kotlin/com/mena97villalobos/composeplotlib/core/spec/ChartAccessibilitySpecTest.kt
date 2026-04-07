package com.mena97villalobos.composeplotlib.core.spec

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChartAccessibilitySpecTest {
    @Test
    fun defaults_requestFocusForAccessibility_isFalse() {
        assertFalse(ChartAccessibilitySpec().requestFocusForAccessibility)
    }

    @Test
    fun requestFocus_canBeEnabled() {
        assertTrue(
            ChartAccessibilitySpec(requestFocusForAccessibility = true).requestFocusForAccessibility,
        )
    }

    @Test
    fun defaults_keyboardViewportNudgeDisabled() {
        assertFalse(ChartAccessibilitySpec().keyboardViewportNudgeEnabled)
    }

    @Test
    fun defaults_selectionLiveRegionPolite() {
        assertEquals(ChartLiveRegionMode.Polite, ChartAccessibilitySpec().selectionLiveRegionMode)
    }

    @Test
    fun defaults_requestFocusOnMarker_isFalse() {
        assertFalse(ChartAccessibilitySpec().requestFocusOnMarker)
    }

    @Test
    fun defaults_keyboardSelectionRoamDisabled() {
        assertFalse(ChartAccessibilitySpec().keyboardSelectionRoamEnabled)
    }
}
