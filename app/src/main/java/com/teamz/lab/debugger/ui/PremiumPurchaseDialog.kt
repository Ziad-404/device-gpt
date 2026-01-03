package com.teamz.lab.debugger.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.teamz.lab.debugger.utils.RevenueCatManager
import kotlinx.coroutines.launch

/**
 * Premium Purchase Dialog
 * 
 * A user-friendly dialog to encourage users to remove ads by subscribing to premium.
 * Focuses on free ad removal as the main benefit.
 * 
 * Usage:
 * ```
 * var showPremiumDialog by remember { mutableStateOf(false) }
 * 
 * if (showPremiumDialog) {
 *     PremiumPurchaseDialog(
 *         onDismiss = { showPremiumDialog = false },
 *         activity = activity
 *     )
 * }
 * ```
 */
@Composable
fun PremiumPurchaseDialog(
    onDismiss: () -> Unit,
    activity: android.app.Activity,
    title: String = "Remove Ads Forever",
    subtitle: String = "Enjoy an ad-free experience and support the app",
    benefits: List<String> = listOf(
        "✅ No ads - ever",
        "✅ Faster app experience",
        "✅ Support development",
        "✅ Cancel anytime"
    )
) {
    var isPurchasing by remember { mutableStateOf(false) }
    var purchaseError by remember { mutableStateOf<String?>(null) }
    var purchaseSuccess by remember { mutableStateOf(false) }
    val premiumStatus by RevenueCatManager.premiumStatusFlow.collectAsState()
    val scope = rememberCoroutineScope()
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Success state
                if (purchaseSuccess || premiumStatus is RevenueCatManager.PremiumStatus.Premium) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Welcome to Premium!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "All ads have been removed. Enjoy your ad-free experience!",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Got it!")
                        }
                    }
                } else {
                    // Purchase flow
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Benefits list
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        benefits.forEach { benefit ->
                            Text(
                                text = benefit,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    
                    // Error message
                    purchaseError?.let { error ->
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            enabled = !isPurchasing
                        ) {
                            Text("Maybe Later")
                        }
                        
                        Button(
                            onClick = {
                                isPurchasing = true
                                purchaseError = null
                                scope.launch {
                                    RevenueCatManager.purchasePremium(
                                        activity = activity,
                                        onSuccess = {
                                            isPurchasing = false
                                            purchaseSuccess = true
                                        },
                                        onError = { error ->
                                            isPurchasing = false
                                            purchaseError = error
                                        }
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isPurchasing
                        ) {
                            if (isPurchasing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Remove Ads")
                            }
                        }
                    }
                    
                    // Restore purchases link
                    TextButton(
                        onClick = {
                            scope.launch {
                                RevenueCatManager.restorePurchases(
                                    onSuccess = {
                                        purchaseError = null
                                        // Check if restore was successful
                                        if (RevenueCatManager.isPremium()) {
                                            purchaseSuccess = true
                                        } else {
                                            purchaseError = "No previous purchases found"
                                        }
                                    },
                                    onError = { error ->
                                        purchaseError = error
                                    }
                                )
                            }
                        },
                        enabled = !isPurchasing
                    ) {
                        Text(
                            text = "Restore Purchases",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

/**
 * Simple premium status indicator
 * Shows a badge or icon when user has premium
 */
@Composable
fun PremiumStatusBadge() {
    val premiumStatus by RevenueCatManager.premiumStatusFlow.collectAsState()
    
    if (premiumStatus is RevenueCatManager.PremiumStatus.Premium) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.small
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Premium",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

