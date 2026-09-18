# Day 6: Non-Newtonian Oobleck

> **Week 1: Home Physics • Mind-Bending Home & Kitchen Physics**  
> *Topic Subtitle: Shear-Thickening Fluid & Dilatancy*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** A liquid you can run across, but will swallow your foot if you stand still for a second.

### Scientific Principles & Mechanism
A dense suspension of cornstarch granules in water creates a classic **dilatant (shear-thickening)** non-Newtonian fluid, colloquially known as **Oobleck**.

Unlike Newtonian fluids (like pure water or oil) whose viscosity remains constant regardless of shear rate, Oobleck's apparent viscosity increases dramatically when subjected to mechanical stress:

1. **Low Shear Rate (Gentle Motion):** Water molecules freely lubricate the microscopic starch granules, allowing them to glide smoothly over one another. The fluid behaves like a regular liquid.
2. **High Shear Rate (Rapid Impact / Strike):** Sudden mechanical shear pushes the starch granules into direct contact, squeezing out the thin lubricating water layer. Friction causes the irregular granules to interlock into rigid **force chains**, undergoing a discontinuous **jamming transition** that momentarily solidifies the mixture.
3. Once the stress is removed, water flows back between the particles, relaxing the force chains and causing the solid to melt back into a puddle.

### Laboratory / Kitchen Protocol (Try It At Home)
> Mix 2 parts cornstarch with 1 part tap water in a bowl. Tap the surface rapidly with your knuckles—it feels like a hard rubber wall! Now slowly rest your fingers on the surface—they sink smoothly to the bottom. Try squeezing a handful into a firm ball, then open your palm: it instantly melts and drips through your fingers!

---

## 2. Mathematical Foundation & Governing Equations

### Ostwald–de Waele Power-Law Model
The relationship between shear stress $\tau$ and shear strain rate $\dot{\gamma} = \frac{du}{dy}$ is governed by the power-law model:

$$
\tau = K \cdot \dot{\gamma}^n
$$

### Apparent Viscosity ($\eta$)
Apparent viscosity is the ratio of shear stress to shear rate:

$$
\eta(\dot{\gamma}) = \frac{\tau}{\dot{\gamma}} = K \cdot \dot{\gamma}^{n-1}
$$

- For **Newtonian fluids**, $n = 1 \implies \eta = K = \text{constant}$.
- For **Shear-thinning (pseudoplastic) fluids** (e.g. ketchup, paint), $n < 1 \implies \eta$ decreases with shear.
- For **Shear-thickening (dilatant) fluids** like Oobleck, $n > 1 \implies \eta$ diverges sharply as shear rate increases!

In this simulation, the power-law exponent $n$ scales with starch concentration:

$$
n = 1.4 + 1.0 \cdot C_{\text{starch}} \quad (1.95 \le n \le 2.15)
$$

### Physical Meaning & Quantities
- **$\dot{\gamma}$ (Shear Rate):** Velocity gradient perpendicular to flow ($0 - 120\text{ s}^{-1}$).
- **$\tau$ (Shear Stress):** Force per unit area transmitted through the fluid ($\text{kPa}$).
- **$\eta$ (Apparent Viscosity):** Dynamic resistance to flow ($\text{Pa}\cdot\text{s}$).
- **$K$ (Consistency Index):** Fluid base consistency factor ($0.08 - 0.23$).
- **$C_{\text{starch}}$ (Starch Concentration):** Solid mass fraction ($55\% - 75\%$).

---

## 3. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`OobleckExperiment`](./OobleckExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week1.Day6`
- **Screen Architecture:** Compose Multiplatform with Canvas rendering & high-frequency physics tick.

### Reactive State Variables
The interactive state is managed via Compose `mutableStateOf` and `mutableStateListOf` variables:

| State Variable | Type / Default | Functional Role in Simulation |
| :--- | :--- | :--- |
| `selectedAction` | `OobleckAction.TOUCH_SWIPE` | Active interaction mode (`TOUCH_SWIPE`, `FAST_STRIKE`, `SLOW_DIP`, `KNEAD_MELT`) |
| `starchConcentration` | `mutableStateOf(0.65f)` | Starch-to-water mass ratio ($55\% - 75\%$) |
| `shearRate` | `mutableStateOf(0f)` ($\text{s}^{-1}$) | Live shear strain rate induced by touch or tools |
| `apparentViscosity` | `mutableStateOf(0.12f)` ($\text{Pa}\cdot\text{s}$) | Dynamically calculated viscosity $\eta$ |
| `shearStress` | `mutableStateOf(0f)` (kPa) | Resulting shear stress $\tau$ |
| `jammingFraction` | `mutableStateOf(0f)` | Solidification metric ($0.0$ fluid to $1.0$ jammed crystal) |
| `touchPos` | `mutableStateOf<Offset?>` | Active user touch coordinate on canvas |
| `touchDepth` | `mutableStateOf(0f)` | Fluid penetration depth ($0.0$ surface to $1.0$ bottom) |
| `demoPhase` | `mutableStateOf(0f)` | Periodic animation driver for automated tool demos |
| `ripples` | `SnapshotStateList<Ripple>` | Active ripples and fracture cracks expanding on surface |

### Frame Loop & Physics Integration
- **High-Precision Physics Loop:** Driven by `withFrameNanos` inside `LaunchedEffect(isRunning, selectedAction, starchConcentration)`.
- **Dynamic Jamming Transition:** Evaluates target jamming from shear rate:
  $$\text{targetJamming} = \text{coerceIn}\left(\frac{\dot{\gamma}}{25}, 0, 1\right)$$
  Smoothly blends jamming fraction with relaxation decay when shear stops.
- **Shear-Dependent Penetration Resistance:** Under low shear ($\dot{\gamma} < 1\text{ s}^{-1}$), probe sinks smoothly; under rapid impact ($\dot{\gamma} > 20\text{ s}^{-1}$), penetration is actively blocked.

### User Gestures & Interactivity
- **Direct Touch & Drag Interaction:** Swipe slowly across the dish to drag a finger through liquid oobleck, or swipe/tap vigorously to trigger surface fracturing.
- **Automated Demonstration Modes:**
  - `👆 Free Touch`: Direct user finger manipulation with speed-based shear detection.
  - `🥊 Fast Strike`: High-speed punch ($v \sim 6\text{ m/s}$) that rebounds elastically off the jammed surface.
  - `🥄 Slow Dip`: Gentle spoon descent ($v \sim 0.1\text{ m/s}$) sinking deep into the liquid.
  - `✊ Roll & Melt`: Continuous high-frequency rolling into a solid sphere followed by relaxation and liquefaction.
- **Starch Concentration Slider:** Adjust solid volume fraction from $55\%$ to $75\%$.

### Canvas Graphics Pipeline
- **Scientific Coordinate Grid:** Delicate lab bench grid with elliptical drop shadow under petri dish.
- **Lab Petri Dish Basin:** Translucent circular glass dish with metallic outer rim (`drawPetriDishRim`).
- **Dynamic Oobleck Surface:** Surface color smoothly transitions from milky liquid turquoise to chalky white-cyan as particle jamming increases (`drawOobleckSurface`).
- **Smooth Ripples vs. Crystalline Fracture Cracks:** Generates smooth circular rings for gentle disturbances, but renders jagged white fracture lines for high-shear strikes (`drawRipplesAndCracks`).
- **Tool Rendering:** Custom procedural graphics for finger probe, impact hammer tool, dipping spoon, and kneading ball with melting driplets.

---

## 4. Suggested Investigations & Parameter Experiments
1. **Strike vs. Dip Comparison:** Switch between `🥊 Fast Strike` and `🥄 Slow Dip`. Observe how the exact same fluid completely repels the fast hammer blow (producing jagged fracture cracks), yet allows the spoon to sink freely to the bottom.
2. **Starch Concentration Tuning:** Increase starch concentration to $75\%$. Notice that the consistency index $K$ and power-law exponent $n$ increase, causing the fluid to jam at much lower shear rates.
3. **Knead and Melt Cycle:** Select `✊ Roll & Melt`. Watch the oobleck maintain a stable solid sphere shape as long as agitation continues, and immediately collapse into a fluid puddle the moment agitation stops.
