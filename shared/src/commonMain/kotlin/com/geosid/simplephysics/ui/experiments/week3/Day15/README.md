# Day 15: Kepler Planetary Orbits

> **Week 3: Celestial Gravity & Orbits • Planetary Orbits & Spaceflight Dynamics**  
> *Topic Subtitle: Elliptical Orbits, Vis-Viva Velocity & Equal Areas Law*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Steer a planet around a gravitational star. Adjust orbital eccentricity in real time, observe the tangent velocity vector surge at perihelion, and watch the equal-area wedge dynamically deform to prove Kepler's Second Law!

### Scientific Principles & Mechanism
Under Newton’s law of universal gravitation, an inverse-square central force governs planetary motion:

1. **Law of Ellipses (Kepler's 1st Law):** Planets do not orbit in simple concentric circles. Every planetary orbit is an ellipse, with the central star situated at one of the two geometric foci ($F_1$). The other focus ($F_2$) remains an empty point in space.
2. **Conservation of Angular Momentum & Equal Areas (Kepler's 2nd Law):** Because gravity acts along the line connecting the planet and the star, the gravitational torque is identically zero ($\boldsymbol{\tau} = \mathbf{r} \times \mathbf{F}_g = \mathbf{0}$). Consequently, the orbital specific angular momentum $\mathbf{h} = \mathbf{r} \times \mathbf{v}$ is strictly conserved throughout the orbit. As the radius vector $\mathbf{r}$ sweeps across space, it covers equal sector areas in equal intervals of time:
   $$
   \frac{dA}{dt} = \frac{1}{2} |\mathbf{r} \times \mathbf{v}| = \frac{h}{2} = \text{constant}
   $$
   When the planet approaches perihelion ($r \to r_p$), the radius vector shrinks, demanding a proportional surge in orbital velocity ($v \to v_p$) to keep the swept areal rate constant. At aphelion ($r \to r_a$), the planet crawls along its orbital apse at minimum speed ($v \to v_a$).
3. **Vis-Viva Mechanical Energy Conservation:** Kinetic energy and gravitational potential energy exchange continuously:
   $$
   E_{\text{total}} = \frac{1}{2} v^2 - \frac{G M}{r} = -\frac{G M}{2a} = \text{constant} < 0
   $$
   As the planet falls deeper into the star's gravitational potential well ($r$ decreases), potential energy converts into kinetic energy, accelerating the planet along its orbital track.

### Laboratory / Kitchen Protocol (Try It At Home)
> Tie a small rubber ball or metal nut to a piece of string. Thread the string through a smooth pen tube held in your hand. Swing the ball in a horizontal circle, then gently pull the hanging bottom of the string downward to shorten the orbiting radius. Notice how the ball immediately whips around drastically faster! This intuitive tabletop demonstration proves angular momentum conservation ($L = m v r = \text{const}$).

---

## 2. Mathematical Foundation & The Three Pillars of Orbital Mechanics

### Pillar 1: Governing Law & Central Gravitational Force

Planetary motion is governed by Newton's Universal Law of Gravitation, which describes the mutual attraction between a central star of mass $M$ and an orbiting planet of mass $m$:

$$
\mathbf{F}_g = -\frac{G M m}{r^2} \mathbf{\hat{r}} = -\frac{G M m}{r^3} \mathbf{r}
$$

Applying Newton's Second Law ($\mathbf{F}_g = m \mathbf{\ddot{r}}$) yields the central field equation of acceleration:

$$
\mathbf{\ddot{r}} = -\frac{G M}{r^3} \mathbf{r} = -\frac{\mu}{r^3} \mathbf{r}
$$

where $\mu \equiv G M$ is the standard gravitational parameter of the primary attractor. 

Because the gravitational force acts along the radial line vector $\mathbf{r}$, the net torque about the primary focus is identically zero:

$$
\boldsymbol{\tau}_{\text{net}} = \mathbf{r} \times \mathbf{F}_g = \mathbf{r} \times \left( -\frac{G M m}{r^3} \mathbf{r} \right) = \mathbf{0}
$$

From the rotational equation of motion $\boldsymbol{\tau} = \frac{d\mathbf{L}}{dt}$, zero external torque guarantees that orbital angular momentum $\mathbf{L} = m (\mathbf{r} \times \mathbf{v})$ and specific angular momentum $\mathbf{h} \equiv \mathbf{L}/m = \mathbf{r} \times \mathbf{v}$ are exact, unbroken invariants of motion.

---

### Pillar 2: Kinematics & Motion Constraints (Kepler I & Eccentric Anomaly)

#### Kepler's First Law (The Ellipse Equation)
In polar coordinates $(r, \theta)$ with origin at the primary focus $F_1$ (the central star), solving the central force differential equation yields a conic section:

$$
r(\theta) = \frac{a(1 - e^2)}{1 + e \cos \theta} = \frac{p}{1 + e \cos \theta}
$$

Where:
- $a$ is the semi-major axis (half the longest diameter of the ellipse).
- $b = a\sqrt{1 - e^2}$ is the semi-minor axis.
- $e = \frac{c}{a} \in [0, 1)$ is the orbital eccentricity ($e = 0$ is a circle, $0 < e < 1$ is an ellipse).
- $c = a \cdot e$ is the linear focal distance from the ellipse geometric center to the star focus $F_1$.
- $p = a(1 - e^2)$ is the semi-latus rectum.
- Perihelion distance (closest approach, $\theta = 0$): $r_p = a(1 - e)$.
- Aphelion distance (farthest approach, $\theta = \pi$): $r_a = a(1 + e)$.

#### Eccentric Anomaly ($E$) Kinematic Formulation
Direct numerical integration of $\mathbf{\ddot{r}} = -GM \mathbf{r}/r^3$ using simple Euler stepping produces artificial numerical energy drift over multiple orbital revolutions. To guarantee an unconditionally stable, closed elliptical orbit, the simulation parameterizes position via the **Eccentric Anomaly** $E \in [0, 2\pi)$:

$$
x(E) = x_{\text{center}} - a \cos E, \quad y(E) = y_{\text{center}} + b \sin E
$$

- At $E = 0$: $x = x_{\text{center}} - a$ (nearest to Sun at $x_{\text{center}} - c$, Perihelion).
- At $E = \pi$: $x = x_{\text{center}} + a$ (farthest from Sun, Aphelion).

The normalized distance from the focus to the planet as a function of $E$ simplifies to:

$$
r(E) = a (1 - e \cos E)
$$

Differentiating **Kepler's Equation** ($M = E - e \sin E = \omega t$, where $M$ is Mean Anomaly):

$$
dM = (1 - e \cos E) \, dE = \omega \, dt \implies \frac{dE}{dt} = \frac{\omega}{1 - e \cos E}
$$

Where $\omega = 1.25 \cdot s_{\text{multiplier}}\,\text{rad/s}$. This kinematic differential equation guarantees that the planet moves strictly along the ellipse boundary while accelerating smoothly at perihelion and decelerating at aphelion.

#### Tangent Velocity and Gravitational Force Unit Vectors
- **Tangent Velocity Unit Vector ($\mathbf{\hat{u}}_v$):**
  $$
  \frac{dx}{dE} = a \sin E, \quad \frac{dy}{dE} = b \cos E \implies \mathbf{\hat{u}}_v = \frac{(a \sin E, \, b \cos E)}{\sqrt{a^2 \sin^2 E + b^2 \cos^2 E}}
  $$
  Vector length scales proportionally with $v_{\text{norm}} = \sqrt{2/r_{\text{norm}} - 1}$.
- **Gravitational Pull Unit Vector ($\mathbf{\hat{u}}_F$):**
  $$
  \mathbf{\hat{u}}_{F} = \frac{\mathbf{r}_{\text{sun}} - \mathbf{r}_{\text{planet}}}{|\mathbf{r}_{\text{sun}} - \mathbf{r}_{\text{planet}}|}
  $$
  Vector length scales with the inverse-square law: $|\mathbf{F}_g| \propto \frac{1}{r_{\text{norm}}^2}$.

---

### Pillar 3: Energy, Work & Angular Momentum Conservation (Kepler II, III & Vis-Viva)

#### Kepler's Second Law (Equal Areas in Equal Times)
The infinitesimal triangular area $dA$ swept out by the radius vector $\mathbf{r}$ during time $dt$ is:

$$
dA = \frac{1}{2} |\mathbf{r} \times d\mathbf{r}| = \frac{1}{2} r^2 \dot{\theta} \, dt
$$

Differentiating with respect to time yields the constant areal velocity:

$$
\frac{dA}{dt} = \frac{1}{2} r^2 \dot{\theta} = \frac{h}{2} = \frac{\pi a b}{T} = \text{constant}
$$

At the apsides (perihelion and aphelion), velocity is strictly perpendicular to the radius vector ($\mathbf{r} \perp \mathbf{v}$), proving the inverse relationship between distance and speed:

$$
v_p \, r_p = v_a \, r_a = h \implies \frac{v_p}{v_a} = \frac{r_a}{r_p} = \frac{1 + e}{1 - e}
$$

#### Kepler's Third Law (Harmonic Period Law)
Integrating areal velocity over one complete orbital period $T$ gives total elliptical area $A = \pi a b$:

$$
\pi a b = \int_0^T \frac{dA}{dt} dt = \frac{h}{2} T = \frac{\sqrt{G M a(1 - e^2)}}{2} T
$$

Substituting $b = a\sqrt{1 - e^2}$ leads directly to Kepler's Third Law:

$$
T = 2\pi \sqrt{\frac{a^3}{G M}} \iff \frac{T^2}{a^3} = \frac{4\pi^2}{G M}
$$

#### The Vis-Viva Equation (Mechanical Energy Conservation)
The specific orbital energy $\mathcal{E}$ is the sum of kinetic and gravitational potential energy per unit mass:

$$
\mathcal{E} = \frac{1}{2} v^2 - \frac{G M}{r} = -\frac{G M}{2a} = \text{constant} < 0
$$

Solving for velocity gives the fundamental **Vis-Viva Equation**:

$$
v(r) = \sqrt{G M \left( \frac{2}{r} - \frac{1}{a} \right)}
$$

In the normalized units of the simulation ($a = 1.0\,\text{AU}$, $GM = 1.0$):

$$
r_{\text{norm}}(E) = 1 - e \cos E, \quad v_{\text{norm}}(E) = \sqrt{\frac{2}{r_{\text{norm}}} - 1}
$$

- At Perihelion ($r_p = 1 - e$): $v_p = \sqrt{\frac{1 + e}{1 - e}}\,v_0$ (maximum kinetic energy).
- At Aphelion ($r_a = 1 + e$): $v_a = \sqrt{\frac{1 - e}{1 + e}}\,v_0$ (minimum kinetic energy).
- Work done by gravity over any complete closed orbit: $\oint \mathbf{F}_g \cdot d\mathbf{r} = 0$ (conservative central force).

---

### Physical Parameters & SI Units Table

| Parameter | Symbol | Simulation Value | Real Astronomical Equivalent | SI Unit | Physical Description |
| :--- | :---: | :---: | :---: | :---: | :--- |
| Central Star Mass | $M_\star$ | $1.0$ | $1.989 \times 10^{30}\,\text{kg}$ ($1.0\,M_\odot$) | $\text{kg}$ | Mass of primary gravitational attractor (Sun) |
| Planetary Mass | $m_p$ | $\ll M_\star$ | $5.972 \times 10^{24}\,\text{kg}$ ($1.0\,M_\oplus$) | $\text{kg}$ | Test mass orbiting in gravitational potential |
| Universal Gravitational Constant | $G$ | $1.0$ (normalized) | $6.6743 \times 10^{-11}$ | $\text{m}^3/(\text{kg}\cdot\text{s}^2)$ | Newton's universal gravitational constant |
| Standard Gravitational Parameter | $\mu = GM$ | $1.0$ | $1.327 \times 10^{20}$ | $\text{m}^3/\text{s}^2$ | Attractor gravitational strength factor |
| Semi-Major Axis | $a$ | $1.0\,\text{AU}$ ($120\text{--}210\,\text{px}$) | $1.496 \times 10^{11}\,\text{m}$ ($1.0\,\text{AU}$) | $\text{m}$ | Half length of the major orbital ellipse diameter |
| Orbital Eccentricity | $e$ | $0.00\text{--}0.88$ (default $0.48$) | $0.0167$ (Earth), $0.2488$ (Pluto) | Dimensionless | Ratio of focal distance to semi-major axis ($c/a$) |
| Semi-Minor Axis | $b$ | $a\sqrt{1 - e^2}$ | $1.495 \times 10^{11}\,\text{m}$ (Earth) | $\text{m}$ | Half length of the minor orbital ellipse diameter |
| Linear Focal Distance | $c$ | $a \cdot e$ | $2.50 \times 10^{9}\,\text{m}$ (Earth) | $\text{m}$ | Offset of Sun focus $F_1$ from ellipse center |
| Perihelion Distance | $r_p$ | $a(1 - e)$ | $1.471 \times 10^{11}\,\text{m}$ (Earth) | $\text{m}$ | Distance at point of closest solar approach |
| Aphelion Distance | $r_a$ | $a(1 + e)$ | $1.521 \times 10^{11}\,\text{m}$ (Earth) | $\text{m}$ | Distance at point of farthest solar separation |
| Base Angular Velocity | $\omega$ | $1.25\,\text{rad/s}$ | $1.991 \times 10^{-7}\,\text{rad/s}$ | $\text{rad/s}$ | Mean motion rate ($\omega = 2\pi / T$) |
| Orbital Period | $T$ | $5.03\,\text{s}$ (at $1.0\times$) | $3.156 \times 10^7\,\text{s}$ ($1.0\,\text{yr}$) | $\text{s}$ | Duration required for one full $360^\circ$ revolution |
| Specific Angular Momentum | $h = L/m$ | $\sqrt{\mu a (1 - e^2)}$ | $4.45 \times 10^{15}$ | $\text{m}^2/\text{s}$ | Invariant angular momentum per unit mass |
| Specific Orbital Energy | $\mathcal{E}$ | $-1/(2a) = -0.5$ | $-4.43 \times 10^{8}$ | $\text{J/kg}$ ($\text{m}^2/\text{s}^2$) | Invariant total mechanical energy per unit mass |
| Simulation Delta Time | $\Delta t$ | $0.001\text{--}0.035$ | Real-time frame clamped | $\text{s}$ | Delta time passed to `withFrameNanos` loop |


---

## 3. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`KeplerOrbitsExperiment`](./KeplerOrbitsExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week3.Day15`
- **Architecture:** Jetpack Compose Multiplatform with custom high-speed Canvas drawing and frame-synchronized `withFrameNanos` integration.

### Domain Models: Celestial Presets (`PlanetPreset`)

| Preset | Title | Symbol | Eccentricity ($e$) | Accent Color | Physical Context |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `CIRCULAR` | Circle | ⚪ | $0.00$ | `Color.White` | Idealized circular orbit ($v = \text{const}$, $\mathbf{F} \perp \mathbf{v}$) |
| `EARTH` | Earth | 🌍 | $0.02$ | `CyanNeon` | Near-circular orbit with subtle perihelion acceleration |
| `MARS` | Mars | 🔴 | $0.09$ | `CoralNeon` | Moderate eccentricity that enabled Kepler to formulate his laws |
| `PLUTO` | Pluto | 🪐 | $0.25$ | `AmberVibrant` | Highly eccentric Kuiper Belt dwarf planet orbit |
| `COMET` | Comet | ☄️ | $0.75$ | `PurpleNeon` | Extreme eccentricity with dramatic perihelion velocity surge |

### Reactive State Variables

| State Variable | Type / Initialization | Functional Role in Simulation |
| :--- | :--- | :--- |
| `selectedPreset` | `PlanetPreset?` (`PLUTO`) | Active astronomical body preset; becomes `null` upon manual drag/slider adjustment |
| `eccentricity` | `Float` ($0.48$) | Orbital eccentricity $e \in [0.00, 0.88]$ |
| `orbitSpeedMultiplier` | `Float` ($1.0$) | Time-warp simulation speed scaler $s \in [0.3, 3.0]$ |
| `showAreaSectors` | `Boolean` (`true`) | Toggle visibility of Kepler's 2nd Law swept-area sector wedge |
| `isRunning` | `Boolean` (`true`) | Physics loop animation running / paused toggle |
| `eccentricAnomalyRad` | `Float` ($0.0\,\text{rad}$) | Instantaneous eccentric anomaly angle $E \in [0, 2\pi)$ parameterizing planet position |
| `starGlow` | `animateFloat` ($0.85 \to 1.15$) | Infinite harmonic pulsing factor for the solar corona |

### Frame Loop & Kinematic Integration
The simulation runs inside a `LaunchedEffect(isRunning, eccentricity, orbitSpeedMultiplier)` block driven by `withFrameNanos`:
1. **Delta-Time Clamping:** $\Delta t = (t_{\text{now}} - t_{\text{last}}) / 10^9$, clamped to $[0.001\,\text{s}, 0.035\,\text{s}]$ to ensure smooth physics across 60 Hz, 90 Hz, and 120 Hz displays.
2. **Differential Step:**
   $$
   \Delta E = \left( \frac{1.25 \cdot s_{\text{multiplier}}}{\max(1 - e \cos E, \, 0.04)} \right) \cdot \Delta t
   $$
3. **Phase Wrap:** $E = (E + \Delta E) \pmod{2\pi}$.
4. **Derived Metrics:**
   - $r_{\text{norm}} = 1 - e \cos E$
   - $v_{\text{norm}} = \sqrt{\max(0.01, \, 2/r_{\text{norm}} - 1)}$

### User Gestures & Interactivity
- **Horizontal Drag Gesture:** Dragging horizontally across the Canvas (`detectDragGestures`) continuously tunes orbital eccentricity ($e \leftarrow (e + \Delta x \cdot 0.002)$ clamped to $[0.0, 0.88]$), clearing `selectedPreset` to indicate a custom orbit.
- **Canvas Tap Gesture:** Tapping anywhere on the Canvas (`detectTapGestures`) toggles animation pause/resume (`isRunning = !isRunning`).
- **Preset Selection Chips:** Instantly switches between Circle, Earth, Mars, Pluto, and Comet presets.
- **Continuous Sliders:** Fine-tunes eccentricity $e$ ($0.00\text{--}0.88$) and simulation speed ($0.3\times\text{--}3.0\times$).
- **Equal Area Wedge Checkbox:** Toggles Kepler's 2nd Law swept sector visualization.
- **Reset Button:** Re-centers simulation to Pluto preset ($e = 0.25$, $1.0\times$, $E = 0$, `isRunning = true`).

### Canvas Graphics Pipeline
1. **Deep Space Starfield (`drawStarfield`):** Coordinate grid ($44\,\text{dp}$ cells) overlaid with 8 distinct background stellar coordinate points with variable brightness and radii.
2. **Orbital Ellipse Track (`drawOrbitalEllipse`):**
   - 80-segment dashed path tracing the ellipse $x = x_c - a\cos\theta, y = y_c + b\sin\theta$.
   - Major axis dashed line extending through both apsides.
   - Colored marker dots for Perihelion (Emerald Neon, closest point to Sun) and Aphelion (Coral Neon, farthest point from Sun).
   - White focal center marker.
3. **Equal Areas Swept Sector (`drawSweptAreaSector`):**
   - Calculates time-equivalent arc span:
     $$
     \Delta E = \text{clamp}\left(\frac{0.42}{\max(1 - e \cos E, \, 0.1)}, \, 0.15, \, 1.2\right)
     $$
   - Draws an amber translucent wedge (`AmberVibrant` at $32\%$ alpha) anchored at the Sun focus $F_1$, sweeping back 24 sub-steps along the orbital perimeter to demonstrate that equal time sweeps equal geometric area.
4. **Radius Vector:** Dashed Cyan line connecting the Sun to the planet's instantaneous center.
5. **Tangent Velocity Vector ($\mathbf{v}$):** Emerald Neon vector pointing along the orbit tangent, dynamically scaled with $v_{\text{norm}}$, complete with perpendicular arrowhead caps.
6. **Gravitational Force Vector ($\mathbf{F}_g$):** Coral Neon vector pointing directly toward the Sun, scaled inversely by the square of normalized distance ($1/r^2$).
7. **Central Star / Sun (`drawSun`):**
   - Pulsing solar corona ($34\,\text{px} \cdot \text{starGlow}$, Amber glow).
   - Middle golden halo ($22\,\text{px}$).
   - White-yellow-orange radial gradient stellar core ($16\,\text{px}$).
8. **Planet Orb:** Multi-layer sphere with radial glare (White core $\to$ planet preset accent color $\to$ `BlueLaser`) and outer white atmospheric rim.
9. **Transparent Telemetry HUD (`TransparentTelemetryHud`):** Floating non-obstructive overlay displaying:
   - Orbit Geometry classification (Circular vs. Elliptical with $e$).
   - Current Radius $r$ in $\text{AU}$ alongside Perihelion and Aphelion limits.
   - Orbital Velocity $v$ in normalized $v_0$ units.
   - Kepler's 2nd Law status ($dA/dt = L/(2m) = \text{const}$).
   - Angular Momentum conservation badge ($L = \mathbf{r} \times \mathbf{p}$).

---

## 4. Suggested Investigations & Parameter Experiments

1. **Equal Area Confirmation (Kepler's Second Law):**
   Enable the **Equal Area Wedge** checkbox and set eccentricity to **Pluto ($e = 0.25$)** or **Comet ($e = 0.75$)**:
   - Near **Perihelion**, the wedge is short in radius but wide in angular sweep.
   - Near **Aphelion**, the wedge is long in radius but narrow in angular sweep.
   - Both wedges enclose identical geometric surface areas because $dA/dt = \text{constant}$!
2. **Vis-Viva Speed Ratio on the Comet Orbit:**
   Select the **Comet** preset ($e = 0.75$):
   - Theoretical perihelion speed:
     $$
     v_p = \sqrt{\frac{1 + e}{1 - e}} = \sqrt{\frac{1 + 0.75}{1 - 0.75}} = \sqrt{\frac{1.75}{0.25}} = \sqrt{7} \approx 2.65\,v_0
     $$
   - Theoretical aphelion speed:
     $$
     v_a = \sqrt{\frac{1 - e}{1 + e}} = \sqrt{\frac{0.25}{1.75}} = \frac{1}{\sqrt{7}} \approx 0.38\,v_0
     $$
   - Notice the extreme velocity ratio: $\frac{v_p}{v_a} = \frac{1 + e}{1 - e} = 7.0\times$! The comet screams past the Sun at over 7 times its crawl speed at the far edge of the solar system.
3. **The Circular Limit ($e = 0.00$):**
   Select the **Circle** preset ($e = 0.00$):
   - Notice the Sun sits exactly at the geometric center ($c = ae = 0$).
   - Orbital speed $v_{\text{norm}}$ remains constant at $1.0\,v_0$ throughout the entire revolution.
   - The velocity vector $\mathbf{v}$ (Emerald) and gravitational force vector $\mathbf{F}_g$ (Coral) remain perpendicular at all times ($\mathbf{F}_g \cdot \mathbf{v} = 0$), proving that gravity does zero work on circular orbits.
4. **Interactive Eccentricity Tuning:**
   Drag horizontally across the space canvas to adjust $e$ smoothly from $0.00$ up to $0.88$. Watch the empty focus $F_2$ pull away from the Sun and observe the dynamic elongation of the major axis.
