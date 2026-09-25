# Day 20: Lagrange Points (L1 to L5)

> **Week 3: Celestial Gravity & Orbits • Planetary Orbits & Spaceflight Dynamics**  
> *Topic Subtitle: Gravitational Equilibrium & Orbital Parking*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Park telescopes at Earth-Sun L1 and L2 points and observe how gravitational pull and centrifugal force balance perfectly.

### Scientific Principles & Mechanism
In celestial mechanics, the **Circular Restricted Three-Body Problem (CR3BP)** models the motion of a test body of negligible mass (such as a spacecraft, space telescope, or asteroid) moving under the gravitational influence of two massive primary bodies ($M_1$ and $M_2$) that revolve in circular orbits about their common center of mass (barycenter).

1. **The Co-Rotating (Synodic) Reference Frame:**
   - In an inertial reference frame, both planets orbit the barycenter with angular velocity $\\mathbf{\\Omega}$.
   - By transforming into a **co-rotating (synodic) reference frame** that rotates synchronously with the two primaries at angular speed $\\Omega = \\sqrt{G(M_1 + M_2)/R^3}$, the two massive bodies become completely stationary.
   - In this rotating frame, any moving test mass experiences two apparent inertial (fictitious) forces:
     - **Centrifugal Force:** Directed radially outward from the axis of rotation:
       $$
       \\mathbf{F}_{\\text{cent}} = -m \\mathbf{\\Omega} \\times (\\mathbf{\\Omega} \\times \\mathbf{r}) = m \\Omega^2 \\mathbf{r}_\\perp
       $$
     - **Coriolis Force:** Perpendicular to the spacecraft's velocity vector in the rotating frame:
       $$
       \\mathbf{F}_{\\text{cor}} = -2 m (\\mathbf{\\Omega} \\times \\mathbf{v}_{\\text{rel}})
       $$

2. **The Effective Gravitational-Centrifugal Potential ($\\Phi_{\\text{eff}}$):**
   - Combining the two Newtonian gravitational potentials with the outward centrifugal potential yields the **effective potential** $\\Phi_{\\text{eff}}(\\mathbf{r})$.
   - The stationary points of this scalar field—where the net force in the rotating frame vanishes identically ($\\nabla \\Phi_{\\text{eff}} = \\mathbf{0}$)—are the **Five Lagrange Points ($L_1, L_2, L_3, L_4, L_5$)**, discovered mathematically by Leonhard Euler (1765, collinear points) and Joseph-Louis Lagrange (1772, triangular points).

3. **The Five Lagrange Points:**
   - **$L_1$ (Inner Collinear Point):** Sits directly between $M_1$ and $M_2$ along the line connecting their centers. Here, the gravitational pull of $M_2$ partially offsets the pull of $M_1$, allowing a satellite to orbit with the same orbital period as $M_2$ despite being closer to $M_1$. Ideal for uninterrupted solar observation: hosts solar observatories such as **SOHO**, **ACE**, and **DSCOVR**.
   - **$L_2$ (Outer Collinear Point):** Located beyond $M_2$ on the side facing away from $M_1$. The combined gravitational pull of $M_1$ and $M_2$ augments the centripetal acceleration, allowing an orbit further out to match the orbital period of $M_2$. In the Earth-Sun system, $L_2$ lies $1.5\\times 10^6\\text{ km}$ behind Earth, providing an unobstructed, thermally quiet deep-space viewpoint permanently shadowed from solar and terrestrial glare: home to the **James Webb Space Telescope (JWST)**, **Gaia**, **Euclid**, **Planck**, and the future **Nancy Grace Roman Space Telescope**.
   - **$L_3$ (Counter-Primary Point):** Located on the far side of $M_1$, slightly outside the orbit of $M_2$.
   - **$L_4$ & $L_5$ (Triangular Equilateral Points):** Form vertices of two equilateral triangles with $M_1$ and $M_2$ in the orbital plane ($60^\\circ$ ahead of and $60^\\circ$ behind $M_2$). Remarkably, the distances from $L_4$ (and $L_5$) to both primaries are exactly equal to the primary separation $R$.

4. **Stability & Routh's Criterion:**
   - **Collinear points ($L_1, L_2, L_3$)** are saddle points (unstable in the orbital plane). A spacecraft placed precisely at $L_1$ or $L_2$ will drift away exponentially on a timescale of weeks unless stabilized by small propulsive station-keeping maneuvers. In practice, space telescopes do not sit frozen at the point; they fly in periodic three-dimensional **halo orbits** or **Lissajous orbits** around the point!
   - **Triangular points ($L_4, L_5$)** are potential hilltops (local maxima of $\\Phi_{\\text{eff}}$), yet they are **linearly dynamically stable** provided the mass ratio $\\mu = M_2 / (M_1 + M_2)$ satisfies **Routh's stability criterion**:
     $$
     \\mu < \\mu_{\\text{crit}} = \\frac{1}{2} \\left(1 - \\sqrt{\\frac{23}{27}}\\right) \\approx 0.0385208\\dots
     $$
     The Coriolis force deflects any departing particle into closed, stable kidney-bean-shaped **tadpole orbits** and **horseshoe orbits**. In the Sun-Jupiter system ($\\mu \\approx 0.00095$), tens of thousands of **Trojan asteroids** congregate stably at $L_4$ and $L_5$.

### Laboratory / Kitchen Protocol (Try It At Home)
> **Observation of Deep-Space Telescopes:** Look up live tracking of the James Webb Space Telescope at NASA's "Where is Webb?" portal. Notice how JWST is not static, but constantly traces an oval halo orbit around Earth-Sun $L_2$, looping around the invisible equilibrium point once every 6 months using only a tiny whisper of propellant ($2-3\\text{ m/s per year}$) for stationkeeping!

---

## 2. Mathematical Foundation & The Three Pillars of Astrodynamics

### Pillar 1: Governing Law & Effective Potential ($\\Phi_{\\text{eff}}$)

#### Normalized Synodic Coordinate System
We adopt standard celestial mechanics canonical units:
- Total mass: $M_1 + M_2 = 1$
- Distance between primaries: $R = 1$
- Gravitational constant: $G = 1$
- Mean motion / orbital frequency: $\\Omega = 1$
- Primary $M_1 = 1 - \\mu$ located at $(-\\mu, 0, 0)$
- Secondary $M_2 = \\mu$ located at $(1 - \\mu, 0, 0)$
- Barycenter at origin $(0, 0, 0)$

The distances from a test mass at $(x, y, z)$ to $M_1$ and $M_2$ are:

$$
r_1 = \\sqrt{(x + \\mu)^2 + y^2 + z^2}, \\quad r_2 = \\sqrt{(x - (1 - \\mu))^2 + y^2 + z^2}
$$

#### Effective Potential Formulation
The effective potential combines gravitational attraction with centrifugal potential:

$$
\\Phi_{\\text{eff}}(x, y, z) = -\\frac{1 - \\mu}{r_1} - \\frac{\\mu}{r_2} - \\frac{1}{2}(x^2 + y^2)
$$

The equilibrium condition defining the Lagrange points is the vanishing of the potential gradient:

$$
\\nabla \\Phi_{\\text{eff}}(\\mathbf{r}) = \\mathbf{0} \\implies \\frac{\\partial \\Phi_{\\text{eff}}}{\\partial x} = 0, \\quad \\frac{\\partial \\Phi_{\\text{eff}}}{\\partial y} = 0, \\quad \\frac{\\partial \\Phi_{\\text{eff}}}{\\partial z} = 0
$$

---

### Pillar 2: Kinematics & Coriolis Equations of Motion

#### Equations of Motion in the Rotating Synodic Frame
Applying Newton's second law in the co-rotating frame with Coriolis acceleration:

$$
\\ddot{x} - 2 \\dot{y} = -\\frac{\\partial \\Phi_{\\text{eff}}}{\\partial x} = x - (1 - \\mu)\\frac{x + \\mu}{r_1^3} - \\mu \\frac{x - (1 - \\mu)}{r_2^3}
$$

$$
\\ddot{y} + 2 \\dot{x} = -\\frac{\\partial \\Phi_{\\text{eff}}}{\\partial y} = y - (1 - \\mu)\\frac{y}{r_1^3} - \\mu \\frac{y}{r_2^3}
$$

$$
\\ddot{z} = -\\frac{\\partial \\Phi_{\\text{eff}}}{\\partial z} = -(1 - \\mu)\\frac{z}{r_1^3} - \\mu \\frac{z}{r_2^3}
$$

Notice the Coriolis cross-coupling terms $+2\\dot{y}$ and $-2\\dot{x}$.

#### Collinear Points ($L_1, L_2, L_3$) Position Solutions
For $y = z = 0$, the $x$-coordinates satisfy Euler's quintic polynomial. Using the Hill radius approximation for small $\\mu$:

$$
r_H = \\left(\\frac{\\mu}{3}\\right)^{1/3}
$$

$$
x_{L1} \\approx 1 - \\mu - r_H \\left(1 - \\frac{1}{3} r_H - \\frac{1}{9} r_H^2\\right)
$$

$$
x_{L2} \\approx 1 - \\mu + r_H \\left(1 + \\frac{1}{3} r_H - \\frac{1}{9} r_H^2\\right)
$$

$$
x_{L3} \\approx -1 - \\frac{5}{12}\\mu
$$

#### Triangular Points ($L_4, L_5$) Exact Solutions
Setting $r_1 = r_2 = 1$:

$$
x_{L4, L5} = \\frac{1}{2} - \\mu, \\quad y_{L4, L5} = \\pm \\frac{\\sqrt{3}}{2}, \\quad z = 0
$$

---

### Pillar 3: Energy & Jacobi Integral Conservation

#### The Jacobi Energy Integral ($C_J$)
Multiplying the equations of motion by $\\dot{x}, \\dot{y}, \\dot{z}$ and integrating with respect to time yields the only known exact scalar invariant of the CR3BP, the **Jacobi Integral**:

$$
C_J = 2 U(x, y, z) - v^2 = -2 \\Phi_{\\text{eff}}(x, y, z) - (\\dot{x}^2 + \\dot{y}^2 + \\dot{z}^2) = \\text{const}
$$

Where $v = \\sqrt{\\dot{x}^2 + \\dot{y}^2 + \\dot{z}^2}$ is the spacecraft's speed relative to the rotating frame.

#### Zero-Velocity Surfaces & Roche Lobes
Setting speed $v = 0$ defines the **Zero-Velocity Surfaces (ZVS)**:

$$
2 U(x, y, z) = C_J
$$

Because $v^2 \\ge 0$, a spacecraft with Jacobi constant $C_J$ is physically forbidden from entering regions where $2 U(x, y, z) < C_J$. As $C_J$ decreases (higher orbital energy):
1. **$C_J > C_{L1}$:** The spacecraft is permanently trapped either around $M_1$ or around $M_2$; no passage is possible.
2. **$C_{L2} < C_J < C_{L1}$:** The neck at $L_1$ opens, allowing transit between primary and secondary (low-energy ballistic capture).
3. **$C_{L3} < C_J < C_{L2}$:** The gateway at $L_2$ opens into interplanetary space.
4. **$C_{L4,5} < C_J < C_{L3}$:** The gateway at $L_3$ opens.
5. **$C_J < C_{L4,5}$:** All barriers dissolve; the spacecraft can roam freely across the entire orbital plane.

---

## 3. Physical Parameters & SI Units Reference

| Symbol | Parameter Name | Default Simulation Value | Physical Range | Canonical / SI Unit |
| :--- | :--- | :--- | :--- | :--- |
| $\\mu$ | Mass Ratio $M_2 / (M_1 + M_2)$ | $0.040$ | $0.001 - 0.480$ | Dimensionless |
| $\\mu_{\\text{crit}}$ | Routh Critical Stability Ratio | $0.03852$ | Fixed Constant | Dimensionless |
| $R$ | Separation between Primaries | $1.00$ | Scaled to Canvas | AU / km |
| $\\Omega$ | Synodic Frame Angular Speed | $1.00$ | Normalised ($2\\pi / T$) | $\\text{rad/s}$ |
| $C_J$ | Jacobi Energy Constant | $\\approx 3.0$ | $2.5 - 4.5$ | Canonical Energy |
| $r_H$ | Secondary Hill Sphere Radius | $(\\mu/3)^{1/3}$ | $0.05 - 0.40$ | AU / Dimensionless |
| $(x_{L1}, y_{L1})$ | Inner Collinear Point $L_1$ | $(\\approx 0.75, 0.0)$ | Between primaries | Synodic Coordinates |
| $(x_{L2}, y_{L2})$ | Outer Collinear Point $L_2$ | $(\\approx 1.18, 0.0)$ | Beyond secondary | Synodic Coordinates |
| $(x_{L4,5}, y_{L4,5})$ | Equilateral Triangular Points | $(0.5 - \\mu, \\pm 0.866)$ | Triangular vertices | Synodic Coordinates |

---

## 4. Simulation Architecture & Kotlin Compose Implementation

### Source Location
- **Primary Composable:** [`LagrangePointsExperiment.kt`](./LagrangePointsExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week3.Day20`
- **Architecture:** Jetpack / Compose Multiplatform Canvas with high-precision Runge-Kutta 4 (RK4) sub-stepped integrator.

### Key Reactive States
- `muParam`: Mass ratio $\\mu$ with real-time Routh stability detection.
- `probePos`, `probeVel`: Position and velocity vectors integrated via 8-stage RK4.
- `trail`: Circular dynamic trail buffer recording trajectory in synodic frame.
- `showInertialFrame`: Toggle transforming coordinates from co-rotating synodic frame to sidereal inertial frame.
- `showEquipotential`: Displays glowing concentric Zero-Velocity Surface contours and Hill sphere radius.

### UI & Layout Compliance
- **Transparent HUD:** `ExperimentHudCard` with transparent background displaying real-time formula, $\\mu$, nearest Lagrange point, Jacobi constant $C_J$, and frame mode.
- **Elevated Canvas Origin:** Barycenter centered at $(w \\times 0.50f, h \\times 0.40f)$, leaving the bottom $35\\%$ completely clear for the control deck.
- **Quick-Jump Chips:** Buttons to instantly launch the spacecraft probe into halo orbits at $L_1, L_2, L_3$ or tadpole orbits at $L_4, L_5$.
