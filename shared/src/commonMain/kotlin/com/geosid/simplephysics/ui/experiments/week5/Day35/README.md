# Day 35: Atmospheric Mirage & Curved Rays

> **Week 5: Optics & Light • Refraction, Total Internal Reflection & Diffraction**  
> *Topic Subtitle: Continuous Refractive Index Gradients in Hot Air*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Simulate highway desert mirages where shimmering pools of water appear on bone-dry scorching asphalt, and polar ocean superior mirages where distant ships float high above the horizon!

### Scientific Principles & Mechanism
We are accustomed to the idea that "light travels in straight lines." However, this holds true only in **homogeneous media** where the refractive index $n$ is strictly uniform. When light propagates through a medium with continuous temperature or density variations, its trajectory continuously curves according to **Fermat's Principle of Least Time**.

1. **Inferior Mirage (Desert Highway Asphalt):**  
   Intense solar radiation superheats road asphalt to $50^\circ\text{C}-70^\circ\text{C}$. Through thermal conduction, a thin boundary layer of air directly above the road becomes intensely hot ($T \approx 60^\circ\text{C}$), while air higher up remains much cooler ($T \approx 25^\circ\text{C}$).  
   By the ideal gas law ($P = \rho R T / M$), heated air expands, reducing its density $\rho$. By the **Gladstone-Dale relation**, the refractive index of air scales directly with density:
   $$n - 1 = k \rho \propto \frac{P}{T}$$
   Therefore, air directly touching the road has a **lower refractive index** ($n_{\text{ground}} \approx 1.000260$) than cooler air aloft ($n_{\text{air}} \approx 1.000293$).  
   As light rays from the sky or a distant tree travel downward toward the road, they encounter layers of progressively lower refractive index. Light travels faster in lower-$n$ air, so the lower wavefront of the light beam accelerates relative to the upper wavefront. This differential velocity bends the ray smoothly upward ($\frac{d^2y}{dx^2} > 0$).  
   When the ray reaches grazing incidence, it curves smoothly back upward into the observer's eyes. Because the human visual system extrapolates rays straight backward along the line of arrival, the observer perceives an inverted image of the sky or distant tree **below the horizon**, appearing identical to a reflective puddle of water!

2. **Superior Mirage & Looming (Cold Polar Seas):**  
   Over cold polar oceans or ice floes, air directly touching the surface is chilled while warmer air flows aloft—a **temperature inversion**. In this case, density and refractive index are highest at the water's surface and decrease with altitude ($\frac{dn}{dy} < 0$). Light rays curve downward toward the ground. Light from ships below the geometric horizon bends over Earth's curvature into the observer's eye, projecting a virtual image floating suspended in mid-air.

3. **Fata Morgana:**  
   When multiple alternating thermal layers form (complex atmospheric stratification with sharp inflection points), rays undergo ducting, producing alternating compressed, stretched, upright, and inverted images that resemble towering castles and mountains.

4. **Graded-Index Waveguides (GRIN Fiber):**  
   In fiber optics, engineered parabolic refractive index profiles $n(r) = n_0 \sqrt{1 - (r/a)^2}$ continually refocus divergent light rays, causing harmonic sinusoidal ray oscillation without sharp boundary reflections.

### Laboratory & Kitchen Protocol (Try It At Home)
> 1. Preheat an iron baking sheet in a hot oven ($220^\circ\text{C}$ or $425^\circ\text{F}$) for 20 minutes.  
> 2. Carefully place the baking sheet on a level heat-resistant counter.  
> 3. Crouch down so your eye is level with the plane of the hot metal surface.  
> 4. Look along the surface at a small object or patterned wall on the far side: observe the shimmering distorted horizon line and the faint inverted reflection skimming the surface!

---

## 2. Mathematical Foundation & The Three Pillars

### Pillar 1: Governing Law & Fermat's Principle (The Continuous Snell Invariant)
Fermat's Principle of Least Time states that light travels along paths for which the optical path length (OPL) is stationary:

$$\delta \int_{A}^{B} n(\mathbf{r}) \, ds = 0$$

In a vertically stratified atmosphere where the refractive index depends only on altitude $y$, $n = n(y)$. By translational symmetry along the horizontal $x$-axis, Noether's theorem dictates that the horizontal optical momentum is an exact invariant along the ray path. This yields **Continuous Snell's Law**:

$$n(y) \cos\theta(y) = n(y_0) \cos\theta_0 = C = \text{constant}$$

where:
- $\theta(y)$ is the ray trajectory angle relative to the horizontal.
- $\theta_0$ is the initial launch angle at altitude $y_0$.
- $C$ is the Snell invariant for that specific ray.

At the **turning point** (the lowest point of the curve where $\theta = 0$ and the ray becomes horizontal):
$$n(y_{\text{turn}}) = C = n(y_0) \cos\theta_0$$

If $C > n_{\text{ground}}$, the ray reaches this turning point at altitude $y_{\text{turn}} > 0$ and smoothly curves upward away from the asphalt without touching the ground!

---

### Pillar 2: Kinematics / Motion Constraint (Ray Curvature Differential Equation)
From the Euler-Lagrange equations applied to the optical Lagrangian $L = n(y) \sqrt{1 + y'^2}$:

$$\frac{d}{dx} \left( \frac{\partial L}{\partial y'} \right) = \frac{\partial L}{\partial y}$$

Substituting $y' = \frac{dy}{dx} = \tan\theta$:

$$\frac{d^2 y}{dx^2} = \frac{1}{n(y)} \frac{dn}{dy} \left[ 1 + \left( \frac{dy}{dx} \right)^2 \right]$$

For grazing rays with small angles ($\left| \frac{dy}{dx} \right| \ll 1$), this simplifies to the canonical ray curvature equation:

$$\frac{d^2 y}{dx^2} \approx \frac{1}{n} \frac{dn}{dy}$$

1. **Local Radius of Curvature ($R$):**  
   The local radius of curvature of the light ray is:
   $$R = \frac{1}{\kappa} = \frac{n(y)}{|\nabla n|} \approx \frac{1}{|\frac{dn}{dy}|}$$
   - When $\frac{dn}{dy} > 0$ (hot ground), rays bend upward ($\frac{d^2y}{dx^2} > 0$, concave up).
   - When $\frac{dn}{dy} < 0$ (cold sea), rays bend downward ($\frac{d^2y}{dx^2} < 0$, concave down).

2. **Step-by-Step Path Integration:**  
   Along arc length $s$, with $\frac{dx}{ds} = \cos\theta$ and $\frac{dy}{ds} = -\sin\theta$:
   $$\frac{d\theta}{ds} = \frac{1}{n(y)} \frac{dn}{dy} \cos\theta$$

---

### Pillar 3: Energy / Work Conservation & Atmospheric Thermodynamics
Light propagation conserves energy flux across the ray bundle. The atmospheric refractivity is coupled to thermal conduction and gas thermodynamics:

1. **Gladstone-Dale Law:**  
   For atmospheric gases at optical frequencies:
   $$n(y) - 1 = k \rho(y)$$
   where $k \approx 0.226 \times 10^{-3} \text{ m}^3/\text{kg}$ is the Gladstone-Dale constant for dry air.

2. **Ideal Gas State Equation:**  
   At atmospheric pressure $P_0$:
   $$\rho(y) = \frac{P_0 M}{R_{\text{gas}} T(y)} \implies n(y) = 1 + (n_0 - 1) \frac{T_0}{T(y)}$$
   where $T_0 = 288.15\text{ K}$ and $n_0 = 1.000293$.

3. **Thermal Boundary Layer Profile:**  
   Convective and conductive heat transfer from the asphalt creates an exponential temperature boundary layer of height $h_{\text{layer}}$:
   $$T(y) = T_{\text{air}} + (T_{\text{ground}} - T_{\text{air}}) e^{-y / h_{\text{layer}}}$$
   Differentiating with respect to $y$:
   $$\frac{dn}{dy} = \frac{dn}{dT} \cdot \frac{dT}{dy} = \left[ -(n_0 - 1) \frac{T_0}{T^2} \right] \cdot \left[ -\frac{T_{\text{ground}} - T_{\text{air}}}{h_{\text{layer}}} e^{-y / h_{\text{layer}}} \right]$$
   Since $T_{\text{ground}} > T_{\text{air}}$, $\frac{dn}{dy} > 0$, confirming that light rays curve upward away from the hot asphalt!

---

## 3. Physical Parameters & SI Units

| Symbol | Parameter Name | Default Simulation Value | SI Unit | Physical Role |
| :---: | :--- | :---: | :---: | :--- |
| $T_{\text{ground}}$ | Surface Asphalt Temp | $58.0$ | $^\circ\text{C}$ ($331.15\text{ K}$) | Temperature of heat-radiating ground boundary |
| $T_{\text{air}}$ | Ambient Air Temp | $26.0$ | $^\circ\text{C}$ ($299.15\text{ K}$) | Far-field atmospheric ambient air temperature |
| $h_{\text{layer}}$ | Boundary Layer Height | $0.8$ | $\text{m}$ | Characteristic exponential decay height of thermal gradient |
| $n_0$ | Reference Refractive Index | $1.000293$ | Dimensionless | Refractive index of standard dry air at $15^\circ\text{C}$ |
| $\Delta n$ | Total Index Contrast | $\approx 3.2 \times 10^{-5}$ | Dimensionless | Difference in refractive index across boundary layer |
| $\frac{dn}{dy}$ | Index Gradient | $\sim 4.0 \times 10^{-5}$ | $\text{m}^{-1}$ | Vertical gradient governing local ray curvature |
| $R$ | Radius of Ray Curvature | $\approx 25,000$ | $\text{m}$ | Local radius of circular arc followed by grazing rays |
| $\theta_0$ | Launch Ray Angle | $-2.8^\circ$ | $\text{deg}$ | Initial ray direction relative to horizontal |
| $\theta_{\text{crit}}$ | Critical Grazing Angle | $\approx 0.46^\circ$ | $\text{deg}$ | Maximum grazing angle for total refraction turning |

---

## 4. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`AtmosphericMirageExperiment`](./AtmosphericMirageExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week5.Day35`
- **Container:** `ResponsiveExperimentContainer` with transparent telemetry card and compact control deck.

### Optical Ray Tracing Pipeline
1. **Stratified Atmosphere Generation:** Evaluates temperature and refractive index profiles $n(y)$ and $\frac{dn}{dy}$ at every altitude.
2. **Ray Stepper:** Integrates ray trajectory points using $\Delta s$ steps, applying $\frac{d\theta}{ds} = \frac{1}{n} \frac{dn}{dy} \cos\theta$.
3. **Turning Point Detection:** Identifies the precise Fermat grazing point where vertical velocity passes through zero.
4. **Virtual Ray Projection:** Extrapolates tangent vectors from the observer's arrival angle backwards, illustrating where the brain perceives the inverted mirage image.
5. **Interactive Controls:** Presets for Desert Inferior Mirage, Arctic Superior Mirage, Fata Morgana, GRIN Fiber, and Laser Probe.
