# Day 17: Coriolis Effect

> **Week 3: Celestial Gravity & Orbits • Planetary Orbits & Spaceflight Dynamics**  
> *Topic Subtitle: Fictitious Forces on Rotating Reference Frames*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Throw a ball across a rotating carousel and watch its path curve dramatically from the spinning observer's view.

### Scientific Principles & Mechanism
The Coriolis effect is a fundamental consequence of observing motion from a **non-inertial, rotating reference frame**.

1. **The Rotating Carousel Paradox:** Imagine standing on a spinning playground carousel rotating counter-clockwise at angular speed $\Omega$. You throw a ball straight toward a friend sitting on the opposite rim:
   - **From the ground (inertial / lab observer):** The ball travels in a perfectly **straight line** at constant velocity (Newton's first law), while the carousel spins underneath it.
   - **From the carousel (rotating observer):** The ball curves smoothly to the **right** in mid-air as if an invisible sideways force is pulling it off course, completely missing your friend!
2. **Radial Tangential Velocity Gradient:** The root of the Coriolis acceleration is the spatial gradient of linear speed with radius:
   $$
   v_{\text{tangential}} = \Omega \, r
   $$
   When a body moves outward from the center ($r \uparrow$), it moves into regions of higher tangential speed. Because the particle retains its initial lower tangential momentum, the turntable surface accelerates ahead of it, causing the particle to fall behind (deflecting right for counter-clockwise rotation).
3. **Planetary Meteorology:** On Earth's rotating sphere ($\Omega \approx 7.292 \times 10^{-5}\text{ rad/s}$), the Coriolis acceleration deflects moving air masses to the **right** in the Northern Hemisphere and to the **left** in the Southern Hemisphere. Air rushing into a low-pressure core deflects rightward, circulating in a counter-clockwise vortex—creating giant oceanic hurricanes and cyclonic weather systems.

### Laboratory / Kitchen Protocol (Try It At Home)
> Place a marble at the center of a rotating kitchen Lazy Susan turntable and give it a quick outward push with your finger. Notice how its path traces an outward spiral curve rather than a straight radial line! Next, spin a turntable coated with washable paint and roll a marble across it: the painted track leaves a sharp curved spiral arc on the platter, yet an overhead smartphone camera confirms the marble traveled in a dead straight line in the room's frame.

---

## 2. Mathematical Foundation & The Three Pillars of Rotating Mechanics

### Pillar 1: Governing Law & Fictitious Forces

#### Frame Transformation Kinematics
Let an inertial reference frame have coordinates $\mathbf{r}$ and a rotating frame with angular velocity vector $\mathbf{\Omega}$ share the same origin. The time derivative of any arbitrary vector $\mathbf{A}$ in the inertial frame is related to its time derivative in the rotating frame by:

$$
\left(\frac{d\mathbf{A}}{dt}\right)_{\text{inertial}} = \left(\frac{d\mathbf{A}}{dt}\right)_{\text{rot}} + \mathbf{\Omega} \times \mathbf{A}
$$

Applying this transformation operator twice to the position vector $\mathbf{r}$:

$$
\mathbf{v}_{\text{inertial}} = \mathbf{v}_{\text{rel}} + \mathbf{\Omega} \times \mathbf{r}
$$

$$
\mathbf{a}_{\text{inertial}} = \mathbf{a}_{\text{rel}} + 2(\mathbf{\Omega} \times \mathbf{v}_{\text{rel}}) + \mathbf{\Omega} \times (\mathbf{\Omega} \times \mathbf{r}) + \frac{d\mathbf{\Omega}}{dt} \times \mathbf{r}
$$

For steady uniform rotation ($\dot{\mathbf{\Omega}} = \mathbf{0}$), setting Newton's second law $\mathbf{F}_{\text{real}} = m \mathbf{a}_{\text{inertial}}$ yields the effective equation of motion in the rotating frame:

$$
m \mathbf{a}_{\text{rel}} = \mathbf{F}_{\text{real}} + \mathbf{F}_{\text{coriolis}} + \mathbf{F}_{\text{centrifugal}}
$$

Where the two fictitious / inertial forces are:

$$
\mathbf{F}_{\text{coriolis}} = -2m (\mathbf{\Omega} \times \mathbf{v}_{\text{rel}})
$$

$$
\mathbf{F}_{\text{centrifugal}} = -m \mathbf{\Omega} \times (\mathbf{\Omega} \times \mathbf{r}) = m \Omega^2 \mathbf{r}_\perp
$$

#### Coriolis Acceleration Vector in 2D
For a planar turntable rotating about the vertical axis $\mathbf{\Omega} = \Omega \mathbf{\hat{z}}$ with relative velocity $\mathbf{v}_{\text{rel}} = (v_x, v_y)$:

$$
\mathbf{\Omega} \times \mathbf{v}_{\text{rel}} = (-\Omega v_y) \mathbf{\hat{x}} + (\Omega v_x) \mathbf{\hat{y}}
$$

$$
\mathbf{a}_{\text{coriolis}} = -2(\mathbf{\Omega} \times \mathbf{v}_{\text{rel}}) = 2\Omega v_y \mathbf{\hat{x}} - 2\Omega v_x \mathbf{\hat{y}}
$$

- **Northern Hemisphere ($\Omega > 0$, CCW):** $\mathbf{a}_{\text{coriolis}} \perp \mathbf{v}_{\text{rel}}$ pointing $90^\circ$ to the **right** of the motion.
- **Southern Hemisphere ($\Omega < 0$, CW):** $\mathbf{a}_{\text{coriolis}} \perp \mathbf{v}_{\text{rel}}$ pointing $90^\circ$ to the **left** of the motion.
- **Magnitude:** $|\mathbf{a}_{\text{coriolis}}| = 2 |\Omega| |\mathbf{v}_{\text{rel}}|$.

---

### Pillar 2: Kinematics & Motion Constraints (Inertial Circles & Balanced Dishes)

#### Flat Turntable vs Balanced Parabolic Dish
On a flat horizontal platter, both Coriolis and Centrifugal accelerations act:

$$
\ddot{x} = 2\Omega \dot{y} + \Omega^2 x
$$

$$
\ddot{y} = -2\Omega \dot{x} + \Omega^2 y
$$

Because the centrifugal term $\Omega^2 \mathbf{r}$ points outward, any moving particle spirals rapidly off the rim.

#### The Parabolic Dish & Pure Coriolis Motion
In laboratory fluid experiments (such as the Taylor-Couette or MIT rotating tables), the turntable is given a parabolic concave curvature:

$$
z(r) = \frac{\Omega^2 r^2}{2g}
$$

The inward component of gravity along the curved surface is:

$$
\mathbf{F}_{g,\parallel} = -m g \nabla z = -m \Omega^2 \mathbf{r}
$$

This inward gravity force **identically cancels the outward centrifugal force**:

$$
\mathbf{F}_{g,\parallel} + \mathbf{F}_{\text{centrifugal}} = -m \Omega^2 \mathbf{r} + m \Omega^2 \mathbf{r} = \mathbf{0}
$$

Under this perfect balance, the particle moves under **pure Coriolis acceleration**:

$$
\ddot{x} = 2\Omega \dot{y}, \quad \ddot{y} = -2\Omega \dot{x}
$$

#### Exact Analytical Solution: Inertial Circles
Defining the complex velocity variable $\xi(t) = \dot{x}(t) + i \dot{y}(t)$:

$$
\dot{\xi} = \ddot{x} + i \ddot{y} = 2\Omega \dot{y} - i 2\Omega \dot{x} = -i (2\Omega)(\dot{x} + i\dot{y}) = -i (2\Omega) \xi
$$

Integrating directly:

$$
\xi(t) = v_0 e^{-i (2\Omega) t}
$$

The particle's velocity vector rotates at a constant frequency $f_c = 2\Omega$. Integrating position gives a **pure closed circle** called an **Inertial Circle**:

$$
R_{\text{inertial}} = \frac{v_0}{2|\Omega|}
$$

The period of one complete inertial circle is:

$$
T_{\text{inertial}} = \frac{2\pi}{2|\Omega|} = \frac{\pi}{|\Omega|} = \frac{1}{2} T_{\text{turntable}}
$$

**Key Discovery:** The inertial period is exactly **half** the rotation period of the turntable! This is identical to the inertial oscillations observed in Earth's oceans and atmosphere at latitude $\phi$, where the local Coriolis parameter is $f = 2\Omega \sin\phi$.

---

### Pillar 3: Energy, Work & The Zero-Work Theorem

#### The Zero-Work Theorem of the Coriolis Force
The instantaneous mechanical work done by the Coriolis force on the moving particle is:

$$
P_{\text{cor}} = \mathbf{F}_{\text{coriolis}} \cdot \mathbf{v}_{\text{rel}} = -2m (\mathbf{\Omega} \times \mathbf{v}_{\text{rel}}) \cdot \mathbf{v}_{\text{rel}}
$$

By the scalar triple product identity, any vector cross product $\mathbf{A} \times \mathbf{B}$ is orthogonal to both $\mathbf{A}$ and $\mathbf{B}$:

$$
(\mathbf{\Omega} \times \mathbf{v}_{\text{rel}}) \perp \mathbf{v}_{\text{rel}} \implies P_{\text{cor}} = 0
$$

**Fundamental Invariant:** The Coriolis force **never does work**. It alters only the direction of motion, never the speed or kinetic energy of a body in the rotating frame!

#### Centrifugal Potential & Jacobi Energy
Because the centrifugal force is conservative, it derives from an effective potential:

$$
\mathbf{F}_{\text{centrifugal}} = -\nabla U_{\text{cent}}, \quad U_{\text{cent}}(\mathbf{r}) = -\frac{1}{2} m \Omega^2 r^2
$$

The total conserved quantity in the rotating frame (in the absence of friction) is the **Jacobi Energy**:

$$
E_J = \frac{1}{2} m v_{\text{rel}}^2 - \frac{1}{2} m \Omega^2 r^2 = \text{const}
$$

As a particle moves outward ($r \uparrow$), its centrifugal potential energy drops, causing its relative speed $v_{\text{rel}}$ to surge:

$$
v_{\text{rel}}(r) = \sqrt{v_0^2 + \Omega^2(r^2 - r_0^2)}
$$

---

## 3. Physical Parameters & System Constants

| Symbol | Parameter Description | Nominal Value | SI Unit | Compose UI Control |
| :--- | :--- | :--- | :--- | :--- |
| $\Omega$ | Turntable Angular Velocity | $2.2$ | $\text{rad/s}$ | `PhysicsSliderControl` (-5.0 to +5.0 rad/s) |
| $v_0$ | Initial Launch Speed | $2.5$ | $\text{m/s}$ | `PhysicsSliderControl` (0.5 to 5.0 m/s) |
| $\theta_0$ | Launch Direction Angle | $0.0$ | $\text{deg}$ | `PhysicsSliderControl` (0° to 360°) |
| $R_{\text{disk}}$ | Turntable Boundary Radius | $3.0$ | $\text{m}$ | Physical geometry scale |
| $a_{\text{cor}}$ | Coriolis Acceleration | $2\|\Omega\| v$ | $\text{m/s}^2$ | Real-time computed HUD metric |
| $a_{\text{cent}}$ | Centrifugal Acceleration | $\Omega^2 r$ | $\text{m/s}^2$ | Real-time computed HUD metric |
| $R_{\text{inert}}$ | Inertial Circle Radius | $v_0 / (2\|\Omega\|)$ | $\text{m}$ | Analytical orbital dimension |
| $\gamma$ | Atmospheric / Surface Drag | $0.04 - 0.35$ | $\text{s}^{-1}$ | Dissipation parameter |

---

## 4. Simulation Architecture & Kotlin Multiplatform Breakdown

### Source Location
- **Primary Composable:** [`CoriolisEffectExperiment`](./CoriolisEffectExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week3.Day17`
- **Architecture:** Dual-frame vector kinematics with sub-stepped Runge-Kutta numerical integration.

### Core Reactive State Variables
- `omegaRadS`: Mutable angular velocity with bi-directional rotation (positive for Northern CCW, negative for Southern CW).
- `ballPosRot`, `ballVelRot`: Position and velocity 2D vectors in the rotating turntable frame.
- `ballPosInert`, `ballVelInert`: Real-time mapped position and velocity in the stationary laboratory frame.
- `turntableAngleRad`: Accumulated rotational angle $\theta(t) = \int \Omega \, dt$ governing visual turntable orientation.
- `isBalancedDish`: Boolean toggle removing the centrifugal term to demonstrate pure Coriolis inertial circles.
- `hasPressureGrad`: Boolean toggle adding inward central force $-k\mathbf{r}$ to model atmospheric cyclone vortex dynamics.
- `activeFrame`: Toggle between `ROTATING` (turntable stationary on screen) and `INERTIAL` (turntable spins on screen).

### Dual-Frame Numerical Integration
The simulation loop uses `withFrameNanos` with 8 sub-steps per frame to guarantee high numerical stability:

```kotlin
// 1. Coriolis Acceleration: a_cor = -2 (Ω x v_rel)
val aCorX = 2f * omegaRadS * ballVelRot.y
val aCorY = -2f * omegaRadS * ballVelRot.x

// 2. Centrifugal Acceleration: a_cent = Ω² r (canceled if balanced parabolic dish)
val aCentX = if (isBalancedDish) 0f else (omegaRadS * omegaRadS * ballPosRot.x)
val aCentY = if (isBalancedDish) 0f else (omegaRadS * omegaRadS * ballPosRot.y)

// 3. Coordinate mapping from rotating frame to inertial laboratory frame:
val cosA = cos(turntableAngleRad)
val sinA = sin(turntableAngleRad)
ballPosInert = Offset(
    ballPosRot.x * cosA - ballPosRot.y * sinA,
    ballPosRot.x * sinA + ballPosRot.y * cosA
)
```

---

## 5. Suggested Investigations & Experiments

1. **The Inertial Circle Observation:** Select the **"Inertial Circle"** preset (`isBalancedDish = true`). Observe how the particle traces an exact closed circle in the rotating frame, while simultaneously moving in a straight line or gentle oscillation in the lab frame.
2. **Hemisphere Reversal:** Slide $\Omega$ from $+3.0\text{ rad/s}$ (Northern Hemisphere) to $-3.0\text{ rad/s}$ (Southern Hemisphere). Notice how the trajectory curvature instantly inverts from a rightward curve to a leftward curve.
3. **Cyclone Vortex Simulation:** Activate the **"Weather Cyclone"** preset. Watch how inward-directed pressure gradient forces combine with rightward Coriolis acceleration to produce a stable, counter-clockwise circulating tropical storm.
