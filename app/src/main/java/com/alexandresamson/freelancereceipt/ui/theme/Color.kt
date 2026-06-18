package com.alexandresamson.freelancereceipt.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Brand Colors ──────────────────────────────────────────────────────────
// Light blue brand identity — friendly, professional, finance-appropriate
val BrandBlue        = Color(0xFF2196F3)  // Primary
val BrandBlueDark    = Color(0xFF1976D2)  // Pressed / accents
val BrandBlueLight   = Color(0xFF64B5F6)  // Hover / highlights
val BrandSky         = Color(0xFFE3F2FD)  // Surface tint / containers
val BrandSkyDeep     = Color(0xFFBBDEFB)  // Card backgrounds (light)

// Accent — warm complementary for premium / success
val AccentGold       = Color(0xFFFFB300)  // Premium badge
val AccentMint       = Color(0xFF26A69A)  // Secondary actions

// Semantic
val SemanticSuccess  = Color(0xFF43A047)
val SemanticWarning  = Color(0xFFFB8C00)
val SemanticError    = Color(0xFFE53935)

// Neutrals (Light Theme)
val NeutralBackground   = Color(0xFFF5F9FD)  // very light blue-grey, not pure white
val NeutralSurface      = Color(0xFFFFFFFF)
val NeutralSurfaceVar   = Color(0xFFE8F1FA)
val NeutralOnBackground = Color(0xFF0D1B2A)
val NeutralOnSurface    = Color(0xFF1B2A3A)
val NeutralOutline      = Color(0xFFB0C4D6)

// Neutrals (Dark Theme)
val DarkBackground   = Color(0xFF0D1B2A)
val DarkSurface      = Color(0xFF152436)
val DarkSurfaceVar   = Color(0xFF1E3148)
val DarkOnBackground = Color(0xFFE3F2FD)
val DarkOnSurface    = Color(0xFFCFE0F0)
val DarkOutline      = Color(0xFF3D5675)

// ─── Stats / Chart Palette ────────────────────────────────────────────────
// Carefully chosen — high contrast, colorblind-friendly, harmonious
val ChartPalette = listOf(
    Color(0xFF1976D2),  // Blue       — primary brand
    Color(0xFF26A69A),  // Teal       — calm secondary
    Color(0xFFFFB300),  // Amber      — warm accent
    Color(0xFFE53935),  // Red        — alert/expense
    Color(0xFF8E24AA),  // Purple     — distinct hue
    Color(0xFF43A047),  // Green      — positive
    Color(0xFFFF7043),  // Deep Orange — contrast
)
