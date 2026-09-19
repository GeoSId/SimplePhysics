# Day 13: Stick-Slip Friction & Stiction

> **Week 2: Classical Mechanics • Collisions, Conservation Laws & Rigid Bodies**  
> *Topic Subtitle: Static vs. Kinetic Friction Transitions & Stiction Limit Cycles*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Pull a heavy sled with an elastic spring and observe sudden jerky slip-stick oscillations.

### Scientific Principles & Mechanism
The phenomenon of **stick-slip** motion (or *stiction*) arises because the static friction coefficient ($\mu_s$) is strictly greater than the kinetic friction coefficient ($\mu_k$):

1. **Stick Phase:** At the microscopic contact interface, microscopic roughness asperities interlock like gear teeth and form cold-weld bonds under the weight of the block ($N = mg$). As a motorized carriage pulls the spring at constant velocity $v_{\text{pull}}$, spring elongation builds tension $F_{\text{spring}} = k(x_{\text{puller}} - x_{\text{block}})$. As long as $F_{\text{spring}} < \mu_s N$, the sled remains stationary ($v = 0$).
2. **Slip Phase:** The moment spring tension exceeds the static friction threshold ($F_{\text{spring}} \ge \mu_s N$), the microscopic asperities shear apart catastrophically. Resistance instantly collapses to the lower kinetic friction level ($F_k = \mu_k N$).
3. **Overshoot & Re-sticking:** The excess force $(F_{\text{spring}} - F_k) > 0$ accelerates the sled forward rapidly. As it shoots forward, the spring contracts and its tension drops below $F_k$. Viscous drag and friction slow the block until its velocity drops back to zero ($v \to 0$), where static bonds instantly reform, re-sticking the sled and repeating the sawtooth cycle indefinitely.

This universal non-linear mechanism is responsible for violin strings singing under a bowed horsehair, screeching car tires and squeaking brakes, squeaky door hinges, and the seismic stick-slip rupture of tectonic earthquake faults.

### Laboratory / Kitchen Protocol (Try It At Home)
> Place a heavy hardcover book on a smooth tabletop. Hook a rubber band around the spine and pull the other end slowly and steadily with your index finger. Notice how the book does not move smoothly, but instead jerks forward in rapid, rhythmic jumps!

---

## 2. Mathematical Foundation & Governing Equations

### Coulomb Friction Thresholds
With normal force $N = m g$:

$$
F_{s,\max} = \mu_s N, \quad F_k = \mu_k N \quad (\mu_s > \mu_k)
$$

### Hooke's Law Spring Force
With pulling carriage position $x_{\text{puller}}(t) = v_{\text{pull}} \cdot t$ and block position $x$:

$$
F_{\text{spring}} = k (x_{\text{puller}} - x)
$$

### Phase-Dependent Equations of Motion

#### 1. Stick Phase ($v = 0$):
The block remains locked in static equilibrium as long as:

$$
|F_{\text{spring}}| < \mu_s N \implies \dot{x} = 0, \quad \ddot{x} = 0, \quad F_{\text{friction}} = F_{\text{spring}}
$$

#### 2. Slip Transition & Dynamic Sliding:
When $|F_{\text{spring}}| \ge \mu_s N$, stiction breaks. Newton's Second Law governs the acceleration:

$$
m \ddot{x} = k (x_{\text{puller}} - x) - \mu_k N \, \text{sgn}(\dot{x}) - c \, \dot{x}
$$

Where $c = 0.8 \sqrt{m k} \times 0.12$ is the light viscous damping coefficient.

#### 3. Re-sticking Criterion:
When relative velocity crosses zero ($\dot{x} \to 0$) and spring tension has decayed back below the static threshold ($|F_{\text{spring}}| \le \mu_s N$), the block re-locks into the **Stick Phase**.

### Physical Meaning & Quantities
- **$m$ (Block Mass):** Sled inertia ($0.25 - 35.0\text{ kg}$) determining normal force $N = mg$.
- **$k$ (Spring Stiffness):** Elastic spring constant ($28 - 220\text{ N/m}$).
- **$\mu_s$ (Static Friction Coefficient):** Peak stiction threshold factor ($0.30 - 1.30$).
- **$\mu_k$ (Kinetic Friction Coefficient):** Dynamic sliding resistance factor ($0.05 - 0.95$, strictly $\mu_k < \mu_s$).
- **$v_{\text{pull}}$ (Carriage Pull Speed):** Motorized pulling velocity ($0.12 - 0.50\text{ m/s}$).
- **$\Delta F = (\mu_s - \mu_k) N$:** Force drop at breakaway driving the amplitude of the sawtooth oscillation.

---

## 3. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`StickSlipFrictionExperiment`](./StickSlipFrictionExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week2.Day13`
- **Screen Architecture:** Compose Multiplatform with Canvas rendering & high-frequency physics tick.

### Reactive State Variables
The interactive state is managed via Compose `mutableStateOf` and `mutableStateListOf` variables:

| State Variable | Type / Default | Functional Role in Simulation |
| :--- | :--- | :--- |
| `selectedPreset` | `StickSlipPreset.HEAVY_SLED` | Active scenario preset (`HEAVY_SLED`, `BRAKE_SQUEAK`, `VIOLIN_BOW`, `TECTONIC_FAULT`) |
| `mass` | `12.0f` (kg) | Sled mass determining normal force $N = mg$ |
| `stiffness` | `45f` (N/m) | Elastic spring constant $k$ pulling the sled |
| `muStatic` ($\mu_s$) | `0.70f` | Static friction coefficient ($F_{s,\max} = \mu_s N$) |
| `muKinetic` ($\mu_k$) | `0.25f` | Kinetic sliding friction coefficient ($F_k = \mu_k N$) |
| `pullSpeed` | `0.25f` (m/s) | Velocity of the motorized pulling carriage $v_{\text{pull}}$ |
| `phase` | `FrictionPhase.STICK` | Discrete state machine (`STICK` vs `SLIP`) |
| `xPuller` | `mutableStateOf(0.40f)` (m) | Coordinate of motorized pulling carriage |
| `xBlock`, `vBlock` | `mutableStateOf(0f)` (m, m/s) | Coordinate and velocity of the sled block |
| `groundScrollOffset` | `mutableStateOf(0f)` | Visual conveyor belt scroll offset tracking relative motion |
| `isDraggingBlock` | `mutableStateOf(false)` | Flag indicating active user pointer override |
| `forceHistory` | `SnapshotStateList<Float>` | Circular buffer feeding real-time in-canvas sawtooth oscilloscope |
| `sparks` | `SnapshotStateList<SlipSpark>` | Ballistic spark particles emitted during high-speed slips |
| `slipCount`, `slipFrequency` | `Int`, `Float` (Hz) | Telemetry metrics tracking frequency and count of slip events |
| `stictionBreakIntensity` | `mutableStateOf(0f)` | Shockwave bloom halo intensity decaying after stiction breaks |

### Frame Loop & Physics Integration
- **High-Precision Physics Loop:** Driven by `withFrameNanos` inside `LaunchedEffect`.
- **8-Substep Numerical Integration:** Subdivides each frame ($\Delta t / 8$) to ensure instantaneous stiction threshold detection ($|F| \ge \mu_s N$) and precise zero-crossing capture without overshoot artifact.
- **Sawtooth Limit Cycle:** Continuous spring elongation during `STICK` builds tension until $F_{\text{spring}} \ge \mu_s N$, instantly transitioning to `SLIP` where resistance collapses to $\mu_k N$, producing the characteristic sawtooth waveform.

### User Gestures & Interactivity
- **Direct Touch Manipulation:** Drag the sled block directly on canvas to manually stretch or compress the spring and observe the release recoil.
- **Tap to Break Stiction:** Tap anywhere on the canvas to impart an instantaneous perturbation impulse that triggers slip.
- **Physical Presets:**
  - `🛷 Heavy Sled` ($m=12\text{kg}, k=45\text{N/m}, \mu_s=0.70, \mu_k=0.25$): Classic mechanical stiction.
  - `🚗 Brake Squeak` ($m=3.5\text{kg}, k=180\text{N/m}, \mu_s=0.90, \mu_k=0.38$): High-frequency automotive squeal.
  - `🎻 Violin Bow` ($m=0.25\text{kg}, k=220\text{N/m}, \mu_s=0.85, \mu_k=0.20$): Acoustic musical oscillation.
  - `🌋 Tectonic Fault` ($m=35\text{kg}, k=28\text{N/m}, \mu_s=1.05, \mu_k=0.22$): Interseismic stress accumulation & earthquake rupture.
- **Dynamic Coupling Sliders:** Sliders for $\mu_s, \mu_k, v_{\text{pull}}$, and stiffness $k$, automatically enforcing $\mu_k < \mu_s$.

### Canvas Graphics Pipeline
- **Sliding Floor & Scrolling Texture:** Laboratory sliding floor with dynamic horizontal displacement marks indicating relative surface motion (`drawSlidingFloor`).
- **Motorized Puller Carriage:** Wheeled pulling cart with cable spool moving at speed $v_{\text{pull}}$ (`drawPullerCarriage`).
- **Dynamic Helical Spring:** Coiled spring whose pitch expands and color modulates from cyan to warning amber as tension approaches $F_{s,\max}$ (`drawHelicalSpring`).
- **Sled Block & Stiction Break Bloom:** Heavy metallic block with weight readout and neon cyan stiction shockwave ring upon slip (`drawSledBlock`).
- **Free-Body Diagram (FBD):** Dynamic force vectors displaying spring tension, friction, normal force $N$, and gravity weight $W$ (`drawForceVectors`).
- **Microscopic Asperity Inset Loupe:** Bottom-left magnifying loupe displaying interlocking roughness teeth during stiction and shearing apart during slip (`drawMicroscopicAsperityInset`).
- **Sawtooth Oscilloscope HUD:** In-canvas top-left oscilloscope plotting live spring tension with dotted threshold lines for $F_{s,\max}$ and $F_k$ (`drawSawtoothOscilloscope`).

---

## 4. Suggested Investigations & Parameter Experiments
1. **$\mu_s - \mu_k$ Contrast & Oscillation Vanishing:** Adjust $\mu_k$ closer and closer to $\mu_s$. Notice how the amplitude of the sawtooth waveform shrinks until slip-stick oscillations completely disappear into smooth continuous sliding.
2. **Pull Speed vs. Frequency:** Increase pull speed $v_{\text{pull}}$ from $0.12\text{ m/s}$ to $0.50\text{ m/s}$. Observe how the stiction break frequency increases, directly demonstrating how drawing a violin bow faster increases vibration cycles.
3. **Earthquake Seismic Rupture:** Select the `🌋 Tectonic Fault` preset. Watch the long, quiet interseismic period where elastic strain slowly builds up, followed by a violent, high-energy co-seismic slip release with intense spark emissions.
