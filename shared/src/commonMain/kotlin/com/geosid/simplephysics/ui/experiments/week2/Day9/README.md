# Day 9: Inelastic Bouncing Ball

> **Week 2: Classical Mechanics • Collisions, Conservation Laws & Rigid Bodies**  
> *Topic Subtitle: Restitution Coefficient & Energy Decay*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Drop a ball from customizable heights and observe height decay, floor deformation, and live kinetic vs potential energy graphs.

### Scientific Principles & Mechanism
During impact, the ball deforms elastically and converts mechanical kinetic energy into internal thermal energy and sound vibrations. The ratio of rebound speed to impact speed is the coefficient of restitution e = v_rebound / v_impact <= 1.0.

### Laboratory / Kitchen Protocol (Try It At Home)
> Drop a cold tennis ball versus a warm tennis ball onto a hard kitchen tile and compare the bounce heights!

---

## 2. Mathematical Foundation & Governing Equations

The physical behavior in this simulation is governed by:

$$
v_+ = -e \cdot v_-, \quad E_{k+} = e^2 E_{k-}, \quad h_n = e^{2n} h_0
$$

### Physical Meaning & Quantities
The mathematical formulation connects key physical parameters:
- **Forces & Accelerations:** Dynamic balance between external driving forces, restoring forces, and frictional/drag damping.
- **Conservation Principles:** Energy, momentum, or probability density conservation in the physical medium.
- **Boundary Conditions:** Interactions occurring at boundaries, surfaces, or event horizons.

---

## 3. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`BouncingBallExperiment`](./BouncingBallExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week2.Day9`
- **Screen Architecture:** Compose Multiplatform with Canvas rendering & high-frequency physics tick.

### Reactive State Variables
The interactive state is managed via Compose `mutableStateOf` variables:

| State Variable | Initialization | Functional Role in Simulation |
| :--- | :--- | :--- |
| `restitution` | `mutableStateOf(0.82f)` | Reactive state tracking physical coordinate or control parameter |
| `airDragK` | `mutableStateOf(0.04f)` | Reactive state tracking physical coordinate or control parameter |
| `ballY` | `mutableStateOf(6.5f)` | Reactive state tracking physical coordinate or control parameter |
| `ballVelocityY` | `mutableStateOf(0f)` | Reactive state tracking physical coordinate or control parameter |
| `isDragging` | `mutableStateOf(false)` | Reactive state tracking physical coordinate or control parameter |
| `bounceCount` | `mutableStateOf(0)` | Reactive state tracking physical coordinate or control parameter |
| `maxHeightReached` | `mutableStateOf(6.5f)` | Reactive state tracking physical coordinate or control parameter |

### Frame Loop & Physics Integration
- **High-Precision Physics Loop:** Driven by `withFrameNanos` inside `LaunchedEffect`.
- **Time-Delta Numerical Integration:** Uses elapsed nanosecond delta ($\Delta t$) to compute velocity changes, angular acceleration, and position updates, ensuring smooth 60–120 FPS execution independent of device refresh rate.
- **Damping & Dissipation:** Exponential or linear damping applied per frame to simulate air resistance, viscosity, or thermal dissipation.

### User Gestures & Interactivity
- **Pointer Drag Gestures:** Configured via `.pointerInput { detectDragGestures { ... } }`, allowing real-time direct manipulation of particles, sources, or boundary walls on screen.
- **HUD & Slider Controls:** Real-time tweaking of physical constants (gravity, charge, index of refraction, viscosity, or frequency).

### Canvas Graphics Pipeline
- **Normalized Coordinates:** Physics calculations mapped to Canvas dimensions (`size.width`, `size.height`) via responsive scaling.
- **Render Functions:** Utilizes Compose DrawScope methods: `drawEnergyBars`, `drawLine`, `drawOval`, `drawRect`, `drawRoundRect`.
- **Visual Polish:** Neon color palette, anti-aliased vectors, radial gradients for glowing fields, and dynamic trail decay.

---

## 4. Suggested Investigations & Parameter Experiments
1. **Extremal Value Testing:** Push sliders to their minimum and maximum bounds to observe physical phase shifts or asymptotic behavior.
2. **Perturbation Dynamics:** Disturb the equilibrium state via touch drag and record how quickly the system dissipates energy back to ground state.
3. **Cross-Platform Verification:** Run across Android, iOS, and Desktop to ensure consistent physics step integration and high-DPI Canvas scaling.
