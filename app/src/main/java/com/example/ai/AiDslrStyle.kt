package com.example.ai

enum class AiDslrStyle(
    val displayName: String,
    val subtitle: String,
    val defaultBlur: Float,
    val defaultSharpness: Float,
    val defaultContrast: Float,
    val defaultSaturation: Float
) {
    NATURAL_DSLR(
        displayName = "Natural DSLR",
        subtitle = "Neutral skin tones & crisp prime lens clarity",
        defaultBlur = 0.25f,
        defaultSharpness = 0.70f,
        defaultContrast = 1.15f,
        defaultSaturation = 1.10f
    ),
    PORTRAIT_BOKEH(
        displayName = "Portrait Bokeh",
        subtitle = "f/1.4 creamy background blur & subject pop",
        defaultBlur = 0.75f,
        defaultSharpness = 0.80f,
        defaultContrast = 1.12f,
        defaultSaturation = 1.05f
    ),
    CINEMATIC_TEAL(
        displayName = "Cine Teal & Orange",
        subtitle = "Blockbuster 35mm film grading & warm highlights",
        defaultBlur = 0.35f,
        defaultSharpness = 0.65f,
        defaultContrast = 1.25f,
        defaultSaturation = 1.20f
    ),
    GOLDEN_HOUR(
        displayName = "Golden Hour",
        subtitle = "Warm amber sunlight & soft romantic contrast",
        defaultBlur = 0.30f,
        defaultSharpness = 0.60f,
        defaultContrast = 1.10f,
        defaultSaturation = 1.25f
    ),
    LEICA_MONO(
        displayName = "Leica Monochrome",
        subtitle = "High contrast tonal range & deep dramatic blacks",
        defaultBlur = 0.20f,
        defaultSharpness = 0.85f,
        defaultContrast = 1.35f,
        defaultSaturation = 0.00f
    ),
    HDR_VIVID(
        displayName = "HDR Vivid",
        subtitle = "Expanded dynamic range & punchy landscape colors",
        defaultBlur = 0.15f,
        defaultSharpness = 0.75f,
        defaultContrast = 1.20f,
        defaultSaturation = 1.35f
    )
}
