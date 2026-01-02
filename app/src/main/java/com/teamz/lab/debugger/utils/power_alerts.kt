package com.teamz.lab.debugger.utils

import android.content.Context
import com.teamz.lab.debugger.utils.PowerConsumptionAggregator.PowerStats
import com.teamz.lab.debugger.utils.PowerConsumptionUtils.PowerConsumptionSummary

/**
 * Power Consumption Alerts System
 * Monitors power consumption and triggers alerts for anomalies
 */
object PowerAlerts {
    
    data class PowerAlert(
        val type: AlertType,
        val title: String,
        val message: String,
        val severity: Severity,
        val component: String? = null,
        val powerValue: Double? = null
    )
    
    enum class AlertType {
        HIGH_POWER_CONSUMPTION,
        POWER_SPIKE,
        COMPONENT_ANOMALY,
        BATTERY_DRAIN,
        TREND_WARNING
    }
    
    enum class Severity {
        CRITICAL, WARNING, INFO
    }
    
    // Thresholds based on research and typical device power consumption
    private const val HIGH_TOTAL_POWER_THRESHOLD = 10000.0 // 10W
    private const val CRITICAL_TOTAL_POWER_THRESHOLD = 15000.0 // 15W
    private const val HIGH_COMPONENT_POWER_THRESHOLD = 3000.0 // 3W per component
    private const val POWER_SPIKE_THRESHOLD_MULTIPLIER = 2.0 // 2x average = spike
    
    /**
     * Check for power consumption alerts based on current data
     */
    fun checkAlerts(
        context: Context,
        powerData: PowerConsumptionSummary?,
        aggregatedStats: PowerStats?
    ): List<PowerAlert> {
        val alerts = mutableListOf<PowerAlert>()
        
        if (powerData == null) return alerts
        
        // Check total power consumption
        alerts.addAll(checkTotalPowerAlerts(powerData, aggregatedStats))
        
        // Check component-specific alerts
        alerts.addAll(checkComponentAlerts(powerData))
        
        // Check power spikes
        alerts.addAll(checkPowerSpikes(powerData, aggregatedStats))
        
        // Check trend warnings
        alerts.addAll(checkTrendWarnings(aggregatedStats))
        
        return alerts
    }
    
    private fun checkTotalPowerAlerts(
        powerData: PowerConsumptionSummary,
        stats: PowerStats?
    ): List<PowerAlert> {
        val alerts = mutableListOf<PowerAlert>()
        val totalPower = powerData.totalPower
        
        if (totalPower >= CRITICAL_TOTAL_POWER_THRESHOLD) {
            alerts.add(
                PowerAlert(
                    type = AlertType.HIGH_POWER_CONSUMPTION,
                    title = "Critical Power Consumption",
                    message = "Your device is consuming ${String.format("%.1f", totalPower / 1000)}W, " +
                            "which is critically high. This will drain your battery very quickly. " +
                            "Consider closing apps and enabling battery saver mode.",
                    severity = Severity.CRITICAL,
                    powerValue = totalPower
                )
            )
        } else if (totalPower >= HIGH_TOTAL_POWER_THRESHOLD) {
            alerts.add(
                PowerAlert(
                    type = AlertType.HIGH_POWER_CONSUMPTION,
                    title = "High Power Consumption",
                    message = "Your device is consuming ${String.format("%.1f", totalPower / 1000)}W, " +
                            "which is above normal. Monitor your battery usage and close unnecessary apps.",
                    severity = Severity.WARNING,
                    powerValue = totalPower
                )
            )
        }
        
        return alerts
    }
    
    private fun checkComponentAlerts(
        powerData: PowerConsumptionSummary
    ): List<PowerAlert> {
        val alerts = mutableListOf<PowerAlert>()
        
        powerData.components.forEach { component ->
            if (component.powerConsumption >= HIGH_COMPONENT_POWER_THRESHOLD) {
                alerts.add(
                    PowerAlert(
                        type = AlertType.COMPONENT_ANOMALY,
                        title = "High ${component.component} Power",
                        message = "${component.component} is consuming ${String.format("%.1f", component.powerConsumption / 1000)}W, " +
                                "which is unusually high. Check if this component is being used unnecessarily.",
                        severity = Severity.WARNING,
                        component = component.component,
                        powerValue = component.powerConsumption
                    )
                )
            }
        }
        
        return alerts
    }
    
    private fun checkPowerSpikes(
        powerData: PowerConsumptionSummary,
        stats: PowerStats?
    ): List<PowerAlert> {
        val alerts = mutableListOf<PowerAlert>()
        
        stats?.let {
            val averagePower = it.averagePower
            val currentPower = powerData.totalPower
            
            // Check if current power is significantly higher than average
            if (averagePower > 0 && currentPower >= averagePower * POWER_SPIKE_THRESHOLD_MULTIPLIER) {
                alerts.add(
                    PowerAlert(
                        type = AlertType.POWER_SPIKE,
                        title = "Power Consumption Spike Detected",
                        message = "Power consumption has spiked to ${String.format("%.1f", currentPower / 1000)}W, " +
                                "which is ${String.format("%.1f", (currentPower / averagePower))}x your average. " +
                                "This may indicate a background process or app consuming excessive power.",
                        severity = Severity.WARNING,
                        powerValue = currentPower
                    )
                )
            }
        }
        
        return alerts
    }
    
    private fun checkTrendWarnings(
        stats: PowerStats?
    ): List<PowerAlert> {
        val alerts = mutableListOf<PowerAlert>()
        
        stats?.let {
            when (it.powerTrend) {
                PowerConsumptionAggregator.PowerTrend.INCREASING -> {
                    if (it.averagePower > HIGH_TOTAL_POWER_THRESHOLD) {
                        alerts.add(
                            PowerAlert(
                                type = AlertType.TREND_WARNING,
                                title = "Increasing Power Trend",
                                message = "Your power consumption is trending upward and is already high. " +
                                        "This may lead to faster battery drain. Consider optimizing your device usage.",
                                severity = Severity.WARNING
                            )
                        )
                    }
                }
                else -> {}
            }
        }
        
        return alerts
    }
    
    /**
     * Get battery drain warning based on power consumption
     */
    fun getBatteryDrainEstimate(
        powerData: PowerConsumptionSummary?,
        batteryCapacityMah: Int = 4000 // Default 4000mAh
    ): String? {
        if (powerData == null) return null
        
        val totalPowerWatts = powerData.totalPower / 1000.0 // Convert mW to W
        if (totalPowerWatts <= 0) return null
        
        // Estimate battery voltage (typically 3.7V for Li-ion)
        val batteryVoltage = 3.7
        val currentAmps = totalPowerWatts / batteryVoltage
        val currentMah = currentAmps * 1000
        
        // Calculate hours until battery drain
        val hoursUntilDrain = batteryCapacityMah / currentMah
        
        if (hoursUntilDrain < 4) {
            return "⚠️ Battery will drain in approximately ${String.format("%.1f", hoursUntilDrain)} hours at current power consumption"
        }
        
        return null
    }
}

