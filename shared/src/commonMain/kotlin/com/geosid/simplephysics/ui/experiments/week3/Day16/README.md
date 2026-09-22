# Day 16: Gyroscopic Precession

> **Week 3: Celestial Gravity & Orbits • Planetary Orbits & Spaceflight Dynamics**  
> *Topic Subtitle: Angular Momentum & Anti-Gravity Wheel*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Spin a heavy bicycle wheel supported by a single string and observe how it rotates sideways instead of falling down.

### Scientific Principles & Mechanism
Gyroscopic precession is one of the most striking and counter-intuitive phenomena in classical mechanics. When a rapidly spinning wheel is supported at only one end of its axle:

1. **Angular Momentum as a Vector:** A wheel of moment of inertia $I$ spinning at high angular velocity $\vec{\omega}$ possesses a large spin angular momentum:
   $$
   \vec{L} = I \vec{\omega}
   $$
   This vector points along the wheel's axle according to the right-hand grip rule.
2. **Perpendicular Gravitational Torque:** The gravitational force $M\vec{g}$ acts downward at the wheel's center of mass, producing a gravitational torque about the support pivot:
   $$
   \vec{\tau}_g = \vec{r} \times (M \vec{g})
   $$
   Because the axle is horizontal (or tilted) and gravity acts vertically downward, the torque vector $\vec{\tau}_g$ is strictly horizontal and **perpendicular** to the angular momentum vector $\vec{L}$.
3. **Directional Steering of Angular Momentum:** Newton's rotational law states:
   $$
   \vec{\tau} = \frac{d\vec{L}}{dt} \implies d\vec{L} = \vec{\tau} \, dt
   $$
   Because $d\vec{L}$ is perpendicular to $\vec{L}$, the gravitational torque does **not** make the wheel tip downward; instead, it steers the vector $\vec{L}$ horizontally. The axle steadily rotates around the vertical pivot in a horizontal circle at the precession rate $\Omega_p$.
4. **The Inverse Speed Paradox:** The faster the wheel spins ($\omega \uparrow$), the *slower* it precesses ($\Omega_p \downarrow$). Conversely, as bearing friction slows the wheel down ($\omega \downarrow$), the precession visibly speeds up until the gyroscope reaches its critical stability limit and topples.

### Laboratory / Kitchen Protocol (Try It At Home)
> Hold the axle of a spinning bicycle wheel by a single loop of string tied to one tip of the axle. Release the wheel from your hands! Instead of falling downward under gravity, the wheel hangs horizontally in mid-air and slowly precesses in a circle around the suspension string. Alternatively, spin a child's toy gyroscope on the tip of a pencil and watch it lean sideways without falling over.

---

## 2. Mathematical Foundation & The Three Pillars of Gyroscopic Mechanics

### Pillar 1: Governing Law & External Torque

Rotational dynamics about the fixed pivot point are governed by Euler's angular momentum equation:

$$
\vec{\tau}_{\text{net}} = \frac{d\vec{L}}{dt}
$$

The gravitational force $M\vec{g}$ acts at the wheel's center of mass at lever-arm displacement $\vec{r}$:

$$
\vec{\tau}_g = \vec{r} \times (M \vec{g})
$$

In a spherical coordinate system where $\theta$ is the polar angle between the vertical $Z$-axis and the axle (or inclination angle $\alpha = 90^\circ - \theta$ from horizontal):

$$
|\vec{\tau}_g| = M g r \sin\theta = M g r \cos\alpha
$$

Because gravity $\vec{g} = -g \mathbf{\hat{z}}$ is vertical and the axle $\mathbf{\hat{n}}$ lies in the $\theta$-direction, the torque vector is purely azimuthal:

$$
\vec{\tau}_g = M g r \sin\theta \, \mathbf{\hat{\phi}}
$$

Crucially, $\vec{\tau}_g \cdot \vec{L} = 0$: the external torque is strictly orthogonal to the spin angular momentum vector.

---

### Pillar 2: Kinematics & Motion Constraints (Precession & Nutation)

#### Steady Precession Angular Velocity
In an infinitesimal time interval $dt$, the angular momentum vector changes by:

$$
d\vec{L} = \vec{\tau}_g \, dt
$$

Because $d\vec{L} \perp \vec{L}$, the magnitude $L = I_s \omega$ remains invariant to first order. The vector tip sweeps out a circular arc of radius $L \sin\theta$ through an azimuthal angle $d\phi$:

$$
|d\vec{L}| = (L \sin\theta) \, d\phi = |\vec{\tau}_g| \, dt = (M g r \sin\theta) \, dt
$$

Dividing by $dt$ yields the steady precession angular velocity $\Omega_p$:

$$
\Omega_p = \frac{d\phi}{dt} = \frac{|\vec{\tau}_g|}{L \sin\theta} = \frac{M g r \sin\theta}{I_s \omega \sin\theta} = \frac{M g r}{I_s \omega}
$$

**Remarkable Property:** The factor $\sin\theta$ cancels out entirely. To first order, the steady precession rate $\Omega_p$ is independent of the axle tilt angle $\theta$!

#### Vector Form of Precession
Precession can be expressed as a continuous vector rotation around the vertical axis $\mathbf{\hat{z}}$:

$$
\frac{d\vec{L}}{dt} = \vec{\Omega}_p \times \vec{L} = \vec{\tau}_g
$$

Where $\vec{\Omega}_p = \Omega_p \mathbf{\hat{z}} = \frac{M g r}{I_s \omega} \mathbf{\hat{z}}$.

#### Precession Period
The time required for the gyroscope to complete one full $360^\circ$ horizontal orbit is:

$$
T_p = \frac{2\pi}{\Omega_p} = \frac{2\pi I_s \omega}{M g r}
$$

#### Nutation Dynamics (Second-Order Perturbation)
When the gyroscope is released from rest or perturbed, the axle exhibits a secondary high-frequency nodding oscillation called **nutation**:

$$
\theta(t) = \theta_0 + \Delta\theta \cos(\omega_{\text{nut}} t)
$$

Where the nutation angular frequency is:

$$
\omega_{\text{nut}} = \frac{I_s \omega}{I_\perp}
$$

Where $I_\perp = I_{\text{pivot}} = I_{\text{cm}} + M r^2$ is the moment of inertia about the transverse axis through the pivot.

---

### Pillar 3: Energy, Work & Stability Criterion

#### Kinetic Energy Decomposition
The total kinetic energy of the precessing gyroscope is partitioned into spin and precession components:

$$
E_{\text{total}} = \frac{1}{2} I_s \omega^2 + \frac{1}{2} I_\perp \Omega_p^2 \sin^2\theta + M g r \cos\theta
$$

In the fast-gyroscope regime ($\omega \gg \Omega_p$), the spin kinetic energy $E_{\text{spin}} = \frac{1}{2} I_s \omega^2$ accounts for over $99\%$ of the system's mechanical energy.

#### Zero Work Done by Precession
The instantaneous rate of mechanical work done by the gravitational torque is:

$$
P = \vec{\tau}_g \cdot \vec{\Omega}_p = (M g r \sin\theta \, \mathbf{\hat{\phi}}) \cdot (\Omega_p \mathbf{\hat{z}}) = 0
$$

Because the torque vector is perpendicular to the precession angular velocity vector at all times, **gravity does zero mechanical work** on the gyroscope during steady precession!

#### Gyroscopic Stability Limit (Topple Criterion)
For a gyroscope spinning at low speed, the simple approximation breaks down when the spin kinetic energy cannot sustain the gyroscopic restoring reaction. Stable precession requires:

$$
\omega \ge \omega_{\text{crit}} = \sqrt{\frac{4 M g r I_\perp}{I_s^2} \cos\theta}
$$

Below $\omega_{\text{crit}}$, the gyroscope becomes unstable and tumbles downward under gravity.

---

### Physical Parameters & SI Units Table

| Parameter | Symbol | Simulation Value | Real Physical Equivalent | SI Unit | Physical Description |
| :--- | :---: | :---: | :---: | :---: | :--- |
| Wheel Mass | $M$ | $2.2$ (default) | $0.5\text{--}5.0\,\text{kg}$ | $\text{kg}$ | Mass of the spinning flywheel or bicycle wheel rim |
| Wheel Outer Radius | $R$ | $0.25$ | $0.25\,\text{m}$ ($25\,\text{cm}$) | $\text{m}$ | Radius of the flywheel disc / bicycle wheel |
| Axle Lever-Arm Distance | $r$ | $0.22$ | $0.22\,\text{m}$ ($22\,\text{cm}$) | $\text{m}$ | Distance from support pivot point to wheel center of mass |
| Spin Moment of Inertia | $I_s$ | $\frac{1}{2} M R^2 \approx 0.06875$ | $0.015\text{--}0.15$ | $\text{kg}\cdot\text{m}^2$ | Rotational inertia around the wheel symmetry axis |
| Spin Angular Velocity | $\omega$ | $130.0$ ($1241\,\text{RPM}$) | $25\text{--}320\,\text{rad/s}$ | $\text{rad/s}$ | High-speed rotation rate of wheel on its axle |
| Gravitational Acceleration | $g$ | $9.81$ | $9.80665$ | $\text{m/s}^2$ | Earth's standard surface gravitational acceleration |
| Axle Inclination Angle | $\alpha$ | $15.0^\circ$ | $-35^\circ\text{--}+60^\circ$ | $\text{rad}$ / ${}^\circ$ | Angle between axle and horizontal plane ($\theta = 90^\circ - \alpha$) |
| Gravitational Torque | $\tau_g$ | $4.59$ | $M g r \cos\alpha$ | $\text{N}\cdot\text{m}$ | Torque exerted by gravity about the support pivot |
| Spin Angular Momentum | $L$ | $8.94$ | $I_s \omega$ | $\text{kg}\cdot\text{m}^2/\text{s}$ | Invariant angular momentum vector along the axle |
| Precession Angular Velocity | $\Omega_p$ | $0.513$ ($29.4^\circ/\text{s}$) | $\frac{M g r}{I_s \omega}$ | $\text{rad/s}$ | Horizontal rotation rate around the vertical pivot |
| Precession Period | $T_p$ | $12.2$ | $\frac{2\pi}{\Omega_p}$ | $\text{s}$ | Time required to complete one full horizontal revolution |
| Nutation Frequency | $\omega_{\text{nut}}$ | $110.5$ | $\approx 0.85\,\omega$ | $\text{rad/s}$ | High-frequency nodding oscillation rate of the axle |
| Simulation Frame Delta | $\Delta t$ | $0.001\text{--}0.035$ | Frame-synchronized loop | $\text{s}$ | Discrete numerical integration step |

---

## 3. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`GyroscopicPrecessionExperiment`](./GyroscopicPrecessionExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week3.Day16`
- **Architecture:** Jetpack Compose Multiplatform with 3D axonometric projection Canvas rendering and `withFrameNanos` physics stepping.

### Domain Models: Gyroscope Presets (`GyroPreset`)

| Preset | Title | Icon | Mass ($M$) | Spin ($\omega$) | Tilt ($\alpha$) | Color | Physical Context |
| :--- | :--- | :---: | :---: | :---: | :---: | :---: | :--- |
| `BICYCLE` | Bike Wheel | 🚲 | $2.2\,\text{kg}$ | $130\,\text{rad/s}$ | $15^\circ$ | `CyanNeon` | Classic lecture hall bicycle wheel demonstration |
| `LAB_GYRO` | Lab Gyro | 🌀 | $0.6\,\text{kg}$ | $260\,\text{rad/s}$ | $25^\circ$ | `AmberVibrant` | Precision brass rotor with ultra-high spin and slow precession |
| `HEAVY_FLYWHEEL` | Flywheel | ⚙️ | $5.0\,\text{kg}$ | $95\,\text{rad/s}$ | $10^\circ$ | `PurpleNeon` | Heavy industrial gyroscopic stabilizer (ships, spacecraft) |
| `SLOW_TOPPLE` | Critical | ⚠️ | $2.0\,\text{kg}$ | $32\,\text{rad/s}$ | $30^\circ$ | `CoralNeon` | Low-spin regime near critical stability showing rapid precession |

### Reactive State Variables

| State Variable | Type / Initialization | Functional Role in Simulation |
| :--- | :--- | :--- |
| `selectedPreset` | `GyroPreset?` (`BICYCLE`) | Active apparatus preset; resets upon manual slider or drag adjustments |
| `wheelMassKg` | `Float` ($2.2\,\text{kg}$) | Flywheel mass $M \in [0.5, 5.0]\,\text{kg}$ |
| `spinSpeedRadS` | `Float` ($130.0\,\text{rad/s}$) | Wheel spin speed $\omega \in [25, 320]\,\text{rad/s}$ ($240\text{--}3055\,\text{RPM}$) |
| `axleTiltDeg` | `Float` ($15.0^\circ$) | Axle inclination angle $\alpha \in [-35^\circ, 60^\circ]$ from horizontal |
| `showNutation` | `Boolean` (`false`) | Toggle for secondary high-frequency nutation wobble ripple |
| `isRunning` | `Boolean` (`true`) | Animation loop play/pause toggle |
| `precessionAngleRad` | `Float` ($0.0\,\text{rad}$) | Instantaneous azimuthal precession angle $\phi \in [0, 2\pi)$ |
| `spinAngleRad` | `Float` ($0.0\,\text{rad}$) | Instantaneous wheel rotation phase angle $\psi \in [0, 2\pi)$ |
| `nutationPhaseRad` | `Float` ($0.0\,\text{rad}$) | High-frequency nutation oscillation phase |

### Frame Loop & Numerical Integration
The simulation runs inside a `LaunchedEffect(isRunning, precessionSpeedRadS, spinSpeedRadS, showNutation)` block driven by `withFrameNanos`:
1. **Delta-Time Clamping:** $\Delta t = (t_{\text{now}} - t_{\text{last}}) / 10^9$, clamped to $[0.001\,\text{s}, 0.035\,\text{s}]$.
2. **Precession Stepping:**
   $$
   \Delta\phi = \Omega_p \cdot \Delta t = \left( \frac{M g r \cos\alpha}{I_s \omega} \right) \cdot \Delta t
   $$
3. **Spin Stepping:**
   $$
   \Delta\psi = \omega \cdot \Delta t
   $$
4. **Phase Wrap:** $\phi = (\phi + \Delta\phi) \pmod{2\pi}$, $\psi = (\psi + \Delta\psi) \pmod{2\pi}$.

### Canvas 3D Graphics Pipeline
1. **Coordinate Floor (`drawGyroFloor`):** Ground plane with perspective grid lines and concentric circular base rings representing the floor stand.
2. **Support Pedestal (`drawPedestal`):** Vertical cylindrical support post with metallic gradient and light highlights.
3. **Precession Orbit Track (`drawPrecessionOrbit`):** Cyan dashed horizontal ellipse in 3D representing the path swept by the wheel center.
4. **Rigid Axle:** Solid metallic bar with specular highlight connecting the pivot ball to the outer wheel axle tip.
5. **Spinning Flywheel (`drawSpinningFlywheel`):**
   - Perspective-projected rim ellipse dynamically foreshortened with view angle.
   - 6 rotating spokes rotating at instantaneous angle $\psi(t)$.
   - Central wheel hub with neon accents.
6. **Vector Telemetry Overlay (`drawVectorArrow`):**
   - **$\vec{L}$ (Angular Momentum, CyanNeon):** Vector pointing outward along the axle ($\vec{L} = I \omega \mathbf{\hat{n}}$).
   - **$\vec{\tau}_g$ (Gravitational Torque, CoralNeon):** Horizontal vector perpendicular to the axle ($\vec{\tau} = \vec{r} \times M\vec{g}$).
   - **$\vec{\Omega}_p$ (Precession Velocity, EmeraldNeon):** Vertical vector pointing straight upward along the precession axis.
   - **$M\vec{g}$ (Gravity, CoralNeon dashed):** Downward force vector from the wheel center of mass.
7. **Transparent Telemetry HUD (`TransparentTelemetryHud`):** Displays real-time RPM, precession speed, angular momentum, and torque.

---

## 4. Suggested Investigations & Parameter Experiments

1. **The Inverse Speed Paradox:**
   Start with the **Bicycle Wheel** preset ($\omega = 130\,\text{rad/s}, \Omega_p \approx 29^\circ/\text{s}$). Slowly reduce the **Spin Speed ($\omega$)** slider down toward $30\,\text{rad/s}$:
   - Notice that the precession speed $\Omega_p$ does not slow down; it **races drastically faster**!
   - This occurs because $\Omega_p = \frac{\tau}{L} = \frac{\tau}{I \omega}$. A smaller denominator demands a larger angular precession rate!
2. **Nutation Wobble Demonstration:**
   Enable the **Nutation Wobble (Whip)** checkbox:
   - Watch the axle nod rapidly up and down as it precesses around the circle.
   - Notice how the tip of the axle traces a beautiful cycloidal / sinusoidal rosette curve in 3D space!
3. **The Anti-Gravity Illusion:**
   Observe the vector diagram on screen:
   - Gravity pulls straight DOWN.
   - Yet the wheel does not fall downward—it moves SIDEWAYS!
   - The Coral torque vector $\vec{\tau}$ points exactly along the instantaneous velocity of the Cyan angular momentum vector $\vec{L}$, continuously steering it in a horizontal circle!
4. **Interactive Steering:**
   Drag horizontally across the space canvas to manually push or steer the precession angle $\phi$, or drag vertically to test positive (tilted upward) vs. negative (drooping downward) axle angles.
