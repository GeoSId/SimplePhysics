# Day 8: Projectile Drag & Crosswinds

> **Week 2: Classical Mechanics • Collisions, Conservation Laws & Rigid Bodies**  
> *Topic Subtitle: Aerodynamic Drag & Vector Wind Forces*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Aim a cannon and launch projectiles against quadratic aerodynamic drag and crosswinds.

### Scientific Principles & Mechanism
Real air creates aerodynamic skin friction and form drag proportional to velocity squared: F_drag = 0.5 * rho * C_d * A * |v_rel|^2, directed opposite to relative motion. Combined with gravity and wind gusts, equations of motion become coupled nonlinear differential equations.

### Laboratory / Kitchen Protocol (Try It At Home)
> Drop a flat sheet of paper and a crumpled paper ball simultaneously. The crumpled ball hits first due to lower cross-sectional area and drag!

---

## 2. Mathematical Foundation & Governing Equations

The physical behavior in this simulation is governed by:

$$
m \mathbf{\ddot{r}} = m\mathbf{g} - \frac{1}{2} \rho C_d A |\mathbf{v} - \mathbf{v}_{wind}|(\mathbf{v} - \mathbf{v}_{wind})
$$

### Physical Meaning & Quantities
The mathematical formulation connects key physical parameters:
- **Forces & Accelerations:** Dynamic balance between external driving forces, restoring forces, and frictional/drag damping.
- **Conservation Principles:** Energy, momentum, or probability density conservation in the physical medium.
- **Boundary Conditions:** Interactions occurring at boundaries, surfaces, or event horizons.

---

## 3. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`ProjectileMotionExperiment`](./ProjectileMotionExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week2.Day8`
- **Screen Architecture:** Compose Multiplatform with Canvas rendering & high-frequency physics tick.

### Reactive State Variables
The interactive state is managed via Compose `mutableStateOf` variables:

| State Variable | Initialization | Functional Role in Simulation |
| :--- | :--- | :--- |
| `launchAngleDeg` | `mutableStateOf(45f)` | Reactive state tracking physical coordinate or control parameter |
| `launchSpeed` | `mutableStateOf(45f)` | Reactive state tracking physical coordinate or control parameter |
| `dragCoefficient` | `mutableStateOf(0.35f)` | Reactive state tracking physical coordinate or control parameter |
| `windSpeed` | `mutableStateOf(-5.0f)` | Reactive state tracking physical coordinate or control parameter |
| `showVacuumComparison` | `mutableStateOf(true)` | Reactive state tracking physical coordinate or control parameter |
| `isFlying` | `mutableStateOf(false)` | Reactive state tracking physical coordinate or control parameter |
| `posX` | `mutableStateOf(0f)` | Reactive state tracking physical coordinate or control parameter |
| `posY` | `mutableStateOf(0f)` | Reactive state tracking physical coordinate or control parameter |

### Frame Loop & Physics Integration
- **High-Precision Physics Loop:** Driven by `withFrameNanos` inside `LaunchedEffect`.
- **Time-Delta Numerical Integration:** Uses elapsed nanosecond delta ($\Delta t$) to compute velocity changes, angular acceleration, and position updates, ensuring smooth 60–120 FPS execution independent of device refresh rate.
- **Damping & Dissipation:** Exponential or linear damping applied per frame to simulate air resistance, viscosity, or thermal dissipation.

### User Gestures & Interactivity
- **Pointer Drag Gestures:** Configured via `.pointerInput { detectDragGestures { ... } }`, allowing real-time direct manipulation of particles, sources, or boundary walls on screen.
- **HUD & Slider Controls:** Real-time tweaking of physical constants (gravity, charge, index of refraction, viscosity, or frequency).

### Canvas Graphics Pipeline
- **Normalized Coordinates:** Physics calculations mapped to Canvas dimensions (`size.width`, `size.height`) via responsive scaling.
- **Render Functions:** Utilizes Compose DrawScope methods: `drawCannon`, `drawCircle`, `drawGroundAndGrid`, `drawLine`, `drawPath`, `drawRect`.
- **Visual Polish:** Neon color palette, anti-aliased vectors, radial gradients for glowing fields, and dynamic trail decay.

---

## 4. Suggested Investigations & Parameter Experiments
1. **Extremal Value Testing:** Push sliders to their minimum and maximum bounds to observe physical phase shifts or asymptotic behavior.
2. **Perturbation Dynamics:** Disturb the equilibrium state via touch drag and record how quickly the system dissipates energy back to ground state.
3. **Cross-Platform Verification:** Run across Android, iOS, and Desktop to ensure consistent physics step integration and high-DPI Canvas scaling.
