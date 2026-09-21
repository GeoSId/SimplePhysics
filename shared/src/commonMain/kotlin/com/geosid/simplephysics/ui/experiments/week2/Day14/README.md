# Day 14: Compound Pulley System

> **Week 2: Classical Mechanics • Collisions, Conservation Laws & Rigid Bodies**  
> *Topic Subtitle: Mechanical Advantage & Cable Tension Multiplier*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Rig 1, 2, 4, or 8 pulley wheels to hoist massive industrial loads—from a 50 kg barbell to a 2,200 kg bank vault—using the pulling force of a single human hand.

### Scientific Principles & Mechanism
A pulley is a simple machine that changes the direction and/or magnitude of an applied pulling force. When arranged into compound assemblies (block and tackle), multiple pulley sheaves share the burden of supporting a load:

1. **Fixed Pulley (Direction Changer):** A sheave anchored to an overhead support does not provide force multiplication ($n = 1$). It merely redirects the applied tension vector, allowing gravity to assist the operator pulling downward.
2. **Movable Pulley & Tension Distribution:** When a pulley sheave is anchored directly to the suspended load, the supporting cable loops underneath it. Both the anchored end and the pulling end exert upward tension ($T$). Under static equilibrium, $\sum F_y = 2T - W_{\text{load}} = 0$, cutting the required input force in half ($F_{\text{in}} = W/2$).
3. **Compound Block & Tackle:** By grouping multiple sheaves into fixed upper and movable lower blocks, $n$ distinct cable strands run vertically between the blocks. Each strand bears an equal fraction of the total downward load ($T = W_{\text{load}} / n$).
4. **The Golden Rule of Mechanics:** Work input must equal work output in an ideal machine ($W_{\text{in}} = W_{\text{out}}$). Multiplying the lifting force by $n$ comes at an inescapable geometric cost: to raise the load by height $h$, each of the $n$ supporting strands must shorten by $h$, requiring the winch or worker to pull a cable length $d_{\text{rope}} = n \cdot h$.

Compound pulley systems form the backbone of modern cranes, elevators, sailing ship rigging, rescue winches, and rock-climbing haul systems.

### Laboratory / Kitchen Protocol (Try It At Home)
> Thread a smooth, strong rope around two broomsticks held firmly apart by two friends. You alone can pull the two sticks together even if both friends brace and resist with all their strength! Each wrap of the rope around the broomsticks adds two supporting strands, multiplying your pulling strength 4×, 6×, or 8×.

---

## 2. Mathematical Foundation & Governing Equations

### Ideal Mechanical Advantage (IMA)
The Ideal Mechanical Advantage is determined strictly by the number of load-bearing cable strands $n$ supporting the movable lower block:

$$
\text{IMA} = n
$$

- **$n = 1$ (`SINGLE_FIXED`):** 1 fixed sheave, 0 movable sheaves. Direction changer only ($\text{IMA} = 1$).
- **$n = 2$ (`SINGLE_MOVABLE`):** 1 fixed sheave, 1 movable sheave. Gun tackle ($\text{IMA} = 2$).
- **$n = 4$ (`BLOCK_AND_TACKLE`):** 2 fixed sheaves, 2 movable sheaves. Double luff tackle ($\text{IMA} = 4$).
- **$n = 8$ (`CRANE_RIG`):** 4 fixed sheaves, 4 movable sheaves. Industrial heavy crane rig ($\text{IMA} = 8$).

### Actual Mechanical Advantage (AMA) & Friction Efficiency
In real machines, bearing friction in sheave axles and bending resistance of the cable introduce mechanical losses characterized by system efficiency $\eta \in [0.50, 1.00]$ (default $\eta = 0.95$ or $95\%$):

$$
W_{\text{load}} = m \cdot g
$$

$$
F_{\text{in, ideal}} = \frac{W_{\text{load}}}{n}, \quad F_{\text{in, real}} = \frac{F_{\text{in, ideal}}}{\eta} = \frac{m \cdot g}{n \cdot \eta}
$$

$$
\text{AMA} = \frac{W_{\text{load}}}{F_{\text{in, real}}} = n \cdot \eta
$$

Where:
- $m$ is the suspended load mass in $\text{kg}$ ($g = 9.81\,\text{m/s}^2$).
- $W_{\text{load}}$ is downward gravitational load force in Newtons ($\text{N}$).
- $F_{\text{in, real}}$ is the tension required at the winch or operator hand ($\text{N}$).

### Cable Tension Equilibrium
Assuming negligible cable mass and low friction, each of the $n$ vertical strands supporting the movable block carries uniform tension:

$$
T_i \approx \frac{W_{\text{load}}}{n} \quad (i = 1, \dots, n)
$$

### Kinematics & The Golden Rule of Mechanics
Conservation of cable length dictates that pulling a length $\Delta d_{\text{rope}}$ through the winch raises the suspended load by vertical displacement $\Delta h$:

$$
\Delta d_{\text{rope}} = n \cdot \Delta h \iff v_{\text{load}} = \frac{v_{\text{winch}}}{n}
$$

### Work & Energy Conservation
The output work performed on the load versus the input work delivered by the winch satisfies:

$$
W_{\text{out}} = W_{\text{load}} \cdot h = m \cdot g \cdot h
$$

$$
W_{\text{in}} = F_{\text{in, real}} \cdot d_{\text{rope}} = \left(\frac{m \cdot g}{n \cdot \eta}\right) \cdot (n \cdot h) = \frac{m \cdot g \cdot h}{\eta} = \frac{W_{\text{out}}}{\eta}
$$

- In a lossless machine ($\eta = 1.0$): $W_{\text{in}} = W_{\text{out}}$ (no energy created or destroyed).
- With realistic friction ($\eta < 1.0$): $W_{\text{in}} > W_{\text{out}}$, where excess work $W_{\text{loss}} = W_{\text{in}} - W_{\text{out}} = W_{\text{in}}(1 - \eta)$ dissipates as heat in the sheave bearings.

### Sheave Rotational Kinematics
As the cable travels at linear speed $v_{\text{winch}}$, each pulley sheave rotates with angular displacement:

$$
\Delta \theta = \Delta h \cdot n \cdot 24\,\text{rad}
$$

Spokes on adjacent sheaves counter-rotate to visualize the serpentine routing of the continuous cable.

---

## 3. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`CompoundPulleyExperiment`](./CompoundPulleyExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week2.Day14`
- **Architecture:** Compose Multiplatform with hardware-accelerated Canvas rendering and sub-millisecond physics integration loop.

### Domain Models & Presets

#### Pulley Configurations (`PulleyConfig`)
| Configuration | Title | Strands ($n$) | Fixed Sheaves | Movable Sheaves | Color | Role / Mechanical Description |
| :--- | :--- | :---: | :---: | :---: | :---: | :--- |
| `SINGLE_FIXED` | 1× Fixed | 1 | 1 | 0 | `AmberVibrant` | Direction changer only; $F_{\text{in}} = W_{\text{load}}$ |
| `SINGLE_MOVABLE` | 2× Gun Tackle | 2 | 1 | 1 | `CyanNeon` | 1 moving sheave; halves required input force |
| `BLOCK_AND_TACKLE` | 4× Double Luff | 4 | 2 | 2 | `EmeraldNeon` | Classic block & tackle; divides input effort by 4 |
| `CRANE_RIG` | 8× Heavy Crane | 8 | 4 | 4 | `PurpleNeon` | Industrial multi-sheave crane; divides effort by 8 |

#### Cargo Presets (`PulleyCargo`)
| Cargo Preset | Title | Icon | Mass ($m$) | Accent Color |
| :--- | :--- | :---: | :---: | :---: |
| `WEIGHT_50` | 50 kg Barbell | 🏋️ | $50\,\text{kg}$ | `AmberVibrant` |
| `PIANO_400` | Grand Piano | 🎹 | $380\,\text{kg}$ | `CyanNeon` |
| `ENGINE_800` | V8 Engine | 🚗 | $750\,\text{kg}$ | `CoralNeon` |
| `VAULT_2500` | Gold Vault | 🏦 | $2,200\,\text{kg}$ | `PurpleNeon` |

### Reactive State Variables

| State Variable | Type / Initialization | Functional Role in Simulation |
| :--- | :--- | :--- |
| `selectedConfig` | `PulleyConfig.BLOCK_AND_TACKLE` | Active pulley configuration ($n \in \{1, 2, 4, 8\}$) |
| `selectedCargo` | `PulleyCargo.ENGINE_800` | Selected cargo preset defining mass and visual skin |
| `mass` | `Float` ($750\,\text{kg}$) | Tunable load mass $m \in [10\,\text{kg}, 3000\,\text{kg}]$ |
| `efficiency` | `Float` ($0.95$) | Bearing and cable transmission efficiency $\eta \in [0.50, 1.00]$ |
| `winchSpeed` | `Float` ($0.35\,\text{m/s}$) | Winch cable pull rate $v_{\text{winch}} \in [0.05\,\text{m/s}, 1.20\,\text{m/s}]$ |
| `isRunning` | `Boolean` (`true`) | Motorized winch running / paused state |
| `isMotorReversed` | `Boolean` (`false`) | Winch direction toggle (`false` = Hoisting $\uparrow$, `true` = Lowering $\downarrow$) |
| `loadHeight` | `Float` ($0.20\,\text{m}$) | Current suspended load height coordinate $h \in [0.05\,\text{m}, 1.65\,\text{m}]$ |
| `ropePulledTotal` | `Float` ($0.80\,\text{m}$) | Cumulative cable distance drawn through the winch |
| `sheaveRotationAngle` | `Float` ($0.0\,\text{rad}$) | Instantaneous rotational phase angle of spinning pulley sheaves |
| `isDraggingLoad` | `Boolean` (`false`) | Pointer touch gesture flag overriding winch kinematics during manual dragging |

### Frame Loop & Kinematic Integration
The simulation runs inside a high-frequency `LaunchedEffect` loop driven by `withFrameNanos`:
1. **Delta Time Clamping:** $\Delta t$ is measured in nanoseconds and clamped to $\Delta t \in [0.001\,\text{s}, 0.040\,\text{s}]$ to prevent physics explosions during app lifecycle transitions.
2. **Kinematic Step:**
   $$
   \Delta d_{\text{rope}} = v_{\text{winch}} \cdot \Delta t \cdot \text{dir}, \quad \Delta h = \frac{\Delta d_{\text{rope}}}{n}
   $$
3. **Auto-Reversal at Mechanical Limits:** When the load reaches ceiling clearance ($h \ge 1.65\,\text{m}$), the winch automatically reverses to lowering. When it nears ground contact ($h \le 0.05\,\text{m}$), it auto-reverses to hoisting.
4. **State Accumulation:** Updates `loadHeight`, accumulates `ropePulledTotal = (ropePulledTotal + \Delta h \cdot n)$, and increments `sheaveRotationAngle += \Delta h \cdot n \cdot 24\,\text{rad}$.

### User Gestures & Interactivity
- **Interactive Load Dragging:** Tap and drag vertically on the suspended cargo box (`pointerInput(detectDragGestures)`) to directly lift or lower the load. The dragged displacement $\Delta h$ dynamically back-drives cable pull distance and sheave rotation.
- **Canvas Tap Toggle:** Tap anywhere on the canvas (`detectTapGestures`) to instantly toggle winch direction between Hoisting and Lowering.
- **Pulley Configuration Chips:** Switch between 1×, 2×, 4×, and 8× rigs on the fly.
- **Cargo Quick-Select:** Choose between Barbell ($50\,\text{kg}$), Grand Piano ($380\,\text{kg}$), V8 Engine ($750\,\text{kg}$), or Gold Vault ($2,200\,\text{kg}$).
- **Continuous Sliders:** Fine-tune mass $m$ ($10\text{--}3,000\,\text{kg}$) and winch hoist speed $v$ ($0.05\text{--}1.20\,\text{m/s}$).
- **Control Buttons:** Run/Pause winch motor, toggle Hoist/Lower direction, and reset simulation to default state.

### Canvas Graphics Pipeline
1. **Workshop Backdrop (`drawPulleyWorkshopBackground`):** Dark gradient background (`ScienceDarkBg` to `ScienceDarkSurface`) overlaid with a 36 dp coordinate grid and an industrial concrete floor line at $y = 0.88\,h$.
2. **Ceiling Steel I-Beam Girder (`drawCeilingGirder`):** 20 dp high structural beam with steel borders and periodic rivet bolt studs along the ceiling line.
3. **Upper Fixed Pulley Block (`drawUpperPulleyBlock`):** Ceiling anchor mounting bracket with vertical axle hangers supporting 1, 2, or 4 sheave wheels with alternating spoke rotations.
4. **Lower Movable Pulley Block (`drawLowerPulleyBlock`):** Floating block frame carrying movable sheaves, axle pins, and a curved steel crane hook with amber wear highlight.
5. **Sheave Wheels (`drawSheaveWheel`):** Deep outer groove rims with colored accent borders, 4-spoke internal hubs rotating with $\theta$, and central axle pins.
6. **Continuous Threaded Cable (`drawPulleyRopeSystem`):**
   - For $n = 1$: Single cable path looping over the top sheave from cargo to operator grip.
   - For $n \ge 2$: Parallel vertical cable strands spanning between upper and lower sheaves, connected by smooth Bezier arc turnarounds over each sheave groove, terminating in an angled lead line to the operator puller.
7. **Puller Grip & Force Vector (`drawPullerGrip`):** Amber pulling handle with a downward arrow indicating the direction and point of effort application.
8. **Suspended Cargo (`drawSuspendedCargo`):** High-density cargo box with dual support chains, gradient body, caution hazard diagonal stripes along the bottom edge, center mass badge, and dynamic blue laser outline glow when actively dragged.
9. **Free-Body Force Vectors (`drawPulleyForceVectors`):**
   - **Upward Tension ($T$):** Cyan neon arrows on each of the $n$ vertical supporting strands showing shared upward force.
   - **Downward Gravity ($W = mg$):** Broad Coral neon arrow emanating downward from the cargo's center of mass.
10. **Work-Energy Telemetry HUD Card (`drawWorkEnergyGauge`):** Translucent on-canvas card displaying mechanical advantage ratio, work input vs. work output progress bar, and the Golden Rule indicator dot ($1/n$).

---

## 4. Suggested Investigations & Parameter Experiments

1. **The 8× Heavy Crane Extreme Test:** Select the **Gold Vault (2,200 kg)** with the **8× Heavy Crane** configuration ($n = 8$). Notice how the massive downward weight ($W = 2200 \times 9.81 = 21,582\,\text{N}$, over 2.2 metric tons of gravitational force) is reduced to an input effort of just:
   $$
   F_{\text{in}} = \frac{21,582\,\text{N}}{8 \times 0.95} \approx 2,840\,\text{N} \quad (\approx 289\,\text{kgf})
   $$
   Verify the distance penalty: lifting the vault by $1.5\,\text{m}$ requires the winch to reel in $8 \times 1.5\,\text{m} = 12.0\,\text{m}$ of cable!
2. **Verification of the Golden Rule of Mechanics:** With efficiency at 100% ($\eta = 1.0$), compare total input work $W_{\text{in}} = F_{\text{in}} \cdot d_{\text{rope}}$ against useful potential work $W_{\text{out}} = mgh$. Verify that $W_{\text{in}} = W_{\text{out}}$ identically across all configurations ($1\times, 2\times, 4\times, 8\times$): a pulley multiplies force, but energy is strictly conserved.
3. **Single Fixed Pulley Baseline ($n = 1$):** Select `SINGLE_FIXED` with the **50 kg Barbell**. Notice that input force equals load weight ($F_{\text{in}} = 491\,\text{N}$) and cable pulled equals lift height ($d_{\text{rope}} = h$). A single fixed pulley offers zero mechanical advantage ($\text{IMA} = 1$) and serves solely as an ergonomic directional redirector.
4. **Interactive Manual Dragging:** Grab the suspended engine or piano directly on the canvas and drag it up and down. Observe the live tension vectors and rotating sheave spokes responding proportionally to the mechanical advantage ratio $n$.
