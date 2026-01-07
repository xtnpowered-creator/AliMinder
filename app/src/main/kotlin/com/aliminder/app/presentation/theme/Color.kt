package com.aliminder.app.presentation.theme

import androidx.compose.ui.graphics.Color

// Persona stage colors (Status Rings)
val OptimisticGreen = Color(0xFF4CAF50)
val WearyYellow = Color(0xFFFFEB3B) // New Yellow for Weary
val UrgentOrange = Color(0xFFFF9800) // Renamed from WearyOrange
val LateRed = Color(0xFFF44336) // Renamed from GraveRed

// Legacy aliases to prevent immediate breakages, marked for deprecation
val WearyOrange = UrgentOrange 
val GraveRed = LateRed

// Primary brand colors
val AliBlue = Color(0xFF2979FF) // Electric Blue (Google/Pixel Blue style)
val AliBlueVariant = Color(0xFF1565C0) // Darker variant
val TealAccent = Color(0xFF03DAC5)

// Define the background color - lighter royal blue
val DeepRoyalBlue = Color(0xFF1E3A5F) 

// Background colors
val BackgroundDark = DeepRoyalBlue       // App background is now Deep Royal Blue
val SurfaceDark = Color(0xFF121212)      // Card background remains black
val SurfaceVariant = Color(0xFF2C2C2C)   // Elevated surfaces (remains the same)

// Borders
val BorderDark = Color(0xFF000000)           // Black border

// Text colors
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB3B3B3)
val TextTertiary = Color(0xFF808080)

// Provider badge colors
val Microsoft365Blue = Color(0xFF0078D4)
val GoogleCalendarBlue = Color(0xFF4285F4)
val ShadowCalendarPurple = Color(0xFF9C27B0)
