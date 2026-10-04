# Day 28: Tacoma Narrows Aeroelastic Flutter

> **Week 4: Waves, Sound & Acoustic Resonance**  
> *Topic Subtitle: Self-Excited Torsional Aeroelastic Flutter & Dynamic Stall*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Expose a suspension bridge to 40 mph crosswinds and watch aeroelastic flutter amplify twisting vibrations until total structural failure!

### Scientific Principles & Mechanism
On the morning of November 7, 1940, the Tacoma Narrows Bridge ("Galloping Gertie") in Washington State began violently twisting in 42 mph (18.8 m/s) crosswinds. After over an hour of violent torsional oscillation reaching peak deck angles of $\pm 35^\circ$, the suspension cables and steel deck suffered catastrophic structural failure and plunged into Puget Sound.

For decades, introductory textbooks mischaracterized the disaster as an example of simple forced resonance driven by periodic von Kármán vortex shedding matching the bridge's natural frequency. Modern aerodynamic and structural analysis by Robert Scanlan and colleagues revealed the true culprit: **Self-Excited Aeroelastic Flutter**:

1. **Bluff Body Flow Separation & Vortex Shedding:**  
   The bridge was built with solid 8-foot (2.4 m) vertical plate girders forming a rigid H-shaped cross-section. Rather than cleanly slicing through the horizontal wind, the blunt windward girder forced the airflow to separate into massive alternating vortices above and below the deck.
2. **Angle of Attack & Aerodynamic Coupling:**  
   As the roadway experienced a slight initial torsional twist $\theta$, the angle of attack relative to the oncoming wind shifted. The flow separation points moved dynamically along the upper and lower surfaces, creating an asymmetric aerodynamic pressure distribution.
3. **Negative Aerodynamic Damping:**  
   In most stable aerodynamic shapes (such as an airplane wing or streamlined airfoil), pitching into the wind creates a restoring aerodynamic damping force ($c_{\text{aero}} > 0$) that dissipates oscillatory energy. However, for the bluff H-section girder, vortex shedding lag causes the aerodynamic twisting moment $M_{\text{aero}}$ to be in-phase with the angular velocity $\dot{\theta}$.  
   This creates **negative aerodynamic damping**: the fluid extracts energy from the steady laminar crosswind and pumps it directly into the bridge's torsional motion on every cycle!
4. **Exponential Limit-Cycle Amplification & Cable Failure:**  
   When the negative aerodynamic damping exceeds the bridge's internal structural damping ($c_{\text{aero}} < -c_\theta$), the total net damping turns negative ($c_{\text{net}} < 0$). Oscillations grow exponentially without bound until suspension hangers snap under asymmetric cyclical tension, leading to progressive total structural collapse.
5. **Modern Aerodynamic Truss Retrofits:**  
   Modern suspension bridges prevent flutter by replacing solid vertical girders with open aerodynamic trusses and center aerodynamic venting slots. Air passes freely through the structure, keeping aerodynamic damping strictly positive and preventing flow separation.

### Laboratory & Real-World Protocol (Try It At Home)
> Hold a thin strip of paper (approximately 2 cm wide by 15 cm long) loosely between two fingers and blow air steadily across its upper surface. Notice how the paper does not simply bend downward; instead, it begins fluttering and buzzing violently back and forth! The steady breath provides no periodic timing, yet self-excited aeroelastic instability continuously pumps energy into the vibration.

---

## 2. Mathematical Formulation & The Three Pillars

### Pillar 1: Governing Law & Torsional Equation of Motion
The rotational dynamics of the bridge cross-section around its longitudinal center of rotation are governed by the second-order torsional differential equation:

$$
I_\theta \ddot{\theta} + c_\theta \dot{\theta} + k_\theta \theta = M_{\text{aero}}(U, \theta, \dot{\theta})
$$

where:
- $I_\theta$ is the mass moment of inertia per unit span length ($\text{kg}\cdot\text{m}^2/\text{m}$).
- $c_\theta$ is the intrinsic structural viscous damping coefficient ($\text{N}\cdot\text{m}\cdot\text{s}/\text{rad}$).
- $k_\theta$ is the torsional stiffness provided by the main suspension cables and deck rigidity ($\text{N}\cdot\text{m}/\text{rad}$).
- $M_{\text{aero}}$ is the total self-excited aerodynamic pitching moment per unit span ($\text{N}\cdot\text{m}/\text{m}$).

Following Scanlan's linearized aerodynamic formulation for bridge flutter:

$$
M_{\text{aero}} = \frac{1}{2} \rho U^2 B^2 \left( A_2^* \frac{B \dot{\theta}}{U} + A_3^* \theta \right)
$$

Expanding for arbitrary large angles with dynamic stall nonlinearity:

$$
M_{\text{aero}} = \frac{1}{2} \rho U^2 B^2 \left[ C_{\text{flutter}} \cdot \dot{\theta} + C_{\text{stall}} \cdot \sin(\theta) \right]
$$

where $\rho$ is atmospheric air density ($1.225\text{ kg/m}^3$), $U$ is the horizontal crosswind velocity ($\text{m/s}$), $B$ is the deck width ($11.9\text{ m}$), $C_{\text{flutter}}$ is the aerodynamic flutter velocity coefficient, and $C_{\text{stall}}$ is the aerodynamic restoring/stall torque coefficient.

---

### Pillar 2: Kinematics & Negative Damping Stability Boundary
Rearranging the equation of motion to group velocity-dependent terms yields the effective net torsional damping $c_{\text{net}}$:

$$
I_\theta \ddot{\theta} + \underbrace{\left( c_\theta - \frac{1}{2} \rho U B^2 C_{\text{flutter}} \right)}_{c_{\text{net}}(U)} \dot{\theta} + \left( k_\theta \theta - \frac{1}{2} \rho U^2 B^2 C_{\text{stall}} \sin\theta \right) = 0
$$

The net damping coefficient $c_{\text{net}}$ dictates the stability regime of the bridge:

$$
c_{\text{net}}(U) = c_\theta - \frac{1}{2} \rho U B^2 C_{\text{flutter}}
$$

1. **Sub-Critical Stable Regime ($U < U_{\text{crit}}$):**  
   $c_{\text{net}} > 0$. The roots of the characteristic equation have negative real parts:
   $$
   \lambda = -\gamma \pm i \omega_d, \quad \gamma = \frac{c_{\text{net}}}{2 I_\theta} > 0
   $$
   Any wind gust or mechanical disturbance decays exponentially toward zero:
   $$
   \theta(t) = \theta_0 e^{-\gamma t} \cos(\omega_d t)
   $$
2. **Critical Flutter Boundary ($U = U_{\text{crit}}$):**  
   Setting $c_{\text{net}} = 0$ reveals the critical flutter threshold:
   $$
   U_{\text{crit}} = \frac{2 c_\theta}{\rho B^2 C_{\text{flutter}}} \approx 15.5\text{ m/s} \quad (\approx 35\text{ mph})
   $$
3. **Super-Critical Flutter Runaway ($U > U_{\text{crit}}$):**  
   $c_{\text{net}} < 0$. The real part of the eigenvalue becomes strictly positive ($\gamma < 0$):
   $$
   \theta(t) = \theta_0 e^{|\gamma| t} \cos(\omega_d t)
   $$
   Small rotational perturbations undergo exponential amplitude growth on each cycle!

For an open aerodynamic truss retrofit, flow separation is suppressed, reversing the sign of the aerodynamic flutter derivative ($C_{\text{flutter}} < 0$). In that case, increasing wind speed increases positive aerodynamic damping, making flutter physically impossible at any operational wind speed.

---

### Pillar 3: Energy, Aeroelastic Work & Structural Tension Failure
The mechanical energy of the torsional oscillator is:

$$
E_{\text{mech}} = \frac{1}{2} I_\theta \dot{\theta}^2 + \frac{1}{2} k_\theta \theta^2
$$

Evaluating the time derivative of energy along the trajectory:

$$
\frac{dE_{\text{mech}}}{dt} = \dot{\theta} (I_\theta \ddot{\theta} + k_\theta \theta) = \left( M_{\text{aero}} - c_\theta \dot{\theta} \right) \dot{\theta}
$$

Integrating over one full oscillation cycle of period $T = 2\pi / \omega$:

$$
\Delta E_{\text{cycle}} = \oint M_{\text{aero}} \, d\theta - \oint c_\theta \dot{\theta} \, d\theta = \int_0^T \left( \frac{1}{2} \rho U^2 B^2 C_{\text{flutter}} - c_\theta \right) \dot{\theta}^2 \, dt
$$

When $U > U_{\text{crit}}$, $\Delta E_{\text{cycle}} > 0$: the wind performs net positive work on the structure each second, continuously inflating mechanical energy.

#### Suspension Cable Tension Asymmetry & Collapse Limit
As the deck rotates by angle $\theta$, vertical displacement of the left and right roadway edges is $\pm \frac{1}{2} B \sin\theta$. The instantaneous tensions in the left and right suspension hanger systems are:

$$
T_{\text{left}}(\theta) = T_0 + \frac{1}{2} k_{\text{cable}} B \sin\theta
$$
$$
T_{\text{right}}(\theta) = T_0 - \frac{1}{2} k_{\text{cable}} B \sin\theta
$$

where $T_0 = 140\text{ kN}$ is nominal dead-load pretension and $k_{\text{cable}} = 32\text{ kN/m}$ is cable spring stiffness.

Structural failure occurs when either:
1. Peak hanger cable tension exceeds the steel yield rupture threshold:
   $$
   T_{\text{max}} \ge T_{\text{yield}} = 310\text{ kN}
   $$
2. Torsional twist angle exceeds the mechanical clearance and joint tearing limit:
   $$
   |\theta| \ge \theta_{\text{fail}} = 35^\circ \approx 0.61\text{ rad}
   $$

Once either threshold is crossed, the structural load path fails, triggering complete collapse.

---

## 3. Physical Parameters & System Constants

| Parameter | Symbol | Nominal Value | Range | SI Units | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| Crosswind Velocity | $U$ | `18.8` (42 mph) | `0.0 .. 30.0` | $\text{m/s}$ | Horizontal wind speed incident on bridge |
| Deck Width | $B$ | `11.9` | Fixed | $\text{m}$ | Lateral roadway width (Tacoma span) |
| Air Density | $\rho$ | `1.225` | Fixed | $\text{kg/m}^3$ | Standard atmospheric density at sea level |
| Mass Moment of Inertia | $I_\theta$ | `45000.0` | Fixed | $\text{kg}\cdot\text{m}^2/\text{m}$ | Torsional mass inertia per unit span |
| Torsional Stiffness | $k_\theta$ | `18000.0` | Fixed | $\text{N}\cdot\text{m}/\text{rad}$ | Bridge structural torsional spring constant |
| Structural Damping | $c_\theta$ | `850.0` | Fixed | $\text{N}\cdot\text{m}\cdot\text{s}/\text{rad}$ | Viscous structural damping coefficient |
| Critical Wind Speed | $U_{\text{crit}}$ | `15.5` (~35 mph)| Computed | $\text{m/s}$ | Stability boundary threshold |
| Nominal Cable Tension | $T_0$ | `140000.0` | Fixed | $\text{N}$ | Dead-load gravity pretension |
| Cable Stiffness | $k_{\text{cable}}$ | `32000.0` | Fixed | $\text{N/m}$ | Elastic spring constant of vertical hangers |
| Cable Yield Limit | $T_{\text{yield}}$ | `310000.0` | Fixed | $\text{N}$ | Tensile failure rupture threshold |
| Deck Failure Angle | $\theta_{\text{fail}}$ | `0.61` ($35^\circ$) | Fixed | $\text{rad}$ | Maximum mechanical tilt before structural collapse |
| H-Girder Flutter Coeff | $C_{\text{flutter, H}}$ | `+0.048` | Fixed | Dimensionless | Destabilizing aerodynamic derivative |
| Truss Retrofit Flutter Coeff | $C_{\text{flutter, truss}}$ | `-0.015` | Fixed | Dimensionless | Stabilizing aerodynamic damping derivative |

---

## 4. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`TacomaFlutterExperiment`](./TacomaFlutterExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week4.Day28`
- **Registry Integration:** `ExperimentScreenRegistry.screens["tacoma_flutter"]`
- **Model Definition:** `Experiment.kt` (`id = "tacoma_flutter"`, `day = 28`, `category = WEEK_4_WAVES_SOUND`)

### Reactive State Variables
The simulation state is maintained via Compose `mutableStateOf` variables:

| State Variable | Type | Initialization | Functional Role |
| :--- | :--- | :--- | :--- |
| `windSpeed` | `Float` | `18.8f` | Wind speed in m/s (default 42 mph gale) |
| `isTrussRetrofit` | `Boolean` | `false` | Toggles solid H-plate girder vs open truss |
| `theta` | `Float` | `0.06f` | Instantaneous bridge deck twist angle (rad) |
| `thetaDot` | `Float` | `0.0f` | Instantaneous deck angular velocity (rad/s) |
| `isRunning` | `Boolean` | `true` | Simulation play/pause clock control |
| `isCollapsed` | `Boolean` | `false` | True when structural limits are breached |
| `showStreamlines` | `Boolean` | `true` | Toggles dynamic airflow particle field |
| `showStressField` | `Boolean` | `true` | Toggles cable tension stress vectors & color heatmaps |
| `selectedPreset` | `TacomaPreset?` | `TACOMA_COLLAPSE` | Selected wind/geometry preset chip |

### Sub-Stepped Numerical Integration Loop
Physical integration runs on every frame inside `LaunchedEffect(isRunning, windSpeed, isTrussRetrofit, isCollapsed)` with `withFrameNanos`. To prevent numerical drift and guarantee energy-conserving stability during violent oscillations, the frame delta $\Delta t$ is subdivided into 6 sub-steps:

```kotlin
// 1. Torsional equation of motion and self-excited aerodynamic torque
val aeroTorque = 0.5f * airDensity * windSpeed * windSpeed * deckWidth * deckWidth * (flutterCoeff * thetaDot + stallCoeff * sin(theta))
val angularAccel = (aeroTorque - dampingCoeff * thetaDot - stiffnessCoeff * theta) / momentOfInertia

thetaDot += angularAccel * dt
theta += thetaDot * dt

// Check collapse threshold
val cableTensionL = nominalTension + 0.5f * cableStiffness * deckWidth * abs(sin(theta))
if (abs(theta) >= failureAngleRad || cableTensionL >= maxCableTension) {
    isCollapsed = true
    collapseTime = simTime
}
```

### Visual Rendering Pipeline
1. **Dynamic Wind Particle Streamlines:**  
   Renders 45 continuous streamlines traveling from left to right. When passing the tilted bridge deck, streamline trajectories are dynamically deflected by $\theta$, generating curling vortex wakes downstream of the deck.
2. **Elevated Perspective Deck & Towers:**  
   The suspension towers and bridge deck are anchored with center elevation $y = h \times 0.40$, leaving the bottom 35% completely open for controls.
3. **Deck Cross-Section Geometry:**  
   Renders either the historic solid H-plate girder with vertical wind barriers or the retrofitted aerodynamic open truss with structural cross-bracing.
4. **Stress & Tension Heatmaps:**  
   Vertical suspension cables dynamically change color and stroke weight in real time based on instantaneous tensile load:
   - Green ($T < 180\text{ kN}$): Safe nominal working load.
   - Amber ($180\text{ kN} \le T < 260\text{ kN}$): Elevated cyclical strain.
   - Coral/Red ($T \ge 260\text{ kN}$): Yield rupture warning zone.
5. **Interactive Drag & Tap Controls:**  
   Users can directly touch-drag the deck up or down to impart arbitrary initial angular displacements, or tap the canvas to deliver impulsive angular kicks.

---

## 5. Suggested Investigations & Parameter Experiments

1. **The Sub-Critical Breeze Test ($U = 10\text{ mph}$):**  
   Set wind speed to 10 mph ($4.5\text{ m/s}$). Drag the bridge deck to $20^\circ$ and release it. Watch the oscillation decay smoothly to rest: intrinsic structural damping easily overpowers the weak aerodynamic moment.
2. **The 35 mph Critical Boundary:**  
   Slowly increase wind speed past 35 mph ($15.5\text{ m/s}$). Observe how the decay stops and the deck enters a sustained self-excited oscillation (limit-cycle flutter) where damping and aerodynamic energy input balance.
3. **The 42 mph Historical Collapse:**  
   Select the `Gale (42 mph)` preset. Watch the oscillations grow exponentially cycle after cycle until the tilt angle crosses $35^\circ$, triggering catastrophic cable rupture and bridge collapse!
4. **The Modern Truss Retrofit:**  
   Toggle `Modern Truss` while at 42+ mph. Watch how the open truss instantly suppresses vortex buildup, restoring positive aerodynamic damping and bringing the bridge back to stable equilibrium.
