# Day 19: Roche Limit & Tidal Ring Creation

> **Week 3: Celestial Gravity & Orbits • Planetary Orbits & Spaceflight Dynamics**  
> *Topic Subtitle: Gravitational Tidal Disruption of Moons*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Send a moon orbiting too close to a gas giant and watch planetary tidal forces rip it into glowing rings like Saturn.

### Scientific Principles & Mechanism
The **Roche limit** (named after the French astronomer Édouard Roche, who first calculated it in 1848) marks the minimum orbital distance a celestial body, held together only by its own self-gravity, can approach a second, more massive body before being torn apart by tidal forces.

1. **The Origin of Gravitational Tidal Forces:**
   - Gravity obeys Newton's inverse-square law: gravitational acceleration decreases with the square of the distance ($g \propto 1/r^2$).
   - An orbiting moon is not an infinitesimal point mass; it is an extended celestial body with finite radius $r_m$.
   - The near side of the moon (facing the primary planet) experiences a stronger gravitational attraction than the moon's center of mass.
   - Conversely, the far side of the moon experiences a weaker gravitational pull than the moon's center of mass.
   - In the accelerating reference frame of the moon's center of mass, this differential gravitational pull manifests as a **tensile stretching stress** directed along the radial axis, paired with a compressive stress along the perpendicular axes.

2. **Self-Gravity vs. Tidal Tension:**
   - Every parcel of mass on the moon's surface is pulled toward the moon's center by internal self-gravity.
   - Far from the planet, internal self-gravity easily dominates: the moon retains its spherical shape, experiencing only a minuscule hydrostatic tidal bulge.
   - As the orbital radius $d$ decreases, tidal tension scales inversely with the cube of the distance ($F_{\\text{tidal}} \\propto 1/d^3$), rapidly overtaking internal self-gravity ($F_{\\text{self}} \\propto 1/r_m^2$).
   - At the Roche limit, tidal tensile stress exactly balances self-gravitational cohesion. Any further inward migration triggers catastrophic structural rupture.

3. **Fluid Rubble Piles vs. Rigid Rock:**
   - **Fluid / Rubble Pile Moons ($d_{\\text{Roche}} \\approx 2.44 R (\\rho_M / \\rho_m)^{1/3}$):** Most natural satellites, comets, and asteroids are loose agglomerations of ice, dust, and fractured regolith held together strictly by gravity with near-zero tensile strength. As they near the planet, tidal forces deform them into elongated prolate ellipsoids ($c/a \\approx 1.95$). This elongation shifts the surface mass further from the moon's center, weakening self-gravity and accelerating rupture. Consequently, fluid bodies disrupt at a much greater distance ($2.44 R$).
   - **Rigid Monolithic Rocks ($d_{\\text{Roche, rigid}} \\approx 1.26 R (\\rho_M / \\rho_m)^{1/3}$):** Solid monolithic rocky boulders with high tensile yield strength resist elongation, maintaining a spherical shape until tidal stress exceeds internal mechanical bonding. Their disruption boundary is substantially closer ($1.26 R$).

4. **Keplerian Differential Shear & Ring Disk Formation:**
   - Once the moon ruptures, self-gravity can no longer hold the fragmented debris together.
   - Each individual debris particle enters its own independent Keplerian orbit around the gas giant.
   - By Kepler's Third Law, orbital angular velocity scales as $\\Omega(r) = \\sqrt{G M / r^3}$. Debris on the inner edge orbits faster than debris on the outer edge.
   - This **differential orbital shear** shears the disrupted moon into an elongated arc (a "string of pearls"), which wraps around the entire planetary circumference within several orbits, creating a majestic, ultra-thin planetary ring disk like the rings of Saturn.

5. **Astrophysical Examples in the Solar System:**
   - **Saturn's Rings:** Composed of 99% pure water ice fragments ranging from micrometers to meters, Saturn's main rings lie entirely inside Saturn's fluid Roche limit ($d_{\\text{Roche}} \\approx 147{,}000\\text{ km}$, rings span $70{,}000$ to $140{,}000\\text{ km}$).
   - **Comet Shoemaker-Levy 9 (1992–1994):** Passed within Jupiter's Roche limit in July 1992, disrupting into a 21-fragment train of cometary nuclei that slammed spectacularly into Jupiter in July 1994.
   - **Mars and Phobos:** Mars' moon Phobos orbits at $d \\approx 2.76 R_{\\text{Mars}}$ and spirals inward by $1.8\\text{ cm/year}$ via tidal friction. Within $30$ to $50$ million years, Phobos will cross Mars' Roche limit and pulverize into a bright Martian ring system.

### Laboratory / Kitchen Protocol (Try It At Home)
> **Observation of Planetary Ring Systems:** Look up high-resolution imagery from NASA's *Cassini-Huygens* mission. Notice how the bright main rings (A, B, and C rings) abruptly terminate at the Roche limit boundary, where small "shepherd moons" like Prometheus and Pandora maintain sharp, crisp ring edges through orbital resonances!

---

## 2. Mathematical Foundation & The Three Pillars of Astrodynamics

### Pillar 1: Governing Law & Tidal Force Derivation

#### Newtonian Gravitational Field Gradient
The gravitational field of a spherical primary planet of mass $M$ and radius $R$ at radial distance $r$ is:

$$
\\mathbf{g}(r) = -\\frac{G M}{r^2} \\hat{\\mathbf{r}}
$$

For an orbiting moon of radius $r_m$ and mass $m$ centered at distance $d$, we Taylor expand the gravitational field at the sub-planetary surface ($d - r_m$) and anti-planetary surface ($d + r_m$):

$$
g(d \\pm r_m) = g(d) \\mp \\left. \\frac{dg}{dr} \\right|_{d} r_m + \\mathcal{O}(r_m^2) = -\\frac{G M}{d^2} \\pm \\frac{2 G M r_m}{d^3}
$$

The differential tidal acceleration pulling outward relative to the moon's center of mass is:

$$
a_{\\text{tidal}} = \\frac{2 G M r_m}{d^3}
$$

#### Self-Gravitational Binding Acceleration
The gravitational acceleration at the surface of the spherical moon holding a test mass $m_0$ is:

$$
a_{\\text{self}} = \\frac{G m}{r_m^2} = \\frac{G \\left(\\frac{4}{3} \\pi r_m^3 \\rho_m\\right)}{r_m^2} = \\frac{4}{3} \\pi G \\rho_m r_m
$$

#### The Rigid Roche Limit
Equating the outward tidal acceleration $a_{\\text{tidal}}$ to the inward self-gravitational acceleration $a_{\\text{self}}$:

$$
\\frac{2 G M r_m}{d^3} = \\frac{4}{3} \\pi G \\rho_m r_m \\implies d^3 = \\frac{2 M}{\\frac{4}{3} \\pi \\rho_m} = 2 R^3 \\left(\\frac{\\rho_M}{\\rho_m}\\right)
$$

Taking the cube root yields the classical **rigid Roche limit**:

$$
d_{\\text{Roche, rigid}} = 2^{1/3} R \\left(\\frac{\\rho_M}{\\rho_m}\\right)^{1/3} \\approx 1.2599 R \\left(\\frac{\\rho_M}{\\rho_m}\\right)^{1/3}
$$

#### The Fluid / Rubble Pile Roche Limit
For a fluid or non-cohesive rubble-pile satellite, tidal deformation stretches the body into a prolate ellipsoid with axes $(a, b, c)$ where $a > b \\approx c$. Roche solved the hydrostatic equilibrium of this self-gravitating ellipsoid in the combined tidal and centrifugal potential, yielding an elongation of $a/c \\approx 1.95$ at the brink of instability. This geometric elongation pushes the tip particles further away from the moon's center of mass, shifting the disruption limit out to:

$$
d_{\\text{Roche, fluid}} \\approx 2.423 R \\left(\\frac{\\rho_M}{\\rho_m}\\right)^{1/3} \\approx 2.44 R \\left(\\frac{\\rho_M}{\\rho_m}\\right)^{1/3}
$$

---

### Pillar 2: Kinematics & Differential Keplerian Shear

#### Orbital Angular Velocity Field
Every particle released from the disrupted moon orbits according to Kepler's Third Law:

$$
\\Omega(r) = \\sqrt{\\frac{G M}{r^3}}
$$

#### Keplerian Shear Gradient
The radial derivative of angular velocity represents the differential orbital shear:

$$
\\frac{d\\Omega}{dr} = -\\frac{3}{2} \\sqrt{\\frac{G M}{r^5}} = -\\frac{3}{2} \\frac{\\Omega(r)}{r}
$$

For a particle released at radial offset $\\Delta r = r - d$ from the moon's center:

$$
\\Delta \\Omega \\approx -\\frac{3}{2} \\frac{\\Omega_0}{d} \\Delta r
$$

#### Tidal Stream Elongation & Ring Wrapping Time
The azimuthal angle $\\theta_i(t)$ of particle $i$ evolves over time $t$:

$$
\\theta_i(t) = \\theta_0 + \\Omega(r_i) \\cdot t = \\theta_0 + (\\Omega_0 + \\Delta \\Omega_i) \\cdot t
$$

The angular spread across the full width of the debris cloud ($2 r_m$) grows linearly:

$$
\\Delta \\theta(t) = |\\Delta \\Omega_{\\text{max}}| \\cdot t = 3 \\frac{\\Omega_0 r_m}{d} \\cdot t
$$

The time required for the leading and trailing streams to wrap completely around the planet ($360^\\circ = 2\\pi\\text{ rad}$) and form a continuous closed ring disk is:

$$
T_{\\text{ring}} = \\frac{2\\pi}{|\\Delta \\Omega_{\\text{max}}|} = \\frac{2\\pi}{3 \\frac{\\Omega_0 r_m}{d}} = \\frac{1}{3} \\frac{d}{r_m} T_{\\text{orb}}
$$

Where $T_{\\text{orb}} = 2\\pi / \\Omega_0$ is the orbital period of the original moon.

---

### Pillar 3: Energy & Jacobi Integral Conservation

#### Effective Potential in the Rotating Frame
In the synodic frame co-rotating with the moon at angular speed $\\Omega_0$, the equation of motion for each particle is governed by the effective potential $\\Phi_{\\text{eff}}$:

$$
\\Phi_{\\text{eff}}(\\mathbf{r}) = -\\frac{G M}{|\\mathbf{r}|} - \\frac{1}{2} \\Omega_0^2 |\\mathbf{r} \\times \\hat{\\mathbf{z}}|^2 + \\Phi_{\\text{self}}(\\mathbf{r})
$$

#### Conservation of the Jacobi Constant
The Jacobi energy integral $C_J$ is strictly conserved for each particle in the circular restricted three-body problem:

$$
C_J = 2 \\Phi_{\\text{eff}}(\\mathbf{r}) - v_{\\text{rel}}^2 = \\text{const}
$$

Where $v_{\\text{rel}}$ is the speed relative to the rotating frame. As the moon crosses the Roche limit, the equipotential surfaces open at the inner Lagrange point $L_1$ and outer Lagrange point $L_2$. Material escapes through these saddle points, conserving total angular momentum and Jacobi energy while transforming gravitational potential into an ultra-thin, stable ring disk.

---

## 3. Physical Parameters & SI Units Reference

| Symbol | Parameter Name | Default Simulation Value | Physical Range | SI Unit |
| :--- | :--- | :--- | :--- | :--- |
| $R$ | Planetary Radius | Normalized ($1.0 R_p$) | $10^4 - 10^5$ | $\\text{km}$ |
| $M$ | Planetary Mass | Normalized ($1.0 M_p$) | $10^{24} - 10^{27}$ | $\\text{kg}$ |
| $d / R$ | Orbital Distance Ratio | $2.05 R$ | $1.10 - 3.85$ | Dimensionless |
| $\\rho_M / \\rho_m$ | Primary-to-Moon Density Ratio | $1.00$ | $0.50 - 2.50$ | Dimensionless |
| $C_{\\text{rigidity}}$ | Roche Coefficient | $2.44$ (Fluid) / $1.26$ (Rigid) | $1.26 - 2.44$ | Dimensionless |
| $d_{\\text{Roche}}$ | Roche Disruption Limit | $2.44 R$ | $1.00 - 3.50$ | Dimensionless ($R_p$) |
| $F_{\\text{tidal}} / F_{\\text{self}}$ | Tidal Stress Ratio | Computed $(d_{\\text{Roche}}/d)^3$ | $0.1 - 10.0$ | Dimensionless |
| $N$ | Ring Debris Particle Count | $96$ | $50 - 200$ | Particles |
| $\\Omega(r)$ | Keplerian Angular Velocity | $\\propto r^{-1.5}$ | $0.2 - 2.5$ | $\\text{rad/s}$ |

---

## 4. Simulation Architecture & Kotlin Compose Implementation

### Source Location
- **Primary Composable:** [`RocheLimitExperiment.kt`](./RocheLimitExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week3.Day19`
- **Architecture:** Jetpack / Compose Multiplatform Canvas with high-frequency physical integration tick (`withFrameNanos`).

### Key Reactive States
- `distanceRatioParam`: Normalized orbital distance $d / R$ updated via touch drag or slider.
- `densityRatioParam`: Mass density ratio $\\rho_M / \\rho_m$ controlling the Roche limit radius.
- `rigidityMode`: State toggle between `FLUID_RUBBLE` ($2.44$) and `RIGID_SOLID` ($1.26$).
- `moonOrbitAngle`: Primary orbital phase $\\theta(t)$ advancing at Keplerian frequency.
- `disruptionProgress`: Smooth dynamic transition from $0\\%$ (bound prolate spheroid) to $100\\%$ (sheared annular ring).
- `particles`: List of 96 `RingDebrisParticle` instances with individual Keplerian shear rates.

### Visual & Layout Polish
- **Transparent HUD:** `ExperimentHudCard` with transparent background displaying real-time formula, orbital distance $d$, Roche limit $d_R$, tidal stress ratio, and system phase.
- **Elevated Canvas Origin:** Planet centered at $cy = h \\times 0.39f$, leaving the bottom $35\\%$ completely unobstructed for the compact control deck.
- **Atmospheric & Corona Rendering:** Radial gradient lighting, atmospheric bands, and pulsating corona.
- **Compact Controls Deck:** Paired side-by-side sliders, preset chips, and $34\\text{ dp}$ action buttons.
