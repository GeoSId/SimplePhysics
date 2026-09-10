# Day 4: Disappearing Glass

> **Week 1: Home Physics • Mind-Bending Home & Kitchen Physics**  
> *Topic Subtitle: Index of Refraction Matching*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Submerge a glass tube into a liquid and watch its contours vanish completely before your eyes.

### Scientific Principles & Mechanism
We see transparent glass because light reflects and refracts at the boundary between the surrounding medium and the glass due to differing speeds of light (refractive index n). Pyrex borosilicate glass has n ≈ 1.474. When submerged in vegetable oil or glycerin (which also has n ≈ 1.47), the light travels at the exact same speed in both. Light neither bends nor reflects at the boundary, making the glass 100% invisible!

### Laboratory / Kitchen Protocol (Try It At Home)
> Take a Pyrex glass stirring rod or small Pyrex test tube and place it in a cup of plain water (it will be visible). Now place it in a cup of clear vegetable cooking oil or glycerin—it disappears entirely!

---

## 2. Mathematical Foundation & Governing Equations

The physical behavior in this simulation is governed by:

$$
n_1 \sin(\theta_1) = n_2 \sin(\theta_2) \implies \text{When } n_1 = n_2, \; R = \left(\frac{n_1 - n_2}{n_1 + n_2}\right)^2 = 0
$$

### Physical Meaning & Quantities
The mathematical formulation connects key physical parameters:
- **Forces & Accelerations:** Dynamic balance between external driving forces, restoring forces, and frictional/drag damping.
- **Conservation Principles:** Energy, momentum, or probability density conservation in the physical medium.
- **Boundary Conditions:** Interactions occurring at boundaries, surfaces, or event horizons.

---

## 3. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`RefractionGlassExperiment`](./RefractionGlassExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week1.Day4`
- **Screen Architecture:** Compose Multiplatform with Canvas rendering & high-frequency physics tick.

### Reactive State Variables
The interactive state is managed via Compose `mutableStateOf` variables:

| State Variable | Initialization | Functional Role in Simulation |
| :--- | :--- | :--- |
| `liquidIndex` | `mutableStateOf(1.333f)` | Reactive state tracking physical coordinate or control parameter |
| `liquidName` | `mutableStateOf("Water (n=1.33)")` | Reactive state tracking physical coordinate or control parameter |
| `tubeSubmergedFraction` | `mutableStateOf(0.65f)` | Reactive state tracking physical coordinate or control parameter |
| `showLaserRays` | `mutableStateOf(true)` | Reactive state tracking physical coordinate or control parameter |

### Frame Loop & Physics Integration
- **High-Precision Physics Loop:** Driven by `withFrameNanos` inside `LaunchedEffect`.
- **Time-Delta Numerical Integration:** Uses elapsed nanosecond delta ($\Delta t$) to compute velocity changes, angular acceleration, and position updates, ensuring smooth 60–120 FPS execution independent of device refresh rate.
- **Damping & Dissipation:** Exponential or linear damping applied per frame to simulate air resistance, viscosity, or thermal dissipation.

### User Gestures & Interactivity
- **Pointer Drag Gestures:** Configured via `.pointerInput { detectDragGestures { ... } }`, allowing real-time direct manipulation of particles, sources, or boundary walls on screen.
- **HUD & Slider Controls:** Real-time tweaking of physical constants (gravity, charge, index of refraction, viscosity, or frequency).

### Canvas Graphics Pipeline
- **Normalized Coordinates:** Physics calculations mapped to Canvas dimensions (`size.width`, `size.height`) via responsive scaling.
- **Render Functions:** Utilizes Compose DrawScope methods: `drawBeakerGlass`, `drawCircle`, `drawLaserRays`, `drawLine`, `drawLiquidInBeaker`, `drawPath`.
- **Visual Polish:** Neon color palette, anti-aliased vectors, radial gradients for glowing fields, and dynamic trail decay.

---

## 4. Suggested Investigations & Parameter Experiments
1. **Extremal Value Testing:** Push sliders to their minimum and maximum bounds to observe physical phase shifts or asymptotic behavior.
2. **Perturbation Dynamics:** Disturb the equilibrium state via touch drag and record how quickly the system dissipates energy back to ground state.
3. **Cross-Platform Verification:** Run across Android, iOS, and Desktop to ensure consistent physics step integration and high-DPI Canvas scaling.
