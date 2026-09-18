# Day 8: Projectile Drag & Crosswinds

> **Week 2: Classical Mechanics • Collisions, Conservation Laws & Rigid Bodies**  
> *Topic Subtitle: Aerodynamic Drag & Vector Wind Forces*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Aim a cannon and launch projectiles against quadratic aerodynamic drag and crosswinds.

### Scientific Principles & Mechanism
In elementary physics, projectile motion is treated in a vacuum where horizontal velocity remains constant and vertical velocity experiences uniform gravitational acceleration, producing a symmetric parabola. In the real atmosphere, air exerts quadratic aerodynamic skin friction and form drag proportional to the square of relative velocity:

$$
\mathbf{F}_{\text{drag}} = -\frac{1}{2} \rho C_d A |\mathbf{v}_{\text{rel}}| \mathbf{v}_{\text{rel}}
$$

Because drag opposes the instantaneous direction of motion relative to the moving air mass ($\mathbf{v}_{\text{rel}} = \mathbf{v} - \mathbf{v}_{\text{wind}}$), the horizontal and vertical equations of motion become coupled and nonlinear. Drag causes the projectile's trajectory to lose symmetry, steepening noticeably during descent (the characteristic "plunging" trajectory) and significantly curtailing maximum range.

### Laboratory / Kitchen Protocol (Try It At Home)
> Drop a flat sheet of paper and a crumpled paper ball of the exact same mass simultaneously. The crumpled ball hits the floor first because its frontal cross-sectional area $A$ is vastly smaller, slashing aerodynamic drag!

---

## 2. Mathematical Foundation & Governing Equations

### Relative Wind Vector & Aerodynamic Drag
With projectile velocity $\mathbf{v} = (v_x, v_y)$ and horizontal crosswind $\mathbf{v}_{\text{wind}} = (v_w, 0)$:

$$
\mathbf{v}_{\text{rel}} = (v_x - v_w) \, \mathbf{\hat{i}} + v_y \, \mathbf{\hat{j}}, \quad |\mathbf{v}_{\text{rel}}| = \sqrt{(v_x - v_w)^2 + v_y^2}
$$

The net aerodynamic drag force vector is:

$$
\mathbf{F}_{\text{drag}} = -\frac{1}{2} \rho C_d A |\mathbf{v}_{\text{rel}}| \mathbf{v}_{\text{rel}}
$$

### Coupled Equations of Motion
Applying Newton's Second Law ($\mathbf{F}_{\text{net}} = m \mathbf{a} = m \mathbf{g} + \mathbf{F}_{\text{drag}}$):

$$
m \frac{dv_x}{dt} = -\frac{1}{2} \rho C_d A |\mathbf{v}_{\text{rel}}| (v_x - v_w)
$$

$$
m \frac{dv_y}{dt} = -m g - \frac{1}{2} \rho C_d A |\mathbf{v}_{\text{rel}}| v_y
$$

### Analytic Vacuum Baseline (Ideal Parabola)
When drag is zero ($C_d = 0$):

$$
y(x) = x \tan\theta - \frac{g x^2}{2 v_0^2 \cos^2\theta}, \quad R_{\text{vac}} = \frac{v_0^2 \sin(2\theta)}{g}
$$

### Physical Meaning & Quantities
- **$m$ (Projectile Mass):** $1.2\text{ kg}$.
- **$A$ (Cross-Sectional Frontal Area):** $0.015\text{ m}^2$ ($\approx 14\text{ cm}$ diameter sphere).
- **$\rho$ (Air Density):** $1.225\text{ kg/m}^3$ at standard sea level.
- **$C_d$ (Drag Coefficient):** Dimensionless form factor ($0.00$ for vacuum, $\sim 0.35 - 0.47$ for a sphere).
- **$v_w$ (Crosswind Speed):** Tailwind ($v_w > 0$) or headwind ($v_w < 0$), up to $\pm 20\text{ m/s}$.
- **$\theta, v_0$ (Launch Angle & Muzzle Speed):** Cannon elevation ($10^\circ - 85^\circ$) and velocity ($15 - 75\text{ m/s}$).

---

## 3. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`ProjectileMotionExperiment`](./ProjectileMotionExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week2.Day8`
- **Screen Architecture:** Compose Multiplatform with Canvas rendering & high-frequency physics tick.

### Reactive State Variables
The interactive state is managed via Compose `mutableStateOf` and `mutableStateListOf` variables:

| State Variable | Type / Default | Functional Role in Simulation |
| :--- | :--- | :--- |
| `launchAngleDeg` | `mutableStateOf(45f)` | Cannon barrel elevation angle ($10^\circ - 85^\circ$) |
| `launchSpeed` | `mutableStateOf(45f)` (m/s) | Initial muzzle velocity $v_0$ ($15 - 75\text{ m/s}$) |
| `dragCoefficient` | `mutableStateOf(0.35f)` | Aerodynamic form factor $C_d$ ($0.0 - 0.8$) |
| `windSpeed` | `mutableStateOf(-5.0f)` (m/s) | Horizontal wind speed (+ tailwind, - headwind) |
| `showVacuumComparison` | `mutableStateOf(true)` | Toggles yellow dashed theoretical vacuum parabola |
| `isFlying` | `mutableStateOf(false)` | Active flight trajectory integration flag |
| `posX`, `posY` | `mutableStateOf(0f)` (m) | Instantaneous spatial coordinates of projectile |
| `velX`, `velY` | `mutableStateOf(0f)` (m/s) | Instantaneous velocity components |
| `flightTime` | `mutableStateOf(0f)` (s) | Total elapsed airborne duration |
| `maxHeight` | `mutableStateOf(0f)` (m) | Highest altitude reached along trajectory |
| `targetHit` | `mutableStateOf(false)` | Flag triggered when impact falls within $\pm 5.5\text{ m}$ of $95\text{ m}$ target |
| `trajectoryPoints` | `SnapshotStateList<Offset>` | Recorded path coordinates for rendering trajectory line |
| `smokeParticles` | `SnapshotStateList<MuzzleSmoke>` | Animated particle puffs spawned upon muzzle firing |

### Frame Loop & Physics Integration
- **High-Precision Physics Loop:** Driven by `withFrameNanos` inside `LaunchedEffect(isFlying)`.
- **5-Substep Euler-Cromer Integration:** Subdivides each frame ($\Delta t / 5$) to prevent numerical drift in the nonlinear velocity-squared damping term.
- **Dynamic Ground Interception:** Detects $y \le 0$, clamps altitude, evaluates distance against the target flag at $95\text{ m}$, and ends flight.

### User Gestures & Interactivity
- **Touch-to-Aim Sky Drag:** Drag your finger across the sky viewport to dynamically adjust cannon elevation and launch speed in real time.
- **Precision Sliders:** Fine-tune Launch Angle ($10^\circ - 85^\circ$), Muzzle Speed ($15 - 75\text{ m/s}$), Drag Coefficient ($C_d$), and Crosswind ($-20$ to $+20\text{ m/s}$).
- **Vacuum Parabola Overlay Checkbox:** Live visual toggle enabling direct visual comparison between real atmospheric flight and ideal vacuum theory.
- **Fire & Reset Controls:** `🚀 FIRE!` button with muzzle blast particle animation and reset tool.

### Canvas Graphics Pipeline
- **Ground & Distance Markers:** Soil line with subsurface hatching and distance tick lines every $20\text{ meters}$ (`drawGroundAndGrid`).
- **Animated Wind Streaks:** Cyan streaks drifting across the sky whose speed and direction match the wind vector (`drawWindStreaks`).
- **Ideal Vacuum Parabola:** Dashed amber theoretical ballistic curve (`drawVacuumParabola`).
- **Atmospheric Trajectory Trace:** Solid neon cyan path with amber sample dots along the flight arc.
- **Wheeled Cannon:** Rotating steel barrel on wooden wheeled carriage (`drawCannon`).
- **Muzzle Smoke Puff:** Particle dispersion simulation with expanding radius and alpha fade upon launch.
- **Flying Projectile & Velocity Arrow:** Glowing coral/white sphere with dynamic velocity vector arrow indicating speed and tangent angle.
- **Target Bullseye Flag:** Checkpoint flag at $95\text{ m}$ on the ground that turns bright emerald upon a direct hit (`drawTargetFlag`).

---

## 4. Suggested Investigations & Parameter Experiments
1. **Asymmetry of Atmospheric Trajectories:** Enable `Vacuum Parabola`. Launch at $45^\circ, 50\text{ m/s}$ with $C_d = 0.35$. Notice how the cyan air trajectory starts close to the dashed vacuum line, but falls dramatically short and drops down far more steeply near the end.
2. **Optimal Launch Angle in Atmosphere:** In a vacuum, $45^\circ$ strictly maximizes range. In air with drag ($C_d = 0.35$), test launches from $30^\circ$ to $45^\circ$. Notice that the maximum range angle shifts down to approximately $35^\circ - 38^\circ$ because spending less time in dense air reduces cumulative energy dissipation!
3. **Crosswind Target Challenge:** Try to score a direct hit on the target flag at $95\text{ m}$ under a heavy headwind ($-15\text{ m/s}$) versus a strong tailwind ($+15\text{ m/s}$). Observe how crosswinds dramatically warp the landing point.
4. **Pure Vacuum Test:** Set $C_d = 0$ and Wind $= 0$. Verify that the real trajectory matches the theoretical dashed vacuum parabola with 100% precision.
