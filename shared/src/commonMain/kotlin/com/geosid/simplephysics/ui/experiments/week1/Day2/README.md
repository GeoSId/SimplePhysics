# Day 2: Citrus Peel vs. Balloon

> **Week 1: Home Physics • Mind-Bending Home & Kitchen Physics**  
> *Topic Subtitle: Polymer Dissolution & Tension Burst*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Squeeze an orange peel near an inflated balloon and watch it pop instantaneously without touching.

### Scientific Principles & Mechanism
The skin of citrus fruits contains tiny reservoirs of d-limonene, an organic hydrocarbon oil. Latex rubber balloons are composed of cross-linked polyisoprene chains held under extreme mechanical tensile stress. Under the chemistry rule of 'like dissolves like', the non-polar limonene instantly dissolves the non-polar latex polymer bonds upon contact, causing rapid catastrophic crack propagation at the speed of sound in rubber (~50 m/s)!

### Laboratory / Kitchen Protocol (Try It At Home)
> Blow up a latex balloon until it is tightly inflated. Cut a piece of orange or lemon peel. Hold the peel with the colored rind facing the balloon, squeeze it sharply to express the citrus oil mist, and watch it pop!

---

## 2. Mathematical Foundation & Governing Equations

The physical behavior in this simulation is governed by:

$$
\sigma = \frac{P \cdot r}{2t}, \quad K_I = \sigma \sqrt{\pi a} \ge K_{Ic}
$$

### Physical Meaning & Quantities
The mathematical formulation connects key physical parameters:
- **Forces & Accelerations:** Dynamic balance between external driving forces, restoring forces, and frictional/drag damping.
- **Conservation Principles:** Energy, momentum, or probability density conservation in the physical medium.
- **Boundary Conditions:** Interactions occurring at boundaries, surfaces, or event horizons.

---

## 3. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`CitrusBalloonExperiment`](./CitrusBalloonExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week1.Day2`
- **Screen Architecture:** Compose Multiplatform with Canvas rendering & high-frequency physics tick.

### Reactive State Variables
The interactive state is managed via Compose `mutableStateOf` variables:

| State Variable | Initialization | Functional Role in Simulation |
| :--- | :--- | :--- |
| `isPopped` | `mutableStateOf(false)` | Reactive state tracking physical coordinate or control parameter |
| `balloonColorIndex` | `mutableStateOf(0)` | Reactive state tracking physical coordinate or control parameter |
| `inflationScale` | `mutableStateOf(1f)` | Reactive state tracking physical coordinate or control parameter |
| `peelPos` | `mutableStateOf(Offset(0.78f, 0.45f))` | Reactive state tracking physical coordinate or control parameter |
| `isSqueezing` | `mutableStateOf(false)` | Reactive state tracking physical coordinate or control parameter |
| `shockwaveRadius` | `mutableStateOf(0f)` | Reactive state tracking physical coordinate or control parameter |
| `shockwaveAlpha` | `mutableStateOf(0f)` | Reactive state tracking physical coordinate or control parameter |

### Frame Loop & Physics Integration
- **High-Precision Physics Loop:** Driven by `withFrameNanos` inside `LaunchedEffect`.
- **Time-Delta Numerical Integration:** Uses elapsed nanosecond delta ($\Delta t$) to compute velocity changes, angular acceleration, and position updates, ensuring smooth 60–120 FPS execution independent of device refresh rate.
- **Damping & Dissipation:** Exponential or linear damping applied per frame to simulate air resistance, viscosity, or thermal dissipation.

### User Gestures & Interactivity
- **Pointer Drag Gestures:** Configured via `.pointerInput { detectDragGestures { ... } }`, allowing real-time direct manipulation of particles, sources, or boundary walls on screen.
- **HUD & Slider Controls:** Real-time tweaking of physical constants (gravity, charge, index of refraction, viscosity, or frequency).

### Canvas Graphics Pipeline
- **Normalized Coordinates:** Physics calculations mapped to Canvas dimensions (`size.width`, `size.height`) via responsive scaling.
- **Render Functions:** Utilizes Compose DrawScope methods: `drawArc`, `drawCircle`, `drawCitrusPeel`, `drawOval`, `drawPath`, `drawRect`.
- **Visual Polish:** Neon color palette, anti-aliased vectors, radial gradients for glowing fields, and dynamic trail decay.

---

## 4. Suggested Investigations & Parameter Experiments
1. **Extremal Value Testing:** Push sliders to their minimum and maximum bounds to observe physical phase shifts or asymptotic behavior.
2. **Perturbation Dynamics:** Disturb the equilibrium state via touch drag and record how quickly the system dissipates energy back to ground state.
3. **Cross-Platform Verification:** Run across Android, iOS, and Desktop to ensure consistent physics step integration and high-DPI Canvas scaling.
