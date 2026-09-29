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

## 2. Mathematical Foundation: The 3 Pillars

### Pillar 1: Governing Law & Force
During impact with a rigid horizontal boundary, the normal velocity component is reversed and scaled by the **Coefficient of Restitution** $e$:

$$
v_+ = -e \cdot v_-, \quad e = \frac{|v_+|}{|v_-|} \in [0, 1]
$$

During vertical airborne flight between collisions, the ball experiences downward gravitational acceleration $g$ opposed by quadratic aerodynamic drag:

$$
F_{\text{net}} = -m g - k v |v| \implies a(t) = \frac{dv}{dt} = -g - \frac{k}{m} v |v|
$$

where $k = \frac{1}{2} C_d \rho A$ combines fluid density $\rho$, projected frontal area $A$, and drag coefficient $C_d$.

### Pillar 2: Kinematics & Geometric Motion Constraints
In the absence of air drag, the peak apex height $h_n$ after the $n$-th floor impact follows a strict geometric progression:

$$
h_n = e^{2n} h_0, \quad v_{n, \text{apex}} = 0, \quad v_{n, \text{impact}} = \sqrt{2 g h_{n-1}}
$$

The duration of the $n$-th flight bounce cycle is:

$$
T_n = 2 \frac{v_{n,+}}{g} = 2 e^n \sqrt{\frac{2 h_0}{g}}
$$

Summing all infinite bounce flight times yields a finite convergent geometric series — resolving the classical **bouncing ball paradox (Zeno's settling time)**:

$$
T_{\text{total}} = t_0 + \sum_{n=1}^{\infty} T_n = \sqrt{\frac{2 h_0}{g}} \left( 1 + 2 \sum_{n=1}^\infty e^n \right) = \sqrt{\frac{2 h_0}{g}} \left( \frac{1 + e}{1 - e} \right) < \infty
$$

### Pillar 3: Energy & Work Conservation
Total instantaneous mechanical energy $\mathcal{E}$ is the sum of gravitational potential and translational kinetic energy:

$$
\mathcal{E}(t) = E_p(t) + E_k(t) = m g y(t) + \frac{1}{2} m v(t)^2
$$

During each floor contact, mechanical energy is non-conserved due to inelastic micro-structural hysteresis:

$$
E_{k,+} = \frac{1}{2} m v_+^2 = \frac{1}{2} m (-e v_-)^2 = e^2 E_{k,-}
$$

The dissipated thermal and acoustic energy lost per bounce is:

$$
\Delta \mathcal{E}_{\text{lost}} = (1 - e^2) E_{k,-} = (1 - e^2) m g h_{n-1}
$$

### Physical Quantities & SI Parameter Table

| Symbol | Parameter | Value / Range in App | SI Units | Physical Role |
| :--- | :--- | :--- | :--- | :--- |
| $m$ | Ball Mass | $0.50$ | $\text{kg}$ | Inertial mass for force and energy calculation |
| $g$ | Gravity | $9.81$ | $\text{m/s}^2$ | Downward gravitational field acceleration |
| $e$ | Restitution | $0.05 - 0.98$ (Default $0.82$) | dimensionless | Velocity retention ratio across impact |
| $k$ | Viscous Drag | $0.00 - 0.15$ (Default $0.04$) | $\text{kg/m}$ | Quadratic aerodynamic drag coefficient |
| $y$ | Live Altitude | $0.00 - 8.50$ | $\text{m}$ | Instantaneous elevation above lab floor |
| $v$ | Live Velocity | $-15.0 - +15.0$ | $\text{m/s}$ | Instantaneous vertical speed ($+$ up, $-$ down) |
| $E_p$ | Potential Energy | $0.0 - 41.7$ | $\text{J}$ | Gravitational stored energy ($m g y$) |
| $E_k$ | Kinetic Energy | $0.0 - 45.0$ | $\text{J}$ | Dynamic motion energy ($\frac{1}{2} m v^2$) |
| $\mathcal{E}$ | Total Energy | $0.0 - 45.0$ | $\text{J}$ | Live mechanical energy sum ($E_p + E_k$) |

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
