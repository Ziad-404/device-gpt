package com.teamz.lab.debugger.utils

/**
 * Power Consumption Education Content
 * Based on research papers from latest_power_consumption_research.md
 * 
 * Provides educational content about power consumption for users
 */
object PowerEducation {
    
    data class EducationContent(
        val title: String,
        val content: String,
        val category: Category,
        val researchSource: String? = null
    )
    
    enum class Category {
        BASICS, DISPLAY, CPU, NETWORK, CAMERA, BATTERY, GENERAL
    }
    
    /**
     * Get education content for a specific component
     */
    fun getEducationForComponent(component: String): EducationContent? {
        return when (component.lowercase()) {
            "display", "screen" -> getDisplayEducation()
            "cpu", "processor" -> getCpuEducation()
            "network", "wifi", "cellular" -> getNetworkEducation()
            "camera" -> getCameraEducation()
            "battery" -> getBatteryEducation()
            else -> null
        }
    }
    
    /**
     * Get general power consumption basics
     */
    fun getBasicsEducation(): EducationContent {
        return EducationContent(
            title = "Understanding Power Consumption",
            content = """
                Power consumption measures how much energy your device uses, measured in watts (W) or milliwatts (mW).
                
                🔋 **Why It Matters:**
                • Higher power consumption = faster battery drain
                • Different components consume different amounts of power
                • Power consumption varies based on usage patterns
                
                📊 **How We Measure:**
                • Uses real system data from your device's BatteryManager
                • Formula: Power = Voltage × Current (P = V × I)
                • Based on research from leading universities and tech companies
                
                💡 **Key Insights:**
                • Display brightness is often the biggest power consumer
                • CPU usage spikes can significantly impact battery life
                • Network signal strength affects power consumption
                • Background apps can drain battery even when screen is off
            """.trimIndent(),
            category = Category.BASICS
        )
    }
    
    private fun getDisplayEducation(): EducationContent {
        return EducationContent(
            title = "Display Power Consumption",
            content = """
                Your device's display is typically one of the largest power consumers.
                
                📱 **Research Findings:**
                • LCD displays consume more power at higher brightness levels
                • AMOLED displays are more power-efficient, especially with dark content
                • Reducing brightness by 50% can save up to 40% display power
                • Auto-brightness helps optimize power based on ambient light
                
                💡 **Tips to Reduce Display Power:**
                • Use auto-brightness
                • Use dark mode/theme when available (especially on AMOLED)
                • Reduce screen timeout duration
                • Lower manual brightness in dark environments
                • Disable always-on display if not needed
            """.trimIndent(),
            category = Category.DISPLAY,
            researchSource = "LCD vs AMOLED Power Consumption Research"
        )
    }
    
    private fun getCpuEducation(): EducationContent {
        return EducationContent(
            title = "CPU Power Consumption",
            content = """
                Your device's processor (CPU) power consumption depends on workload and frequency.
                
                🧠 **Research Findings:**
                • CPU power consumption is related to frequency and utilization
                • Higher CPU frequencies consume exponentially more power
                • Background processes can keep CPU active and drain battery
                • Modern CPUs use dynamic frequency scaling to optimize power
                
                💡 **Tips to Reduce CPU Power:**
                • Close unnecessary background apps
                • Enable battery saver mode for lower CPU frequencies
                • Avoid running heavy apps simultaneously
                • Restart device if CPU usage seems stuck high
                • Check for apps with high CPU usage in settings
            """.trimIndent(),
            category = Category.CPU,
            researchSource = "CPU Frequency-Independent Power Consumption Research"
        )
    }
    
    private fun getNetworkEducation(): EducationContent {
        return EducationContent(
            title = "Network Power Consumption",
            content = """
                Network connectivity (Wi-Fi, cellular) consumes power based on signal strength and data transfer.
                
                📶 **Research Findings:**
                • Poor signal strength (low RSSI) significantly increases power consumption
                • Wi-Fi generally consumes less power than cellular data
                • Active data transfers consume more power than idle connections
                • Network scanning and switching between networks uses extra power
                
                💡 **Tips to Reduce Network Power:**
                • Use Wi-Fi instead of mobile data when available
                • Stay closer to Wi-Fi router for better signal
                • Disable mobile data when Wi-Fi is connected
                • Turn off Wi-Fi/Bluetooth scanning when not needed
                • Use airplane mode in areas with no signal
            """.trimIndent(),
            category = Category.NETWORK,
            researchSource = "Network RSSI Power Consumption Research"
        )
    }
    
    private fun getCameraEducation(): EducationContent {
        return EducationContent(
            title = "Camera Power Consumption",
            content = """
                Camera usage is one of the most power-intensive operations on mobile devices.
                
                📷 **Research Findings:**
                • Camera sensors and image processing consume significant power
                • Video recording consumes more power than photo capture
                • Camera apps left open in background continue to consume power
                • Flash usage adds additional power consumption
                
                💡 **Tips to Reduce Camera Power:**
                • Close camera apps when not in use
                • Avoid keeping camera active in background
                • Use flash only when necessary
                • Record videos in lower resolution when possible
                • Use front camera when possible (typically lower power)
            """.trimIndent(),
            category = Category.CAMERA,
            researchSource = "Per-Photo Energy Consumption Research"
        )
    }
    
    private fun getBatteryEducation(): EducationContent {
        return EducationContent(
            title = "Battery Health & Power",
            content = """
                Understanding how power consumption affects your battery life.
                
                🔋 **Key Concepts:**
                • Battery capacity is measured in mAh (milliampere-hours)
                • Power consumption determines how quickly battery drains
                • Higher power = shorter battery life
                • Battery health degrades over time and charge cycles
                
                💡 **Battery Optimization Tips:**
                • Monitor power consumption to identify drain sources
                • Enable battery saver mode when needed
                • Avoid extreme temperatures (hot or cold)
                • Don't let battery drain to 0% regularly
                • Use optimized charging if available
                • Close apps that consume excessive power
            """.trimIndent(),
            category = Category.BATTERY
        )
    }
    
    /**
     * Get quick tip for a component
     */
    fun getQuickTip(component: String): String? {
        return when (component.lowercase()) {
            "display", "screen" -> "💡 Reduce brightness by 50% to save up to 40% display power"
            "cpu", "processor" -> "🧠 Close background apps to reduce CPU power consumption"
            "network", "wifi", "cellular" -> "📶 Use Wi-Fi instead of mobile data for better power efficiency"
            "camera" -> "📷 Close camera apps when not in use to save power"
            "battery" -> "🔋 Monitor power consumption to optimize battery life"
            else -> null
        }
    }
}

