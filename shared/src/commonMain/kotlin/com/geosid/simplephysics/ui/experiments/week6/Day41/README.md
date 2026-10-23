# Day 41: Capillary Action & Meniscus Rise

> **Week 6: Fluid Dynamics • Laminar Flow, Aerodynamics & Pressure Gradients**  
> *Topic Subtitle: Adhesion vs. Cohesion, Laplace Pressure & Jurin's Law*

---

## 1. Physical Phenomenon & Intuitive Background

### Defying Gravity with Molecules
If you dip the tip of an open glass straw into water, the water doesn't merely level out with the reservoir: it spontaneously climbs upward against the relentless pull of Earth's gravity. The narrower the straw, the higher the water climbs, reaching meters in microscopic tubes without any pump or external energy source.

This phenomenon is **Capillary Action** (capillarity), discovered by Leonardo da Vinci in 1490 and mathematically formulated by James Jurin in 1718 and Pierre-Simon Laplace in 1805.

### Scientific Principles & Mechanism
1. **Adhesion vs. Cohesion:**
   - **Cohesive Forces:** Attractive intermolecular forces between like molecules within the bulk liquid (e.g., hydrogen bonds in water, metallic bonds in mercury).
   - **Adhesive Forces:** Attractive electrostatic interactions between liquid molecules and the solid surface of the tube (e.g., polar silanol groups in glass).
2. **Contact Angle ($\theta$) & Wetting:**
   - When adhesion exceeds cohesion ($\theta < 90^\circ$), the liquid wets the solid surface, forming a **concave meniscus** that curves upward along the glass perimeter.
   - When cohesion exceeds adhesion ($\theta > 90^\circ$, like mercury on glass), the liquid is non-wetting, forming a **convex dome** that depresses below the reservoir level (**capillary depression**).
3. **The Young-Laplace Pressure Deficit ($\Delta P$):** Because surface tension acts like an elastic skin under tension, a curved interface produces a pressure jump across the boundary:
   $$\Delta P = \frac{2 \gamma \cos\theta}{r}$$
   Beneath a concave meniscus ($\cos\theta > 0$), the static fluid pressure is lower than ambient atmospheric pressure ($P < P_{\text{atm}}$). This negative gauge pressure literally pulls the liquid column upward until the hydrostatic weight of the suspended column perfectly balances the pressure deficit.
4. **Jurin's Law ($h \propto 1/r$):** Balancing the vertical component of surface tension around the wetted perimeter against the downward gravitational weight gives:
   $$F_{\text{cap}} = 2 \pi r \gamma \cos\theta = \rho (\pi r^2 h) g \implies h = \frac{2 \gamma \cos\theta}{\rho g r}$$

### Laboratory / Kitchen Protocol (Try It At Home)
1. Fill a small glass with water and add a few drops of food coloring.
2. Cut a narrow strip of white paper towel or coffee filter ($1\text{ cm} \times 15\text{ cm}$).
3. Lower the bottom tip of the strip into the colored water, letting it hang vertically:
4. *Observation:* Colored water rapidly climbs upward through the microscopic cellulose fiber pores against gravity, demonstrating capillary imbibition in porous media!

---

## 2. Mathematical Foundation & Governing Equations

### 1. Young-Laplace Interface Condition
For a spherical meniscus of radius of curvature $R = \frac{r}{\cos\theta}$:

$$
\Delta P = P_{\text{vapor}} - P_{\text{liquid}} = \frac{2\gamma}{R} = \frac{2\gamma \cos\theta}{r}
$$

Where:
- $\gamma$ is fluid surface tension ($\text{N/m}$)
- $\theta$ is contact angle between liquid and solid
- $r$ is tube inner radius ($\text{m}$)

### 2. Jurin's Law (Equilibrium Column Height)
At static equilibrium, the Laplace suction pressure balances hydrostatic head $\rho g h$:

$$
\Delta P = \rho g h \implies h = \frac{2\gamma \cos\theta}{\rho g r}
$$

Alternatively, equating upward capillary perimeter force to downward column weight:

$$
F_{\text{up}} = 2\pi r \gamma \cos\theta
$$

$$
W_{\text{down}} = m g = \rho (\pi r^2 h) g
$$

$$
2\pi r \gamma \cos\theta = \rho \pi r^2 g h \implies h = \frac{2\gamma \cos\theta}{\rho g r}
$$

### 3. Dynamic Climbing Rate (Lucas-Washburn Equation)
During early capillary penetration where viscous Poiseuille flow resists upward motion:

$$
\frac{dh}{dt} = \frac{r^2}{8\mu h} \left(\frac{2\gamma \cos\theta}{r} - \rho g h\right)
$$

Neglecting gravity at small times ($h \ll h_{\text{eq}}$):

$$
h(t) = \sqrt{\frac{\gamma r \cos\theta}{2\mu}} \cdot \sqrt{t}
$$

The fluid climbs rapidly proportional to $\sqrt{t}$, before slowing exponentially as it nears Jurin's equilibrium height.

### 4. Capillary Depression (Non-Wetting Liquids)
For contact angles $\theta > 90^\circ$ (such as mercury on glass, $\theta \approx 138^\circ$):

$$
\cos\theta < 0 \implies h < 0
$$

The meniscus forms a convex dome, and the liquid inside the capillary is depressed below the external reservoir surface by:

$$
\Delta h_{\text{depression}} = \frac{2\gamma |\cos\theta|}{\rho g r}
$$

### 5. SI Parameter Reference Table

| Symbol | Parameter Description | Nominal SI Value | Units |
| :--- | :--- | :--- | :--- |
| $\gamma_{\text{water}}$ | Pure water surface tension ($20^\circ\text{C}$) | $0.0728$ | $\text{N/m}$ ($\text{mN/m}$) |
| $\gamma_{\text{soap}}$ | Water with surfactant | $0.0280$ | $\text{N/m}$ ($\text{mN/m}$) |
| $\gamma_{\text{ethanol}}$ | Pure ethanol surface tension | $0.0223$ | $\text{N/m}$ ($\text{mN/m}$) |
| $\gamma_{\text{mercury}}$ | Liquid mercury surface tension | $0.4860$ | $\text{N/m}$ ($\text{mN/m}$) |
| $\theta$ | Contact angle (water/glass) | $0^\circ\text{--}20^\circ$ | degrees |
| $\theta_{\text{Hg}}$ | Contact angle (mercury/glass) | $138^\circ$ | degrees |
| $r$ | Capillary tube inner radius | $0.10\text{--}2.50 \times 10^{-3}$ | $\text{m}$ |
| $\rho_{\text{water}}$ | Water density | $1000$ | $\text{kg/m}^3$ |
| $\rho_{\text{Hg}}$ | Mercury density | $13,546$ | $\text{kg/m}^3$ |
| $h$ | Equilibrium capillary height | $0.005\text{--}0.150$ | $\text{m}$ |
| $\Delta P$ | Laplace pressure difference | $50\text{--}1500$ | $\text{Pa}$ |
| $g$ | Standard acceleration of gravity | $9.807$ | $\text{m/s}^2$ |

---

## 3. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`CapillaryActionExperiment`](./CapillaryActionExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week6.Day41`
- **Registry ID:** `"capillary_action"`
- **Architecture:** Compose Multiplatform with Canvas rendering, real-time meniscus geometry, Jurin's law integration, and interactive parameter controls.

### Presets Implemented
1. **Water & Glass:** High surface tension ($\gamma = 72.8\text{ mN/m}$) and zero contact angle ($\theta = 0^\circ$), showcasing tall concave water columns.
2. **Soapy Water:** Surfactant disrupts intermolecular cohesion, cutting surface tension by $60\%$ and drastically reducing capillary climb.
3. **Ethanol:** Low density ($\rho = 789\text{ kg/m}^3$) compensating for lower surface tension, demonstrating rapid wetting.
4. **Mercury (Capillary Depression):** Contact angle $\theta = 138^\circ$ produces convex dome depression below the basin surface.
5. **Tree Xylem:** Microscopic botanical capillary radius ($r = 0.15\text{ mm}$), illustrating immense capillary suction aiding tree transpiration.

### Reactive State Variables
- `surfaceTensionMNm`: Fluid surface tension ($20\text{--}500\text{ mN/m}$).
- `contactAngleDeg`: Contact angle $\theta$ ($0^\circ\text{--}150^\circ$).
- `densityKgM3`: Fluid mass density $\rho$ ($700\text{--}14,000\text{ kg/m}^3$).
- `primaryRadiusMm`: Inner tube radius $r$ ($0.10\text{--}2.50\text{ mm}$).
- `showMultiTubes`: Toggles multi-tube array showing the $1/r$ hyperbolic envelope.
- `showForceVectors`: Renders perimeter surface tension vector arrows along contact angle $\theta$.
- `showLaplaceDeficit`: Highlights Laplace pressure jump across the meniscus.

---

## 4. Suggested Investigations & Parameter Experiments
1. **Hyperbolic Radius Scaling ($1/r$):** Reduce tube radius by half (e.g. from $1.0\text{ mm}$ to $0.5\text{ mm}$). Verify that the equilibrium height $h$ exactly doubles ($2\times$), confirming Jurin's inverse proportionality law.
2. **Wetting Transition ($\theta = 90^\circ$):** Sweep the contact angle slider from $0^\circ$ toward $90^\circ$. Observe how the meniscus flattens and capillary rise vanishes at $\theta = 90^\circ$, then flips into capillary depression for $\theta > 90^\circ$.
3. **Surfactant Depression:** Compare Water vs. Soapy Water. Observe how adding surfactant weakens cohesive surface forces, pulling fluid to a much lower height despite identical tube geometry.
