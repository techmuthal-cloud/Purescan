package com.example.ui.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Central HapticsManager object providing subtle, consistent physical feedback
 * for camera capture, successful ingredient analysis, and error/warning states,
 * following the ScanSafe.AI / PureScan design guidelines.
 */
object HapticsManager {

    @Volatile
    var isEnabled: Boolean = true

    /**
     * Crisp, subtle single shutter pulse when capturing a product label or sample.
     */
    fun triggerCameraCapture(context: Context, view: View? = null) {
        if (!isEnabled) return
        runCatching {
            view?.performHapticFeedback(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    HapticFeedbackConstants.CONFIRM
                } else {
                    HapticFeedbackConstants.KEYBOARD_TAP
                }
            )
            val vibrator = resolveVibrator(context) ?: return
            if (!vibrator.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(18L, 140))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(18L)
            }
        }
    }

    /**
     * Gentle two-step confirmation pulse when ingredient analysis completes successfully.
     */
    fun triggerAnalysisSuccess(context: Context, view: View? = null) {
        if (!isEnabled) return
        runCatching {
            view?.performHapticFeedback(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    HapticFeedbackConstants.CONFIRM
                } else {
                    HapticFeedbackConstants.VIRTUAL_KEY
                }
            )
            val vibrator = resolveVibrator(context) ?: return
            if (!vibrator.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0L, 16L, 65L, 24L)
                val amplitudes = intArrayOf(0, 110, 0, 165)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0L, 16L, 65L, 24L), -1)
            }
        }
    }

    /**
     * Distinct double-tap alert pulse for error states, unreadable OCR labels, or validation errors.
     */
    fun triggerErrorState(context: Context, view: View? = null) {
        if (!isEnabled) return
        runCatching {
            view?.performHapticFeedback(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    HapticFeedbackConstants.REJECT
                } else {
                    HapticFeedbackConstants.LONG_PRESS
                }
            )
            val vibrator = resolveVibrator(context) ?: return
            if (!vibrator.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0L, 28L, 55L, 38L)
                val amplitudes = intArrayOf(0, 185, 0, 215)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0L, 28L, 55L, 38L), -1)
            }
        }
    }

    /**
     * Ultra-light selection tick for filter chips and mode switches.
     */
    fun triggerSelectionTap(context: Context, view: View? = null) {
        if (!isEnabled) return
        runCatching {
            view?.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            val vibrator = resolveVibrator(context) ?: return
            if (!vibrator.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(10L, 85))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(10L)
            }
        }
    }

    private fun resolveVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}
