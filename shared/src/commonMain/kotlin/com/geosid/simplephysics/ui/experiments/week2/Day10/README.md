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

## 2. Mathematical Foundation & Governing Equations

### Conservation Laws
For an ideal $N$-ball elastic collision:

$$
\sum_{i=1}^{N} m_i u_i = \sum_{i=1}^{N} m_i v_i, \quad \sum_{i=1}^{N} \frac{1}{2} m_i u_i^2 = \sum_{i=1}^{N} \frac{1}{2} m_i v_i^2
$$

### 1D Impact with Coefficient of Restitution ($e$)
When sphere $i$ strikes adjacent sphere $i+1$ with approach velocity $(u_1 - u_2) > 0$:

$$
v_1 = \frac{u_1 + u_2 - e(u_1 - u_2)}{2}, \quad v_2 = \frac{u_1 + u_2 + e(u_1 - u_2)}{2}
$$

For hardened chrome steel ($e \approx 0.99$), velocity transfer is almost $100\%$ complete ($v_1 \approx u_2, v_2 \approx u_1$).

### Pendulum Motion & Air Damping
Each suspended sphere of mass $m$ on string length $L$ oscillates under gravity $g$ with angular acceleration:

$$
\alpha_i = \frac{d^2\theta_i}{dt^2} = -\frac{g}{L} \sin\theta_i - b \, \omega_i
$$

### Physical Meaning & Quantities
- **$m$ (Ball Mass):** $0.12\text{ kg}$ per steel sphere.
- **$L$ (Suspension String Length):** $0.38\text{ m}$, setting the natural oscillation period $T \approx 2\pi\sqrt{L/g} \approx 1.24\text{ s}$.
- **$\theta_i, \omega_i$:** Angular deflection (rad) and angular velocity (rad/s) for sphere $i \in \{0, 1, 2, 3, 4\}$.
- **$e$ (Restitution / Elasticity):** Ratio of relative separation speed to approach speed ($0.85 - 1.00$).
- **$b$ (Damping Factor):** Aerodynamic damping coefficient modeling energy dissipation to ambient air.
- **$P_{\text{total}} = \sum m |v_i|$ & $E_{k,\text{total}} = \sum \frac{1}{2}m v_i^2$:** Total instantaneous momentum and kinetic energy.

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
