# Day 5: Cartesian Diver

> **Week 1: Home Physics • Mind-Bending Home & Kitchen Physics**  
> *Topic Subtitle: Boyle's Law, Compressibility & Buoyancy*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Command a submarine diver inside a sealed water bottle to dive, hover, and surface using your hands.

### Scientific Principles & Mechanism
Named after René Descartes. The diver contains a trapped bubble of air. Squeezing the bottle transmits pressure equally throughout the fluid (Pascal's law). The increased pressure compresses the trapped air bubble (Boyle's law: P·V = const), reducing its displaced volume. When average density exceeds water, gravity overcomes buoyant force and it sinks.

### Laboratory / Kitchen Protocol (Try It At Home)
> Attach modeling clay to a plastic pen cap until it barely floats upright in a cup of water. Place inside a 2L bottle filled to the brim with water, cap tightly, and squeeze!

---

## 2. Mathematical Foundation & Governing Equations

The physical behavior in this simulation is governed by:

$$
P_1 V_1 = P_2 V_2, \quad F_b = \rho_{water} V_{displaced} g
$$

### Physical Meaning & Quantities
The mathematical formulation connects key physical parameters:
- **Forces & Accelerations:** Dynamic balance between external driving forces, restoring forces, and frictional/drag damping.
- **Conservation Principles:** Energy, momentum, or probability density conservation in the physical medium.
- **Boundary Conditions:** Interactions occurring at boundaries, surfaces, or event horizons.

---

## 3. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`CartesianDiverExperiment`](./CartesianDiverExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week1.Day5`
- **Screen Architecture:** Compose Multiplatform with Canvas rendering & high-frequency physics tick.

### Reactive State Variables
The interactive state is managed via Compose `mutableStateOf` variables:

| State Variable | Initialization | Functional Role in Simulation |
| :--- | :--- | :--- |
| `appliedPressureAtm` | `mutableStateOf(1.0f)` | Reactive state tracking physical coordinate or control parameter |
| `diverDepth` | `mutableStateOf(0.12f)` | Reactive state tracking physical coordinate or control parameter |
| `diverVelocityY` | `mutableStateOf(0f)` | Reactive state tracking physical coordinate or control parameter |

### Frame Loop & Physics Integration
- **High-Precision Physics Loop:** Driven by `withFrameNanos` inside `LaunchedEffect`.
- **Time-Delta Numerical Integration:** Uses elapsed nanosecond delta ($\Delta t$) to compute velocity changes, angular acceleration, and position updates, ensuring smooth 60–120 FPS execution independent of device refresh rate.
- **Damping & Dissipation:** Exponential or linear damping applied per frame to simulate air resistance, viscosity, or thermal dissipation.

### User Gestures & Interactivity
- **Pointer Drag Gestures:** Configured via `.pointerInput { detectDragGestures { ... } }`, allowing real-time direct manipulation of particles, sources, or boundary walls on screen.
- **HUD & Slider Controls:** Real-time tweaking of physical constants (gravity, charge, index of refraction, viscosity, or frequency).

### Canvas Graphics Pipeline
- **Normalized Coordinates:** Physics calculations mapped to Canvas dimensions (`size.width`, `size.height`) via responsive scaling.
- **Render Functions:** Utilizes Compose DrawScope methods: `drawBottlePlasticShell`, `drawBottleWater`, `drawCartesianDiver`, `drawCircle`, `drawLine`, `drawPath`.
- **Visual Polish:** Neon color palette, anti-aliased vectors, radial gradients for glowing fields, and dynamic trail decay.

---

## 4. Suggested Investigations & Parameter Experiments
1. **Extremal Value Testing:** Push sliders to their minimum and maximum bounds to observe physical phase shifts or asymptotic behavior.
2. **Perturbation Dynamics:** Disturb the equilibrium state via touch drag and record how quickly the system dissipates energy back to ground state.
3. **Cross-Platform Verification:** Run across Android, iOS, and Desktop to ensure consistent physics step integration and high-DPI Canvas scaling.
