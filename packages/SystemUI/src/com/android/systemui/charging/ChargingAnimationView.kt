/*
 * Copyright (C) 2024-2025 Lunaris AOSP
 * Copyright (C) 2026 The XPerience Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.systemui.charging

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.*
import android.widget.FrameLayout
import androidx.core.view.isVisible
import kotlin.math.*
import kotlin.random.Random

class ChargingAnimationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "ChargingAnimationView"

        const val STYLE_AOSP_RIPPLE = 0
        const val STYLE_XPERIENCE = 1
        const val STYLE_WARP_PULSE = 2
        const val STYLE_XIAOMI_HYPEROS = 3
        const val STYLE_OPPO_SUPERVOOC = 4
        const val STYLE_BUBBLE_STREAM = 5
        const val STYLE_NEON = 6
        const val STYLE_BEAM = 7
        const val STYLE_PLASMA = 8
        const val STYLE_QUANTUM_SPARKS = 9
        const val STYLE_NEBULA = 10
        const val STYLE_DIGITAL_MATRIX = 11
        const val STYLE_GEOFLOW = 12

        const val COLOR_MODE_DEFAULT = 0
        const val COLOR_MODE_ACCENT = 1
        const val COLOR_MODE_RAINBOW = 2

        private const val DEFAULT_DURATION_MS = 3800L
        private const val FADE_DURATION_MS = 350L

        private const val MAX_BUBBLES = 30
        private const val MAX_PARTICLES = 40
        private const val MAX_BEAM_PARTICLES = 25
        private const val MAX_QUANTUM_SPARKS = 50
        private const val MAX_NEBULA_CLOUDS = 20
        private const val MAX_MATRIX_CHARS = 30
        private const val MAX_GEO_SHAPES = 25
        private const val MAX_VORTEX_PARTICLES = 36
        private const val MAX_STARS = 45
        private const val MAX_FIBER_PARTICLES = 28
    }

    // Shared paints
    private val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val neonGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val beamPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val plasmaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val sparkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val nebulaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val matrixPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textSize = 22f
        typeface = Typeface.MONOSPACE
    }
    private val geoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val geoFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    // Rings & Text Paints
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val glowRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val centerGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val solidBlackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.BLACK
    }
    private val textNumberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.RIGHT
        typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        color = Color.WHITE
    }
    private val textDecimalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.LEFT
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        color = Color.WHITE
    }
    private val thinNumberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.RIGHT
        typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        color = Color.WHITE
    }
    private val thinDecimalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.LEFT
        typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        color = Color.WHITE
    }
    private val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
        letterSpacing = 0.15f
    }
    private val scrimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0xEA000000.toInt()
    }
    private val boltPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }
    private val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    private val boltPath = Path()
    private val smallBoltPath = Path()
    private val shapePath = Path()

    var batteryLevel: Int = 0
        set(value) {
            field = value.coerceIn(0, 100)
            invalidate()
        }

    var animationStyle: Int = STYLE_XPERIENCE
        set(value) {
            field = value.coerceIn(STYLE_AOSP_RIPPLE, STYLE_GEOFLOW)
            invalidate()
        }

    var colorMode: Int = COLOR_MODE_DEFAULT
        set(value) {
            field = value.coerceIn(COLOR_MODE_DEFAULT, COLOR_MODE_RAINBOW)
            invalidate()
        }

    var rippleOpacity: Float = 0.6f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    var glowIntensity: Float = 0.8f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    var arcCount: Int = 3
        set(value) {
            field = value.coerceIn(1, 6)
            invalidate()
        }

    var defaultChargingColor: Int = 0xFF00E5FF.toInt()
    var accentColor: Int = 0xFF2196F3.toInt()

    private var currentAlpha: Float = 0f
    private var isAnimating: Boolean = false
    private var hideRunnable: Runnable? = null

    // Animators
    private var mainAnimator: ValueAnimator? = null
    private var fadeAnimator: ValueAnimator? = null
    private var rainbowAnimator: ValueAnimator? = null
    private var rainbowHue: Float = 0f

    // Styles state
    private var animProgress: Float = 0f
    private var rotationAngle: Float = 0f

    // Particles Data Structures
    private data class Bubble(
        var x: Float = 0f, var y: Float = 0f, var velocityY: Float = 0f,
        var velocityX: Float = 0f, var life: Float = 0f, var size: Float = 0f,
        var wobblePhase: Float = 0f, var wobbleSpeed: Float = 0f, var active: Boolean = false
    )

    private data class Particle(
        var x: Float = 0f, var y: Float = 0f, var velocityY: Float = 0f,
        var velocityX: Float = 0f, var life: Float = 0f, var size: Float = 0f,
        var active: Boolean = false
    )

    private data class BeamParticle(
        var x: Float = 0f, var y: Float = 0f, var velocityY: Float = 0f,
        var size: Float = 0f, var life: Float = 0f, var active: Boolean = false,
        val trail: MutableList<Pair<Float, Float>> = mutableListOf()
    )

    private data class PlasmaBlob(
        var x: Float = 0f, var y: Float = 0f, var radius: Float = 0f,
        var velocityX: Float = 0f, var velocityY: Float = 0f, var phase: Float = 0f,
        var hueOffset: Float = 0f, var active: Boolean = false
    )

    private data class QuantumSpark(
        var x: Float = 0f, var y: Float = 0f, var velocityX: Float = 0f,
        var velocityY: Float = 0f, var life: Float = 0f, var size: Float = 0f,
        var brightness: Float = 0f, var active: Boolean = false,
        val sparkTrail: MutableList<Pair<Float, Float>> = mutableListOf()
    )

    private data class NebulaCloud(
        var x: Float = 0f, var y: Float = 0f, var radius: Float = 0f,
        var velocityY: Float = 0f, var life: Float = 0f, var expansion: Float = 0f,
        var rotation: Float = 0f, var opacity: Float = 0f, var active: Boolean = false
    )

    private data class MatrixChar(
        var x: Float = 0f, var y: Float = 0f, var velocityY: Float = 0f,
        var char: String = "", var brightness: Float = 0f, var life: Float = 0f,
        var active: Boolean = false,
        val trail: MutableList<Triple<Float, Float, Float>> = mutableListOf()
    )

    private data class GeoShape(
        var x: Float = 0f, var y: Float = 0f, var velocityY: Float = 0f,
        var size: Float = 0f, var rotation: Float = 0f, var rotationSpeed: Float = 0f,
        var life: Float = 0f, var shapeType: Int = 0, var targetShape: Int = 0,
        var morphProgress: Float = 0f, var active: Boolean = false
    )

    private data class VortexParticle(
        var angle: Float = 0f, var radius: Float = 0f, var speed: Float = 0f,
        var size: Float = 0f, var alpha: Float = 0f, var active: Boolean = false
    )

    private data class StarParticle(
        var normX: Float = 0f, var normY: Float = 0f, var size: Float = 0f,
        var alpha: Float = 0f, var twinkleSpeed: Float = 0f, var twinklePhase: Float = 0f
    )

    private data class FiberParticle(
        var lane: Int = 0, var progress: Float = 0f, var speed: Float = 0f,
        var size: Float = 0f, var alpha: Float = 0f
    )

    // Pools
    private val bubbles = mutableListOf<Bubble>()
    private val particles = mutableListOf<Particle>()
    private val beamParticles = mutableListOf<BeamParticle>()
    private val plasmaBlobs = mutableListOf<PlasmaBlob>()
    private val quantumSparks = mutableListOf<QuantumSpark>()
    private val nebulaClouds = mutableListOf<NebulaCloud>()
    private val matrixChars = mutableListOf<MatrixChar>()
    private val geoShapes = mutableListOf<GeoShape>()
    private val vortexParticles = mutableListOf<VortexParticle>()
    private val starParticles = mutableListOf<StarParticle>()
    private val fiberParticles = mutableListOf<FiberParticle>()

    private val matrixCharSet = "0101010101XYZW".map { it.toString() }

    init {
        setWillNotDraw(false)
        visibility = View.GONE
        initBoltPath()
        initVortexParticles()
        initStars()
        initFibers()
    }

    private fun initBoltPath() {
        boltPath.reset()
        boltPath.moveTo(0f, -28f)
        boltPath.lineTo(-14f, 2f)
        boltPath.lineTo(-2f, 2f)
        boltPath.lineTo(-6f, 28f)
        boltPath.lineTo(14f, -2f)
        boltPath.lineTo(2f, -2f)
        boltPath.close()

        smallBoltPath.reset()
        smallBoltPath.moveTo(0f, -10f)
        smallBoltPath.lineTo(-5f, 1f)
        smallBoltPath.lineTo(-1f, 1f)
        smallBoltPath.lineTo(-2f, 10f)
        smallBoltPath.lineTo(5f, -1f)
        smallBoltPath.lineTo(1f, -1f)
        smallBoltPath.close()
    }

    private fun initVortexParticles() {
        vortexParticles.clear()
        for (i in 0 until MAX_VORTEX_PARTICLES) {
            vortexParticles.add(
                VortexParticle(
                    angle = Random.nextFloat() * 360f,
                    radius = 120f + Random.nextFloat() * 140f,
                    speed = 1.2f + Random.nextFloat() * 2.5f,
                    size = 2f + Random.nextFloat() * 4f,
                    alpha = 0.3f + Random.nextFloat() * 0.7f,
                    active = true
                )
            )
        }
    }

    private fun initStars() {
        starParticles.clear()
        for (i in 0 until MAX_STARS) {
            // inside a disk with r <= 0.85
            val r = sqrt(Random.nextFloat()) * 0.82f
            val theta = Random.nextFloat() * 2f * PI.toFloat()
            starParticles.add(
                StarParticle(
                    normX = cos(theta) * r,
                    normY = sin(theta) * r,
                    size = 1f + Random.nextFloat() * 2.2f,
                    alpha = 0.3f + Random.nextFloat() * 0.7f,
                    twinkleSpeed = 0.04f + Random.nextFloat() * 0.08f,
                    twinklePhase = Random.nextFloat() * 2f * PI.toFloat()
                )
            )
        }
    }

    private fun initFibers() {
        fiberParticles.clear()
        for (i in 0 until MAX_FIBER_PARTICLES) {
            fiberParticles.add(
                FiberParticle(
                    lane = Random.nextInt(-9, 10),
                    progress = Random.nextFloat(),
                    speed = 0.015f + Random.nextFloat() * 0.025f,
                    size = 1.5f + Random.nextFloat() * 2.5f,
                    alpha = 0.4f + Random.nextFloat() * 0.6f
                )
            )
        }
    }

    fun show(level: Int = batteryLevel, duration: Long = DEFAULT_DURATION_MS) {
        batteryLevel = level
        isAnimating = true
        visibility = View.VISIBLE
        bringToFront()

        hideRunnable?.let { removeCallbacks(it) }

        startAnimation()

        // Fade in
        fadeAnimator?.cancel()
        fadeAnimator = ValueAnimator.ofFloat(currentAlpha, 1f).apply {
            this.duration = FADE_DURATION_MS
            addUpdateListener {
                currentAlpha = it.animatedValue as Float
                alpha = currentAlpha
                invalidate()
            }
            start()
        }

        if (duration > 0) {
            val task = Runnable { hide() }
            hideRunnable = task
            postDelayed(task, duration)
        }
    }

    fun hide() {
        hideRunnable?.let { removeCallbacks(it) }
        hideRunnable = null

        fadeAnimator?.cancel()
        fadeAnimator = ValueAnimator.ofFloat(currentAlpha, 0f).apply {
            this.duration = FADE_DURATION_MS
            addUpdateListener {
                currentAlpha = it.animatedValue as Float
                alpha = currentAlpha
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    isAnimating = false
                    visibility = View.GONE
                    stopAllAnimations()
                }
            })
            start()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (isVisible && isAnimating) {
            hide()
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun getCurrentColor(): Int {
        return when (colorMode) {
            COLOR_MODE_ACCENT -> accentColor
            COLOR_MODE_RAINBOW -> hsvToColor(rainbowHue)
            else -> when (animationStyle) {
                STYLE_XPERIENCE -> if (batteryLevel >= 90) 0xFF00E676.toInt() else 0xFF00E5FF.toInt()
                STYLE_WARP_PULSE -> 0xFF00E676.toInt()
                STYLE_XIAOMI_HYPEROS -> 0xFF2979FF.toInt() // Vibrant electric blue / violet
                STYLE_OPPO_SUPERVOOC -> 0xFF00E5FF.toInt()
                STYLE_DIGITAL_MATRIX -> 0xFF00FF66.toInt()
                else -> defaultChargingColor
            }
        }
    }

    private fun hsvToColor(hue: Float): Int {
        val hsv = floatArrayOf(hue, 0.85f, 0.95f)
        return Color.HSVToColor(hsv)
    }

    private fun startAnimation() {
        stopAllAnimations()

        if (colorMode == COLOR_MODE_RAINBOW) {
            rainbowAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
                duration = 4000L
                repeatCount = ValueAnimator.INFINITE
                interpolator = LinearInterpolator()
                addUpdateListener {
                    rainbowHue = it.animatedValue as Float
                    invalidate()
                }
                start()
            }
        }

        mainAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1600L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                animProgress = animator.animatedValue as Float
                rotationAngle = (rotationAngle + 2.5f) % 360f
                updateParticles()
                invalidate()
            }
            start()
        }
    }

    private fun stopAllAnimations() {
        mainAnimator?.cancel()
        mainAnimator = null
        rainbowAnimator?.cancel()
        rainbowAnimator = null
    }

    private fun updateParticles() {
        val usbY = height.toFloat()
        val centerX = width / 2f
        val centerY = height * 0.57f

        when (animationStyle) {
            STYLE_XPERIENCE -> {
                vortexParticles.forEach { p ->
                    p.angle = (p.angle + p.speed) % 360f
                    p.radius -= 1.2f
                    if (p.radius < 80f) {
                        p.radius = 120f + Random.nextFloat() * 120f
                        p.angle = Random.nextFloat() * 360f
                    }
                }
            }
            STYLE_XIAOMI_HYPEROS -> {
                fiberParticles.forEach { fp ->
                    fp.progress += fp.speed
                    if (fp.progress >= 1f) {
                        fp.progress = 0f
                        fp.lane = Random.nextInt(-9, 10)
                    }
                }
            }
            STYLE_OPPO_SUPERVOOC -> {
                starParticles.forEach { sp ->
                    sp.twinklePhase = (sp.twinklePhase + sp.twinkleSpeed) % (2f * PI.toFloat())
                }
            }
            STYLE_WARP_PULSE -> {
                if (Random.nextFloat() < 0.25f && quantumSparks.size < MAX_QUANTUM_SPARKS) {
                    val angle = Random.nextFloat() * 2f * PI.toFloat()
                    val speed = 3f + Random.nextFloat() * 5f
                    quantumSparks.add(
                        QuantumSpark(
                            x = centerX,
                            y = centerY,
                            velocityX = cos(angle) * speed,
                            velocityY = sin(angle) * speed,
                            life = 1f,
                            size = 3f + Random.nextFloat() * 5f,
                            brightness = 1f,
                            active = true
                        )
                    )
                }
                val iter = quantumSparks.iterator()
                while (iter.hasNext()) {
                    val spark = iter.next()
                    spark.x += spark.velocityX
                    spark.y += spark.velocityY
                    spark.life -= 0.03f
                    if (spark.life <= 0f) iter.remove()
                }
            }
            STYLE_BUBBLE_STREAM -> {
                if (Random.nextFloat() < 0.2f && bubbles.size < MAX_BUBBLES) {
                    bubbles.add(
                        Bubble(
                            x = centerX + (Random.nextFloat() - 0.5f) * 60f,
                            y = usbY,
                            velocityY = -4f - Random.nextFloat() * 4f,
                            velocityX = (Random.nextFloat() - 0.5f) * 1.5f,
                            life = 1f,
                            size = 12f + Random.nextFloat() * 18f,
                            wobblePhase = Random.nextFloat() * 360f,
                            wobbleSpeed = 0.05f + Random.nextFloat() * 0.05f,
                            active = true
                        )
                    )
                }
                val iter = bubbles.iterator()
                while (iter.hasNext()) {
                    val b = iter.next()
                    b.wobblePhase += b.wobbleSpeed
                    b.x += b.velocityX + sin(b.wobblePhase) * 2f
                    b.y += b.velocityY
                    b.life -= 0.008f
                    if (b.life <= 0f || b.y < height * 0.4f) iter.remove()
                }
            }
            STYLE_DIGITAL_MATRIX -> {
                if (Random.nextFloat() < 0.2f && matrixChars.size < MAX_MATRIX_CHARS) {
                    matrixChars.add(
                        MatrixChar(
                            x = Random.nextFloat() * width,
                            y = usbY,
                            velocityY = -5f - Random.nextFloat() * 6f,
                            char = matrixCharSet.random(),
                            brightness = 0.8f + Random.nextFloat() * 0.2f,
                            life = 1f,
                            active = true
                        )
                    )
                }
                val iter = matrixChars.iterator()
                while (iter.hasNext()) {
                    val c = iter.next()
                    c.y += c.velocityY
                    c.life -= 0.012f
                    if (c.life <= 0f || c.y < height * 0.35f) iter.remove()
                }
            }
            else -> {}
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (currentAlpha <= 0f) return

        val widthF = width.toFloat()
        val heightF = height.toFloat()
        val centerX = widthF / 2f
        // Lowered position to sit in the lower-middle half, below clock and widgets
        val centerY = heightF * 0.57f
        val currentColor = getCurrentColor()

        // Dark cinematic scrim backdrop
        scrimPaint.alpha = (235 * currentAlpha).toInt().coerceIn(0, 255)
        canvas.drawRect(0f, 0f, widthF, heightF, scrimPaint)

        when (animationStyle) {
            STYLE_AOSP_RIPPLE -> drawAospRipple(canvas, centerX, centerY, currentColor)
            STYLE_XPERIENCE -> drawXPerience(canvas, centerX, centerY, currentColor)
            STYLE_WARP_PULSE -> drawWarpPulse(canvas, centerX, centerY, currentColor)
            STYLE_XIAOMI_HYPEROS -> drawXiaomiHyperOS(canvas, centerX, centerY, heightF, currentColor)
            STYLE_OPPO_SUPERVOOC -> drawOppoSuperVOOC(canvas, centerX, centerY, currentColor)
            STYLE_BUBBLE_STREAM -> drawBubbleStream(canvas, centerX, centerY, currentColor)
            STYLE_NEON -> drawNeon(canvas, centerX, centerY, heightF, currentColor)
            STYLE_BEAM -> drawBeam(canvas, centerX, centerY, heightF, currentColor)
            STYLE_PLASMA -> drawPlasma(canvas, centerX, centerY, currentColor)
            STYLE_QUANTUM_SPARKS -> drawQuantumSparks(canvas, centerX, centerY, currentColor)
            STYLE_NEBULA -> drawNebula(canvas, centerX, centerY, currentColor)
            STYLE_DIGITAL_MATRIX -> drawDigitalMatrix(canvas, centerX, centerY, heightF, currentColor)
            STYLE_GEOFLOW -> drawGeoFlow(canvas, centerX, centerY, heightF, currentColor)
        }
    }

    /**
     * Xiaomi HyperOS (Turbo Charge):
     * Faithful recreation of hyperos.jpg:
     * Dense glowing fiber tail from bottom USB port into ring, 360 radial bloom rays,
     * concentric side shockwave arcs, pure black interior, clean thin font with live decimals,
     * and gold 67W MAX with lightning bolt.
     */
    private fun drawXiaomiHyperOS(canvas: Canvas, cx: Float, cy: Float, h: Float, color: Int) {
        val density = resources.displayMetrics.density
        val baseRadius = 86f * density
        val usbY = h

        // 1. Vertical Light Fiber Tail from USB port up to ring
        val fiberWidth = 32f * density
        val topY = cy + baseRadius - 2f * density
        val trunkHeight = usbY - topY

        // Glowing trunk background gradient
        val trunkShader = LinearGradient(
            cx, usbY, cx, topY,
            intArrayOf(0x002979FF, (0xCC2979FF).toInt(), 0xFF00E5FF.toInt()),
            floatArrayOf(0f, 0.4f, 1f),
            Shader.TileMode.CLAMP
        )
        glowRingPaint.shader = trunkShader
        glowRingPaint.strokeWidth = 4f * density
        glowRingPaint.alpha = (210 * currentAlpha).toInt().coerceIn(0, 255)

        // Multiple vertical fiber lines
        for (i in -8..8) {
            val fx = cx + (i * 1.8f * density)
            val lineAlpha = ((1f - abs(i) / 9f) * 180 * currentAlpha).toInt().coerceIn(0, 255)
            glowRingPaint.alpha = lineAlpha
            canvas.drawLine(fx, usbY, fx, topY, glowRingPaint)
        }
        glowRingPaint.shader = null

        // Flowing light particles along fibers
        sparkPaint.color = 0xFFFFFFFF.toInt()
        fiberParticles.forEach { fp ->
            val fx = cx + (fp.lane * 1.6f * density)
            val fy = usbY - (fp.progress * trunkHeight)
            val pAlpha = (fp.alpha * 255 * currentAlpha).toInt().coerceIn(0, 255)
            sparkPaint.alpha = pAlpha
            canvas.drawCircle(fx, fy, fp.size * density, sparkPaint)
        }

        // 2. 360 Radial Blooming Flare Rays
        val rayPaint = sparkPaint
        val numRays = 32
        for (i in 0 until numRays) {
            val angle = Math.toRadians((i * (360f / numRays) + rotationAngle * 0.4f).toDouble())
            val innerR = baseRadius * 0.95f
            val rayLength = (baseRadius * (1.18f + 0.22f * sin(animProgress * 2f * PI.toFloat() + i))).toFloat()
            val startX = cx + (cos(angle) * innerR).toFloat()
            val startY = cy + (sin(angle) * innerR).toFloat()
            val endX = cx + (cos(angle) * rayLength).toFloat()
            val endY = cy + (sin(angle) * rayLength).toFloat()

            rayPaint.color = if (i % 2 == 0) 0xFF7C4DFF.toInt() else 0xFF2979FF.toInt()
            rayPaint.strokeWidth = 2.5f * density
            rayPaint.alpha = (100 * glowIntensity * currentAlpha).toInt().coerceIn(0, 255)
            canvas.drawLine(startX, startY, endX, endY, rayPaint)
        }

        // 3. Side Curved Shockwave Energy Arcs (Left & Right)
        ringPaint.strokeWidth = 2.2f * density
        ringPaint.color = 0xFF2979FF.toInt()
        val pulseDist = (12f + (animProgress * 18f)) * density
        val sideRect = RectF(cx - baseRadius - pulseDist, cy - baseRadius - pulseDist,
                             cx + baseRadius + pulseDist, cy + baseRadius + pulseDist)
        ringPaint.alpha = ((1f - animProgress) * 160 * currentAlpha).toInt().coerceIn(0, 255)
        canvas.drawArc(sideRect, 130f, 100f, false, ringPaint)
        canvas.drawArc(sideRect, -50f, 100f, false, ringPaint)

        // 4. Central Solid Black Core
        solidBlackPaint.alpha = (255 * currentAlpha).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, baseRadius * 0.92f, solidBlackPaint)

        // 5. Main Intense Glowing Circular Rim
        ringPaint.strokeWidth = 4.5f * density
        ringPaint.color = 0xFF2979FF.toInt()
        ringPaint.alpha = (255 * currentAlpha).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, baseRadius, ringPaint)

        glowRingPaint.strokeWidth = 2f * density
        glowRingPaint.color = Color.WHITE
        glowRingPaint.alpha = (220 * currentAlpha).toInt().coerceIn(0, 255)
        val rimRect = RectF(cx - baseRadius, cy - baseRadius, cx + baseRadius, cy + baseRadius)
        canvas.save()
        canvas.rotate(rotationAngle * 1.6f, cx, cy)
        canvas.drawArc(rimRect, 0f, 120f, false, glowRingPaint)
        canvas.drawArc(rimRect, 180f, 120f, false, glowRingPaint)
        canvas.restore()

        // 6. Battery % with clean typography & live decimals
        val bigSize = 50f * density
        val decSize = 20f * density
        thinNumberPaint.textSize = bigSize
        thinNumberPaint.alpha = (255 * currentAlpha).toInt().coerceIn(0, 255)
        thinDecimalPaint.textSize = decSize
        thinDecimalPaint.alpha = (235 * currentAlpha).toInt().coerceIn(0, 255)

        val intStr = "$batteryLevel"
        val fastDecimal = ((SystemClock.uptimeMillis() / 25) % 100).toInt()
        val decStr = String.format(".%02d%%", fastDecimal)

        val intW = thinNumberPaint.measureText(intStr)
        val decW = thinDecimalPaint.measureText(decStr)
        val totalW = intW + decW
        val textStartX = cx - (totalW / 2f) + intW
        val textY = cy + (bigSize * 0.12f)

        canvas.drawText(intStr, textStartX, textY, thinNumberPaint)
        canvas.drawText(decStr, textStartX + 3f * density, textY - (bigSize * 0.28f), thinDecimalPaint)

        // 7. Gold / Yellow "67W MAX" + Lightning Bolt
        val goldColor = 0xFFFFD600.toInt()
        badgePaint.textSize = 15f * density
        badgePaint.color = goldColor
        badgePaint.alpha = (255 * currentAlpha).toInt().coerceIn(0, 255)
        canvas.drawText("67W MAX", cx, cy + (34f * density), badgePaint)

        canvas.save()
        canvas.translate(cx, cy + (50f * density))
        canvas.scale(density * 0.8f, density * 0.8f)
        boltPaint.color = goldColor
        boltPaint.alpha = (255 * currentAlpha).toInt().coerceIn(0, 255)
        canvas.drawPath(smallBoltPath, boltPaint)
        canvas.restore()
    }

    /**
     * OnePlus / Oppo SUPERVOOC:
     * Faithful recreation of supervooc.jpg:
     * Overlapping iridescent glass bubble orbs, deep cosmic night lens with glittering dust,
     * chromatic dispersion prismatic rim, clean bold white font with decimals, and SUPERVOOC™ 100W badge.
     */
    private fun drawOppoSuperVOOC(canvas: Canvas, cx: Float, cy: Float, color: Int) {
        val density = resources.displayMetrics.density
        val baseRadius = 90f * density

        // 1. Overlapping Soft Iridescent Glass Bubble Orbs behind lens
        val bubblePaintLocal = bubblePaint
        bubblePaintLocal.style = Paint.Style.STROKE
        bubblePaintLocal.strokeWidth = 3f * density

        // Top-left orb
        val orbShader1 = RadialGradient(
            cx - 50f * density, cy - 40f * density, baseRadius * 1.2f,
            intArrayOf(0x357C4DFF.toInt(), 0x15FF6D00.toInt(), Color.TRANSPARENT),
            floatArrayOf(0.3f, 0.7f, 1f),
            Shader.TileMode.CLAMP
        )
        centerGlowPaint.shader = orbShader1
        canvas.drawCircle(cx - 50f * density, cy - 40f * density, baseRadius * 1.15f, centerGlowPaint)

        // Right orb
        val orbShader2 = RadialGradient(
            cx + 60f * density, cy + 20f * density, baseRadius * 1.1f,
            intArrayOf(0x3000E5FF.toInt(), 0x10FF4081.toInt(), Color.TRANSPARENT),
            floatArrayOf(0.3f, 0.7f, 1f),
            Shader.TileMode.CLAMP
        )
        centerGlowPaint.shader = orbShader2
        canvas.drawCircle(cx + 60f * density, cy + 20f * density, baseRadius * 1.05f, centerGlowPaint)

        // 2. Cosmic Dark Lens Interior (#0B0B1A)
        val lensShader = RadialGradient(
            cx, cy, baseRadius,
            intArrayOf(0xFF14142B.toInt(), 0xFF070710.toInt()),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        centerGlowPaint.shader = lensShader
        canvas.drawCircle(cx, cy, baseRadius, centerGlowPaint)

        // 3. Glittering Star Dust / Cosmic Particles inside the lens
        sparkPaint.color = Color.WHITE
        starParticles.forEach { s ->
            val px = cx + (s.normX * baseRadius)
            val py = cy + (s.normY * baseRadius)
            val twinkle = (0.5f + 0.5f * sin(s.twinklePhase))
            val starAlpha = (s.alpha * twinkle * 255 * currentAlpha).toInt().coerceIn(0, 255)
            sparkPaint.alpha = starAlpha
            canvas.drawCircle(px, py, s.size * density, sparkPaint)
        }

        // 4. Prismatic / Chromatic Dispersion Border Rim
        val rainbowColors = intArrayOf(
            0xFF00E5FF.toInt(), 0xFF2979FF.toInt(), 0xFF7C4DFF.toInt(),
            0xFFFF4081.toInt(), 0xFFFF6D00.toInt(), 0xFFFFD600.toInt(),
            0xFF00E5FF.toInt()
        )
        val chromaticShader = SweepGradient(cx, cy, rainbowColors, null)
        ringPaint.shader = chromaticShader
        ringPaint.strokeWidth = 3.8f * density
        ringPaint.alpha = (245 * currentAlpha).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, baseRadius, ringPaint)
        ringPaint.shader = null

        // Glossy Specular Reflection Arc on Top-Left
        glowRingPaint.strokeWidth = 2.2f * density
        glowRingPaint.color = Color.WHITE
        glowRingPaint.alpha = (230 * currentAlpha).toInt().coerceIn(0, 255)
        val rimRect = RectF(cx - baseRadius, cy - baseRadius, cx + baseRadius, cy + baseRadius)
        canvas.drawArc(rimRect, 200f, 85f, false, glowRingPaint)

        // 5. Battery % (Bold White with decimals: 86.20%)
        val bigSize = 44f * density
        val decSize = 26f * density
        textNumberPaint.textSize = bigSize
        textNumberPaint.alpha = (255 * currentAlpha).toInt().coerceIn(0, 255)
        textDecimalPaint.textSize = decSize
        textDecimalPaint.alpha = (240 * currentAlpha).toInt().coerceIn(0, 255)

        val intStr = "$batteryLevel"
        val fastDecimal = ((SystemClock.uptimeMillis() / 30) % 100).toInt()
        val decStr = String.format(".%02d%%", fastDecimal)

        val intW = textNumberPaint.measureText(intStr)
        val decW = textDecimalPaint.measureText(decStr)
        val totalW = intW + decW
        val textStartX = cx - (totalW / 2f) + intW
        val textY = cy + (bigSize * 0.08f)

        canvas.drawText(intStr, textStartX, textY, textNumberPaint)
        canvas.drawText(decStr, textStartX + 2f * density, textY - (bigSize * 0.05f), textDecimalPaint)

        // 6. Pill Badge with ⚡ SUPERVOOC™ and 100W
        val badgeY = cy + (36f * density)
        val pillWidth = 110f * density
        val pillHeight = 22f * density
        val pillRect = RectF(cx - pillWidth / 2f, badgeY - pillHeight / 2f,
                             cx + pillWidth / 2f, badgeY + pillHeight / 2f)

        pillPaint.color = 0x33FFFFFF.toInt()
        pillPaint.alpha = (180 * currentAlpha).toInt().coerceIn(0, 255)
        canvas.drawRoundRect(pillRect, pillHeight / 2f, pillHeight / 2f, pillPaint)

        // Lightning bolt in badge
        canvas.save()
        canvas.translate(cx - (pillWidth * 0.35f), badgeY)
        canvas.scale(density * 0.7f, density * 0.7f)
        boltPaint.color = Color.WHITE
        boltPaint.alpha = (255 * currentAlpha).toInt().coerceIn(0, 255)
        canvas.drawPath(smallBoltPath, boltPaint)
        canvas.restore()

        // Text SUPERVOOC™
        badgePaint.textSize = 10f * density
        badgePaint.color = Color.WHITE
        badgePaint.alpha = (255 * currentAlpha).toInt().coerceIn(0, 255)
        canvas.drawText("SUPERVOOC™ 100W", cx + (6f * density), badgeY + (3.5f * density), badgePaint)
    }

    /**
     * XPerience:
     * Concentric dual rotating rings, swirling vortex particles into center,
     * bold percentage with live decimal counter, and XPERIENCE badge.
     */
    private fun drawXPerience(canvas: Canvas, cx: Float, cy: Float, color: Int) {
        val density = resources.displayMetrics.density
        val baseRadius = 88f * density

        // 1. Swirling Vortex Particles into center
        sparkPaint.color = color
        vortexParticles.forEach { p ->
            val rad = Math.toRadians(p.angle.toDouble())
            val px = cx + (cos(rad) * p.radius * density * 0.8f).toFloat()
            val py = cy + (sin(rad) * p.radius * density * 0.8f).toFloat()
            sparkPaint.alpha = (p.alpha * 255 * currentAlpha).toInt().coerceIn(0, 255)
            canvas.drawCircle(px, py, p.size * density, sparkPaint)
        }

        // 2. Central Glow Aura
        val glowRadius = baseRadius * 1.35f
        val glowAlpha = (110 * glowIntensity * currentAlpha).toInt().coerceIn(0, 255)
        centerGlowPaint.shader = RadialGradient(
            cx, cy, glowRadius,
            intArrayOf(color and 0x00FFFFFF or (glowAlpha shl 24), Color.TRANSPARENT),
            floatArrayOf(0.4f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, glowRadius, centerGlowPaint)

        // 3. Outer Ring with Arcs (Clockwise)
        ringPaint.strokeWidth = 3.5f * density
        ringPaint.color = color
        ringPaint.alpha = (230 * currentAlpha).toInt().coerceIn(0, 255)
        val outerRect = RectF(cx - baseRadius, cy - baseRadius, cx + baseRadius, cy + baseRadius)

        canvas.save()
        canvas.rotate(rotationAngle, cx, cy)
        canvas.drawArc(outerRect, 0f, 110f, false, ringPaint)
        canvas.drawArc(outerRect, 180f, 110f, false, ringPaint)
        canvas.restore()

        // 4. Inner Ring (Counter-Clockwise)
        val innerRadius = baseRadius * 0.88f
        val innerRect = RectF(cx - innerRadius, cy - innerRadius, cx + innerRadius, cy + innerRadius)
        glowRingPaint.strokeWidth = 2f * density
        glowRingPaint.color = Color.WHITE
        glowRingPaint.alpha = (180 * currentAlpha).toInt().coerceIn(0, 255)

        canvas.save()
        canvas.rotate(-rotationAngle * 1.4f, cx, cy)
        canvas.drawArc(innerRect, 45f, 60f, false, glowRingPaint)
        canvas.drawArc(innerRect, 165f, 60f, false, glowRingPaint)
        canvas.drawArc(innerRect, 285f, 60f, false, glowRingPaint)
        canvas.restore()

        // 5. Battery Percentage with Fast Decimal Counter
        val bigTextSize = 48f * density
        val decTextSize = 20f * density
        textNumberPaint.textSize = bigTextSize
        textNumberPaint.alpha = (255 * currentAlpha).toInt().coerceIn(0, 255)
        textDecimalPaint.textSize = decTextSize
        textDecimalPaint.alpha = (230 * currentAlpha).toInt().coerceIn(0, 255)

        val intStr = "$batteryLevel"
        val fastDecimal = ((SystemClock.uptimeMillis() / 25) % 100).toInt()
        val decStr = String.format(".%02d%%", fastDecimal)

        val intWidth = textNumberPaint.measureText(intStr)
        val decWidth = textDecimalPaint.measureText(decStr)
        val totalTextWidth = intWidth + decWidth
        val startX = cx - (totalTextWidth / 2f) + intWidth
        val textY = cy + (bigTextSize * 0.32f)

        canvas.drawText(intStr, startX, textY, textNumberPaint)
        canvas.drawText(decStr, startX + (4f * density), textY - (bigTextSize * 0.22f), textDecimalPaint)

        // 6. XPerience Badge
        val badgeY = cy + baseRadius + (32f * density)
        badgePaint.textSize = 14f * density
        badgePaint.color = color
        badgePaint.alpha = (240 * currentAlpha).toInt().coerceIn(0, 255)
        canvas.drawText("XPERIENCE", cx, badgeY, badgePaint)
    }

    /**
     * Flash Energy / Warp Pulse:
     * Pulsing concentric energy waves, bright electric lightning bolt, sparks, and WARP CHARGE badge.
     */
    private fun drawWarpPulse(canvas: Canvas, cx: Float, cy: Float, color: Int) {
        val density = resources.displayMetrics.density

        // 1. Concentric Expanding Pulse Waves
        ringPaint.strokeWidth = 2.5f * density
        ringPaint.color = color
        for (i in 0..2) {
            val waveProgress = (animProgress + i * 0.333f) % 1f
            val waveRadius = (40f + waveProgress * 90f) * density
            val waveAlpha = ((1f - waveProgress) * 220 * currentAlpha).toInt().coerceIn(0, 255)
            ringPaint.alpha = waveAlpha
            canvas.drawCircle(cx, cy, waveRadius, ringPaint)
        }

        // 2. Center Energy Circle
        centerGlowPaint.shader = null
        centerGlowPaint.color = color and 0x00FFFFFF or ((160 * currentAlpha).toInt() shl 24)
        canvas.drawCircle(cx, cy, 38f * density, centerGlowPaint)

        // 3. Lightning Bolt in Center
        canvas.save()
        canvas.translate(cx, cy)
        canvas.scale(density * 0.9f, density * 0.9f)
        boltPaint.color = Color.WHITE
        boltPaint.alpha = (255 * currentAlpha).toInt().coerceIn(0, 255)
        canvas.drawPath(boltPath, boltPaint)
        canvas.restore()

        // 4. Electric Sparks
        sparkPaint.color = color
        quantumSparks.forEach { s ->
            sparkPaint.alpha = (s.life * 255 * currentAlpha).toInt().coerceIn(0, 255)
            canvas.drawCircle(s.x, s.y, s.size * density, sparkPaint)
        }

        // 5. Battery Text & Badge
        textNumberPaint.textAlign = Paint.Align.CENTER
        textNumberPaint.textSize = 34f * density
        textNumberPaint.alpha = (255 * currentAlpha).toInt().coerceIn(0, 255)
        canvas.drawText("$batteryLevel%", cx, cy + (64f * density), textNumberPaint)
        textNumberPaint.textAlign = Paint.Align.RIGHT

        badgePaint.textSize = 14f * density
        badgePaint.color = color
        badgePaint.alpha = (240 * currentAlpha).toInt().coerceIn(0, 255)
        canvas.drawText("WARP CHARGE", cx, cy + (88f * density), badgePaint)
    }

    private fun drawAospRipple(canvas: Canvas, cx: Float, cy: Float, color: Int) {
        val density = resources.displayMetrics.density
        ringPaint.strokeWidth = 3f * density
        ringPaint.color = color

        for (i in 0..2) {
            val p = (animProgress + i * 0.33f) % 1f
            val r = (30f + p * 120f) * density
            val a = ((1f - p) * 200 * currentAlpha).toInt().coerceIn(0, 255)
            ringPaint.alpha = a
            canvas.drawCircle(cx, cy, r, ringPaint)
        }

        textNumberPaint.textAlign = Paint.Align.CENTER
        textNumberPaint.textSize = 44f * density
        textNumberPaint.alpha = (255 * currentAlpha).toInt().coerceIn(0, 255)
        canvas.drawText("$batteryLevel%", cx, cy + (14f * density), textNumberPaint)
        textNumberPaint.textAlign = Paint.Align.RIGHT
    }

    private fun drawBubbleStream(canvas: Canvas, cx: Float, cy: Float, color: Int) {
        bubbles.forEach { b ->
            val alpha = (255 * b.life * currentAlpha * rippleOpacity).toInt().coerceIn(0, 255)
            bubblePaint.color = color and 0x00FFFFFF or (alpha shl 24)
            canvas.drawCircle(b.x, b.y, b.size, bubblePaint)
        }
        drawCenterBatteryInfo(canvas, cx, cy, color, "CHARGING")
    }

    private fun drawNeon(canvas: Canvas, cx: Float, cy: Float, usbY: Float, color: Int) {
        val glowHeight = height * 0.4f
        val shader = LinearGradient(
            cx, usbY, cx, usbY - glowHeight,
            intArrayOf(color and 0x00FFFFFF or ((200 * glowIntensity * currentAlpha).toInt() shl 24), Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        neonGlowPaint.shader = shader
        canvas.drawRect(cx - 70f, usbY - glowHeight, cx + 70f, usbY, neonGlowPaint)
        drawCenterBatteryInfo(canvas, cx, cy, color, "NEON CHARGE")
    }

    private fun drawBeam(canvas: Canvas, cx: Float, cy: Float, usbY: Float, color: Int) {
        val density = resources.displayMetrics.density
        beamPaint.color = color
        for (i in 0..4) {
            val p = (animProgress + i * 0.2f) % 1f
            val y = usbY - (p * height * 0.5f)
            val alpha = ((1f - p) * 255 * currentAlpha).toInt().coerceIn(0, 255)
            beamPaint.alpha = alpha
            canvas.drawCircle(cx + (i - 2) * 18f * density, y, (6f + i * 2f) * density, beamPaint)
        }
        drawCenterBatteryInfo(canvas, cx, cy, color, "ENERGY BEAM")
    }

    private fun drawPlasma(canvas: Canvas, cx: Float, cy: Float, color: Int) {
        val density = resources.displayMetrics.density
        plasmaPaint.color = color
        for (i in 0..3) {
            val angle = Math.toRadians((animProgress * 360f + i * 90f).toDouble())
            val px = cx + (cos(angle) * 45f * density).toFloat()
            val py = cy + (sin(angle) * 45f * density).toFloat()
            plasmaPaint.alpha = (160 * currentAlpha).toInt().coerceIn(0, 255)
            canvas.drawCircle(px, py, 35f * density, plasmaPaint)
        }
        drawCenterBatteryInfo(canvas, cx, cy, color, "PLASMA")
    }

    private fun drawQuantumSparks(canvas: Canvas, cx: Float, cy: Float, color: Int) {
        sparkPaint.color = color
        val density = resources.displayMetrics.density
        for (i in 0..12) {
            val angle = Math.toRadians((animProgress * 720f + i * 30f).toDouble())
            val dist = (20f + (i * 7f % 60f)) * density
            val px = cx + (cos(angle) * dist).toFloat()
            val py = cy + (sin(angle) * dist).toFloat()
            sparkPaint.alpha = (230 * currentAlpha).toInt().coerceIn(0, 255)
            canvas.drawCircle(px, py, (2f + (i % 3)) * density, sparkPaint)
        }
        drawCenterBatteryInfo(canvas, cx, cy, color, "QUANTUM")
    }

    private fun drawNebula(canvas: Canvas, cx: Float, cy: Float, color: Int) {
        val density = resources.displayMetrics.density
        nebulaPaint.color = color
        for (i in 0..2) {
            val r = (60f + i * 25f) * density
            nebulaPaint.alpha = (80 * currentAlpha).toInt().coerceIn(0, 255)
            canvas.drawCircle(cx, cy, r, nebulaPaint)
        }
        drawCenterBatteryInfo(canvas, cx, cy, color, "COSMIC")
    }

    private fun drawDigitalMatrix(canvas: Canvas, cx: Float, cy: Float, usbY: Float, color: Int) {
        matrixPaint.color = color
        matrixChars.forEach { c ->
            val alpha = (c.life * 255 * currentAlpha).toInt().coerceIn(0, 255)
            matrixPaint.alpha = alpha
            canvas.drawText(c.char, c.x, c.y, matrixPaint)
        }
        drawCenterBatteryInfo(canvas, cx, cy, color, "CYBER MATRIX")
    }

    private fun drawGeoFlow(canvas: Canvas, cx: Float, cy: Float, usbY: Float, color: Int) {
        val density = resources.displayMetrics.density
        geoPaint.color = color
        geoPaint.alpha = (200 * currentAlpha).toInt().coerceIn(0, 255)
        for (i in 0..3) {
            val p = (animProgress + i * 0.25f) % 1f
            val y = usbY - (p * height * 0.45f)
            val size = (16f + i * 8f) * density
            canvas.save()
            canvas.translate(cx, y)
            canvas.rotate(p * 360f)
            canvas.drawRect(-size / 2f, -size / 2f, size / 2f, size / 2f, geoPaint)
            canvas.restore()
        }
        drawCenterBatteryInfo(canvas, cx, cy, color, "GEOFLOW")
    }

    private fun drawCenterBatteryInfo(canvas: Canvas, cx: Float, cy: Float, color: Int, badge: String) {
        val density = resources.displayMetrics.density
        textNumberPaint.textAlign = Paint.Align.CENTER
        textNumberPaint.textSize = 42f * density
        textNumberPaint.alpha = (255 * currentAlpha).toInt().coerceIn(0, 255)
        canvas.drawText("$batteryLevel%", cx, cy, textNumberPaint)
        textNumberPaint.textAlign = Paint.Align.RIGHT

        badgePaint.textSize = 13f * density
        badgePaint.color = color
        badgePaint.alpha = (230 * currentAlpha).toInt().coerceIn(0, 255)
        canvas.drawText(badge, cx, cy + 28f * density, badgePaint)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        hideRunnable?.let { removeCallbacks(it) }
        hideRunnable = null
        fadeAnimator?.cancel()
        fadeAnimator = null
        stopAllAnimations()
    }
}
