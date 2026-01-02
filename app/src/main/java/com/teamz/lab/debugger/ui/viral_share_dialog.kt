package com.teamz.lab.debugger.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import com.teamz.lab.debugger.utils.AnalyticsEvent
import com.teamz.lab.debugger.utils.AnalyticsUtils
import com.teamz.lab.debugger.utils.ReferralManager
import com.teamz.lab.debugger.R
import com.teamz.lab.debugger.utils.string

/**
 * Viral Share Dialog - Makes sharing easy and tracks viral growth
 * Provides quick share buttons for popular platforms
 */
@Composable
fun ViralShareDialog(
    onDismiss: () -> Unit,
    context: Context,
    shareText: String = "",
    showReferralCode: Boolean = true,
    powerData: com.teamz.lab.debugger.utils.PowerConsumptionUtils.PowerConsumptionSummary? = null,
    aggregatedStats: com.teamz.lab.debugger.utils.PowerConsumptionAggregator.PowerStats? = null
) {
    val referralCode = remember { ReferralManager.getOrCreateReferralCode(context) }
    val referralLink = remember { ReferralManager.getShortReferralLink(context) }
    val referralCount = remember { ReferralManager.getReferralCount(context) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with gradient background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                        .padding(top = 32.dp, bottom = 24.dp, start = 24.dp, end = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Icon with background circle
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "Share",
                                    modifier = Modifier.size(36.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            "Share with Friends! 🚀",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Help your friends discover their device health and track your referrals",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = TextUnit(20f, TextUnitType.Sp)
                        )
                    }
                }
                
                // Referral stats
                if (showReferralCode && referralCount > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "$referralCount",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Friends Referred",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.People,
                                        contentDescription = "Referrals",
                                        modifier = Modifier.size(28.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Social share buttons section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Text(
                        "Share via",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                
                // Generate power insights text if power data is available
                val powerInsightsText = remember(powerData, aggregatedStats) {
                    if (powerData != null && aggregatedStats != null) {
                        generatePowerShareText(powerData, aggregatedStats, context)
                    } else {
                        ""
                    }
                }
                
                val finalShareText = if (powerInsightsText.isNotEmpty()) {
                    if (shareText.isNotEmpty()) "$shareText\n\n$powerInsightsText" else powerInsightsText
                } else {
                    shareText
                }
                
                    // WhatsApp
                    ShareButton(
                        icon = Icons.Default.Chat,
                        text = "WhatsApp",
                        containerColor = Color(0xFF25D366), // WhatsApp green
                        contentColor = Color.White,
                        onClick = {
                            shareToWhatsApp(context, finalShareText, referralLink, referralCode)
                            onDismiss()
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Telegram
                    ShareButton(
                        icon = Icons.Default.Send,
                        text = "Telegram",
                        containerColor = Color(0xFF0088CC), // Telegram blue
                        contentColor = Color.White,
                        onClick = {
                            shareToTelegram(context, finalShareText, referralLink, referralCode)
                            onDismiss()
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // SMS
                    ShareButton(
                        icon = Icons.Default.Message,
                        text = "SMS",
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        onClick = {
                            shareToSMS(context, finalShareText, referralLink, referralCode)
                            onDismiss()
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Email
                    ShareButton(
                        icon = Icons.Default.Email,
                        text = "Email",
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary,
                        onClick = {
                            shareToEmail(context, finalShareText, referralLink, referralCode)
                            onDismiss()
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Generic share
                    ShareButton(
                        icon = Icons.Default.Share,
                        text = "More Options",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = {
                            ReferralManager.shareReferralLink(context, finalShareText)
                            onDismiss()
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Referral code display
                if (showReferralCode) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Your Referral Code",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 24.dp, vertical = 12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        referralCode,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        letterSpacing = TextUnit(2f, TextUnitType.Sp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "Track how many friends you've helped!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Close button
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        LocalContext.current.string(R.string.close),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ShareButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = text,
                modifier = Modifier.size(24.dp),
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}

private fun shareToWhatsApp(context: Context, shareText: String, referralLink: String, referralCode: String) {
    val defaultText = """
        🔍 Check out this amazing device health checker app!
        
        📱 Get detailed insights about your phone's performance, battery, storage, and security.
        
        Use my referral code: $referralCode
        
        Download now: $referralLink
        
        #PhoneHealth #DeviceChecker
    """.trimIndent()
    
    val finalText = if (shareText.isNotEmpty()) "$shareText\n\n$defaultText" else defaultText
    
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            setPackage("com.whatsapp")
            putExtra(Intent.EXTRA_TEXT, finalText)
        }
        context.startActivity(intent)
        AnalyticsUtils.logEvent(AnalyticsEvent.ShareToWhatsApp, mapOf("referral_code" to referralCode))
    } catch (e: Exception) {
        // Fallback to generic share
        ReferralManager.shareReferralLink(context, finalText)
    }
}

private fun shareToTelegram(context: Context, shareText: String, referralLink: String, referralCode: String) {
    val defaultText = """
        🔍 Check out this amazing device health checker app!
        
        📱 Get detailed insights about your phone's performance, battery, storage, and security.
        
        Use my referral code: $referralCode
        
        Download now: $referralLink
    """.trimIndent()
    
    val finalText = if (shareText.isNotEmpty()) "$shareText\n\n$defaultText" else defaultText
    
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            setPackage("org.telegram.messenger")
            putExtra(Intent.EXTRA_TEXT, finalText)
        }
        context.startActivity(intent)
        AnalyticsUtils.logEvent(AnalyticsEvent.ShareToTelegram, mapOf("referral_code" to referralCode))
    } catch (e: Exception) {
        ReferralManager.shareReferralLink(context, finalText)
    }
}

private fun shareToSMS(context: Context, shareText: String, referralLink: String, referralCode: String) {
    val defaultText = """
        Check out this device health app! Use my code: $referralCode
        $referralLink
    """.trimIndent()
    
    val finalText = if (shareText.isNotEmpty()) "$shareText\n\n$defaultText" else defaultText
    
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("smsto:")
        putExtra("sms_body", finalText)
    }
    
    try {
        context.startActivity(intent)
        AnalyticsUtils.logEvent(AnalyticsEvent.ShareToSMS, mapOf("referral_code" to referralCode))
    } catch (e: Exception) {
        ReferralManager.shareReferralLink(context, finalText)
    }
}

private fun shareToEmail(context: Context, shareText: String, referralLink: String, referralCode: String) {
    val defaultText = """
        Check out this amazing device health checker app!
        
        Get detailed insights about your phone's performance, battery, storage, and security.
        
        Use my referral code: $referralCode
        
        Download now: $referralLink
    """.trimIndent()
    
    val finalText = if (shareText.isNotEmpty()) "$shareText\n\n$defaultText" else defaultText
    
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Check out this amazing device health app!")
        putExtra(Intent.EXTRA_TEXT, finalText)
    }
    
    try {
        context.startActivity(Intent.createChooser(intent, "Share via Email"))
        AnalyticsUtils.logEvent(AnalyticsEvent.ShareToEmail, mapOf("referral_code" to referralCode))
    } catch (e: Exception) {
        ReferralManager.shareReferralLink(context, finalText)
    }
}

private fun generatePowerShareText(
    powerData: com.teamz.lab.debugger.utils.PowerConsumptionUtils.PowerConsumptionSummary,
    aggregatedStats: com.teamz.lab.debugger.utils.PowerConsumptionAggregator.PowerStats,
    context: Context
): String {
    val appName = try {
        val applicationInfo = context.packageManager.getApplicationInfo(context.packageName, 0)
        context.packageManager.getApplicationLabel(applicationInfo).toString()
    } catch (e: Exception) {
        "DeviceGPT"
    }
    
    val topConsumers = powerData.components
        .sortedByDescending { it.powerConsumption }
        .take(3)
    
    return buildString {
        appendLine("⚡ Power Consumption Insights")
        appendLine()
        appendLine("📊 Total Power: ${String.format("%.1f", powerData.totalPower / 1000)}W")
        appendLine("📈 Trend: ${aggregatedStats.powerTrend.name.lowercase().replaceFirstChar { it.uppercase() }}")
        appendLine("📉 Average: ${String.format("%.1f", aggregatedStats.averagePower / 1000)}W")
        appendLine("🔝 Peak: ${String.format("%.1f", aggregatedStats.peakPower / 1000)}W")
        appendLine()
        
        if (topConsumers.isNotEmpty()) {
            appendLine("🔝 Top Power Consumers:")
            topConsumers.forEachIndexed { index, component ->
                appendLine("${index + 1}. ${component.component}: ${String.format("%.1f", component.powerConsumption / 1000)}W")
            }
            appendLine()
        }
        
        appendLine("📱 Generated by $appName")
        appendLine("🔗 Download: https://play.google.com/store/apps/details?id=${context.packageName}")
    }
}

