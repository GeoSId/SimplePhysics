# Day 1: Static Straw Levitation

> **Week 1: Home Physics • Mind-Bending Home & Kitchen Physics**  
> *Topic Subtitle: Electrostatic Induction & Dipole Torque*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Spin and steer a balanced plastic straw through empty air without ever touching it with your hands.

### Scientific Principles & Mechanism
Rubbing a plastic wand transfers electrons via the triboelectric effect, accumulating negative charge (-Q). When brought close to the uncharged straw, it induces electric polarization: electrons in the straw are repelled to the far side, leaving a net positive charge on the near side. The resulting electrostatic attraction creates a net torque (tau = r x F) about the balance point.

### Laboratory / Kitchen Protocol (Try It At Home)
> Balance a lightweight plastic straw across the top of a glass bottle or upside-down cup. Rub another straw, plastic ruler, or balloon vigorously against a wool sweater or dry hair, then bring it near the end of the balanced straw.

---

## 2. Mathematical Foundation & Governing Equations

The physical behavior in this simulation is governed by:

$$
F = \frac{1}{4\pi \varepsilon_0} \frac{q_1 q_2}{r^2}, \quad \tau = I \frac{d^2\theta}{dt^2} + b\frac{d\theta}{dt}
$$

### Physical Meaning & Quantities
The mathematical formulation connects key physical parameters:
- **Forces & Accelerations:** Dynamic balance between external driving forces, restoring forces, and frictional/drag damping.
- **Conservation Principles:** Energy, momentum, or probability density conservation in the physical medium.
- **Boundary Conditions:** Interactions occurring at boundaries, surfaces, or event horizons.

---

## 3. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`StaticStrawExperiment`](./StaticStrawExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week1.Day1`
- **Screen Architecture:** Compose Multiplatform with Canvas rendering & high-frequency physics tick.

### Reactive State Variables
The interactive state is managed via Compose `mutableStateOf` variables:

| State Variable | Initialization | Functional Role in Simulation |
| :--- | :--- | :--- |
| `wandPos` | `mutableStateOf(Offset(0.72f, 0.38f))` | Reactive state tracking physical coordinate or control parameter |
| `wandCharge` | `mutableStateOf(-0.85f)` | Reactive state tracking physical coordinate or control parameter |
| `strawAngleRad` | `mutableStateOf(0.3f)` | Reactive state tracking physical coordinate or control parameter |
| `strawAngularVelocity` | `mutableStateOf(0f)` | Reactive state tracking physical coordinate or control parameter |
| `isRubbing` | `mutableStateOf(false)` | Reactive state tracking physical coordinate or control parameter |

### Frame Loop & Physics Integration
- **High-Precision Physics Loop:** Driven by `withFrameNanos` inside `LaunchedEffect`.
- **Time-Delta Numerical Integration:** Uses elapsed nanosecond delta ($\Delta t$) to compute velocity changes, angular acceleration, and position updates, ensuring smooth 60–120 FPS execution independent of device refresh rate.
- **Damping & Dissipation:** Exponential or linear damping applied per frame to simulate air resistance, viscosity, or thermal dissipation.

### User Gestures & Interactivity
- **Pointer Drag Gestures:** Configured via `.pointerInput { detectDragGestures { ... } }`, allowing real-time direct manipulation of particles, sources, or boundary walls on screen.
- **HUD & Slider Controls:** Real-time tweaking of physical constants (gravity, charge, index of refraction, viscosity, or frequency).

### Canvas Graphics Pipeline
- **Normalized Coordinates:** Physics calculations mapped to Canvas dimensions (`size.width`, `size.height`) via responsive scaling.
- **Render Functions:** Utilizes Compose DrawScope methods: `drawBalancedStraw`, `drawChargedWand`, `drawCircle`, `drawElectrostaticField`, `drawGlassBottlePivot`, `drawLine`.
- **Visual Polish:** Neon color palette, anti-aliased vectors, radial gradients for glowing fields, and dynamic trail decay.

---

## 4. Suggested Investigations & Parameter Experiments
1. **Extremal Value Testing:** Push sliders to their minimum and maximum bounds to observe physical phase shifts or asymptotic behavior.
2. **Perturbation Dynamics:** Disturb the equilibrium state via touch drag and record how quickly the system dissipates energy back to ground state.
3. **Cross-Platform Verification:** Run across Android, iOS, and Desktop to ensure consistent physics step integration and high-DPI Canvas scaling.
