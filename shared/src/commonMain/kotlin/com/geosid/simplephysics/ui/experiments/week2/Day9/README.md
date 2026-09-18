# Day 9: Inelastic Bouncing Ball

> **Week 2: Classical Mechanics • Collisions, Conservation Laws & Rigid Bodies**  
> *Topic Subtitle: Restitution Coefficient & Energy Decay*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Drop a ball from customizable heights and observe height decay, floor deformation, and live kinetic vs potential energy graphs.

### Scientific Principles & Mechanism
When a ball strikes a rigid floor, the impact is partially **inelastic**. During the compression phase, kinetic energy is converted into elastic potential energy stored in the deformed material, while a fraction is irreversibly lost to internal friction, thermal heating, and acoustic sound waves. During restitution, the ball pushes off the floor, but leaves with reduced rebound speed $v_+ < |v_-|$.

The ratio of post-collision speed to pre-collision speed defines the **Coefficient of Restitution** ($e$):

$$
e = \frac{v_+}{|v_-|} \le 1.0
$$

Since kinetic energy scales quadratically ($E_k \propto v^2$), the fractional kinetic energy retained after each bounce is $e^2$, and the maximum rebound height follows a geometric decay sequence:

$$
h_n = e^{2n} h_0
$$

### Laboratory / Kitchen Protocol (Try It At Home)
> Drop a cold tennis ball (fresh from the freezer) versus a warm tennis ball (warmed in your pocket or sun) onto a hard kitchen tile from the same height. The warm ball bounces significantly higher because warm rubber is more elastic (higher $e$) and dissipates less energy internally!

---

## 2. Mathematical Foundation & Governing Equations

### Restitution & Energy Decay
For a bounce with rebound coefficient $e \in (0, 1)$:

$$
v_+ = -e \cdot v_-, \quad E_{k+} = e^2 E_{k-}, \quad \Delta E_{\text{lost}} = (1 - e^2) E_{k-}
$$

### Flight Dynamics with Quadratic Air Resistance
Between bounces, the ball falls under gravity $g$ and quadratic aerodynamic drag:

$$
F_{\text{drag}} = -k \, v \, |v| \implies a(t) = -g - \frac{k}{m} v |v|
$$

### Mechanical Energy Transformation
At any height $y$ and vertical velocity $v$:

$$
E_p = m g y, \quad E_k = \frac{1}{2} m v^2, \quad E_{\text{total}} = E_p + E_k
$$

As the ball falls, potential energy $E_p$ continuously converts into kinetic energy $E_k$, reaching maximum $E_k$ just prior to impact.

### Physical Meaning & Quantities
- **$m$ (Ball Mass):** $0.5\text{ kg}$.
- **$g$ (Gravitational Acceleration):** $9.81\text{ m/s}^2$.
- **$y$ (Height):** Instantaneous altitude above the floor ($0.0 - 8.5\text{ m}$).
- **$v$ (Velocity):** Instantaneous vertical velocity (m/s). Negative downward, positive upward.
- **$e$ (Coefficient of Restitution):** Material elasticity factor ($0.05 - 0.98$).
- **$k$ (Air Drag Coefficient):** Viscous resistance factor ($0.00 - 0.15$).
- **$h_n$ (Apex Height):** Peak altitude achieved on the $n$-th bounce cycle.

---

## 3. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`BouncingBallExperiment`](./BouncingBallExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week2.Day9`
- **Screen Architecture:** Compose Multiplatform with Canvas rendering & high-frequency physics tick.

### Reactive State Variables
The interactive state is managed via Compose `mutableStateOf` variables:

| State Variable | Type / Default | Functional Role in Simulation |
| :--- | :--- | :--- |
| `restitution` | `mutableStateOf(0.82f)` | Coefficient of restitution $e$ ($0.05 - 0.98$) |
| `airDragK` | `mutableStateOf(0.04f)` | Aerodynamic viscous drag constant $k$ ($0.00 - 0.15$) |
| `ballY` | `mutableStateOf(6.5f)` | Live height of ball center above floor in meters |
| `ballVelocityY` | `mutableStateOf(0f)` | Current vertical velocity $v$ in m/s |
| `isDragging` | `mutableStateOf(false)` | Flag indicating active user pointer drag interaction |
| `bounceCount` | `mutableStateOf(0)` | Total number of recorded floor impacts |
| `maxHeightReached` | `mutableStateOf(6.5f)` | Peak apex height recorded for ghost marker display |

### Frame Loop & Physics Integration
- **High-Precision Physics Loop:** Driven by `withFrameNanos` inside `LaunchedEffect(isDragging, restitution, airDragK)`.
- **6-Substep Numerical Integration:** Subdivides each frame ($\Delta t / 6$) for smooth aerodynamic trajectory computation and exact zero-crossing floor collision detection.
- **Rest-State Settling Threshold:** If $|v| < 0.25\text{ m/s}$ at $y \le 0$, velocity is zeroed out to prevent infinite micro-jitter (the *inelastic chatter / Zeno's bouncing ball* phenomenon).

### User Gestures & Interactivity
- **Vertical Drag & Drop:** Touch and drag the ball vertically on canvas to reposition it anywhere between $0\text{ m}$ and $8.5\text{ m}$. Releasing drops the ball with zero initial velocity.
- **Material Preset Chips:**
  - `Superball` ($e = 0.92$): High resilience polybutadiene rubber.
  - `Tennis` ($e = 0.75$): Pressurized rubber with felt cover.
  - `Wood` ($e = 0.50$): Dense wooden sphere.
  - `Putty` ($e = 0.15$): Viscoelastic modeling clay (near-total energy dissipation).
- **Interactive Action Buttons:**
  - `🏀 Drop Ball (7m)`: Resets the ball to $7\text{ m}$ height.
  - `Reset Icon`: Restores default values ($e = 0.82, k = 0.04, y = 6.5\text{ m}$).

### Canvas Graphics Pipeline
- **Lab Floor & Grid:** Horizontal surface line with subtle floor shading and laboratory backdrop.
- **Height Metric Ruler:** Left-aligned measuring ruler with metric markings and major ticks every $2\text{ meters}$.
- **Dynamic Floor Shadow:** Soft elliptical contact shadow beneath the ball that expands and darkens as the ball nears the floor.
- **Squash-and-Stretch Deformation:** Procedural aspect ratio squashing during impact ($y \le 0.05\text{ m}$) proportional to velocity, compressing vertically and expanding horizontally.
- **Apex Ghost Marker:** Dashed horizontal line indicating the maximum rebound height reached.
- **Live Mechanical Energy Split Card (`drawEnergyBars`):** In-canvas bar charts displaying live Potential Energy $E_p$ (Cyan), Kinetic Energy $E_k$ (Emerald), and Total Energy $E_{\text{total}}$ (Amber).

---

## 4. Suggested Investigations & Parameter Experiments
1. **Geometric Height Decay:** Drop the ball from $8\text{ m}$ with $e = 0.80$. Record the apex height of successive bounces ($h_1 \approx 5.12\text{ m}$, $h_2 \approx 3.28\text{ m}$, $h_3 \approx 2.10\text{ m}$) and verify the relation $h_n = e^{2n} h_0$.
2. **Superball vs. Putty:** Select `Superball` ($e = 0.92$, losing only $\sim 15\%$ energy per bounce) and observe sustained bouncing. Then switch to `Putty` ($e = 0.15$, losing $\sim 98\%$ energy per bounce) and watch the ball hit the floor with a heavy thud, stopping almost immediately.
3. **Air Drag Influence:** Drop the ball from $8.5\text{ m}$ with Air Drag set to `0.00` vs. `0.15`. Observe how aerodynamic drag reduces the peak velocity just before impact, causing even faster height decay than predicted by restitution alone.
4. **Energy Equipartition at Mid-Flight:** Watch the live energy bar chart. At half the peak drop height, notice how Potential Energy ($E_p$) and Kinetic Energy ($E_k$) are exactly equal!
