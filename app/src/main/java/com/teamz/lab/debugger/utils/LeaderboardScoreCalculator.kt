package com.teamz.lab.debugger.utils

/**
 * Leaderboard Score Calculator
 * 
 * Calculates scores for all 9 leaderboard categories
 */
object LeaderboardScoreCalculator {
    
    /**
     * Calculate all scores for a leaderboard entry
     */
    fun calculateAllScores(
        powerEfficiency: PowerEfficiencyData,
        cpuPerformance: CpuPerformanceData,
        cameraEfficiency: CameraEfficiencyData,
        displayEfficiency: DisplayEfficiencyData,
        healthScore: HealthScoreData,
        powerTrend: PowerTrendData,
        componentOptimization: ComponentOptimizationData,
        thermalEfficiency: ThermalEfficiencyData,
        performanceConsistency: PerformanceConsistencyData,
        userEngagement: UserEngagementData
    ): Map<String, Double> {
        return mapOf(
            "power_efficiency" to powerEfficiency.score,
            "cpu_performance" to cpuPerformance.score,
            "camera_efficiency" to cameraEfficiency.score,
            "display_efficiency" to displayEfficiency.score,
            "health_score" to healthScore.score,
            "power_trend" to powerTrend.score,
            "component_optimization" to componentOptimization.score,
            "thermal_efficiency" to thermalEfficiency.score,
            "performance_consistency" to performanceConsistency.score
        )
    }
    
    /**
     * Power Efficiency: Lower power = Higher score
     */
    fun calculatePowerEfficiencyScore(
        avgPower: Double,
        trend: String,
        componentBalance: Double
    ): Double {
        if (avgPower <= 0) return 0.0
        
        // Base score: lower power = higher score (max 100W = 0, 0W = 100)
        val baseScore = (100.0 - (avgPower * 10.0)).coerceIn(0.0, 100.0)
        
        // Trend bonus
        val trendBonus = when(trend.uppercase()) {
            "DECREASING" -> 10.0
            "STABLE" -> 5.0
            "INCREASING" -> 0.0
            else -> 0.0
        }
        
        // Balance bonus (more balanced power usage = better)
        val balanceBonus = (componentBalance * 5.0).coerceIn(0.0, 10.0)
        
        return (baseScore + trendBonus + balanceBonus).coerceIn(0.0, 100.0)
    }
    
    /**
     * CPU Performance: Higher performance per watt = Higher score
     */
    fun calculateCpuPerformanceScore(
        microbenchResults: List<PowerConsumptionUtils.CpuBenchPoint>
    ): Double {
        if (microbenchResults.isEmpty()) return 0.0
        
        val avgDeltaPower = microbenchResults.map { it.deltaPowerW }.average()
        val avgUtilization = microbenchResults.map { it.observedUtilPercent.toDouble() }.average()
        
        // Efficiency = utilization / power (higher is better)
        // Normalize to 0-100 scale
        val efficiency = if (avgDeltaPower > 0) {
            (avgUtilization / avgDeltaPower) * 10.0
        } else {
            avgUtilization * 2.0 // If no power increase, reward high utilization
        }
        
        return efficiency.coerceIn(0.0, 100.0)
    }
    
    /**
     * Camera Efficiency: Lower energy = Higher score
     */
    fun calculateCameraEfficiencyScore(energyPerPhoto: Double): Double {
        if (energyPerPhoto <= 0) return 0.0
        
        // Lower energy per photo = higher score
        // Typical range: 0.1-2.0 Joules per photo
        // Score = 100 - (energy * 50)
        val score = (100.0 - (energyPerPhoto * 50.0)).coerceIn(0.0, 100.0)
        return score
    }
    
    /**
     * Display Efficiency: Higher brightness per watt = Higher score
     */
    fun calculateDisplayEfficiencyScore(
        powerSweepResults: List<PowerConsumptionUtils.DisplayPowerPoint>
    ): Double {
        if (powerSweepResults.isEmpty()) return 0.0
        
        val avgBrightness = powerSweepResults.map { it.brightnessLevel.toDouble() }.average()
        val avgPower = powerSweepResults.map { it.powerW }.average()
        
        if (avgPower <= 0) return 0.0
        
        // Efficiency = brightness / power (higher is better)
        // Normalize to 0-100 scale
        val efficiency = (avgBrightness / avgPower) * 2.0
        return efficiency.coerceIn(0.0, 100.0)
    }
    
    /**
     * Thermal Efficiency: Lower temperature = Higher score
     */
    fun calculateThermalEfficiencyScore(avgTemperature: Double): Double {
        if (avgTemperature <= 0) return 0.0
        
        // Lower temperature = higher score
        // Typical range: 25-50°C
        // Score = 100 - (temp / 2)
        val score = (100.0 - (avgTemperature / 2.0)).coerceIn(0.0, 100.0)
        return score
    }
    
    /**
     * Health Score: Combined score
     */
    fun calculateHealthScore(
        currentScore: Int,
        streak: Int,
        totalScans: Int
    ): Double {
        // Base score from current health (1-10 scale, convert to 0-100)
        val baseScore = currentScore * 10.0
        
        // Streak bonus (max 5 points)
        val streakBonus = (streak * 0.5).coerceIn(0.0, 5.0)
        
        // Engagement bonus (max 5 points)
        val engagementBonus = (totalScans * 0.1).coerceIn(0.0, 5.0)
        
        return (baseScore + streakBonus + engagementBonus).coerceIn(0.0, 100.0)
    }
    
    /**
     * Power Trend: DECREASING is best
     */
    fun calculatePowerTrendScore(
        trend: String,
        improvementPercent: Double
    ): Double {
        val baseScore = when(trend.uppercase()) {
            "DECREASING" -> 100.0
            "STABLE" -> 50.0
            "INCREASING" -> 0.0
            else -> 25.0
        }
        
        // Add improvement bonus
        val improvementBonus = (improvementPercent * 10.0).coerceIn(0.0, 20.0)
        
        return (baseScore + improvementBonus).coerceIn(0.0, 100.0)
    }
    
    /**
     * Component Optimization: More balanced = Higher score
     */
    fun calculateComponentOptimizationScore(
        maxComponentPower: Double,
        balanceScore: Double
    ): Double {
        // Base score: lower max component power = higher score
        val baseScore = (100.0 - (maxComponentPower * 5.0)).coerceIn(0.0, 100.0)
        
        // Balance bonus
        val balanceBonus = (balanceScore * 10.0).coerceIn(0.0, 20.0)
        
        return (baseScore + balanceBonus).coerceIn(0.0, 100.0)
    }
    
    /**
     * Performance Consistency: Higher FPS, lower drops = Higher score
     */
    fun calculatePerformanceConsistencyScore(
        avgFPS: Int,
        frameDropRate: Double
    ): Double {
        // FPS component (max 60 points for 60 FPS)
        val fpsScore = (avgFPS * 1.0).coerceIn(0.0, 60.0)
        
        // Frame drop penalty (max 40 points penalty)
        val dropPenalty = (frameDropRate * 10.0).coerceIn(0.0, 40.0)
        
        return (fpsScore + (40.0 - dropPenalty)).coerceIn(0.0, 100.0)
    }
    
    /**
     * User Engagement: Higher engagement = Higher score
     */
    fun calculateUserEngagementScore(
        streak: Int,
        totalScans: Int
    ): Double {
        // Streak component (max 50 points)
        val streakScore = (streak * 5.0).coerceIn(0.0, 50.0)
        
        // Total scans component (max 50 points)
        val scansScore = (totalScans * 0.5).coerceIn(0.0, 50.0)
        
        return (streakScore + scansScore).coerceIn(0.0, 100.0)
    }
    
    /**
     * Calculate component balance score (0.0-1.0)
     * Higher = more balanced power distribution
     */
    fun calculateComponentBalance(componentBreakdown: Map<String, Double>): Double {
        if (componentBreakdown.isEmpty()) return 0.0
        
        val totalPower = componentBreakdown.values.sum()
        if (totalPower <= 0) return 0.0
        
        // Calculate standard deviation of power distribution
        val percentages = componentBreakdown.values.map { (it / totalPower) * 100.0 }
        val avgPercentage = percentages.average()
        val diffSquared = percentages.map { value -> 
            val diff = value - avgPercentage
            diff * diff // Square the difference instead of using pow
        }
        val variance = diffSquared.average()
        val stdDev = kotlin.math.sqrt(variance)
        
        // Lower std dev = more balanced = higher score
        // Normalize to 0.0-1.0
        val balanceScore = (1.0 - (stdDev / 50.0)).coerceIn(0.0, 1.0)
        return balanceScore
    }
}

