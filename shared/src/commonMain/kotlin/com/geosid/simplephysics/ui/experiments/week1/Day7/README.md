# Day 7: Bernoulli Levitating Ball

> **Week 1: Home Physics • Mind-Bending Home & Kitchen Physics**  
> *Topic Subtitle: Aerodynamic Coandă Effect & Pressure Gradient*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Hover a lightweight ball inside an invisible stream of angled air without dropping.

### Scientific Principles & Mechanism
A ping-pong ball stably levitating in a high-speed air column from a hairdryer is one of the most celebrated demonstrations of fluid dynamics, driven by **Bernoulli's Principle** and the **Coandă Effect**:

1. **Bernoulli's Principle:** Along an inviscid streamline, an increase in fluid velocity occurs simultaneously with a decrease in static pressure ($P + \frac{1}{2}\rho v^2 = \text{const}$). The high-speed air jet has a significantly lower static pressure than the surrounding motionless ambient room air.
2. **Inward Restoring Suction:** If the ball drifts sideways away from the center of the air column, the higher ambient atmospheric pressure on the outside pushes it back into the low-pressure jet core.
3. **Coandă Effect & Tilted Stability:** When the blower nozzle is tilted at an angle (up to $\sim 40^\circ$), viscous boundary layer adhesion causes the air stream to curve around the sphere's contour (the Coandă effect). The ball deflects the air jet downward, generating an equal and opposite upward aerodynamic lift force (Newton's Third Law) that keeps the ball suspended against gravity!

### Laboratory / Kitchen Protocol (Try It At Home)
> Switch a hairdryer to its coolest setting and point the nozzle straight up. Place a lightweight ping-pong ball directly into the airstream. Once it floats stably, slowly tilt the hairdryer up to 35–45 degrees off-vertical—the ball remains magically trapped inside the tilted stream!

---

## 2. Mathematical Foundation & The 3 Pillars

### Pillar 1: Governing Law & Static Pressure Gradient
Along an incompressible streamline with fluid density $\rho$, velocity $v$, and static pressure $P$:

$$P + \frac{1}{2}\rho v^2 + \rho g h = \text{constant}$$

The dynamic pressure $q$ represents fluid kinetic energy per unit volume:

$$q = \frac{1}{2} \rho v^2$$

The lateral velocity gradient between the high-speed jet axis ($v = v_{\text{jet}}$) and ambient air ($v = 0$) produces a steep radial static pressure drop:

$$\Delta P(r) = -q \cdot \left(\frac{r}{w_{\text{jet}}}\right)$$

This pressure difference produces an inward restoring force acting across the ball's projected area:

$$\mathbf{F}_{\text{Bernoulli}} = -\nabla P \cdot V_{\text{eff}} \approx -\Delta P(r) \cdot A_{\text{proj}} \, \hat{n}_{\perp}$$

### Pillar 2: Kinematics & Aerodynamic Drag Equilibrium
Aerodynamic drag along the jet streamline acts against gravity:

$$F_{\text{drag}} = \frac{1}{2} \rho v^2 C_d A_{\text{ball}}$$

where $A_{\text{ball}} = \pi r^2$ is the projected frontal area and $C_d \approx 0.47$ is the drag coefficient of a smooth sphere.

Decomposing forces in the tilted reference frame at nozzle inclination angle $\theta$:
- **Longitudinal Axis (Along Jet):** Upward drag balances the parallel gravitational component:
  $$F_{\parallel} = F_{\text{drag}} - m g \cos\theta = m \, a_{\parallel}$$
- **Transverse Axis (Perpendicular to Jet):** Bernoulli pressure suction and Coandă streamline curvature balance the lateral gravitational component:
  $$F_{\perp} = F_{\text{Bernoulli}} - m g \sin\theta = m \, a_{\perp}$$

At stable hover equilibrium, $a_{\parallel} = 0$ and $a_{\perp} = 0$.

### Pillar 3: Energy & Incompressible Work Conservation
Under steady, incompressible flow with negligible heat transfer, the total stagnation pressure $P_0$ is strictly conserved along each streamline:

$$P_0 = P_{\text{static}} + \frac{1}{2}\rho v^2 = \text{constant}$$

Mechanical work done by the blower motor is converted into fluid kinetic energy, which is subsequently dissipated via viscous shear in the wake:

$$P_{\text{diss}} = \mathbf{F}_{\text{drag}} \cdot \mathbf{v}_{\text{rel}}$$

As the jet entrains stationary ambient air, total jet momentum is conserved while spreading out into a diverging conical profile:

$$w_{\text{jet}}(s) = w_0 + 2 s \tan(\alpha_{\text{div}})$$

---

## 3. Physical Parameters & SI Units

| Symbol | Parameter Description | Nominal Range | Default Value | SI Unit |
| :--- | :--- | :--- | :--- | :--- |
| $\rho$ | Ambient Air Density at Sea Level | $1.20 - 1.25$ | $1.225$ | $\text{kg}/\text{m}^3$ |
| $v$ | Airflow Nozzle Velocity | $10.0 - 30.0$ | $16.0$ | $\text{m}/\text{s}$ |
| $q$ | Dynamic Pressure ($\frac{1}{2}\rho v^2$) | $60.0 - 550.0$ | $156.8$ | $\text{Pa}$ |
| $\theta$ | Blower Nozzle Tilt Angle | $-40.0 - +40.0$ | $0.0$ | $\text{deg}$ |
| $m_{\text{ball}}$ | Ping-Pong Ball Mass | $1.2 - 20.0$ | $2.7$ | $\text{g}$ |
| $r_{\text{ball}}$ | Ball Radius | $0.015 - 0.025$ | $0.020$ ($40\text{ mm}$ dia) | $\text{m}$ |
| $C_d$ | Sphere Drag Coefficient | $0.45 - 0.50$ | $0.47$ | Dimensionless |
| $g$ | Standard Gravitational Acceleration | Constant | $9.81$ | $\text{m}/\text{s}^2$ |
| $w_0$ | Initial Nozzle Exit Width | Fixed | $35$ | $\text{px}$ |

---

## 4. Simulation Architecture & Numerical Stepping

### Source Location
- **Primary Composable:** [`BernoulliBallExperiment`](./BernoulliBallExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week1.Day7`
- **Architecture:** Jetpack Compose Multiplatform Canvas with elevated origin ($c_y = 0.40 \cdot h$), transparent telemetry HUD, and compact interactive controls deck.

### Numerical Integration Loop
1. **Frame Tick:** Driven by `withFrameNanos` with clamped time step $\Delta t \in [0.001\text{ s}, 0.033\text{ s}]$.
2. **Coordinate Projection:** Evaluates ball position relative to nozzle swivel anchor $(x_0, y_0) = (0.50 \cdot w, 0.62 \cdot h)$:
   $$s = (\mathbf{r} - \mathbf{r}_0) \cdot \hat{u}_{\text{stream}}$$
   $$r_{\perp} = (\mathbf{r} - \mathbf{r}_0) \cdot \hat{n}_{\text{stream}}$$
3. **Core Jet Capture & Force Stepping:**
   - When inside the diverging conical jet ($|r_{\perp}| < 1.5 \cdot w_{\text{jet}}$), computes inward suction gradient:
     $$F_{\perp} = -\left(\frac{r_{\perp}}{w_{\text{jet}}}\right) \cdot (1.8 \cdot q)$$
   - Computes quadratic velocity drag:
     $$F_{\parallel} = \frac{1}{2} \rho v(s)^2 C_d A$$
4. **Velocity Integration & Aerodynamic Damping:**
   $$\mathbf{a} = \frac{\mathbf{F}_{\text{total}}}{m}$$
   $$\mathbf{v}_{t+\Delta t} = \mathbf{v}_t + (\mathbf{a} - \gamma \mathbf{v}_t) \Delta t$$
   $$\mathbf{r}_{t+\Delta t} = \mathbf{r}_t + \mathbf{v}_{t+\Delta t} \Delta t$$

### Procedural Rendering Pipeline
- **Elevated Canvas Origin:** Nozzle positioned at $y = 0.62 \cdot h$, positioning the hover equilibrium at $y \approx 0.40 \cdot h$, leaving the bottom $35\%$ clear.
- **Airflow Jet Visualization:** Radial gradient cone with animated upward-flowing dashed streamlines.
- **Coandă Deflection Arc:** Highlights streamline curvature wrapping around the ball's upper hemisphere.
- **Transparent HUD:** `ExperimentHudCard` with transparent background displays live airspeed, dynamic pressure, nozzle tilt, and trapping state.
