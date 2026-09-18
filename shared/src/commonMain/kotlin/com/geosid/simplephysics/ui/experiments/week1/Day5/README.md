# Day 5: Cartesian Diver

> **Week 1: Home Physics • Mind-Bending Home & Kitchen Physics**  
> *Topic Subtitle: Boyle's Law, Compressibility & Buoyancy*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Command a submarine diver inside a sealed water bottle to dive, hover, and surface using your hands.

### Scientific Principles & Mechanism
Named after French philosopher and mathematician René Descartes, the **Cartesian Diver** demonstrates the direct coupling between fluid pressure, gas compressibility, and buoyant forces.

Inside a sealed, flexible plastic bottle filled with water sits a small inverted dropper or pen cap containing a trapped air bubble. The diver is calibrated so that its average density (pen cap plus trapped air) is slightly less than water, causing it to float at the surface:

1. **Pascal's Principle:** Squeezing the plastic bottle increases hydrostatic pressure equally throughout the entire fluid.
2. **Boyle's Law ($P_1 V_1 = P_2 V_2$):** Water is virtually incompressible, but the trapped air bubble inside the diver compresses, shrinking its volume.
3. **Archimedes' Principle ($F_b = \rho V g$):** As the air bubble shrinks, water enters the diver. The volume of displaced water decreases, reducing the upward buoyant force $F_b$.
4. When $F_b < W_{\text{gravity}}$, the diver sinks to the bottom. Releasing the squeeze allows the air bubble to expand back to its original volume, restoring buoyancy so the diver floats back to the top.

### Laboratory / Kitchen Protocol (Try It At Home)
> Attach a small blob of modeling clay to the clip of a plastic pen cap. Adjust the clay until the cap barely floats upright in a glass of water with just its tip breaching the surface. Insert it into a 2-liter plastic soda bottle filled to the brim with water, screw the cap on tightly, and squeeze the sides with your hands!

---

## 2. Mathematical Foundation & Governing Equations

### Boyle's Law (Isothermal Gas Compression)
Under constant temperature, the trapped air bubble volume $V$ scales inversely with absolute pressure $P$:

$$
P \cdot V = \text{constant} \implies V(P) = V_0 \left(\frac{P_0}{P}\right)
$$

### Archimedes' Buoyancy vs. Weight
The net vertical force acting on the diver of total mass $m_{\text{diver}}$ is:

$$
F_{\text{net}} = F_b - W = \rho_{\text{water}} \cdot V_{\text{displaced}}(P) \cdot g - m_{\text{diver}} g
$$

### Neutral Buoyancy Condition (Hovering)
Neutral equilibrium occurs when upward buoyant force exactly equals gravitational weight ($F_{\text{net}} = 0$):

$$
F_b = W \implies \rho_{\text{avg}} = \frac{m_{\text{diver}}}{V_{\text{displaced}}(P^*)} = \rho_{\text{water}}
$$

In this simulation, neutral equilibrium is calibrated at $P^* \approx 1.65\text{ atm}$. Pressures below $1.65\text{ atm}$ result in net upward acceleration; pressures above $1.65\text{ atm}$ result in net sinking acceleration.

### Physical Meaning & Quantities
- **$P$ (Hydrostatic Pressure):** Ambient pressure ($1.0\text{ atm}$ or $101.3\text{ kPa}$) up to full squeeze ($3.5\text{ atm}$ or $354.6\text{ kPa}$).
- **$V(P) / V_0$:** Trapped air bubble volume fraction ($100\%$ at rest down to $\sim 28\%$ under maximum squeeze).
- **$F_b$ (Buoyant Force):** Upward thrust exerted by the displaced liquid.
- **$W$ (Diver Weight):** Downward gravitational force.

---

## 3. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`CartesianDiverExperiment`](./CartesianDiverExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week1.Day5`
- **Screen Architecture:** Compose Multiplatform with Canvas rendering & high-frequency physics tick.

### Reactive State Variables
The interactive state is managed via Compose `mutableStateOf` variables:

| State Variable | Type / Default | Functional Role in Simulation |
| :--- | :--- | :--- |
| `appliedPressureAtm` | `mutableStateOf(1.0f)` | Current hydrostatic internal pressure ($1.0 - 3.5\text{ atm}$) |
| `targetPressureAtm` | `mutableStateOf(1.0f)` | User-demanded target pressure from slider or touch squeeze |
| `isPressureLocked` | `mutableStateOf(false)` | Flag keeping pressure sustained without holding finger down |
| `isTouchingBottle` | `mutableStateOf(false)` | Active touch detection for real-time elastic squeeze interaction |
| `diverDepth` | `mutableStateOf(0.12f)` | Normalized diver position ($0.12$ surface to $0.86$ bottom) |
| `diverVelocityY` | `mutableStateOf(0f)` | Vertical ascent/descent velocity |
| `bubbleAnimPhase` | `Float` | Phase parameter driving rising micro-bubbles animation |

### Frame Loop & Physics Integration
- **High-Precision Physics Loop:** Driven by `withFrameNanos` inside `LaunchedEffect(isPressureLocked)`.
- **Elastic Plastic Bottle Relaxation:** When touch pressure is released and not locked, bottle elastically returns to $1.0\text{ atm}$ via spring interpolation:
  $$P \leftarrow P + (P_{\text{target}} - P) \cdot \min(1, 10\Delta t)$$
- **Hydrodynamic Acceleration & Damping:** Computes net acceleration $a_y = -F_{\text{net}}$ with viscous drag damping ($b = 3.2$) to model terminal terminal drift in water.

### User Gestures & Interactivity
- **Direct Squeeze Touch:** Touch and drag downward directly on the bottle canvas to squeeze it; releasing immediately lets the bottle spring back.
- **Tap to Squeeze:** Tap on the bottle to apply an instantaneous $2.8\text{ atm}$ pressure pulse.
- **Preset Action Chips:**
  - `🌊 Float (1.0 atm)`: Ambient room pressure; diver floats securely at the surface.
  - `⚖️ Hover (1.65 atm)`: Perfect neutral buoyancy; diver remains suspended midway.
  - `✊ Sink (2.8 atm)`: Heavy squeeze; air bubble shrinks and diver sinks to the bottom.
- **Continuous Pressure Slider:** Fine-tune hydrostatic pressure from $1.0\text{ atm}$ to $3.5\text{ atm}$.

### Canvas Graphics Pipeline
- **Scientific Coordinate Grid & Floor Shadow:** Coordinate grid with soft elliptical floor shadow under the bottle base.
- **Water Column Gradient:** Vertical gradient representing increasing hydrostatic head pressure with depth (`drawBottleWater`).
- **Pliable Plastic Bottle with Squeeze Indentation:** Deforms dynamically inward when squeezed with grip highlight lines (`drawBottlePlasticShell`).
- **Rising Micro-Bubbles:** Streams of bubbles that detach and rise whenever the diver sinks (`drawRisingMicroBubbles`).
- **Cartesian Diver & Air Bubble Meniscus:** Rendered diver body with visible internal water-air interface that visibly compresses as pressure rises (`drawCartesianDiver`).
- **Dynamic Force Vector Diagram:** Live arrows beside the diver showing upward buoyant force $F_b$ vs. downward gravity $W$ (`drawForceVectors`).

---

## 4. Suggested Investigations & Parameter Experiments
1. **The Neutral Hover Challenge:** Use the slider or select `⚖️ Hover (1.65 atm)`. Notice how delicate neutral buoyancy is: at $1.64\text{ atm}$ the diver slowly drifts up, while at $1.66\text{ atm}$ it slowly sinks down!
2. **Compressibility Verification (Boyle's Law):** Squeeze the bottle to $3.0\text{ atm}$. Observe the trapped air bubble inside the pen cap shrink to one-third ($33\%$) of its original volume.
3. **Elastic Rebound Dynamics:** Press and hold down on the bottle to drive the diver to the bottom. Abruptly release your finger: observe how rapidly the plastic walls snap back, the bubble expands, and buoyant force shoots the diver back to the top.
