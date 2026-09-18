# Day 7: Bernoulli Levitating Ball

> **Week 1: Home Physics • Mind-Bending Home & Kitchen Physics**  
> *Topic Subtitle: Aerodynamic Coandă Effect & Pressure Gradient*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Hover a lightweight ball inside an invisible stream of angled air without dropping.

### Scientific Principles & Mechanism
A ping-pong ball stably levitating in a stream of air from a hairdryer is one of the most striking demonstrations of fluid dynamics, driven by **Bernoulli's Principle** and the **Coandă Effect**:

1. **Bernoulli's Principle:** Faster-moving fluid exerts lower static pressure ($P + \frac{1}{2}\rho v^2 = \text{const}$). The high-speed air column has a significantly lower static pressure than the surrounding stationary ambient air.
2. **Inward Restoring Force:** If the ball drifts sideways away from the center of the air column, higher ambient atmospheric pressure on the outside pushes it back into the low-pressure core.
3. **Coandă Effect & Angled Lift:** Even when the blower nozzle is tilted at an angle (up to $\sim 40^\circ$), airflow curves around the ball's contour due to surface adhesion (viscous entrainment). The ball deflects the airstream downward, producing an equal and opposite upward reaction force (Newton's Third Law) that keeps the ball levitating against gravity!

### Laboratory / Kitchen Protocol (Try It At Home)
> Switch a hairdryer to its coolest setting and point the nozzle straight up. Place a lightweight ping-pong ball directly into the airstream. Once it floats stably, slowly tilt the hairdryer up to 35–45 degrees off-vertical—the ball remains magically trapped inside the tilted stream!

---

## 2. Mathematical Foundation & Governing Equations

### Bernoulli's Incompressible Energy Relation
Along a streamline of air flowing with density $\rho$ and velocity $v$:

$$
P + \frac{1}{2}\rho v^2 + \rho g h = \text{constant}
$$

### Dynamic Pressure ($q$)
The dynamic pressure within the jet core is:

$$
q = \frac{1}{2} \rho v^2
$$

### Radial Pressure Gradient (Restoring Force)
As the ball is displaced laterally by distance $r$ from the jet axis into an expanding jet of width $w_{\text{jet}}$, the transverse pressure gradient generates an inward restoring suction force:

$$
F_{\text{Bernoulli}} = -\nabla P \propto -\left(\frac{r}{w_{\text{jet}}}\right) \cdot \left(\frac{1}{2}\rho v^2\right)
$$

### Aerodynamic Drag Along Streamline
The drag force balancing gravity along the stream vector is:

$$
F_{\text{drag}} = \frac{1}{2} \rho v^2 C_d A_{\text{ball}}
$$

At equilibrium hover height, upward drag balances the longitudinal component of gravity ($W_\parallel = mg \cos\theta$), while inward Bernoulli suction balances the lateral component ($W_\perp = mg \sin\theta$).

### Physical Meaning & Quantities
- **$\rho = 1.225\text{ kg/m}^3$:** Ambient air density at sea level.
- **$v$ (Airflow Speed):** Air jet velocity ($10 - 30\text{ m/s}$).
- **$q = \frac{1}{2}\rho v^2$:** Dynamic pressure ($60 - 550\text{ Pa}$).
- **$\theta$ (Nozzle Tilt):** Angle of the air column ($-40^\circ$ to $+40^\circ$).
- **$m$ (Ball Mass):** Ping-Pong ($2.7\text{ g}$), Foam ($1.2\text{ g}$), or Wooden ($20.0\text{ g}$).
- **$C_d \approx 0.47$:** Aerodynamic drag coefficient for a smooth sphere.

---

## 3. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`BernoulliBallExperiment`](./BernoulliBallExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week1.Day7`
- **Screen Architecture:** Compose Multiplatform with Canvas rendering & high-frequency physics tick.

### Reactive State Variables
The interactive state is managed via Compose `mutableStateOf` variables:

| State Variable | Type / Default | Functional Role in Simulation |
| :--- | :--- | :--- |
| `ballType` | `BallType.PING_PONG` | Active ball preset (`PING_PONG`, `FOAM`, `WOODEN`) |
| `airSpeed` | `mutableStateOf(16f)` (m/s) | Blower air jet velocity ($10 - 30\text{ m/s}$) |
| `tiltAngleDeg` | `mutableStateOf(0f)` (deg) | Blower nozzle inclination angle ($-40^\circ$ to $+40^\circ$) |
| `ballPos` | `mutableStateOf<Offset?>` | Live Cartesian position of the levitating ball |
| `ballVelocity` | `mutableStateOf(Offset.Zero)` | 2D velocity vector tracking aerodynamic oscillation |
| `isDraggingBall` | `mutableStateOf(false)` | Flag indicating active user pointer displacement |
| `flowAnimPhase` | `Float` | Continuous cyclic phase driving airflow streamline particle animation |

### Frame Loop & Physics Integration
- **High-Precision Physics Loop:** Driven by `withFrameNanos` inside `LaunchedEffect(isRunning, airSpeed, tiltAngleDeg, ballType)`.
- **2D Jet Vector Decomposition:** Decomposes forces into stream-parallel and stream-normal axes based on nozzle tilt $\theta$.
- **In-Jet Trapping & Turbulent Wobble:**
  - Evaluates whether the ball lies within the expanding conical jet core ($w_{\text{jet}} \propto \text{distance}$).
  - Computes radial inward Bernoulli pressure gradient restoring the ball toward the axis.
  - Adds stochastic turbulence wobbles ($\sin(\omega t)$) characteristic of real ping-pong ball levitation.

### User Gestures & Interactivity
- **Direct Ball Drag & Release:** Touch and pull the ball out of the stream with your finger; release it to see if the suction cone captures it or if it falls to the floor.
- **Ball Material Selector:**
  - `🏓 Ping-Pong` ($2.7\text{ g}$): Classic lightweight hollow plastic sphere (balanced levitation).
  - `⚪ Foam Ball` ($1.2\text{ g}$): Ultra-lightweight foam (soars high in the air column).
  - `🪵 Wooden Ball` ($20.0\text{ g}$): Heavy solid wood (demonstrates gravity overcoming available aerodynamic lift).
- **Precision Sliders:** Real-time sliders for Airflow Speed ($10 - 30\text{ m/s}$) and Nozzle Tilt Angle ($-40^\circ$ to $+40^\circ$).

### Canvas Graphics Pipeline
- **Scientific Coordinate Grid:** Subtle background grid with floor shadow under the blower base.
- **Expanding Airflow Jet:** Glowing conical air column with animated streamlines and velocity-dependent glow (`drawAirflowJet`).
- **Inward Bernoulli Pressure Differential Arrows:** Inward-pointing vector arrows showing the net lateral atmospheric restoring force (`drawBernoulliPressureArrows`).
- **Levitating Sphere Shader:** 3D ball with radial specular highlights, color-coded by material preset, with rapid micro-wobble motion (`drawLevitatingBall`).
- **Rotating Blower Nozzle:** Metallic hairdryer nozzle at base that physically rotates with the tilt slider (`drawBlowerNozzle`).

---

## 4. Suggested Investigations & Parameter Experiments
1. **The Angled Coandă Limit:** With the Ping-Pong ball levitating stably at $0^\circ$, slowly increase the nozzle tilt angle to $30^\circ$, then $40^\circ$. Observe how the ball remains levitated sideways without falling, proving that the Coandă effect redirects airflow downward to generate vertical lift!
2. **Density & Mass Threshold:** Switch from `🏓 Ping-Pong` ($2.7\text{ g}$) to `🪵 Wooden Ball` ($20\text{ g}$). Notice that gravity immediately overwhelms the aerodynamic drag and Bernoulli forces, dropping the wooden ball to the ground.
3. **Airspeed vs. Equilibrium Height:** At $0^\circ$ tilt, adjust Airspeed from $10\text{ m/s}$ up to $30\text{ m/s}$. Observe how higher airspeed increases dynamic pressure $q = \frac{1}{2}\rho v^2$, pushing the equilibrium hover height substantially further away from the nozzle.
