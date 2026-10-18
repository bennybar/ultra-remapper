package com.bennybar.ultraremapper

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.os.IBinder

/**
 * Target for Samsung's `setting_action_button_press`; the system starts it on every Action button event.
 * ACTION_KEY_EVENT: 0=down, 1=up, 2=single click, 3=long press, 5=multi press (ACTION_KEY_COUNT).
 */
class ActionButtonService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val gesture = when (intent?.getIntExtra("ACTION_KEY_EVENT", -1)) {
            2 -> "single"
            3 -> "long"
            5 -> if (intent.getIntExtra("ACTION_KEY_COUNT", 0) == 2) "double" else null
            else -> null
        }
        if (gesture != null) {
            if (Mapping.get(this, gesture) == "original") {
                // Hand the event to Samsung's handler exactly as the system would have.
                startService(Intent(intent).setComponent(SAMSUNG_HANDLER))
            } else {
                Mapping.perform(this, gesture)
            }
        }
        stopSelf(startId)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        val SAMSUNG_HANDLER = ComponentName(
            "com.samsung.android.wear.shealth",
            "com.samsung.android.wear.shealth.service.ActionButtonService",
        )
    }
}
