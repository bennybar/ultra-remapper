package com.bennybar.ultraremapper

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.view.KeyEvent

/** Gesture → action assignments. An action is a media/volume id from [actions] or "app:<package>". */
object Mapping {
    val actionGestures = listOf("single" to "Single press", "double" to "Double press", "long" to "Long press")
    val bottomGestures = listOf("bottom_long" to "Long press")
    val gestures = actionGestures + bottomGestures

    val actions = listOf(
        "none" to "Nothing",
        "original" to "Original (Samsung)",
        "play_pause" to "Play / pause",
        "next" to "Next track",
        "previous" to "Previous track",
        "vol_up" to "Volume up",
        "vol_down" to "Volume down",
    )

    private val defaults = mapOf(
        "single" to "app:com.limegreenv.geartracker",
        "double" to "next",
        "long" to "play_pause",
        "bottom_long" to "app:com.google.android.apps.walletnfcrel",
    )

    private fun prefs(context: Context) = context.getSharedPreferences("mapping", Context.MODE_PRIVATE)

    fun get(context: Context, gesture: String): String =
        prefs(context).getString(gesture, defaults[gesture]) ?: "none"

    fun set(context: Context, gesture: String, action: String) =
        prefs(context).edit().putString(gesture, action).apply()

    /** Apps in which the bottom button is left completely to the system (e.g. Samsung Health workouts). */
    fun passThroughApps(context: Context): Set<String> =
        prefs(context).getStringSet("passthrough", setOf("com.samsung.android.wear.shealth"))!!.toSet()

    fun setPassThroughApps(context: Context, apps: Set<String>) =
        prefs(context).edit().putStringSet("passthrough", apps).apply()

    fun label(context: Context, action: String): String {
        if (!action.startsWith("app:")) return actions.toMap()[action] ?: action
        val pkg = action.removePrefix("app:")
        return try {
            context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(pkg, 0)).toString()
        } catch (e: Exception) {
            pkg
        }
    }

    /** [backToHome]: Back from a launched app returns to the watch face instead of the previous task. */
    fun perform(context: Context, gesture: String, backToHome: Boolean = false) {
        val action = get(context, gesture)
        val audio = context.getSystemService(AudioManager::class.java)
        when (action) {
            "play_pause" -> sendMediaKey(audio, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            "next" -> sendMediaKey(audio, KeyEvent.KEYCODE_MEDIA_NEXT)
            "previous" -> sendMediaKey(audio, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
            "vol_up" -> audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
            "vol_down" -> audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
            else -> if (action.startsWith("app:")) {
                context.packageManager.getLaunchIntentForPackage(action.removePrefix("app:"))
                    ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    ?.apply { if (backToHome) addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME) }
                    ?.let { context.startActivity(it) }
            }
        }
    }

    private fun sendMediaKey(audio: AudioManager, code: Int) {
        audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, code))
        audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, code))
    }
}
