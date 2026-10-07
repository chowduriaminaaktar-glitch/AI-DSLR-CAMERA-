package com.example.ai

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import kotlin.math.max
import kotlin.math.min

object ImageProcessingUtils {

    /**
     * Applies full AI DSLR enhancement pipeline:
     * 1. Dynamic range, exposure, contrast, and color grading according to style.
     * 2. Bokeh depth-of-field simulation (f/1.4 blur falloff outside central subject zone).
     * 3. Micro-contrast & edge sharpening kernel.
     */
    fun processDslrEnhancement(
        source: Bitmap,
        style: AiDslrStyle,
        overallStrength: Float = 0.85f,
        blurAmount: Float = style.defaultBlur,
        focusCenterX: Float = 0.5f,
        focusCenterY: Float = 0.45f
    ): Bitmap {
        val width = source.width
        val height = source.height

        // Step 1: Color Grading, Contrast, Saturation & Dynamic Range
        val gradedBitmap = applyColorGrading(source, style, overallStrength)

        // Step 2: Bokeh / Depth-of-Field Blur if requested
        val finalWithBokeh = if (blurAmount > 0.05f) {
            applyDslrBokeh(gradedBitmap, blurAmount * overallStrength, focusCenterX, focusCenterY)
        } else {
            gradedBitmap
        }

        // Step 3: DSLR Lens Micro-contrast & Sharpening
        val sharpnessLevel = (style.defaultSharpness * overallStrength).coerceIn(0f, 1f)
        return if (sharpnessLevel > 0.1f) {
            applySharpening(finalWithBokeh, sharpnessLevel)
        } else {
            finalWithBokeh
        }
    }

    private fun applyColorGrading(source: Bitmap, style: AiDslrStyle, strength: Float): Bitmap {
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        val cm = ColorMatrix()

        val targetSat = 1.0f + (style.defaultSaturation - 1.0f) * strength
        val targetContrast = 1.0f + (style.defaultContrast - 1.0f) * strength

        if (style == AiDslrStyle.LEICA_MONO) {
            cm.setSaturation(0f)
            val contrastMatrix = ColorMatrix(
                floatArrayOf(
                    targetContrast, 0f, 0f, 0f, 128f * (1f - targetContrast),
                    0f, targetContrast, 0f, 0f, 128f * (1f - targetContrast),
                    0f, 0f, targetContrast, 0f, 128f * (1f - targetContrast),
                    0f, 0f, 0f, 1f, 0f
                )
            )
            cm.postConcat(contrastMatrix)
        } else {
            cm.setSaturation(targetSat)

            // Dynamic contrast adjustment
            val c = targetContrast
            val t = 128f * (1f - c)
            val contrastMatrix = ColorMatrix(
                floatArrayOf(
                    c, 0f, 0f, 0f, t,
                    0f, c, 0f, 0f, t,
                    0f, 0f, c, 0f, t,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            cm.postConcat(contrastMatrix)

            // Style-specific tone curve / tint matrix
            when (style) {
                AiDslrStyle.CINEMATIC_TEAL -> {
                    // Warm golden highlights, cool cyan/teal shadows
                    val tealOrangeMatrix = ColorMatrix(
                        floatArrayOf(
                            1.08f, 0.00f, 0.00f, 0f, 8f * strength,
                            0.00f, 1.02f, 0.00f, 0f, 2f * strength,
                            -0.05f, 0.02f, 1.15f, 0f, 12f * strength,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                    cm.postConcat(tealOrangeMatrix)
                }
                AiDslrStyle.GOLDEN_HOUR -> {
                    // Rich warm amber glow
                    val warmMatrix = ColorMatrix(
                        floatArrayOf(
                            1.15f, 0.00f, 0.00f, 0f, 14f * strength,
                            0.00f, 1.05f, 0.00f, 0f, 6f * strength,
                            0.00f, 0.00f, 0.90f, 0f, -10f * strength,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                    cm.postConcat(warmMatrix)
                }
                AiDslrStyle.PORTRAIT_BOKEH -> {
                    // Flattering skin tone warmth & gentle shadow lift
                    val portraitMatrix = ColorMatrix(
                        floatArrayOf(
                            1.06f, 0.00f, 0.00f, 0f, 6f * strength,
                            0.00f, 1.03f, 0.00f, 0f, 4f * strength,
                            0.00f, 0.00f, 0.98f, 0f, 0f,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                    cm.postConcat(portraitMatrix)
                }
                AiDslrStyle.HDR_VIVID -> {
                    // Punchy greens, deep blues, lifted shadows
                    val hdrMatrix = ColorMatrix(
                        floatArrayOf(
                            1.04f, 0.00f, 0.00f, 0f, 4f * strength,
                            0.00f, 1.08f, 0.00f, 0f, 8f * strength,
                            0.00f, 0.00f, 1.12f, 0f, 10f * strength,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                    cm.postConcat(hdrMatrix)
                }
                AiDslrStyle.NATURAL_DSLR -> {
                    // True-to-life neutral color rendition with prime lens pop
                    val naturalMatrix = ColorMatrix(
                        floatArrayOf(
                            1.03f, 0.00f, 0.00f, 0f, 2f * strength,
                            0.00f, 1.03f, 0.00f, 0f, 2f * strength,
                            0.00f, 0.00f, 1.02f, 0f, 1f * strength,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                    cm.postConcat(naturalMatrix)
                }
                else -> {}
            }
        }

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return result
    }

    /**
     * Simulates DSLR lens shallow depth of field (Bokeh):
     * The subject in the center/tap region stays tack-sharp,
     * while the periphery gradually melts into creamy bokeh.
     */
    private fun applyDslrBokeh(
        sharpSource: Bitmap,
        blurIntensity: Float,
        centerXRatio: Float,
        centerYRatio: Float
    ): Bitmap {
        val width = sharpSource.width
        val height = sharpSource.height

        // Downscale blur buffer for fast smooth bokeh computation
        val scale = 0.25f
        val smallW = max(1, (width * scale).toInt())
        val smallH = max(1, (height * scale).toInt())
        val scaled = Bitmap.createScaledBitmap(sharpSource, smallW, smallH, true)

        val radius = (12f * blurIntensity).toInt().coerceIn(2, 25)
        val blurredSmall = fastBoxBlur(scaled, radius)
        val blurredFull = Bitmap.createScaledBitmap(blurredSmall, width, height, true)

        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // Draw blurred background first
        canvas.drawBitmap(blurredFull, 0f, 0f, null)

        // Create sharp subject mask with radial feathering
        val centerX = width * centerXRatio
        val centerY = height * centerYRatio
        val focusRadius = min(width, height) * (0.35f + (1f - blurIntensity) * 0.2f)

        val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                centerX, centerY, focusRadius,
                intArrayOf(Color.WHITE, Color.WHITE, Color.TRANSPARENT),
                floatArrayOf(0f, 0.55f, 1.0f),
                Shader.TileMode.CLAMP
            )
        }

        // Draw sharp image over blur with mask
        val maskBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val maskCanvas = Canvas(maskBitmap)
        maskCanvas.drawCircle(centerX, centerY, focusRadius, maskPaint)

        val blendPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
        }

        val sharpSubject = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val sharpCanvas = Canvas(sharpSubject)
        sharpCanvas.drawBitmap(maskBitmap, 0f, 0f, null)
        sharpCanvas.drawBitmap(sharpSource, 0f, 0f, blendPaint)

        // Composite sharp subject over blurred background
        canvas.drawBitmap(sharpSubject, 0f, 0f, null)

        return output
    }

    /**
     * High-speed Box Blur algorithm for photographic depth simulation.
     */
    private fun fastBoxBlur(src: Bitmap, radius: Int): Bitmap {
        val w = src.width
        val h = src.height
        val pix = IntArray(w * h)
        src.getPixels(pix, 0, w, 0, 0, w, h)

        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1

        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)
        var rsum: Int
        var gsum: Int
        var bsum: Int
        var x: Int
        var y: Int
        var i: Int
        var p: Int
        var yp: Int
        var yi: Int
        val vmin = IntArray(max(w, h))

        var divsum = (div + 1) shr 1
        divsum *= divsum
        val dv = IntArray(256 * divsum)
        for (idx in 0 until 256 * divsum) {
            dv[idx] = idx / divsum
        }

        yi = 0
        var yw = 0

        for (curY in 0 until h) {
            rsum = 0
            gsum = 0
            bsum = 0
            for (curI in -radius..radius) {
                p = pix[yi + min(wm, max(curI, 0))]
                rsum += (p and 0xff0000) shr 16
                gsum += (p and 0x00ff00) shr 8
                bsum += (p and 0x0000ff)
            }
            for (curX in 0 until w) {
                r[yi] = dv[rsum]
                g[yi] = dv[gsum]
                b[yi] = dv[bsum]

                if (curY == 0) {
                    vmin[curX] = min(curX + radius + 1, wm)
                }
                val p1 = pix[yw + vmin[curX]]
                val p2 = pix[yw + max(curX - radius, 0)]

                rsum += ((p1 and 0xff0000) - (p2 and 0xff0000)) shr 16
                gsum += ((p1 and 0x00ff00) - (p2 and 0x00ff00)) shr 8
                bsum += ((p1 and 0x0000ff) - (p2 and 0x0000ff))
                yi++
            }
            yw += w
        }

        for (curX in 0 until w) {
            rsum = 0
            gsum = 0
            bsum = 0
            yp = -radius * w
            for (curI in -radius..radius) {
                yi = max(0, yp) + curX
                rsum += r[yi]
                gsum += g[yi]
                bsum += b[yi]
                yp += w
            }
            yi = curX
            for (curY in 0 until h) {
                pix[yi] = (-0x1000000 and pix[yi]) or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]
                if (curX == 0) {
                    vmin[curY] = min(curY + radius + 1, hm) * w
                }
                val p1 = curX + vmin[curY]
                val p2 = curX + max(curY - radius, 0) * w

                rsum += r[p1] - r[p2]
                gsum += g[p1] - g[p2]
                bsum += b[p1] - b[p2]

                yi += w
            }
        }

        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        out.setPixels(pix, 0, w, 0, 0, w, h)
        return out
    }

    /**
     * DSLR lens sharpening using 3x3 unsharp convolution kernel.
     * High performance: processed in downscaled-friendly chunks if large.
     */
    private fun applySharpening(source: Bitmap, amount: Float): Bitmap {
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // Sample pixels for sharpening
        val pixels = IntArray(width * height)
        val outPixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val centerWeight = 1f + 4f * (amount * 0.45f)
        val edgeWeight = -(amount * 0.45f)

        // Process inner rectangle
        for (y in 1 until height - 1) {
            val rowOffset = y * width
            val upOffset = (y - 1) * width
            val downOffset = (y + 1) * width

            for (x in 1 until width - 1) {
                val center = pixels[rowOffset + x]
                val top = pixels[upOffset + x]
                val bottom = pixels[downOffset + x]
                val left = pixels[rowOffset + x - 1]
                val right = pixels[rowOffset + x + 1]

                val a = (center shr 24) and 0xFF

                val cr = (center shr 16) and 0xFF
                val cg = (center shr 8) and 0xFF
                val cb = center and 0xFF

                val tr = (top shr 16) and 0xFF
                val tg = (top shr 8) and 0xFF
                val tb = top and 0xFF

                val br = (bottom shr 16) and 0xFF
                val bg = (bottom shr 8) and 0xFF
                val bb = bottom and 0xFF

                val lr = (left shr 16) and 0xFF
                val lg = (left shr 8) and 0xFF
                val lb = left and 0xFF

                val rr = (right shr 16) and 0xFF
                val rg = (right shr 8) and 0xFF
                val rb = right and 0xFF

                val nr = (cr * centerWeight + (tr + br + lr + rr) * edgeWeight).toInt().coerceIn(0, 255)
                val ng = (cg * centerWeight + (tg + bg + lg + rg) * edgeWeight).toInt().coerceIn(0, 255)
                val nb = (cb * centerWeight + (tb + bb + lb + rb) * edgeWeight).toInt().coerceIn(0, 255)

                outPixels[rowOffset + x] = (a shl 24) or (nr shl 16) or (ng shl 8) or nb
            }
        }

        // Copy borders
        for (x in 0 until width) {
            outPixels[x] = pixels[x]
            outPixels[(height - 1) * width + x] = pixels[(height - 1) * width + x]
        }
        for (y in 0 until height) {
            outPixels[y * width] = pixels[y * width]
            outPixels[y * width + (width - 1)] = pixels[y * width + (width - 1)]
        }

        output.setPixels(outPixels, 0, width, 0, 0, width, height)
        return output
    }
}
