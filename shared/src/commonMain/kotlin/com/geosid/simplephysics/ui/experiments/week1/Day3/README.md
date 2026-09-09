# Day 3: Pencil Through Water Bag

> **Week 1: Home Physics • Mind-Bending Home & Kitchen Physics**  
> *Topic Subtitle: Polymer Elasticity & Self-Sealing*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Poke sharp pencils straight through a water-filled plastic bag without spilling a single drop!

### Scientific Principles & Mechanism
Plastic zipper sandwich bags are made of High-Density Polyethylene (HDPE) or Low-Density Polyethylene (LDPE). These are long chains of repeating polymer molecules. When pierced by a smooth, sharp pencil tip, the flexible polymer chains separate and stretch tightly around the shaft of the pencil like rubber bands, forming a watertight temporary gasket.

### Laboratory / Kitchen Protocol (Try It At Home)
> Fill a clean zip-lock bag 3/4 full with water and seal it. Sharpen 4-6 round pencils. With one smooth, steady motion, poke each pencil through one side and out the other over a sink or bowl!

---

## 2. Mathematical Foundation & Governing Equations

The physical behavior in this simulation is governed by:

$$
P = \rho g h \quad \text{sealed by} \quad \sigma_\theta = \frac{P \cdot r}{t}
$$

### Physical Meaning & Quantities
The mathematical formulation connects key physical parameters:
- **Forces & Accelerations:** Dynamic balance between external driving forces, restoring forces, and frictional/drag damping.
- **Conservation Principles:** Energy, momentum, or probability density conservation in the physical medium.
- **Boundary Conditions:** Interactions occurring at boundaries, surfaces, or event horizons.

---

## 3. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`PencilWaterBagExperiment`](./PencilWaterBagExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week1.Day3`
- **Screen Architecture:** Compose Multiplatform with Canvas rendering & high-frequency physics tick.

### Reactive State Variables
The interactive state is managed via Compose `mutableStateOf` variables:

| State Variable | Initialization | Functional Role in Simulation |
| :--- | :--- | :--- |
| `waterLevel` | `mutableStateOf(0.78f)` | Reactive state tracking physical coordinate or control parameter |
| `leakActive` | `mutableStateOf(false)` | Reactive state tracking physical coordinate or control parameter |

### Frame Loop & Physics Integration
- **High-Precision Physics Loop:** Driven by `withFrameNanos` inside `LaunchedEffect`.
- **Time-Delta Numerical Integration:** Uses elapsed nanosecond delta ($\Delta t$) to compute velocity changes, angular acceleration, and position updates, ensuring smooth 60–120 FPS execution independent of device refresh rate.
- **Damping & Dissipation:** Exponential or linear damping applied per frame to simulate air resistance, viscosity, or thermal dissipation.

### User Gestures & Interactivity
- **Pointer Drag Gestures:** Configured via `.pointerInput { detectDragGestures { ... } }`, allowing real-time direct manipulation of particles, sources, or boundary walls on screen.
- **HUD & Slider Controls:** Real-time tweaking of physical constants (gravity, charge, index of refraction, viscosity, or frequency).

### Canvas Graphics Pipeline
- **Normalized Coordinates:** Physics calculations mapped to Canvas dimensions (`size.width`, `size.height`) via responsive scaling.
- **Render Functions:** Utilizes Compose DrawScope methods: `drawArc`, `drawCircle`, `drawLine`, `drawPath`, `drawPiercingPencil`, `drawRoundRect`.
- **Visual Polish:** Neon color palette, anti-aliased vectors, radial gradients for glowing fields, and dynamic trail decay.

---

## 4. Suggested Investigations & Parameter Experiments
1. **Extremal Value Testing:** Push sliders to their minimum and maximum bounds to observe physical phase shifts or asymptotic behavior.
2. **Perturbation Dynamics:** Disturb the equilibrium state via touch drag and record how quickly the system dissipates energy back to ground state.
3. **Cross-Platform Verification:** Run across Android, iOS, and Desktop to ensure consistent physics step integration and high-DPI Canvas scaling.
