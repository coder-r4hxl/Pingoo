package com.pingoo.app.ui.alarm

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pingoo.app.domain.model.Language
import com.pingoo.app.service.LocationTrackingService
import com.pingoo.app.ui.i18n.Translations
import com.pingoo.app.ui.theme.PingooTheme

class AlarmActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Allow Activity to display on top of screen lock and turn screen on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        val destName = intent.getStringExtra(EXTRA_DEST_NAME) ?: "Destination"
        val langCode = intent.getStringExtra(EXTRA_LANGUAGE) ?: "en"
        val language = Language.fromCode(langCode)
        val strings = Translations.get(language)

        setContent {
            PingooTheme {
                AlarmScreen(
                    destinationName = destName,
                    strings = strings,
                    onSnooze = {
                        val snoozeIntent = Intent(this, LocationTrackingService::class.java).apply {
                            action = LocationTrackingService.ACTION_SNOOZE_ALARM
                        }
                        startService(snoozeIntent)
                        finish()
                    },
                    onDismiss = {
                        val dismissIntent = Intent(this, LocationTrackingService::class.java).apply {
                            action = LocationTrackingService.ACTION_DISMISS_ALARM
                        }
                        startService(dismissIntent)
                        finish()
                    }
                )
            }
        }
    }

    companion object {
        const val EXTRA_DEST_NAME = "extra_dest_name"
        const val EXTRA_LANGUAGE = "extra_language"
    }
}

@Composable
fun AlarmScreen(
    destinationName: String,
    strings: com.pingoo.app.ui.i18n.AppStrings,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.NotificationsActive,
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.onErrorContainer
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = strings.alarmArrivalHeader,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = destinationName,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = strings.alarmArrivedMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(
                onClick = onSnooze,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Text(strings.alarmSnoozeButton)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Icon(Icons.Default.AlarmOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.alarmDismissButton)
            }
        }
    }
}
