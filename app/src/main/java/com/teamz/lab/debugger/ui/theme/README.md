# Theme System Documentation

## Overview

This theme system provides a flexible way to manage multiple themes in your DeviceGPT app. It includes:

1. **Design System Theme** (New) - Based on the provided design system with Poppins font
2. **Original Debugger Theme** (Backup) - The original purple theme with JetBrains Mono font

## Design System Colors

Based on the design system image:

- **White:** `#FFFFFF` - Box/section color on light background
- **Light:** `#F4F5F5` - App background in light mode, Button color on dark background
- **Dark:** `#12151A` - App background in dark mode, Button color on light background
- **Dark II:** `#1D1F25` - Box/section color on dark background
- **Neon Green:** `#D9FE06` - Primary accent color
- **Border:** `#DDDDDD` - Border color for sections

## Font System

- **Primary:** Poppins (Light, Regular, Medium, Semi-Bold, Bold)
- **Backup:** JetBrains Mono (all weights)

## Usage

### Basic Theme Usage

```kotlin
// In your MainActivity
setContent {
    // Initialize theme manager
    ThemeManager.initialize(this)
    
    // Provide theme manager to the composition
    CompositionLocalProvider(LocalThemeManager provides ThemeManager) {
        ThemeAwareContent {
            YourAppContent()
        }
    }
}
```

### Theme Switching

```kotlin
// Switch to Design System Light
context.switchToDesignSystemLight()

// Switch to Design System Dark
context.switchToDesignSystemDark()

// Switch to Original Theme
context.switchToOriginalTheme()

// Toggle Dark Mode
context.toggleDarkMode()
```

### Using Theme Selector Component

```kotlin
// Add to your drawer or settings screen
ThemeSelector(
    modifier = Modifier.padding(16.dp)
)
```

### Quick Theme Switcher

```kotlin
// Add to toolbar or FAB
QuickThemeSwitcher(
    modifier = Modifier.padding(8.dp)
)
```

### Accessing Theme Manager in Composables

```kotlin
@Composable
fun MyComponent() {
    val themeManager = useThemeManager()
    
    // Access current theme
    val currentTheme = themeManager.currentTheme
    val isDarkMode = themeManager.isDarkMode
    
    // Your component content
}
```

## Theme Structure

### AppTheme Enum
- `DESIGN_SYSTEM_LIGHT` - New light theme with Poppins
- `DESIGN_SYSTEM_DARK` - New dark theme with Poppins
- `DEBUGGER_DARK` - Original purple theme with JetBrains Mono

### Color Schemes
- `DesignSystemLightColorScheme` - Light theme colors
- `DesignSystemDarkColorScheme` - Dark theme colors
- `DebuggerDarkColorScheme` - Original theme colors

### Typography
- `DesignSystemTypography` - Poppins-based typography
- `OriginalTypography` - JetBrains Mono-based typography

## Adding New Themes

To add a new theme in the future:

1. **Add new color scheme:**
```kotlin
val NewThemeColorScheme = lightColorScheme(
    primary = Color(0xFFYourColor),
    // ... other colors
)
```

2. **Add to AppTheme enum:**
```kotlin
enum class AppTheme {
    DESIGN_SYSTEM_LIGHT,
    DESIGN_SYSTEM_DARK,
    DEBUGGER_DARK,
    NEW_THEME // Add here
}
```

3. **Update theme selection logic:**
```kotlin
val colorScheme = when (theme) {
    AppTheme.DESIGN_SYSTEM_LIGHT -> DesignSystemLightColorScheme
    AppTheme.DESIGN_SYSTEM_DARK -> DesignSystemDarkColorScheme
    AppTheme.DEBUGGER_DARK -> DebuggerDarkColorScheme
    AppTheme.NEW_THEME -> NewThemeColorScheme // Add here
}
```

4. **Add convenience function:**
```kotlin
fun Context.switchToNewTheme() {
    ThemeManager.setTheme(AppTheme.NEW_THEME, this)
}
```

## Files Structure

```
ui/theme/
├── Theme.kt              # Main theme definitions and color schemes
├── Type.kt               # Typography definitions
├── ThemeManager.kt       # Theme management and switching logic
├── ThemeSelector.kt      # UI components for theme selection
└── README.md            # This documentation
```

## Best Practices

1. **Always use MaterialTheme.colorScheme** instead of hardcoded colors
2. **Use the theme manager** for dynamic theme switching
3. **Test both light and dark modes** for all components
4. **Keep original theme as backup** for compatibility
5. **Use semantic color names** (primary, secondary, etc.) rather than specific colors

## Migration from Old Theme

The old theme is preserved as `AppTheme.DEBUGGER_DARK`. To migrate:

1. Replace `DebuggerTheme { }` with `ThemeAwareContent { }`
2. Update color references to use `MaterialTheme.colorScheme`
3. Test all components with the new theme
4. Gradually migrate to the new design system theme 