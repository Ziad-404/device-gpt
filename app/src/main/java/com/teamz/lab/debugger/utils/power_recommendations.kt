package com.teamz.lab.debugger.utils

import android.content.Context
import com.teamz.lab.debugger.utils.PowerConsumptionAggregator.PowerStats
import com.teamz.lab.debugger.utils.PowerConsumptionUtils.ComponentPowerData
import com.teamz.lab.debugger.utils.PowerConsumptionUtils.PowerConsumptionSummary

/**
 * Power Consumption Recommendations Engine
 * Based on research papers from latest_power_consumption_research.md
 * 
 * Provides actionable recommendations to optimize power consumption
 */
object PowerRecommendations {
    
    data class PowerRecommendation(
        val title: String,
        val description: String,
        val priority: Priority,
        val category: Category,
        val researchSource: String? = null
    )
    
    enum class Priority {
        HIGH, MEDIUM, LOW
    }
    
    enum class Category {
        DISPLAY, CPU, NETWORK, CAMERA, BATTERY, GENERAL
    }
    
    /**
     * Generate recommendations based on current power consumption data
     */
    fun generateRecommendations(
        context: Context,
        powerData: PowerConsumptionSummary?,
        aggregatedStats: PowerStats?
    ): List<PowerRecommendation> {
        val recommendations = mutableListOf<PowerRecommendation>()
        
        if (powerData == null) return recommendations
        
        // Display brightness recommendations (from LCD vs AMOLED research)
        recommendations.addAll(getDisplayRecommendations(powerData, aggregatedStats))
        
        // CPU frequency scaling advice (from frequency-independent research)
        recommendations.addAll(getCpuRecommendations(powerData, aggregatedStats))
        
        // Network power optimization (from RSSI research)
        recommendations.addAll(getNetworkRecommendations(powerData, aggregatedStats))
        
        // Camera usage optimization (from per-photo energy research)
        recommendations.addAll(getCameraRecommendations(powerData, aggregatedStats))
        
        // Battery health recommendations
        recommendations.addAll(getBatteryRecommendations(context, powerData, aggregatedStats))
        
        // General power optimization tips
        recommendations.addAll(getGeneralRecommendations(powerData, aggregatedStats))
        
        // Sort by priority
        return recommendations.sortedBy { 
            when (it.priority) {
                Priority.HIGH -> 0
                Priority.MEDIUM -> 1
                Priority.LOW -> 2
            }
        }
    }
    
    private fun getDisplayRecommendations(
        powerData: PowerConsumptionSummary,
        stats: PowerStats?
    ): List<PowerRecommendation> {
        val recommendations = mutableListOf<PowerRecommendation>()
        
        val displayComponent = powerData.components.find { it.component == "Display" }
        val displayPower = displayComponent?.powerConsumption ?: 0.0
        
        // High display power consumption
        if (displayPower > 2000.0) { // > 2W
            recommendations.add(
                PowerRecommendation(
                    title = "Reduce Screen Brightness",
                    description = "Your display is consuming ${String.format("%.1f", displayPower / 1000)}W. " +
                            "According to research, reducing brightness by 50% can save up to 40% display power. " +
                            "Consider using auto-brightness or manual adjustment.",
                    priority = Priority.HIGH,
                    category = Category.DISPLAY,
                    researchSource = "LCD vs AMOLED Power Consumption Research"
                )
            )
        }
        
        // Display trend analysis
        stats?.let {
            if (it.powerTrend == PowerConsumptionAggregator.PowerTrend.INCREASING && displayPower > 1500.0) {
                recommendations.add(
                    PowerRecommendation(
                        title = "Display Power Trend Increasing",
                        description = "Your display power consumption is trending upward. " +
                                "Consider enabling dark mode or reducing screen-on time to optimize battery life.",
                        priority = Priority.MEDIUM,
                        category = Category.DISPLAY
                    )
                )
            }
        }
        
        return recommendations
    }
    
    private fun getCpuRecommendations(
        powerData: PowerConsumptionSummary,
        stats: PowerStats?
    ): List<PowerRecommendation> {
        val recommendations = mutableListOf<PowerRecommendation>()
        
        val cpuComponent = powerData.components.find { it.component == "CPU" }
        val cpuPower = cpuComponent?.powerConsumption ?: 0.0
        
        // High CPU power consumption
        if (cpuPower > 3000.0) { // > 3W
            recommendations.add(
                PowerRecommendation(
                    title = "Optimize CPU Usage",
                    description = "Your CPU is consuming ${String.format("%.1f", cpuPower / 1000)}W. " +
                            "Research shows that CPU frequency scaling can significantly impact power. " +
                            "Close unnecessary background apps and consider using battery saver mode.",
                    priority = Priority.HIGH,
                    category = Category.CPU,
                    researchSource = "CPU Frequency-Independent Power Consumption Research"
                )
            )
        }
        
        // CPU usage details
        cpuComponent?.details?.let { details ->
            if (details.contains("%") && details.contains("80")) {
                recommendations.add(
                    PowerRecommendation(
                        title = "High CPU Utilization Detected",
                        description = "Your CPU is running at high utilization. " +
                                "Check for background processes and consider restarting your device if performance is slow.",
                        priority = Priority.MEDIUM,
                        category = Category.CPU
                    )
                )
            }
        }
        
        return recommendations
    }
    
    private fun getNetworkRecommendations(
        powerData: PowerConsumptionSummary,
        stats: PowerStats?
    ): List<PowerRecommendation> {
        val recommendations = mutableListOf<PowerRecommendation>()
        
        val networkComponent = powerData.components.find { it.component == "Network" }
        val networkPower = networkComponent?.powerConsumption ?: 0.0
        
        // High network power consumption
        if (networkPower > 1500.0) { // > 1.5W
            recommendations.add(
                PowerRecommendation(
                    title = "Optimize Network Usage",
                    description = "Network is consuming ${String.format("%.1f", networkPower / 1000)}W. " +
                            "Research indicates that poor RSSI (signal strength) significantly increases power consumption. " +
                            "Consider moving closer to Wi-Fi router or switching to Wi-Fi if using mobile data.",
                    priority = Priority.MEDIUM,
                    category = Category.NETWORK,
                    researchSource = "Network RSSI Power Consumption Research"
                )
            )
        }
        
        return recommendations
    }
    
    private fun getCameraRecommendations(
        powerData: PowerConsumptionSummary,
        stats: PowerStats?
    ): List<PowerRecommendation> {
        val recommendations = mutableListOf<PowerRecommendation>()
        
        val cameraComponent = powerData.components.find { it.component == "Camera" }
        val cameraPower = cameraComponent?.powerConsumption ?: 0.0
        
        // Camera power consumption
        if (cameraPower > 2000.0) { // > 2W
            recommendations.add(
                PowerRecommendation(
                    title = "Camera Power Consumption High",
                    description = "Camera is consuming ${String.format("%.1f", cameraPower / 1000)}W. " +
                            "Research shows that camera usage significantly impacts battery life. " +
                            "Close camera apps when not in use and avoid keeping camera active in background.",
                    priority = Priority.MEDIUM,
                    category = Category.CAMERA,
                    researchSource = "Per-Photo Energy Consumption Research"
                )
            )
        }
        
        return recommendations
    }
    
    private fun getBatteryRecommendations(
        context: Context,
        powerData: PowerConsumptionSummary,
        stats: PowerStats?
    ): List<PowerRecommendation> {
        val recommendations = mutableListOf<PowerRecommendation>()
        
        val batteryComponent = powerData.components.find { it.component == "Battery" }
        val totalPower = powerData.totalPower
        
        // High total power consumption
        if (totalPower > 8000.0) { // > 8W
            recommendations.add(
                PowerRecommendation(
                    title = "High Overall Power Consumption",
                    description = "Your device is consuming ${String.format("%.1f", totalPower / 1000)}W total. " +
                            "This is significantly high and will drain battery quickly. " +
                            "Enable battery saver mode and close unnecessary apps.",
                    priority = Priority.HIGH,
                    category = Category.BATTERY
                )
            )
        }
        
        // Power trend analysis
        stats?.let {
            when (it.powerTrend) {
                PowerConsumptionAggregator.PowerTrend.INCREASING -> {
                    recommendations.add(
                        PowerRecommendation(
                            title = "Power Consumption Increasing",
                            description = "Your device power consumption is trending upward. " +
                                    "Monitor which components are consuming more power and optimize accordingly.",
                            priority = Priority.MEDIUM,
                            category = Category.BATTERY
                        )
                    )
                }
                PowerConsumptionAggregator.PowerTrend.DECREASING -> {
                    recommendations.add(
                        PowerRecommendation(
                            title = "Power Consumption Improving",
                            description = "Great! Your power consumption is decreasing. " +
                                    "Keep monitoring to maintain optimal battery life.",
                            priority = Priority.LOW,
                            category = Category.BATTERY
                        )
                    )
                }
                else -> {}
            }
        }
        
        return recommendations
    }
    
    private fun getGeneralRecommendations(
        powerData: PowerConsumptionSummary,
        stats: PowerStats?
    ): List<PowerRecommendation> {
        val recommendations = mutableListOf<PowerRecommendation>()
        
        // Top consumers analysis
        val topConsumers = powerData.components
            .sortedByDescending { it.powerConsumption }
            .take(3)
        
        if (topConsumers.isNotEmpty() && topConsumers[0].powerConsumption > 1000.0) {
            val topConsumer = topConsumers[0]
            recommendations.add(
                PowerRecommendation(
                    title = "Top Power Consumer: ${topConsumer.component}",
                    description = "${topConsumer.component} is your highest power consumer at " +
                            "${String.format("%.1f", topConsumer.powerConsumption / 1000)}W. " +
                            "Consider optimizing this component's usage.",
                    priority = Priority.MEDIUM,
                    category = Category.GENERAL
                )
            )
        }
        
        return recommendations
    }
}

