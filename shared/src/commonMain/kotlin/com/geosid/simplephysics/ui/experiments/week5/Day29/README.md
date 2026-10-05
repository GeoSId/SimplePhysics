# Day 29: Total Internal Reflection (Laser Light Fountain)

> **Week 5: Optics & Light • Refraction, Total Internal Reflection & Diffraction**  
> *Topic Subtitle: Critical Angle, Optical Waveguides & Parabolic Light Guidance*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Bending laser beams inside an arcing water stream without escaping!

### Scientific Principles & Mechanism
In 1841, Swiss physicist Jean-Daniel Colladon and French physicist Jacques Babinet discovered that light could be guided along a curved jet of water. In 1870, John Tyndall famously popularized the demonstration before the Royal Institution in London: a bright light beam injected into a draining water vessel was trapped inside the curving liquid stream, illuminating the receiving bucket in brilliant color. This historic apparatus, the **Laser Light Fountain**, demonstrates the fundamental physical principle powering modern global telecommunications: **Total Internal Reflection (TIR)**.

1. **Refraction & Snell's Law:**  
   When light travels across an interface between two optical media with differing refractive indices ($n_1$ and $n_2$), the change in propagation speed causes the light ray to bend according to Snell's Law:
   $$n_1 \sin\theta_1 = n_2 \sin\theta_2$$
   When propagating from an optically denser medium ($n_1 > n_2$, such as water into air), the refracted ray bends away from the surface normal ($\theta_2 > \theta_1$).
2. **The Critical Angle Boundary ($\theta_c$):**  
   As the angle of incidence $\theta_1$ increases, the angle of refraction $\theta_2$ approaches $90^\circ$ (grazing along the surface). The angle of incidence that produces a $90^\circ$ refraction is called the **Critical Angle**:
   $$\sin\theta_c = \frac{n_2}{n_1} \implies \theta_c = \arcsin\left(\frac{n_{\text{air}}}{n_{\text{liquid}}}\right)$$
   For water ($n = 1.333$) in air ($n = 1.000$):
   $$\theta_c = \arcsin\left(\frac{1.000}{1.333}\right) \approx 48.61^\circ \approx 48.8^\circ$$
3. **Total Internal Reflection (TIR):**  
   When the angle of incidence exceeds the critical angle ($\theta_i \ge \theta_c$), Snell's law requires $\sin\theta_2 > 1$, which has no real solution for the transmission angle. Consequently, no light can refract into the second medium: $100\%$ of the optical power is reflected back into the liquid with zero radiative loss.
4. **Parabolic Stream Curvature & Optical Waveguiding:**  
   As the water stream falls under gravity, it forms a parabolic trajectory. As long as the local curvature of the jet is sufficiently gentle, light continues striking the boundary at grazing angles $\theta_i > \theta_c$, zigzagging through successive reflections from the nozzle all the way down to the catch basin.
5. **Optical Leakage & Minimum Bend Radius:**  
   If the water velocity is too low or the nozzle angle is too steep, the parabolic arc curves sharply. When the local boundary angle forces $\theta_i < \theta_c$, total internal reflection breaks down: light refracts through the surface and escapes into the surrounding air. This exact phenomenon governs the **minimum bend radius** of commercial glass optical fiber cables.

### Laboratory & Kitchen Protocol (Try It At Home)
> 1. Take a clean, clear plastic 1-liter soda bottle and poke a clean 5 mm circular hole roughly 5 cm above the base.  
> 2. Seal the hole with tape, fill the bottle with water, and place it beside a kitchen sink in a darkened room.  
> 3. Remove the tape so a smooth, laminar stream of water pours into the sink.  
> 4. Shine a handheld red or green laser pointer horizontally through the back of the bottle directly into the exit orifice.  
> 5. Watch in amazement as the beam becomes trapped inside the water arc, following the curve down and lighting up your hand or the sink basin!

---

## 2. Mathematical Formulation & The Three Pillars

### Pillar 1: Governing Law & Snell's Law Critical Angle
The propagation of monochromatic electromagnetic waves across the liquid-air dielectric boundary is governed by Maxwell's equations and Snell's Law:

$$
n_1 \sin\theta_i = n_2 \sin\theta_t
$$

where:
- $n_1 = n_{\text{liquid}}$ is the refractive index of the liquid ($1.333$ for pure water, $1.473$ for glycerol).
- $n_2 = n_{\text{air}} \approx 1.000$ is the refractive index of ambient air.
- $\theta_i$ is the angle of incidence measured from the inward surface normal $\vec{N}$.
- $\theta_t$ is the angle of transmitted refraction.

Setting the transmitted refraction angle to the critical limit $\theta_t = 90^\circ$ ($\sin\theta_t = 1$):

$$
\sin\theta_c = \frac{n_2}{n_1} \implies \theta_c = \arcsin\left(\frac{n_{\text{air}}}{n_{\text{liquid}}}\right)
$$

The Total Internal Reflection criterion is:

$$
\text{TIR Condition: } \quad \theta_i \ge \theta_c
$$

When $\theta_i \ge \theta_c$, the electromagnetic field in the air decays exponentially as an **evanescent wave**:

$$
\vec{E}_{\text{evanescent}}(z) = \vec{E}_0 e^{-\kappa z} e^{i(k_x x - \omega t)}, \quad \kappa = \frac{2\pi}{\lambda_0}\sqrt{n_1^2 \sin^2\theta_i - n_2^2}
$$

Because the field decays within fractions of an optical wavelength without propagating energy across the boundary, time-averaged Poynting flux into the air is zero, yielding perfect reflection.

---

### Pillar 2: Kinematics & Parabolic Fluid Jet Geometry
Water draining under atmospheric head pressure exits a horizontal nozzle of radius $r_0$ at position $(x_0, y_0)$ with velocity $v_0$ governed by Torricelli's Law:

$$
v_0 = \sqrt{2 g H_{\text{eff}}}
$$

Under uniform gravitational acceleration $g = 9.81\text{ m/s}^2$ and neglecting air drag, the coordinates of the stream centerline follow projectile kinematics:

$$
x(t) = x_0 + v_0 t
$$
$$
y(t) = y_0 + \frac{1}{2} g t^2
$$

Eliminating parameter $t$ yields the parabolic stream trajectory:

$$
y(x) = y_0 + \frac{g}{2 v_0^2} (x - x_0)^2
$$

The local tangent slope $y'(x)$ and inward surface normal vector $\vec{N}(x)$ are:

$$
y'(x) = \frac{dy}{dx} = \frac{g}{v_0^2}(x - x_0) = \tan\phi(x)
$$
$$
\vec{N}_{\text{top}}(x) = \frac{1}{\sqrt{1 + (y')^2}} \begin{pmatrix} -y' \\ 1 \end{pmatrix}, \quad \vec{N}_{\text{bottom}}(x) = -\vec{N}_{\text{top}}(x)
$$

The local radius of curvature $R(x)$ of the stream centerline is given by:

$$
R(x) = \frac{\left[1 + (y'(x))^2\right]^{3/2}}{|y''(x)|} = \frac{\left[1 + \left(\frac{g(x - x_0)}{v_0^2}\right)^2\right]^{3/2}}{\frac{g}{v_0^2}}
$$

For an optical waveguide of thickness $d$, light remains trapped if the curvature parameter $\kappa(x) = 1/R(x)$ satisfies the guidance threshold:

$$
\kappa(x) \le \kappa_{\text{max}} = \frac{2}{d} \left(1 - \frac{n_{\text{air}}}{n_{\text{liquid}}}\right)
$$

---

### Pillar 3: Energy, Fresnel Coefficients & Optical Power Transmission
At each boundary interaction, optical power distribution is governed by the Fresnel reflection coefficients for unpolarized light.

#### 1. In the Sub-Critical Regime ($\theta_i < \theta_c$):
Light partially reflects and partially escapes into the air at refraction angle $\theta_t = \arcsin\left(\frac{n_1}{n_2}\sin\theta_i\right)$. The reflection power coefficient $R$ is:

$$
r_s = -\frac{\sin(\theta_i - \theta_t)}{\sin(\theta_i + \theta_t)}, \quad r_p = \frac{\tan(\theta_i - \theta_t)}{\tan(\theta_i + \theta_t)}
$$
$$
R = \frac{1}{2}\left( r_s^2 + r_p^2 \right) < 1.0
$$

The transmitted leakage power fraction into the air is:

$$
T_{\text{leak}} = 1 - R > 0
$$

#### 2. In the Total Internal Reflection Regime ($\theta_i \ge \theta_c$):
$$
R_{\text{TIR}} = 1.000, \quad T_{\text{leak}} = 0.000
$$

#### Cumulative Power Transmission Along the Jet:
For a ray executing $N$ sequential bounces inside the water stream with incident angles $\{\theta_{i,1}, \theta_{i,2}, \dots, \theta_{i,N}\}$:

$$
P_{\text{out}} = P_{\text{in}} \prod_{k=1}^N R(\theta_{i,k})
$$

When all bounces satisfy the TIR condition $\theta_{i,k} \ge \theta_c$, $P_{\text{out}} = P_{\text{in}}$ (100% optical power preserved from nozzle to catch basin).

---

## 3. Physical Parameters & System Constants

| Parameter | Symbol | Nominal Value | Range | SI Units | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| Liquid Refractive Index | $n_{\text{liquid}}$ | `1.333` (Water) | `1.15 .. 1.55` | Dimensionless | Phase velocity ratio $c / v$ in liquid |
| Ambient Air Index | $n_{\text{air}}$ | `1.000` | Fixed | Dimensionless | Optical index of ambient air |
| Water Exit Velocity | $v_0$ | `4.2` | `2.0 .. 6.0` | $\text{m/s}$ | Initial horizontal discharge velocity |
| Laser Injection Angle | $\alpha_{\text{laser}}$ | `+3.0` | `-20.0 .. +20.0` | $\text{deg}$ | Tilt angle relative to horizontal nozzle |
| Critical Angle (Water) | $\theta_c$ | `48.61` | `40.2 .. 60.4` | $\text{deg}$ | Minimum incidence angle for TIR |
| Gravitational Acceleration | $g$ | `9.81` | Fixed | $\text{m/s}^2$ | Uniform downward acceleration |
| Laser Wavelength (Red) | $\lambda$ | `650` | `405 .. 650` | $\text{nm}$ | Ruby diode wavelength |
| Stream Nozzle Diameter | $d_0$ | `0.015` | Fixed | $\text{m}$ | Orifice hydraulic diameter |
| Max Guidance Curvature | $\kappa_{\text{max}}$ | `0.085` | Fixed | $\text{m}^{-1}$ | Maximum curvature before leakage |

---

## 4. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`LaserFountainExperiment`](./LaserFountainExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week5.Day29`
- **Registry Integration:** `ExperimentScreenRegistry.screens["laser_light_fountain"]`
- **Model Definition:** `Experiment.kt` (`id = "laser_light_fountain"`, `day = 29`, `category = WEEK_5_OPTICS`)

### Reactive State Variables
The interactive optical state is managed via Compose `mutableStateOf` variables:

| State Variable | Type | Initialization | Functional Role |
| :--- | :--- | :--- | :--- |
| `nLiquid` | `Float` | `1.333f` | Liquid refractive index (Water $1.333$, Glycerol $1.473$) |
| `streamSpeed` | `Float` | `4.2f` | Initial water jet exit speed ($2.0 .. 6.0\text{ m/s}$) |
| `laserAngleDeg` | `Float` | `3.0f` | Laser diode injection angle relative to nozzle |
| `selectedColor` | `LaserColor` | `RUBY_RED` | Laser wavelength & signature neon color |
| `selectedPreset` | `FountainPreset?`| `PERFECT_GUIDE`| Active experimental preset chip |
| `showNormals` | `Boolean` | `true` | Toggles normal vectors and critical angle indicators |
| `isRunning` | `Boolean` | `true` | Fluid flow animation clock toggle |
| `simTime` | `Float` | `0f` | Elapsed continuous simulation clock |

### High-Performance Ray Tracing Engine
On every frame, the simulation calculates the parabolic boundaries of the water jet using 90 sample slices and performs multi-bounce geometric ray-boundary intersection:
1. **Ray Boundary Intersection:**  
   Computes exact segment collisions between the ray trajectory $(x + t \cos\alpha, y + t \sin\alpha)$ and boundary points.
2. **Local Tangent & Normal:**  
   Evaluates inward unit normal $\vec{N}$ to the boundary curve.
3. **Snell Criterion:**  
   Computes $\theta_i = \arccos(|\vec{d} \cdot \vec{N}|)$.
4. **Specular Reflection Vector:**  
   Calculates reflected direction $\vec{d}' = \vec{d} - 2(\vec{d}\cdot\vec{N})\vec{N}$.
5. **Escape Ray Emission:**  
   If $\theta_i < \theta_c$, generates escaping refracted ray and attenuates internal beam intensity.

### Visual Polish & Layer Hierarchy
- **Acrylic Supply Reservoir:** Clear tank with animated liquid meniscus and volumetric measurement scale.
- **Translucent Arcing Jet:** Smooth gradient body with crisp neon upper and lower boundary edges.
- **Dynamic Fluid Bubbles:** 28 micro-bubbles moving along the parabolic path to visualize fluid motion.
- **Laser Emitter Assembly:** Rotating collimator housing showing actual injection angle.
- **Reflection Sparks:** Bright glowing spark points rendered at every internal reflection node.
- **Catch Basin Pool:** Collects light at the bottom, illuminating the water pool with the trapped laser color.

---

## 5. Suggested Investigations & Parameter Experiments

1. **The Critical Angle Threshold Test:**  
   Set stream speed to 4.2 m/s. Slowly increase the laser injection angle from $0^\circ$ toward $15^\circ$. Watch the reflection points: at a specific tilt, the angle of incidence drops below $48.8^\circ$, and bright escaping leakage beams suddenly radiate outward!
2. **The High-Index Glycerol Advantage:**  
   Select the `Glycerol (n=1.47)` preset. Observe how the critical angle drops from $48.8^\circ$ down to $42.8^\circ$, allowing the water stream to guide much steeper laser angles without leaking.
3. **The Minimum Bend Radius Leakage:**  
   Select `Bending Leakage (2.7 m/s)`. Lower water velocity increases gravitational curvature. Observe how light stays guided near the nozzle but escapes midway down the arc where stream curvature peaks!
4. **Wavelength Switching:**  
   Tap the canvas or use color chips to switch from Ruby Red (650 nm) to Emerald Green (532 nm) or Cyan Blue (488 nm), observing consistent geometric optics ray tracing across the spectrum.
