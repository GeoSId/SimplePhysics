# Day 7: Bernoulli Levitating Ball

> **Week 1: Home Physics • Mind-Bending Home & Kitchen Physics**  
> *Topic Subtitle: Aerodynamic Coanda Effect & Pressure Gradient*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Hover a lightweight ball inside an invisible stream of angled air without dropping.

### Scientific Principles & Mechanism
Bernoulli's principle states that faster-moving air creates lower static pressure. As high-speed air rushes past the ball, the static pressure inside the stream drops below ambient room pressure. If the ball tries to leave the airstream, higher external atmospheric pressure pushes it back in.

### Laboratory / Kitchen Protocol (Try It At Home)
> Turn on a hairdryer on cool air. Place a ping pong ball into the upward stream and slowly tilt the dryer up to 45 degrees!

---

## 2. Mathematical Foundation & Governing Equations

The physical behavior in this simulation is governed by:

$$
P + \frac{1}{2}\rho v^2 + \rho g h = \text{constant}
$$

### Physical Meaning & Quantities
The mathematical formulation connects key physical parameters:
- **Forces & Accelerations:** Dynamic balance between external driving forces, restoring forces, and frictional/drag damping.
- **Conservation Principles:** Energy, momentum, or probability density conservation in the physical medium.
- **Boundary Conditions:** Interactions occurring at boundaries, surfaces, or event horizons.

---

## 3. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`BernoulliBallExperiment`](./BernoulliBallExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week1.Day7`
- **Screen Architecture:** Compose Multiplatform with Canvas rendering & high-frequency physics tick.

### Reactive State Variables
The interactive state is managed via Compose `mutableStateOf` variables:

| State Variable | Initialization | Functional Role in Simulation |
| :--- | :--- | :--- |
| `paramPrimary` | `mutableStateOf(0.5f)` | Reactive state tracking physical coordinate or control parameter |
| `paramSecondary` | `mutableStateOf(0.7f)` | Reactive state tracking physical coordinate or control parameter |
| `isRunning` | `mutableStateOf(true)` | Reactive state tracking physical coordinate or control parameter |
| `touchPos` | `mutableStateOf(Offset(0.5f, 0.5f))` | Reactive state tracking physical coordinate or control parameter |

### Frame Loop & Physics Integration
- **High-Precision Physics Loop:** Driven by `withFrameNanos` inside `LaunchedEffect`.
- **Time-Delta Numerical Integration:** Uses elapsed nanosecond delta ($\Delta t$) to compute velocity changes, angular acceleration, and position updates, ensuring smooth 60–120 FPS execution independent of device refresh rate.
- **Damping & Dissipation:** Exponential or linear damping applied per frame to simulate air resistance, viscosity, or thermal dissipation.

### User Gestures & Interactivity
- **Pointer Drag Gestures:** Configured via `.pointerInput { detectDragGestures { ... } }`, allowing real-time direct manipulation of particles, sources, or boundary walls on screen.
- **HUD & Slider Controls:** Real-time tweaking of physical constants (gravity, charge, index of refraction, viscosity, or frequency).

### Canvas Graphics Pipeline
- **Normalized Coordinates:** Physics calculations mapped to Canvas dimensions (`size.width`, `size.height`) via responsive scaling.
- **Render Functions:** Utilizes Compose DrawScope methods: `drawCircle`, `drawLine`, `drawPath`.
- **Visual Polish:** Neon color palette, anti-aliased vectors, radial gradients for glowing fields, and dynamic trail decay.

---

## 4. Suggested Investigations & Parameter Experiments
1. **Extremal Value Testing:** Push sliders to their minimum and maximum bounds to observe physical phase shifts or asymptotic behavior.
2. **Perturbation Dynamics:** Disturb the equilibrium state via touch drag and record how quickly the system dissipates energy back to ground state.
3. **Cross-Platform Verification:** Run across Android, iOS, and Desktop to ensure consistent physics step integration and high-DPI Canvas scaling.
