package com.jeda.android.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeda.android.data.remote.BackendConfig
import com.jeda.android.data.remote.BackendRepository
import kotlinx.coroutines.launch

@Composable
fun MindfulAiCard(
    backendConfig: BackendConfig,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var aiMessage by remember {
        mutableStateOf("Pause for a moment. What are you looking to feel right now?")
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x3306B6D4), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF06B6D4))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MINDFUL AI COACH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color(0xFF06B6D4)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x2206B6D4))
                        .clickable(enabled = !isLoading) {
                            isLoading = true
                            scope.launch {
                                aiMessage = BackendRepository.getCognitiveMessage(
                                    riskLevel = "HIGH",
                                    config = backendConfig
                                )
                                isLoading = false
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            color = Color(0xFF06B6D4),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "REFRESH",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF06B6D4)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "\"$aiMessage\"",
                fontSize = 15.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AiPromptChip("Urge to scroll") {
                    isLoading = true
                    scope.launch {
                        aiMessage = BackendRepository.getCognitiveMessage("ELEVATED", backendConfig)
                        isLoading = false
                    }
                }
                AiPromptChip("Reset focus") {
                    isLoading = true
                    scope.launch {
                        aiMessage = BackendRepository.getCognitiveMessage("HIGH", backendConfig)
                        isLoading = false
                    }
                }
            }
        }
    }
}

@Composable
private fun AiPromptChip(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(Color(0xFF1E293B))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color(0xFF94A3B8)
        )
    }
}
