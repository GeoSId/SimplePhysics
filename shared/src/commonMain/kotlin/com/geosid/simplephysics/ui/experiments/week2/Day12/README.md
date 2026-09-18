# Day 12: Terminal Velocity & Parachute

> **Week 2: Classical Mechanics • Collisions, Conservation Laws & Rigid Bodies**  
> *Topic Subtitle: Equilibrium of Gravitational & Aerodynamic Forces*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Drop skydivers from 4,000 meters, toggle body orientations, and deploy parachutes to observe velocity equilibrium.

### Scientific Principles & Mechanism
As a falling body accelerates due to gravity, upward aerodynamic drag grows quadratically with velocity ($F_d \propto v^2$). Eventually, upward drag precisely balances downward gravitational weight ($F_{\text{net}} = F_g - F_d = 0$). At this dynamic equilibrium, acceleration drops to zero and the object falls at a steady terminal velocity $v_t$. Deploying a parachute drastically increases frontal surface area $A$ and drag coefficient $C_d$, producing a massive upward deceleration spike until a much slower equilibrium speed is reached.

### Laboratory / Kitchen Protocol (Try It At Home)
> Drop a single paper cupcake liner versus a stack of 4 liners nested together. The stack falls significantly faster because its mass is $4\times$ greater while its frontal area $A$ remains identical ($v_t \propto \sqrt{m}$)!

---

## 2. Mathematical Foundation & Governing Equations

The net downward acceleration is determined by Newton's Second Law with quadratic fluid drag:

$$
F_{\text{net}} = m g - \frac{1}{2} \rho v^2 C_d A = m \frac{dv}{dt}
$$

Setting $F_{\text{net}} = 0$ ($a = 0$) yields the steady-state terminal velocity:

$$
v_t = \sqrt{\frac{2 m g}{\rho A C_d}}
$$

### Physical Meaning & Quantities
- **$m$ (Mass):** Inertia of the falling object (kg). Higher mass yields higher terminal velocity ($v_t \propto \sqrt{m}$).
- **$g$ (Gravitational Acceleration):** Downward acceleration due to gravity ($9.81\text{ m/s}^2$).
- **$\rho$ (Fluid Density):** Density of the surrounding medium ($1.225\text{ kg/m}^3$ for sea-level air, $1000\text{ kg/m}^3$ for water, $0\text{ kg/m}^3$ for vacuum).
- **$A$ (Cross-Sectional Area):** Frontal area projected against the airflow ($\text{m}^2$).
- **$C_d$ (Drag Coefficient):** Dimensionless aerodynamic form factor (e.g., $0.47$ for a sphere, $1.20$ for belly-to-earth skydiver, $1.75$ for an open parachute).
- **$F_g = mg$ vs. $F_d = \frac{1}{2}\rho v^2 C_d A$:** Balance between downward weight and upward aerodynamic resistance.

---

## 3. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`TerminalVelocityExperiment`](./TerminalVelocityExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week2.Day12`
- **Screen Architecture:** Compose Multiplatform with Canvas rendering & high-frequency physics tick.

### Reactive State Variables
The interactive state is managed via Compose `mutableStateOf` variables:

| State Variable | Type / Default | Functional Role in Simulation |
| :--- | :--- | :--- |
| `selectedObject` | `TerminalVelocityObject.SKYDIVERR` | Active object preset (`SKYDIVERR`, `BOWLING_BALL`, `BASEBALL`, `COFFEE_FILTER`) |
| `selectedMedium` | `MediumType.AIR_SEA_LEVEL` | Fluid medium preset (`AIR_SEA_LEVEL`, `WATER`, `VACUUM`) |
| `isParachuteDeployed` | `mutableStateOf(false)` | Toggles canopy deployment ($A \to 25\text{ m}^2$, $C_d \to 1.75$, $m \to m + 5\text{ kg}$) |
| `mass` | `80.0f` (kg) | Falling body mass ($0.001\text{ kg} - 150\text{ kg}$) |
| `area` | `0.70f` ($\text{m}^2$) | Frontal cross-sectional area ($0.001\text{ m}^2 - 2.5\text{ m}^2$) |
| `dragCoeff` | `1.20f` | Form drag coefficient $C_d$ |
| `velocity` | `mutableStateOf(0f)` (m/s) | Live falling speed $v$ |
| `distanceFallen` | `mutableStateOf(0f)` (m) | Accumulated vertical fall distance |
| `simTime` | `mutableStateOf(0f)` (s) | Elapsed physics simulation time |
| `dragOffsetY` | `mutableStateOf(0f)` | Interactive touch drag offset for vertical repositioning |
| `velocityHistory` | `SnapshotStateList<Float>` | Circular buffer tracking velocity trajectory over time |

### Frame Loop & Physics Integration
- **High-Precision Physics Loop:** Driven by `withFrameNanos` inside `LaunchedEffect`.
- **4-Substep Euler-Cromer Integration:** Subdivides each frame ($\Delta t / 4$) to guarantee numerical stability during high-speed drag transitions.
- **Equilibrium & Braking Detection:** Triggers equilibrium visual states when $|v - v_t| / v_t < 3.5\%$ and braking states when $F_d > 1.08 F_g$.

### User Gestures & Interactivity
- **Vertical Drag Gesture:** Touch and drag the falling object vertically on canvas with responsive bobbing within the wind tunnel.
- **Tap to Deploy Parachute:** Tap directly on the canvas or press `🪂 Deploy Parachute` when the skydiver is active.
- **Fluid Medium Switching:** Test behavior across Air ($1.225\text{ kg/m}^3$), Water ($1000\text{ kg/m}^3$), and Vacuum ($0\text{ kg/m}^3$).
- **Custom Mass & Area Tuning:** Real-time dual sliders for precision scaling.

### Canvas Graphics Pipeline
- **Chamber Guideway & Vertical Speed Tape:** Left-hand wall tick marks with real-time velocity fill tape and target $v_t$ indicator line.
- **Dynamic Upward Air Streamlines:** Upward streaks whose flow velocity matches falling speed, including lateral diversion curves around the falling body and deployed canopy.
- **Wake Vortices:** Counter-rotating vortex shedding behind the body at speeds above $3\text{ m/s}$.
- **Free-Body Diagram (FBD):** Dynamic force vectors showing downward gravity $F_g$ (Amber), upward drag $F_d$ (Cyan/Coral/Emerald), and a pulsing equilibrium ring when $F_d \approx F_g$.
- **Procedural Object Models:** Detailed vector rendering for freefall belly-to-earth skydiver, deployed multi-color parachute canopy with suspension lines, shiny bowling ball, stitched baseball, and fluted coffee filter.

---

## 4. Suggested Investigations & Parameter Experiments
1. **The Parachute Deceleration Spike:** Drop the skydiver until reaching terminal freefall (~$54\text{ m/s}$ or $194\text{ km/h}$), then tap to deploy the parachute. Watch the upward drag vector explode in size ($F_d \gg F_g$), causing abrupt braking down to a safe landing speed (~$5.3\text{ m/s}$).
2. **Vacuum Chamber Drop (Galileo's Leaning Tower):** Switch the medium from Air to Vacuum ($\rho = 0$). Note that drag collapses to zero, $v_t \to \infty$, and both bowling ball and feather fall identically with constant downward acceleration $g = 9.81\text{ m/s}^2$.
3. **Mass vs. Area Scaling:** Compare the Bowling Ball vs. Feather in Air. Increase the mass slider while holding area constant to verify that $v_t \propto \sqrt{m}$.
4. **Fluid Density Contrast:** Switch between Air ($\rho = 1.225\text{ kg/m}^3$) and Water ($\rho = 1000\text{ kg/m}^3$) to observe how dense fluids dramatically compress terminal velocity.
