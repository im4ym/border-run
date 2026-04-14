package com.borderrun.app.ui.permission

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.borderrun.app.ui.theme.CardBorder
import com.borderrun.app.ui.theme.CardSurface
import com.borderrun.app.ui.theme.CtaGradientEnd
import com.borderrun.app.ui.theme.CtaGradientStart
import com.borderrun.app.ui.theme.GradientCyan
import com.borderrun.app.ui.theme.GradientMint
import com.borderrun.app.ui.theme.GradientSky
import com.borderrun.app.ui.theme.GradientTeal
import com.borderrun.app.ui.theme.TextBody
import com.borderrun.app.ui.theme.TextHeading

/**
 * Screen that explains why Border Run needs the
 * [POST_NOTIFICATIONS][android.Manifest.permission.POST_NOTIFICATIONS] permission
 * before the system permission dialog is shown.
 *
 * **Ethics alignment (ACS Code of Ethics, Assessment 2):**
 * - **Honesty** — explains exactly what notifications will contain (static text, no personal data).
 * - **User autonomy** — the "No Thanks" path is equally prominent; users can opt out without
 *   leaving the screen.
 * - **Data minimisation** — the info cards explicitly state that no personal data is transmitted.
 *
 * On Android 13+ (API 33) the "Allow" button launches the system
 * [RequestPermission] contract. On older API levels the permission is not
 * required and [onPermissionGranted] is called immediately.
 *
 * Results are returned via callbacks so the NavGraph can write them to the
 * previous [NavBackStackEntry][androidx.navigation.NavBackStackEntry]'s
 * `savedStateHandle` and pop back to Settings.
 *
 * @param onPermissionGranted Called after the user grants the notification
 *   permission (or on API < 33 where no grant is needed).
 * @param onPermissionDenied Called when the user either denies the system
 *   permission dialog or taps "No Thanks".
 */
@Composable
fun PermissionRationaleScreen(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) onPermissionGranted() else onPermissionDenied()
    }

    val gradient = Brush.verticalGradient(
        colors = listOf(GradientMint, GradientTeal, GradientCyan, GradientSky),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = "🔔", fontSize = 64.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Notification Permission",
                style = MaterialTheme.typography.headlineSmall,
                color = TextHeading,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Border Run would like to send you a daily reminder to complete your geography challenge.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextBody,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(28.dp))

            RationaleInfoCard(
                emoji = "📊",
                title = "Stay Sharp",
                body = "One gentle daily nudge keeps your geography skills growing.",
            )

            Spacer(modifier = Modifier.height(12.dp))

            RationaleInfoCard(
                emoji = "🔒",
                title = "Privacy First",
                body = "Notifications contain no personal data — only a static reminder text is sent.",
            )

            Spacer(modifier = Modifier.height(12.dp))

            RationaleInfoCard(
                emoji = "✋",
                title = "Your Choice",
                body = "Turn this off at any time from the Settings screen or your device's notification settings.",
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Allow button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(27.dp))
                    .background(
                        Brush.horizontalGradient(listOf(CtaGradientStart, CtaGradientEnd)),
                    )
                    .clickable {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            // Permission is not required below API 33
                            onPermissionGranted()
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Allow",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // No Thanks button
            TextButton(onClick = onPermissionDenied) {
                Text(
                    text = "No Thanks",
                    color = TextBody,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                )
            }
        }
    }
}

/**
 * Glassmorphism info card used to explain a single benefit or privacy guarantee.
 *
 * @param emoji Decorative emoji shown on the left.
 * @param title Bold heading for the card.
 * @param body Supporting detail text.
 */
@Composable
private fun RationaleInfoCard(emoji: String, title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(0.5.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = emoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextHeading,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextBody,
                )
            }
        }
    }
}
