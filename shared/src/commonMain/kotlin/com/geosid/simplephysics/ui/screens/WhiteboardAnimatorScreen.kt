package com.geosid.simplephysics.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geosid.simplephysics.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.random.Random

enum class WhiteboardAction(val title: String, val category: String) {
    // Body & Acting
    WAVE_INTRO("👋 Wave & Intro", "Body"),
    POINT_EXPLAIN("👉 Point & Explain", "Body"),
    EUREKA("💡 Eureka Moment!", "Body"),
    PONDER_WALK("🤔 Ponder & Think", "Body"),
    MOON_JUMP("🌙 Moon Float Jump", "Body"),
    VICTORY_DANCE("🎉 Victory Dance", "Body"),

    // Drawing & Math
    DRAW_SINE_WAVE("〰️ Draw Sine Wave", "Drawing"),
    DRAW_PROJECTILE("🎯 Draw Projectile Arc", "Drawing"),
    DRAW_ATOM("⚛️ Draw Atom Orbit", "Drawing"),
    DRAW_FOURIER("🌀 Draw Fourier Wave", "Drawing"),
    DRAW_INCLINE("📐 Draw Incline Forces", "Drawing"),
    DRAW_MAGNET("🧲 Draw Magnetic Field", "Drawing"),
    ERASE_BOARD("🧽 Erase Whiteboard", "Drawing"),

    // Physics & Props
    DROP_BALL("🏀 Drop Bouncing Ball", "Physics"),
    BALLOON_POP("🎈 Citrus Balloon Pop", "Physics"),
    PENCIL_WATER_BAG("💧 Pencil & Water Bag", "Physics"),
    SWING_PENDULUM("⏳ Swing Pendulum", "Physics"),
    STATIC_SHOCK("⚡ Van de Graaff Shock", "Physics")
}

sealed interface BoardDrawing {
    data class SineWave(val progress: Float = 1f) : BoardDrawing
    data class ProjectileArc(val progress: Float = 1f) : BoardDrawing
    data class AtomOrbit(val progress: Float = 1f) : BoardDrawing
    data class FourierEpicycles(val progress: Float = 1f) : BoardDrawing
    data class InclineForces(val progress: Float = 1f) : BoardDrawing
    data class MagneticField(val progress: Float = 1f) : BoardDrawing
}

data class ConfettiParticle(
    var xRatio: Float,
    var yRatio: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    val size: Float
)

@Composable
fun WhiteboardAnimatorScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    // Character State
    var currentAction by remember { mutableStateOf(WhiteboardAction.WAVE_INTRO) }
    var selectedCategory by remember { mutableStateOf("All") }
    var speechText by remember { mutableStateOf("Hi! I'm Dr. Kelvin. Tap any movement below to control me on the whiteboard!") }
    var isExecutingAction by remember { mutableStateOf(false) }

    // Normalized coordinates (0f to 1f) for responsive scaling
    val personXRatio = remember { Animatable(0.22f) }
    val armAngle = remember { Animatable(-30f) }
    val jumpYRatio = remember { Animatable(0f) }
    val eyesShocked = remember { mutableStateOf(false) }
    val isStaticShocked = remember { mutableStateOf(false) }
    val lightbulbGlow = remember { Animatable(0f) }
    val isThinking = remember { mutableStateOf(false) }

    // Responsive Whiteboard drawings list
    val drawings = remember { mutableStateListOf<BoardDrawing>() }
    var activeDrawingProgress by remember { mutableStateOf(1f) }

    // Physical interactive props
    var ballDropActive by remember { mutableStateOf(false) }
    val ballHeightRatio = remember { Animatable(0f) } // 0 = at hand, 1 = at floor

    var balloonActive by remember { mutableStateOf(false) }
    var balloonPopState by remember { mutableStateOf(0) } // 0=intact, 1=spraying, 2=exploded

    var waterBagActive by remember { mutableStateOf(false) }
    var waterBagPunctured by remember { mutableStateOf(false) }
    var waterBagLeaking by remember { mutableStateOf(false) }

    var pendulumActive by remember { mutableStateOf(false) }
    val pendulumAngle = remember { Animatable(0f) }

    var vanDeGraaffActive by remember { mutableStateOf(false) }
    var confettiActive by remember { mutableStateOf(false) }
    val confettiList = remember { mutableStateListOf<ConfettiParticle>() }

    // Continuous idle breathing
    val infiniteTransition = rememberInfiniteTransition()
    val breathOffset by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Execute movements when action is chosen
    fun executeAction(action: WhiteboardAction) {
        if (isExecutingAction) return
        isExecutingAction = true
        currentAction = action

        coroutineScope.launch {
            when (action) {
                WhiteboardAction.WAVE_INTRO -> {
                    speechText = "Hello physics creators! Ready to demonstrate some scientific magic?"
                    eyesShocked.value = false
                    isStaticShocked.value = false
                    isThinking.value = false
                    personXRatio.animateTo(0.22f, tween(500))
                    repeat(3) {
                        armAngle.animateTo(-75f, tween(240))
                        armAngle.animateTo(-45f, tween(240))
                    }
                    armAngle.animateTo(-30f, tween(300))
                    isExecutingAction = false
                }

                WhiteboardAction.POINT_EXPLAIN -> {
                    speechText = "Notice how mathematical symmetry predicts conservation of energy and momentum!"
                    eyesShocked.value = false
                    isThinking.value = false
                    personXRatio.animateTo(0.30f, tween(500))
                    armAngle.animateTo(-55f, tween(400))
                    delay(1200)
                    armAngle.animateTo(-30f, tween(300))
                    isExecutingAction = false
                }

                WhiteboardAction.EUREKA -> {
                    speechText = "EUREKA! The conservation laws hold across all inertial frames!"
                    eyesShocked.value = false
                    personXRatio.animateTo(0.28f, tween(400))
                    launch {
                        jumpYRatio.animateTo(-0.06f, tween(200))
                        jumpYRatio.animateTo(0f, tween(200))
                    }
                    armAngle.animateTo(-80f, tween(250))
                    lightbulbGlow.animateTo(1f, tween(300))
                    delay(1200)
                    lightbulbGlow.animateTo(0f, tween(400))
                    armAngle.animateTo(-30f, tween(300))
                    isExecutingAction = false
                }

                WhiteboardAction.PONDER_WALK -> {
                    speechText = "Hmm... let me reconsider the Dirichlet boundary conditions..."
                    isThinking.value = true
                    personXRatio.animateTo(0.18f, tween(600))
                    armAngle.animateTo(-15f, tween(300))
                    delay(600)
                    personXRatio.animateTo(0.38f, tween(1200))
                    armAngle.animateTo(-40f, tween(400))
                    delay(600)
                    personXRatio.animateTo(0.25f, tween(1000))
                    speechText = "Aha! The eigenvalues are quantized: λ_n = (nπ/L)²!"
                    delay(800)
                    isThinking.value = false
                    armAngle.animateTo(-30f, tween(300))
                    isExecutingAction = false
                }

                WhiteboardAction.MOON_JUMP -> {
                    speechText = "Setting gravity to Moon surface: g = 1.62 m/s²! Watch the float!"
                    personXRatio.animateTo(0.20f, tween(400))
                    delay(300)
                    jumpYRatio.animateTo(0.02f, tween(200))
                    speechText = "One small step for a physicist, one giant leap for science!"
                    launch {
                        personXRatio.animateTo(0.48f, tween(1600, easing = LinearEasing))
                    }
                    jumpYRatio.animateTo(-0.25f, tween(800, easing = LinearOutSlowInEasing))
                    jumpYRatio.animateTo(0f, tween(800, easing = FastOutLinearInEasing))
                    jumpYRatio.animateTo(0.02f, tween(150))
                    jumpYRatio.animateTo(0f, tween(180))
                    speechText = "Lunar gravity gives 6x higher jumps than on Earth!"
                    isExecutingAction = false
                }

                WhiteboardAction.VICTORY_DANCE -> {
                    speechText = "YES! The experimental data matches the theoretical model with 5-sigma certainty!"
                    personXRatio.animateTo(0.32f, tween(400))
                    confettiActive = true
                    confettiList.clear()
                    val colors = listOf(CyanNeon, AmberVibrant, CoralNeon, EmeraldNeon, PurpleNeon)
                    for (i in 0..35) {
                        confettiList.add(
                            ConfettiParticle(
                                xRatio = 0.35f + Random.nextFloat() * 0.45f,
                                yRatio = 0.25f + Random.nextFloat() * 0.35f,
                                vx = (Random.nextFloat() - 0.5f) * 60f,
                                vy = -Random.nextFloat() * 50f,
                                color = colors[i % colors.size],
                                size = 5f + Random.nextFloat() * 5f
                            )
                        )
                    }

                    repeat(3) {
                        launch {
                            jumpYRatio.animateTo(-0.04f, tween(160))
                            jumpYRatio.animateTo(0f, tween(160))
                        }
                        armAngle.animateTo(-85f, tween(160))
                        armAngle.animateTo(-25f, tween(160))
                    }
                    delay(800)
                    confettiActive = false
                    armAngle.animateTo(-30f, tween(300))
                    isExecutingAction = false
                }

                WhiteboardAction.DRAW_SINE_WAVE -> {
                    speechText = "Let's draw a classical harmonic wave: u(x, t) = A sin(kx - ωt)!"
                    eyesShocked.value = false
                    personXRatio.animateTo(0.28f, tween(600))
                    armAngle.animateTo(-40f, tween(300))

                    activeDrawingProgress = 0f
                    val item = BoardDrawing.SineWave(0f)
                    drawings.removeAll { it is BoardDrawing.SineWave }
                    drawings.add(item)

                    for (step in 1..25) {
                        activeDrawingProgress = step / 25f
                        val idx = drawings.indexOf(item)
                        if (idx >= 0) drawings[idx] = BoardDrawing.SineWave(activeDrawingProgress)
                        armAngle.snapTo(-35f + sin(activeDrawingProgress * 4 * PI.toFloat()) * 18f)
                        delay(40)
                    }
                    speechText = "Notice crests, troughs, and wavelength λ! Wave speed c = λ · f."
                    armAngle.animateTo(-25f, tween(300))
                    isExecutingAction = false
                }

                WhiteboardAction.DRAW_PROJECTILE -> {
                    speechText = "Drawing a 2D projectile trajectory with quadratic aerodynamic drag!"
                    personXRatio.animateTo(0.26f, tween(500))
                    armAngle.animateTo(-45f, tween(300))

                    activeDrawingProgress = 0f
                    val item = BoardDrawing.ProjectileArc(0f)
                    drawings.removeAll { it is BoardDrawing.ProjectileArc }
                    drawings.add(item)

                    for (step in 1..25) {
                        activeDrawingProgress = step / 25f
                        val idx = drawings.indexOf(item)
                        if (idx >= 0) drawings[idx] = BoardDrawing.ProjectileArc(activeDrawingProgress)
                        armAngle.snapTo(-50f + activeDrawingProgress * 25f)
                        delay(40)
                    }
                    speechText = "Under air drag, the descent is steeper and shorter than an ideal parabola!"
                    armAngle.animateTo(-30f, tween(300))
                    isExecutingAction = false
                }

                WhiteboardAction.DRAW_ATOM -> {
                    speechText = "Sketching the Rutherford-Bohr atomic model with quantized orbital shells!"
                    personXRatio.animateTo(0.32f, tween(500))
                    armAngle.animateTo(-50f, tween(300))

                    drawings.removeAll { it is BoardDrawing.AtomOrbit }
                    drawings.add(BoardDrawing.AtomOrbit(1f))
                    delay(600)
                    speechText = "Electrons occupy quantized angular momentum states: L = n · ℏ!"
                    armAngle.animateTo(-30f, tween(300))
                    isExecutingAction = false
                }

                WhiteboardAction.DRAW_FOURIER -> {
                    speechText = "Drawing rotating Fourier phasor epicycles synthesizing a square step wave!"
                    personXRatio.animateTo(0.28f, tween(500))
                    armAngle.animateTo(-45f, tween(300))

                    drawings.removeAll { it is BoardDrawing.FourierEpicycles }
                    drawings.add(BoardDrawing.FourierEpicycles(1f))
                    delay(700)
                    speechText = "Rotating circles sum together to create sharp corners! That's Fourier Series!"
                    armAngle.animateTo(-30f, tween(300))
                    isExecutingAction = false
                }

                WhiteboardAction.DRAW_INCLINE -> {
                    speechText = "Drawing an inclined plane with free-body force vectors!"
                    personXRatio.animateTo(0.28f, tween(500))
                    armAngle.animateTo(-45f, tween(300))

                    drawings.removeAll { it is BoardDrawing.InclineForces }
                    drawings.add(BoardDrawing.InclineForces(1f))
                    delay(700)
                    speechText = "Gravity mg splits into mg sin(θ) down the slope and mg cos(θ) normal!"
                    armAngle.animateTo(-30f, tween(300))
                    isExecutingAction = false
                }

                WhiteboardAction.DRAW_MAGNET -> {
                    speechText = "Drawing a dipole magnet and magnetic flux vector loops!"
                    personXRatio.animateTo(0.30f, tween(500))
                    armAngle.animateTo(-45f, tween(300))

                    drawings.removeAll { it is BoardDrawing.MagneticField }
                    drawings.add(BoardDrawing.MagneticField(1f))
                    delay(700)
                    speechText = "Magnetic flux loops never cross and have zero divergence: ∇ · B = 0!"
                    armAngle.animateTo(-30f, tween(300))
                    isExecutingAction = false
                }

                WhiteboardAction.DROP_BALL -> {
                    speechText = "Dropping an elastic ball! Watch the restitution and height decay!"
                    personXRatio.animateTo(0.35f, tween(500))
                    armAngle.animateTo(-65f, tween(400))

                    ballDropActive = true
                    ballHeightRatio.snapTo(0.15f)
                    var currBounceH = 0.55f
                    repeat(4) {
                        ballHeightRatio.animateTo(0.85f, tween(260, easing = FastOutLinearInEasing))
                        currBounceH *= 0.52f
                        ballHeightRatio.animateTo(0.85f - currBounceH, tween(240, easing = LinearOutSlowInEasing))
                    }
                    ballHeightRatio.animateTo(0.85f, tween(100))
                    speechText = "Restitution e = 0.75! Mechanical kinetic energy converts into heat and sound!"
                    isExecutingAction = false
                }

                WhiteboardAction.BALLOON_POP -> {
                    speechText = "Watch what happens when I spray citrus peel mist onto this inflated balloon..."
                    personXRatio.animateTo(0.30f, tween(500))
                    balloonActive = true
                    balloonPopState = 0
                    delay(700)

                    speechText = "Squeezing limonene oil mist..."
                    armAngle.animateTo(-50f, tween(300))
                    balloonPopState = 1
                    delay(600)

                    balloonPopState = 2
                    eyesShocked.value = true
                    speechText = "BOOM! Tensile rupture! The solvent unzipped the latex polymer chains!"
                    launch {
                        jumpYRatio.animateTo(-0.06f, tween(120))
                        jumpYRatio.animateTo(0f, tween(180))
                    }
                    personXRatio.animateTo(0.18f, tween(200))
                    delay(1200)
                    eyesShocked.value = false
                    isExecutingAction = false
                }

                WhiteboardAction.PENCIL_WATER_BAG -> {
                    speechText = "Stabbing a plastic bag of water with a sharp pencil! Watch for leaks!"
                    personXRatio.animateTo(0.28f, tween(500))
                    waterBagActive = true
                    waterBagPunctured = false
                    waterBagLeaking = false
                    delay(600)

                    speechText = "Puncturing through both sides of the HDPE polymer... ZERO LEAKS!"
                    armAngle.animateTo(-45f, tween(300))
                    waterBagPunctured = true
                    delay(1200)

                    speechText = "Now pulling the pencil back out! Look at that high-pressure leak stream!"
                    armAngle.animateTo(-20f, tween(300))
                    waterBagLeaking = true
                    delay(1500)
                    speechText = "Torricelli's Law: exit speed v = √(2gh)!"
                    isExecutingAction = false
                }

                WhiteboardAction.SWING_PENDULUM -> {
                    speechText = "Setting up a simple gravity pendulum! Watch harmonic energy exchange!"
                    personXRatio.animateTo(0.32f, tween(500))
                    pendulumActive = true

                    armAngle.animateTo(-65f, tween(400))
                    pendulumAngle.animateTo(0.65f, tween(500))
                    delay(400)

                    speechText = "Released from rest! Period T = 2π√(L/g), independent of mass!"
                    armAngle.animateTo(-30f, tween(300))

                    var amp = 0.65f
                    repeat(6) {
                        pendulumAngle.animateTo(-amp, tween(420, easing = FastOutSlowInEasing))
                        amp *= 0.86f
                        pendulumAngle.animateTo(amp, tween(420, easing = FastOutSlowInEasing))
                        amp *= 0.86f
                    }
                    pendulumAngle.animateTo(0f, tween(300))
                    isExecutingAction = false
                }

                WhiteboardAction.STATIC_SHOCK -> {
                    speechText = "Touching the Van de Graaff electrostatic dome... accumulating 50,000 Volts!"
                    personXRatio.animateTo(0.32f, tween(500))
                    vanDeGraaffActive = true
                    armAngle.animateTo(-55f, tween(400))
                    delay(600)

                    eyesShocked.value = true
                    isStaticShocked.value = true
                    speechText = "ZAP! Electrostatic charge transfer! Like charges repel, standing each hair on end!"
                    repeat(6) {
                        jumpYRatio.animateTo(-0.015f, tween(60))
                        jumpYRatio.animateTo(0.015f, tween(60))
                    }
                    jumpYRatio.animateTo(0f, tween(100))
                    delay(1400)
                    eyesShocked.value = false
                    isStaticShocked.value = false
                    vanDeGraaffActive = false
                    armAngle.animateTo(-30f, tween(300))
                    isExecutingAction = false
                }

                WhiteboardAction.ERASE_BOARD -> {
                    speechText = "Cleaning the whiteboard for our next experiment..."
                    eyesShocked.value = false
                    isStaticShocked.value = false
                    isThinking.value = false
                    personXRatio.animateTo(0.50f, tween(500))
                    armAngle.animateTo(-45f, tween(300))

                    repeat(3) {
                        personXRatio.animateTo(0.30f, tween(260))
                        personXRatio.animateTo(0.55f, tween(260))
                    }
                    drawings.clear()
                    ballDropActive = false
                    balloonActive = false
                    waterBagActive = false
                    pendulumActive = false
                    vanDeGraaffActive = false
                    speechText = "Whiteboard squeaky clean and ready for fresh science demonstrations!"
                    personXRatio.animateTo(0.22f, tween(500))
                    armAngle.animateTo(-30f, tween(300))
                    isExecutingAction = false
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B1120))
    ) {
        // Compact Top Header
        Surface(
            color = ScienceDarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ScienceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = ScienceDarkSurfaceVariant, contentColor = TextPrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("← Back", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Text(
                        text = "🧑‍🏫 Whiteboard Animator",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon
                    )
                }

                Surface(
                    color = if (isExecutingAction) AmberVibrant.copy(alpha = 0.2f) else EmeraldNeon.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isExecutingAction) AmberVibrant else EmeraldNeon)
                ) {
                    Text(
                        text = if (isExecutingAction) "⏳ Moving..." else "✅ Ready",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isExecutingAction) AmberVibrant else EmeraldNeon,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // Full Screen Responsive Whiteboard
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(5.dp, Color(0xFF94A3B8)), // Responsive aluminum frame
                shadowElevation = 12.dp,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // 1. Responsive Faint Grid Dots
                        val stepPx = max(28f, w * 0.05f)
                        var gx = stepPx
                        while (gx < w - stepPx) {
                            var gy = stepPx
                            while (gy < h - 25f) {
                                drawCircle(Color(0xFFCBD5E1), 1.2f, Offset(gx, gy))
                                gy += stepPx
                            }
                            gx += stepPx
                        }

                        // 2. Render Scaled Drawings on Board (100% visible on any screen size!)
                        drawings.forEach { drawing ->
                            drawResponsiveDrawing(drawing, w, h)
                        }

                        // 3. Render Responsive Physical Props
                        // A: Bouncing Ball
                        if (ballDropActive) {
                            val bX = w * 0.72f
                            val floorY = h * 0.85f
                            val bY = h * ballHeightRatio.value
                            drawOval(Color.Black.copy(alpha = 0.25f), Offset(bX - 14f, floorY - 3f), Size(28f, 6f))
                            drawCircle(
                                brush = Brush.radialGradient(listOf(Color.White, AmberVibrant, Color(0xFFE65100))),
                                radius = min(16f, w * 0.035f),
                                center = Offset(bX, bY)
                            )
                        }

                        // B: Balloon
                        if (balloonActive) {
                            val bX = w * 0.70f
                            val bY = h * 0.44f
                            val bRadius = min(22f, w * 0.055f)
                            if (balloonPopState < 2) {
                                drawOval(
                                    brush = Brush.radialGradient(listOf(Color.White, CoralNeon, Color(0xFFC2185B))),
                                    topLeft = Offset(bX - bRadius, bY - bRadius * 1.3f),
                                    size = Size(bRadius * 2f, bRadius * 2.6f)
                                )
                                drawLine(Color(0xFF94A3B8), Offset(bX, bY + bRadius * 1.3f), Offset(bX, bY + bRadius * 2.4f), 1.8f)

                                if (balloonPopState == 1) {
                                    for (i in 1..6) {
                                        val mx = bX - bRadius * 1.5f + i * 4f
                                        val my = bY - 10f + (i % 3) * 8f
                                        drawCircle(AmberVibrant, 2f, Offset(mx, my))
                                    }
                                }
                            } else {
                                for (i in 0..7) {
                                    val angle = i * (2 * PI.toFloat() / 8f)
                                    val sx = bX + cos(angle) * (bRadius * 1.8f)
                                    val sy = bY + sin(angle) * (bRadius * 1.8f)
                                    drawCircle(CoralNeon, 4f, Offset(sx, sy))
                                }
                                drawCircle(Color.White.copy(alpha = 0.5f), bRadius * 2.2f, Offset(bX, bY), style = Stroke(width = 2f))
                            }
                        }

                        // C: Water Bag
                        if (waterBagActive) {
                            val bagW = min(65f, w * 0.16f)
                            val bagH = bagW * 1.4f
                            val bagX = w * 0.65f
                            val bagY = h * 0.38f

                            drawRoundRect(
                                color = CyanNeon.copy(alpha = 0.35f),
                                topLeft = Offset(bagX, bagY),
                                size = Size(bagW, bagH),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
                            )
                            drawRoundRect(
                                color = Color(0xFF0288D1),
                                topLeft = Offset(bagX, bagY),
                                size = Size(bagW, bagH),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f),
                                style = Stroke(width = 2f)
                            )

                            if (waterBagPunctured) {
                                drawLine(
                                    color = AmberVibrant,
                                    start = Offset(bagX - bagW * 0.4f, bagY + bagH * 0.45f),
                                    end = Offset(bagX + bagW * 1.4f, bagY + bagH * 0.45f),
                                    strokeWidth = 5f,
                                    cap = StrokeCap.Round
                                )
                            }
                            if (waterBagLeaking) {
                                val sprayPath = Path().apply {
                                    moveTo(bagX + bagW, bagY + bagH * 0.45f)
                                    quadraticTo(bagX + bagW + 40f, bagY + bagH * 0.55f, bagX + bagW + 80f, bagY + bagH * 1.1f)
                                }
                                drawPath(sprayPath, CyanNeon, style = Stroke(width = 3f))
                            }
                        }

                        // D: Pendulum
                        if (pendulumActive) {
                            val pivotX = w * 0.72f
                            val pivotY = h * 0.18f
                            val armLen = min(130f, h * 0.35f)
                            val bobX = pivotX + sin(pendulumAngle.value) * armLen
                            val bobY = pivotY + cos(pendulumAngle.value) * armLen

                            drawCircle(Color(0xFF475569), 5f, Offset(pivotX, pivotY))
                            drawLine(Color(0xFF1E293B), Offset(pivotX, pivotY), Offset(bobX, bobY), 2f)
                            drawCircle(
                                brush = Brush.radialGradient(listOf(Color.White, Color(0xFF78909C), Color(0xFF263238))),
                                radius = min(14f, w * 0.035f),
                                center = Offset(bobX, bobY)
                            )
                        }

                        // E: Van de Graaff
                        if (vanDeGraaffActive) {
                            val genX = w * 0.70f
                            val domeY = h * 0.42f
                            val domeR = min(28f, w * 0.07f)
                            drawRect(Color(0xFF64748B), Offset(genX - domeR * 0.3f, domeY + domeR * 0.7f), Size(domeR * 0.6f, h * 0.35f))
                            drawCircle(
                                brush = Brush.radialGradient(listOf(Color.White, Color(0xFFB0BEC5), Color(0xFF37474F))),
                                radius = domeR,
                                center = Offset(genX, domeY)
                            )
                            val sparkPath = Path().apply {
                                moveTo(genX - domeR * 0.7f, domeY)
                                lineTo(genX - domeR * 1.3f, domeY - 10f)
                                lineTo(genX - domeR * 1.1f, domeY + 5f)
                                lineTo(genX - domeR * 1.7f, domeY - 5f)
                            }
                            drawPath(sparkPath, CyanNeon, style = Stroke(width = 2.5f))
                        }

                        // F: Confetti
                        if (confettiActive) {
                            confettiList.forEach { p ->
                                val px = w * p.xRatio
                                val py = h * p.yRatio
                                drawCircle(p.color, p.size, Offset(px, py))
                            }
                        }

                        // 4. Whiteboard Aluminum Marker Tray at bottom
                        val trayY = h - 16f
                        drawRect(Color(0xFF64748B), Offset(15f, trayY), Size(w - 30f, 12f))
                        listOf(Color(0xFF0F172A), Color(0xFFFF1744), Color(0xFF2979FF), Color(0xFF00E676)).forEachIndexed { i, col ->
                            drawRoundRect(col, Offset(35f + i * 26f, trayY - 3f), Size(18f, 5f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f))
                        }
                        drawRoundRect(Color(0xFF1E293B), Offset(w - 75f, trayY - 5f), Size(36f, 8f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f))

                        // 5. Draw the Full-Body Animated Scientist Person (Properly Scaled!)
                        val personPxX = w * personXRatio.value
                        val groundY = h * 0.88f + (h * jumpYRatio.value) + breathOffset
                        val scientistHeight = min(h * 0.46f, 210f)

                        drawResponsiveScientist(
                            centerX = personPxX,
                            groundY = groundY,
                            totalHeight = scientistHeight,
                            armAngle = armAngle.value,
                            eyesShocked = eyesShocked.value,
                            isStaticShocked = isStaticShocked.value,
                            lightbulbGlow = lightbulbGlow.value,
                            isThinking = isThinking.value,
                            isErasing = currentAction == WhiteboardAction.ERASE_BOARD
                        )
                    }

                    // Top Subtitle / Teleprompter Card (100% visible on all screens!)
                    Surface(
                        color = Color.White.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFCBD5E1)),
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth(0.94f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔬 DR. KELVIN: ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0288D1)
                            )
                            Text(
                                text = speechText,
                                color = Color(0xFF0F172A),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }

        // Bottom Action Command Center
        Surface(
            color = ScienceDarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ScienceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                // Category Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        "All" to WhiteboardAction.values().size,
                        "Drawing" to WhiteboardAction.values().count { it.category == "Drawing" },
                        "Physics" to WhiteboardAction.values().count { it.category == "Physics" },
                        "Body" to WhiteboardAction.values().count { it.category == "Body" }
                    ).forEach { (cat, count) ->
                        val isSel = selectedCategory == cat
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedCategory = cat },
                            label = { Text("$cat ($count)", fontSize = 11.sp) },
                            modifier = Modifier.height(28.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanNeon,
                                selectedLabelColor = ScienceDarkBg,
                                containerColor = ScienceDarkSurfaceVariant,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                val displayedActions = remember(selectedCategory) {
                    if (selectedCategory == "All") {
                        WhiteboardAction.values().toList()
                    } else {
                        WhiteboardAction.values().filter { it.category == selectedCategory }
                    }
                }

                // Actions Row
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayedActions) { action ->
                        val isCurrent = currentAction == action && isExecutingAction
                        Button(
                            onClick = { executeAction(action) },
                            enabled = !isExecutingAction,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCurrent) AmberVibrant else ScienceDarkSurfaceVariant,
                                contentColor = if (isCurrent) ScienceDarkBg else TextPrimary,
                                disabledContainerColor = ScienceDarkSurfaceVariant.copy(alpha = 0.5f),
                                disabledContentColor = TextSecondary.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isCurrent) AmberVibrant else ScienceBorder
                            )
                        ) {
                            Text(
                                text = action.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

// Draw Fully Responsive Math/Physics Diagrams on Whiteboard
private fun DrawScope.drawResponsiveDrawing(drawing: BoardDrawing, width: Float, height: Float) {
    val drawStartX = width * 0.40f
    val drawEndX = width * 0.92f
    val drawCenterY = height * 0.48f

    when (drawing) {
        is BoardDrawing.SineWave -> {
            val path = Path()
            path.moveTo(drawStartX, drawCenterY)
            val steps = 30
            val maxStep = (steps * drawing.progress).toInt().coerceAtLeast(1)
            val spanX = drawEndX - drawStartX
            val ampY = min(height * 0.16f, 45f)

            for (i in 1..maxStep) {
                val t = i.toFloat() / steps
                val px = drawStartX + t * spanX
                val py = drawCenterY - sin(t * 3.5f * PI.toFloat()) * ampY
                path.lineTo(px, py)
            }
            drawPath(path, CyanNeon, style = Stroke(width = 3.5f, cap = StrokeCap.Round))
        }

        is BoardDrawing.ProjectileArc -> {
            val path = Path()
            val groundY = height * 0.75f
            path.moveTo(drawStartX, groundY)
            val steps = 25
            val maxStep = (steps * drawing.progress).toInt().coerceAtLeast(1)
            val spanX = drawEndX - drawStartX
            val apexH = min(height * 0.32f, 95f)

            for (i in 1..maxStep) {
                val t = i.toFloat() / steps
                val px = drawStartX + t * spanX
                val py = groundY - (4f * apexH * (t - t * t * 0.92f))
                path.lineTo(px, py)
            }
            drawPath(path, AmberVibrant, style = Stroke(width = 3.5f, cap = StrokeCap.Round))
        }

        is BoardDrawing.AtomOrbit -> {
            val cx = (drawStartX + drawEndX) / 2f
            val cy = drawCenterY
            val rx = (drawEndX - drawStartX) * 0.35f
            val ry = rx * 0.45f

            // Nucleus
            drawCircle(CoralNeon, 6f, Offset(cx, cy))

            // Orbit 1
            val o1 = Path().apply {
                for (i in 0..25) {
                    val a = i * 2 * PI.toFloat() / 25f
                    val px = cx + cos(a) * rx
                    val py = cy + sin(a) * ry
                    if (i == 0) moveTo(px, py) else lineTo(px, py)
                }
                close()
            }
            drawPath(o1, EmeraldNeon, style = Stroke(width = 2.5f))

            // Orbit 2 (Tilted)
            val o2 = Path().apply {
                for (i in 0..25) {
                    val a = i * 2 * PI.toFloat() / 25f
                    val lx = cos(a) * rx
                    val ly = sin(a) * ry
                    // Rotate 60 deg
                    val px = cx + lx * 0.5f - ly * 0.866f
                    val py = cy + lx * 0.866f + ly * 0.5f
                    if (i == 0) moveTo(px, py) else lineTo(px, py)
                }
                close()
            }
            drawPath(o2, CyanNeon, style = Stroke(width = 2f))
        }

        is BoardDrawing.FourierEpicycles -> {
            val cx = drawStartX + (drawEndX - drawStartX) * 0.22f
            val cy = drawCenterY
            val r1 = min(36f, width * 0.08f)
            val r2 = r1 * 0.33f

            // Circle 1
            drawCircle(CyanNeon, r1, Offset(cx, cy), style = Stroke(width = 2f))
            val tip1 = Offset(cx + r1 * 0.7f, cy - r1 * 0.7f)
            drawLine(CyanNeon, Offset(cx, cy), tip1, 2f)

            // Circle 2
            drawCircle(AmberVibrant, r2, tip1, style = Stroke(width = 1.5f))
            val tip2 = Offset(tip1.x + r2, tip1.y)
            drawLine(AmberVibrant, tip1, tip2, 2f)

            // Step wave
            val waveX = cx + r1 * 1.5f
            val stepPath = Path().apply {
                moveTo(waveX, tip2.y)
                val topY = cy - r1 * 0.7f
                val botY = cy + r1 * 0.7f
                lineTo(waveX + 20f, topY)
                lineTo(waveX + 50f, topY)
                lineTo(waveX + 55f, botY)
                lineTo(waveX + 85f, botY)
                lineTo(waveX + 90f, topY)
            }
            drawPath(stepPath, EmeraldNeon, style = Stroke(width = 2.5f))
        }

        is BoardDrawing.InclineForces -> {
            val rampW = (drawEndX - drawStartX) * 0.7f
            val rampH = rampW * 0.45f
            val rampBottom = drawCenterY + rampH * 0.5f

            val rampPath = Path().apply {
                moveTo(drawStartX, rampBottom)
                lineTo(drawStartX + rampW, rampBottom)
                lineTo(drawStartX + rampW, rampBottom - rampH)
                close()
            }
            drawPath(rampPath, Color(0xFF64748B), style = Stroke(width = 3f))

            // Block on slope
            val bx = drawStartX + rampW * 0.5f
            val by = rampBottom - rampH * 0.5f
            drawRoundRect(AmberVibrant, Offset(bx - 12f, by - 12f), Size(24f, 16f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f))
            // Gravity vector mg down
            drawLine(CoralNeon, Offset(bx, by), Offset(bx, by + 32f), 2.5f, cap = StrokeCap.Round)
        }

        is BoardDrawing.MagneticField -> {
            val cx = (drawStartX + drawEndX) / 2f
            val cy = drawCenterY
            val mw = min(50f, width * 0.12f)
            val mh = 22f

            // Red North, Blue South
            drawRect(Color(0xFFD32F2F), Offset(cx - mw, cy - mh / 2f), Size(mw, mh))
            drawRect(Color(0xFF1976D2), Offset(cx, cy - mh / 2f), Size(mw, mh))

            // Closed loops
            val loop1 = Path().apply {
                moveTo(cx - mw * 0.6f, cy - mh / 2f)
                cubicTo(cx - mw, cy - mh * 2.2f, cx + mw, cy - mh * 2.2f, cx + mw * 0.6f, cy - mh / 2f)
            }
            drawPath(loop1, CyanNeon, style = Stroke(width = 2f))
        }
    }
}

// Draw Fully Responsive Scientist
private fun DrawScope.drawResponsiveScientist(
    centerX: Float,
    groundY: Float,
    totalHeight: Float,
    armAngle: Float,
    eyesShocked: Boolean,
    isStaticShocked: Boolean,
    lightbulbGlow: Float,
    isThinking: Boolean,
    isErasing: Boolean
) {
    val scale = totalHeight / 190f
    val headR = 24f * scale
    val headY = groundY - totalHeight + headR
    val torsoTopY = headY + headR * 0.8f
    val torsoBottomY = groundY - (totalHeight * 0.34f)

    val skinColor = Color(0xFFFFCC80)
    val outlineColor = Color(0xFF1E293B)

    // Eureka Lightbulb
    if (lightbulbGlow > 0.05f) {
        val bulbY = headY - 38f * scale
        drawCircle(AmberVibrant.copy(alpha = 0.35f * lightbulbGlow), 24f * scale * lightbulbGlow, Offset(centerX, bulbY))
        drawCircle(Color(0xFFFFD54F), 11f * scale, Offset(centerX, bulbY))
        drawCircle(Color.White, 6f * scale, Offset(centerX, bulbY))
    }

    // Thinking bubble
    if (isThinking) {
        val cy = headY - 32f * scale
        drawCircle(Color(0xFFE2E8F0), 5f * scale, Offset(centerX + 16f * scale, headY - 12f * scale))
        drawCircle(Color.White, 12f * scale, Offset(centerX + 32f * scale, cy))
        drawCircle(Color(0xFF0288D1), 2.5f * scale, Offset(centerX + 32f * scale, cy - 2f))
    }

    // 1. Legs & Shoes
    val hipY = torsoBottomY
    val leftFootX = centerX - 12f * scale
    val rightFootX = centerX + 12f * scale

    drawLine(Color(0xFF1E3A8A), Offset(centerX - 8f * scale, hipY), Offset(leftFootX, groundY - 8f * scale), strokeWidth = 8f * scale, cap = StrokeCap.Round)
    drawLine(Color(0xFF1E3A8A), Offset(centerX + 8f * scale, hipY), Offset(rightFootX, groundY - 8f * scale), strokeWidth = 8f * scale, cap = StrokeCap.Round)
    drawRoundRect(Color(0xFF0F172A), Offset(leftFootX - 10f * scale, groundY - 10f * scale), Size(18f * scale, 10f * scale), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f))
    drawRoundRect(Color(0xFF0F172A), Offset(rightFootX - 6f * scale, groundY - 10f * scale), Size(18f * scale, 10f * scale), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f))

    // 2. White Lab Coat
    val coatPath = Path().apply {
        moveTo(centerX - 16f * scale, torsoTopY)
        lineTo(centerX + 16f * scale, torsoTopY)
        lineTo(centerX + 22f * scale, torsoBottomY + 10f * scale)
        lineTo(centerX - 22f * scale, torsoBottomY + 10f * scale)
        close()
    }
    drawPath(coatPath, Color.White)
    drawPath(coatPath, outlineColor, style = Stroke(width = 2.5f * scale))
    drawLine(Color(0xFF0288D1), Offset(centerX, torsoTopY), Offset(centerX, torsoBottomY + 10f * scale), 2f * scale)

    // Pocket Protector
    drawRect(Color(0xFFE2E8F0), Offset(centerX + 6f * scale, torsoTopY + 14f * scale), Size(10f * scale, 12f * scale))

    // 3. Hair (Static shock expands!)
    val hairColor = Color(0xFFE2E8F0)
    val hairR = if (isStaticShocked) 18f * scale else 11f * scale
    val spikeReach = if (isStaticShocked) 22f * scale else 2f * scale

    for (i in -3..3) {
        val hairAngle = i * 24f
        rotate(hairAngle, pivot = Offset(centerX, headY)) {
            val hy = headY - headR - spikeReach
            drawCircle(hairColor, hairR, Offset(centerX, hy))
            drawCircle(outlineColor, hairR, Offset(centerX, hy), style = Stroke(width = 2f * scale))
        }
    }

    // 4. Head
    drawCircle(skinColor, headR, Offset(centerX, headY))
    drawCircle(outlineColor, headR, Offset(centerX, headY), style = Stroke(width = 2.5f * scale))

    // 5. Cyan Safety Goggles
    val goggleR = 10f * scale
    val gL = Offset(centerX - 9f * scale, headY - 2f * scale)
    val gR = Offset(centerX + 9f * scale, headY - 2f * scale)
    drawLine(Color(0xFF334155), Offset(centerX - headR, headY - 2f * scale), Offset(centerX + headR, headY - 2f * scale), 3f * scale)
    listOf(gL, gR).forEach { c ->
        drawCircle(Color(0xCC00E5FF), goggleR, c)
        drawCircle(outlineColor, goggleR, c, style = Stroke(width = 2.5f * scale))
        drawLine(Color.White, Offset(c.x - 4f * scale, c.y - 4f * scale), Offset(c.x + 2f * scale, c.y + 2f * scale), 1.5f * scale)
    }

    // Eyes / Mouth
    if (eyesShocked || isStaticShocked) {
        drawCircle(Color.Black, 4f * scale, gL)
        drawCircle(Color.Black, 4f * scale, gR)
        drawCircle(Color(0xFFD32F2F), 5f * scale, Offset(centerX, headY + 11f * scale))
    } else {
        drawOval(Color.Black, Offset(gL.x - 2f * scale, gL.y - 3f * scale), Size(4f * scale, 6f * scale))
        drawOval(Color.Black, Offset(gR.x - 2f * scale, gR.y - 3f * scale), Size(4f * scale, 6f * scale))
        drawArc(
            color = outlineColor,
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(centerX - 6f * scale, headY + 6f * scale),
            size = Size(12f * scale, 8f * scale),
            style = Stroke(width = 2f * scale, cap = StrokeCap.Round)
        )
    }

    // 6. Left Arm
    drawLine(Color.White, Offset(centerX - 16f * scale, torsoTopY + 6f * scale), Offset(centerX - 28f * scale, torsoTopY + 34f * scale), strokeWidth = 7f * scale, cap = StrokeCap.Round)
    drawCircle(skinColor, 5f * scale, Offset(centerX - 28f * scale, torsoTopY + 34f * scale))

    // 7. Right Arm
    val shoulderR = Offset(centerX + 16f * scale, torsoTopY + 6f * scale)
    rotate(armAngle, pivot = shoulderR) {
        val elbow = Offset(shoulderR.x + 22f * scale, shoulderR.y - 8f * scale)
        val hand = Offset(elbow.x + 20f * scale, elbow.y - 12f * scale)

        drawLine(Color.White, shoulderR, elbow, strokeWidth = 7f * scale, cap = StrokeCap.Round)
        drawLine(outlineColor, shoulderR, elbow, strokeWidth = 2f * scale, cap = StrokeCap.Round)
        drawLine(Color.White, elbow, hand, strokeWidth = 6f * scale, cap = StrokeCap.Round)
        drawLine(outlineColor, elbow, hand, strokeWidth = 2f * scale, cap = StrokeCap.Round)

        drawCircle(skinColor, 5f * scale, hand)

        if (isErasing) {
            drawRoundRect(Color(0xFF1E293B), Offset(hand.x - 5f * scale, hand.y - 10f * scale), Size(18f * scale, 11f * scale), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f))
        } else {
            drawLine(Color(0xFF263238), Offset(hand.x - 3f * scale, hand.y + 3f * scale), Offset(hand.x + 16f * scale, hand.y - 12f * scale), 4f * scale, StrokeCap.Round)
            drawCircle(Color(0xFFFF1744), 3f * scale, Offset(hand.x + 16f * scale, hand.y - 12f * scale))
        }
    }
}
