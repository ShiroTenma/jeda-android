package com.jeda.android.ui.rules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RulesScreen(modifier: Modifier = Modifier) {
    var protectTwitter by remember { mutableStateOf(true) }
    var protectInstagram by remember { mutableStateOf(true) }
    var protectTikTok by remember { mutableStateOf(true) }
    var protectBrave by remember { mutableStateOf(true) }
    var protectChrome by remember { mutableStateOf(true) }
    var protectFirefox by remember { mutableStateOf(true) }

    var strictnessLevel by remember { mutableStateOf("Strict") }
    var nightModeActive by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "Protection Rules",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Customize monitored apps and intervention strictness",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF94A3B8)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Monitored Apps Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Monitored Apps",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Behavioral doomscrolling & visual relapse triggers are shielded here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(16.dp))

                AppToggleRow("X / Twitter", "com.twitter.android", protectTwitter) { protectTwitter = it }
                AppToggleRow("Instagram", "com.instagram.android", protectInstagram) { protectInstagram = it }
                AppToggleRow("TikTok", "com.zhiliaoapp.musically", protectTikTok) { protectTikTok = it }
                AppToggleRow("Brave Browser", "com.brave.browser", protectBrave) { protectBrave = it }
                AppToggleRow("Google Chrome", "com.android.chrome", protectChrome) { protectChrome = it }
                AppToggleRow("Mozilla Firefox", "org.mozilla.firefox", protectFirefox) { protectFirefox = it }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Strictness Card (Commercial Freemium Feature)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Shield Strictness",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "PRO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF59E0B),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x22F59E0B))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Strict: Auto-exits to home screen and blocks interactions with a 60s hard pause overlay.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Night Owl Multiplier
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1E293B))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Night Owl Shield", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text("1.5x stricter thresholds between 23:00 - 04:00", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    }
                    Switch(
                        checked = nightModeActive,
                        onCheckedChange = { nightModeActive = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF10B981))
                    )
                }
            }
        }
    }
}

@Composable
private fun AppToggleRow(
    appName: String,
    pkg: String,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(appName, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.White)
            Text(pkg, fontSize = 11.sp, color = Color(0xFF64748B))
        }
        Switch(
            checked = enabled,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF10B981),
                uncheckedThumbColor = Color(0xFF94A3B8),
                uncheckedTrackColor = Color(0xFF334155)
            )
        )
    }
}
