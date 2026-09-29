# Day 10: Newton's Cradle

> **Week 2: Classical Mechanics • Collisions, Conservation Laws & Rigid Bodies**  
> *Topic Subtitle: Momentum Shockwaves & Elastic Collisions*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Pull back 1, 2, or 3 steel balls and watch compression shockwaves propagate instantly through the chain.

### Scientific Principles & Mechanism
Newton's Cradle demonstrates the simultaneous conservation of linear momentum ($\sum \mathbf{p} = \text{const}$) and kinetic energy ($\sum E_k = \text{const}$) during near-elastic collisions. When an incoming sphere collides with the stationary chain, it creates a compressive acoustic shockwave that propagates through the touching intermediate spheres without moving them, ejecting only the sphere(s) on the opposite end at equal velocity.

Because both laws must hold simultaneously:

$$
m v_{\text{in}} = m v_{\text{out}}, \quad \frac{1}{2} m v_{\text{in}}^2 = \frac{1}{2} m v_{\text{out}}^2
$$

A single incoming ball of mass $m$ at velocity $v$ **cannot** eject two stationary balls at velocity $v/2$ (which would conserve momentum but destroy half the kinetic energy). The number of departing spheres and their velocities are strictly uniquely determined.

### Laboratory / Kitchen Protocol (Try It At Home)
> Line up 5 identical coins touching in a straight line on a smooth table. Flick a coin hard into one end; watch only the single coin at the far end shoot away while the middle coins remain completely still!

---

## 2. Mathematical Foundation: The 3 Pillars

```
                     ┌────────────────────────────────────────┐
                     │          THE 3 PILLARS OF DAY 10       │
                     └────────────────────────────────────────┘
                                         │
         ┌───────────────────────────────┼───────────────────────────────┐
         ▼                               ▼                               ▼
┌─────────────────────────┐ ┌─────────────────────────┐ ┌─────────────────────────┐
│     1. GOVERNING LAW    │ │      2. KINEMATICS      │ │  3. ENERGY CONSERVATION │
│  Simultaneous Momentum  │ │  1D Restitution Impact  │ │ Mechanical Energy Balance│
│  & Energy Conservation  │ │ Shockwave Speed & Phase │ │ & Viscous Air Resistance│
└─────────────────────────┘ └─────────────────────────┘ └─────────────────────────┘
```

### Pillar 1: Governing Law & Simultaneous Conservation
For an ideal chain of $N$ identical spherical masses $m$, linear momentum and kinetic energy must be conserved simultaneously across any impact event:

$$
\sum_{i=1}^{N} m_i u_i = \sum_{i=1}^{N} m_i v_i, \quad \sum_{i=1}^{N} \frac{1}{2} m_i u_i^2 = \sum_{i=1}^{N} \frac{1}{2} m_i v_i^2
$$

When $k$ balls of mass $m$ strike the stationary chain at incoming velocity $v_{\text{in}}$, the simultaneous equations have a unique physically admissible solution: exactly $k$ balls must depart from the opposite end with velocity $v_{\text{out}} = v_{\text{in}}$.

### Pillar 2: Kinematics & Hertzian 1D Impact Restitution
When sphere $i$ collides with adjacent sphere $i+1$ with approach velocity $(u_i - u_{i+1}) > 0$, the post-collision velocities are determined by the coefficient of restitution $e$:

$$
v_i = \frac{u_i + u_{i+1} - e(u_i - u_{i+1})}{2}, \quad v_{i+1} = \frac{u_i + u_{i+1} + e(u_i - u_{i+1})}{2}
$$

For hardened chrome steel ($e \approx 0.99$), velocity transfer is virtually complete ($v_i \approx u_{i+1}, v_{i+1} \approx u_i$). The compressive acoustic shockwave travels through the steel spheres at the speed of sound in steel ($c_{\text{sound}} \approx 5,960\text{ m/s}$), rendering momentum transfer instantaneous on the pendulum's timescale.

### Pillar 3: Pendulum Restoring Dynamics & Energy Conservation
Each suspended sphere of mass $m$ on string length $L$ acts as a simple gravitational pendulum. In polar coordinates, the angular equation of motion with aerodynamic drag is:

$$
\frac{d^2\theta_i}{dt^2} = -\frac{g}{L} \sin\theta_i - b \, \frac{d\theta_i}{dt}
$$

The total mechanical energy of each pendulum is the sum of kinetic and gravitational potential energy:

$$
\mathcal{E}_i = \frac{1}{2} m (L \omega_i)^2 + m g L (1 - \cos\theta_i)
$$

In the absence of drag ($b = 0$) and with perfectly elastic impacts ($e = 1$), the total system energy $\sum \mathcal{E}_i$ is strictly constant.

---

## 3. Physical Parameters & SI Units Table

| Symbol | Parameter Description | Nominal Value (App) | Standard SI Units |
| :--- | :--- | :--- | :--- |
| $N$ | Number of Spheres | $5$ | count |
| $m$ | Sphere Mass (Chrome Steel) | $0.12$ | $\text{kg}$ |
| $r$ | Sphere Radius | $0.018$ | $\text{m}$ ($1.8\text{ cm}$) |
| $L$ | Suspension String Length | $0.38$ | $\text{m}$ ($38\text{ cm}$) |
| $g$ | Gravitational Acceleration | $9.81$ | $\text{m/s}^2$ |
| $T$ | Natural Pendulum Period | $2\pi\sqrt{L/g} \approx 1.24$ | $\text{s}$ |
| $e$ | Coefficient of Restitution | $0.85 \dots 1.00$ ($0.99$ default) | dimensionless |
| $b$ | Viscous Air Damping Factor | $0.002$ | $\text{s}^{-1}$ |
| $P$ | Total Linear Momentum | $\sum m_i \|v_i\|$ | $\text{kg}\cdot\text{m/s}$ |
| $E_k$ | Total Kinetic Energy | $\sum \frac{1}{2}m_i v_i^2$ | $\text{J}$ |

---

## 3. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`NewtonsCradleExperiment`](./NewtonsCradleExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week2.Day10`
- **Screen Architecture:** Compose Multiplatform with Canvas rendering & high-frequency physics tick.

### Reactive State Variables
The interactive state is managed via Compose `mutableStateOf` and `mutableStateListOf` structures:

| State Variable | Type / Default | Functional Role in Simulation |
| :--- | :--- | :--- |
| `angles` | `SnapshotStateList<Float>` (size 5) | Instantaneous deflection angle $\theta_i$ for each sphere |
| `angularVelocities` | `SnapshotStateList<Float>` (size 5) | Instantaneous angular velocity $\omega_i$ for each sphere |
| `impulseFlashes` | `SnapshotStateList<Float>` (size 5) | Shockwave visual bloom intensity ($1.0 \to 0.0$ decay) |
| `selectedPreset` | `CradlePreset.ONE_BALL` | Active configuration (`ONE_BALL`, `TWO_BALLS`, `THREE_BALLS`, `DUEL`) |
| `restitution` | `mutableStateOf(0.99f)` | Collision elasticity coefficient $e$ ($85\% - 100\%$) |
| `damping` | `mutableStateOf(0.002f)` | Viscous air resistance factor |
| `collisionCount` | `mutableStateOf(0)` | Total number of registered elastic impacts |
| `draggedBallIndex` | `mutableStateOf<Int?>(null)` | Index of currently dragged ball ($0..4$) |
| `isRunning` | `mutableStateOf(true)` | Active simulation loop runner |

### Frame Loop & Physics Integration
- **High-Precision Physics Loop:** Driven by `withFrameNanos` inside `LaunchedEffect(isRunning, restitution, damping)`.
- **16-Substep Collision Integration:** Subdivides each frame ($\Delta t / 16$) to ensure overlap separation and instantaneous momentum transfer without tunneling.
- **Bidirectional Collision Sweep:** Alternates forward (left-to-right) and backward (right-to-left) passes per sub-step, accurately propagating shockwaves across multi-ball contacts (e.g. 2 or 3 balls simultaneously).

### User Gestures & Interactivity
- **Direct Interactive Drag:** Touch and pull any ball outward to any release angle. Pulling an outer ball further automatically displaces adjacent balls outward.
- **Tap Canvas to Pause/Run:** Quick tap anywhere on the canvas toggles simulation playback.
- **Scenario Presets:**
  - `⚪ 1 Ball`: Standard single-ball impulse propagation.
  - `⚪⚪ 2 Balls`: Dual-ball displacement ejecting 2 balls on the opposite side.
  - `⚪⚪⚪ 3 Balls`: Triple-ball displacement demonstrating center-ball pass-through.
  - `⚔️ Duel (1 vs 1)`: Symmetric outer balls released simultaneously.
- **Elasticity & Damping Sliders:** Adjust restitution from hardened steel ($99\%$) down to inelastic alloy ($85\%$).

### Canvas Graphics Pipeline
- **Coordinate Grid Backdrop:** Scientific grid lines (`drawScientificGrid`).
- **Chrome Overhead Frame:** Metallic crossbar with vertical pillars and cast iron feet (`drawCradleFrame`).
- **Dual V-Suspension Strings:** Twin angled cords per sphere anchored to chrome mounting pegs, preventing out-of-plane twisting.
- **3D Metallic Chrome Spheres:** High-specular multi-stop radial gradient with intense white highlight, crisp chrome rim, and bottom bounce reflection (`drawChromeSphere`).
- **Impulse Shockwave Bloom:** Expanding neon cyan rings radiating from collision points upon impact.
- **Dynamic Floor Shadows:** Elliptical shadows under each ball whose scale and opacity modulate with ball altitude.

---

## 4. Suggested Investigations & Parameter Experiments
1. **Multi-Ball Shockwave Propagation:** Select the `⚪⚪ 2 Balls` preset. Observe how exactly 2 balls swing out on the far side, leaving the center ball temporarily stationary. Repeat with `⚪⚪⚪ 3 Balls` to observe the middle ball participating in both incoming and outgoing waves.
2. **Symmetric Duel Collision:** Select `⚔️ Duel (1 vs 1)`. Both outer balls collide simultaneously with the static middle three, bouncing symmetrically and maintaining perfect phase reflection.
3. **Hardened Steel vs. Inelastic Damping:** Lower elasticity from $99\%$ to $85\%$. Watch how the crisp, distinct click-clack transitions into a rapid chaotic cluster as kinetic energy dissipates into internal heat.
