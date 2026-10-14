# Day 38: Marangoni Surface Tension Boat

> **Week 6: Fluid Dynamics • Laminar Flow, Aerodynamics & Pressure Gradients**  
> *Topic Subtitle: Surface Tension Gradients, Capillary Thrust & Monolayer Spreading*

---

## 1. Physical Phenomenon & Intuitive Background

### The Kitchen Rocket Engine
If you place a lightweight paper or card boat onto still water, it rests in static equilibrium. The high surface tension of pure water ($\gamma \approx 72.8\text{ mN/m}$) pulls uniformly on every millimeter of its perimeter. 

However, if you place a single drop of liquid dish soap into a rear notch cut into the stern, the boat instantly rockets forward across the basin without any external mechanical propulsion or moving parts.

### Scientific Principles & Mechanism
1. **Surfactant Molecular Dynamics:** Liquid soap and dish detergents are surfactants consisting of amphiphilic molecules (a hydrophilic polar head and a hydrophobic hydrocarbon tail). When added to water, the hydrophobic tails are driven to the air-water interface, disrupting the cohesive hydrogen bonding network between adjacent water molecules.
2. **Surface Tension Gradient ($\nabla\gamma$):** The soap lowers the local surface tension at the stern to $\approx 25\text{ mN/m}$, while undisturbed pure water ahead of the bow maintains $\approx 72.8\text{ mN/m}$.
3. **The Marangoni Effect:** Fluid naturally flows from regions of low surface tension toward regions of high surface tension. Because the fluid at the rear has lower surface tension than the clean water ahead, a powerful net forward capillary force pulls the boat forward, while soap molecules spread rapidly backward across the water surface in an insoluble monolayer.
4. **Finite Lifetime & Saturation:** As the surfactant monolayer spreads across the basin, it eventually coats the entire water surface, reducing the surface tension everywhere and eliminating the gradient ($\Delta\gamma \to 0$).

### Laboratory / Kitchen Protocol (Try It At Home)
1. Cut a small wedge or triangle ($3\text{--}5\text{ cm}$ long) out of an index card, cardboard, or foam plate.
2. Cut a small notch or hole at the rear stern with a narrow exit slot.
3. Float the boat in a wide dish or baking tray of clean tap water.
4. Dip a toothpick in liquid dish soap and lightly touch the water inside the rear notch.
5. *Observation:* The boat surges forward. Subsequent attempts require washing and refilling the tray with fresh water because surfactant residue coats the surface.

---

## 2. Mathematical Foundation & Governing Equations

### 1. Marangoni Interfacial Shear Stress
At the boundary between water and air, spatial variations in chemical surfactant concentration $c$ or temperature $T$ produce a gradient in surface tension $\gamma$:

$$
\tau = \frac{d\gamma}{dx} = \left(\frac{\partial \gamma}{\partial c}\right) \frac{\partial c}{\partial x} + \left(\frac{\partial \gamma}{\partial T}\right) \frac{\partial T}{\partial x}
$$

By Newton's law of viscosity at the free surface ($z = 0$):

$$
\tau = \mu \left.\frac{\partial u}{\partial z}\right|_{z = 0} = \frac{d\gamma}{dx}
$$

This interfacial shear stress pumps fluid along the surface from low-tension regions to high-tension regions, transporting momentum into the bulk subphase.

### 2. Net Capillary Thrust on a Floating Hull
For a boat of transverse waterline width $w$, the net capillary pulling force $F_{\text{net}}$ is the contour integral of surface tension along the hull boundary:

$$
F_{\text{net}} = \oint \gamma (\mathbf{s}) \, (\hat{\mathbf{n}} \cdot \hat{\mathbf{x}}) \, ds = (\gamma_{\text{bow}} - \gamma_{\text{stern}}) \cdot w = \Delta\gamma \cdot w
$$

Where:
- $\gamma_{\text{bow}} = \gamma_{\text{water}} \approx 0.0728\text{ N/m}$
- $\gamma_{\text{stern}} = \gamma_{\text{surfactant}} \approx 0.0250\text{ N/m}$
- $\Delta\gamma = \gamma_{\text{bow}} - \gamma_{\text{stern}} \approx 0.0478\text{ N/m}$
- $w$ is the effective rear beam width ($\approx 0.02\text{--}0.03\text{ m}$)

### 3. Hydrodynamic Drag & Terminal Glide Velocity
As the boat accelerates through the water, it experiences form drag (turbulent boundary layer separation) and skin friction drag (viscous shear):

$$
F_{\text{drag}} = \frac{1}{2} C_d \rho A v^2 + \mu_{\text{visc}} v
$$

Where:
- $\rho \approx 1000\text{ kg/m}^3$ is water density
- $C_d \approx 0.85$ is the drag coefficient of the shallow bluff hull
- $A = w \cdot d_{\text{draft}}$ is the submerged frontal cross-sectional area
- $v$ is forward glide speed

Setting $F_{\text{net}} = F_{\text{drag}}$ gives the asymptotic terminal velocity:

$$
v_{\text{term}} \approx \sqrt{\frac{\Delta\gamma \cdot w}{\frac{1}{2} C_d \rho A}}
$$

For typical experimental values ($w = 2.5\text{ cm}, d_{\text{draft}} = 2\text{ mm}, \Delta\gamma = 47.8\text{ mN/m}$):

$$
v_{\text{term}} \approx \sqrt{\frac{0.0478 \times 0.025}{0.5 \times 1000 \times 0.85 \times (0.025 \times 0.002)}} \approx 0.237\text{ m/s} \approx 24\text{ cm/s}
$$

### 4. Surfactant Monolayer Radial Spreading
Surfactants deposited on clean water spread as a 2D monolayer driven by Marangoni stress, advancing radially according to:

$$
r(t) = r_0 + k \cdot t^{3/4}
$$

As the monolayer expands to cover pool area $A_{\text{pool}}$, surface saturation $S(t)$ degrades available tension differential:

$$
\Delta\gamma_{\text{eff}}(t) = \Delta\gamma_0 \cdot \left(1 - \frac{r(t)}{R_{\text{pool}}}\right)
$$

### 5. SI Parameter Reference Table

| Symbol | Parameter Description | Nominal SI Value | Units |
| :--- | :--- | :--- | :--- |
| $\gamma_{\text{water}}$ | Pure water surface tension ($20^\circ\text{C}$) | $0.0728$ | $\text{N/m}$ ($\text{mN/m}$) |
| $\gamma_{\text{soap}}$ | Dish soap surfactant surface tension | $0.0250$ | $\text{N/m}$ ($\text{mN/m}$) |
| $\gamma_{\text{alcohol}}$ | Isopropyl / ethanol surface tension | $0.0220$ | $\text{N/m}$ ($\text{mN/m}$) |
| $\gamma_{\text{camphor}}$ | Camphor-saturated water surface tension | $0.0400$ | $\text{N/m}$ ($\text{mN/m}$) |
| $\Delta\gamma$ | Net surface tension differential | $0.010\text{--}0.050$ | $\text{N/m}$ |
| $w$ | Hull transverse beam width | $0.015\text{--}0.035$ | $\text{m}$ |
| $m$ | Paper boat dry mass | $0.2\text{--}0.5 \times 10^{-3}$ | $\text{kg}$ |
| $\rho$ | Fluid bulk density (water) | $1000$ | $\text{kg/m}^3$ |
| $C_d$ | Bluff hull form drag coefficient | $0.85$ | dimensionless |
| $v_{\text{term}}$ | Terminal glide velocity | $0.15\text{--}0.35$ | $\text{m/s}$ |

---

## 3. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`MarangoniPropulsionExperiment`](./MarangoniPropulsionExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week6.Day38`
- **Registry ID:** `"marangoni_propulsion"`
- **Architecture:** Compose Multiplatform with Canvas rendering, real-time `withFrameNanos` integration, and interactive physical parameter tuning.

### Presets Implemented
1. **Dish Detergent Rocket:** High surface tension gradient ($\Delta\gamma = 47.8\text{ mN/m}$) with rapid acceleration and expanding surfactant wake.
2. **Rubbing Alcohol Sprint:** Highly volatile alcohol producing sharp initial acceleration with rapid evaporative recovery.
3. **Camphor Dance:** Continuous asymmetric dissolution creating erratic rotational oscillations and persistent chaotic darting.
4. **Olive Oil Cruise:** Moderate surface tension depression ($\Delta\gamma = 40.8\text{ mN/m}$) producing a steady, smooth gliding velocity.
5. **Tears of Wine:** Demonstrates the vertical Marangoni effect where alcohol evaporation up a wine glass wall pulls fluid upward into falling droplets.

### Reactive State Variables
- `waterTensionMNm`: Pure solvent surface tension ($72.8\text{ mN/m}$).
- `surfactantTensionMNm`: Injected surfactant tension ($20\text{--}50\text{ mN/m}$).
- `boatWidthCm`: Hull width affecting capillary force scaling.
- `boatMassGrams`: Inertial mass governing acceleration $a = (F_{\text{net}} - F_{\text{drag}})/m$.
- `showSurfaceTensionGradient`: Toggles surface energy color heatmap.
- `showMarangoniStreamlines`: Toggles backward surfactant monolayer dispersion lines.
- `showForceVectors`: Renders forward $F_{\text{net}}$ and opposing $F_{\text{drag}}$ vector arrows.

### Frame-Loop Numerical Integration
Driven by `withFrameNanos`, calculating per-frame time step $\Delta t$:
```kotlin
// 1. Marangoni capillary thrust from surface tension gradient
val deltaGamma = (waterTensionMNm - surfactantTensionMNm).coerceAtLeast(0f) * 1e-3f // N/m
val rawCapillaryForce = deltaGamma * boatWidthM
val activeCapillaryForce = rawCapillaryForce * saturationFactor

// 2. Hydrodynamic drag force
val dragForce = 0.5f * waterDensity * dragCoeff * submergedAreaM2 * boatSpeedMs * boatSpeedMs +
        viscousShearCoeff * boatSpeedMs

// 3. Newton's 2nd Law acceleration
val acceleration = (activeCapillaryForce - dragForce) / boatMassKg
boatSpeedMs = (boatSpeedMs + acceleration * dt).coerceAtLeast(0f)
```

---

## 4. Suggested Investigations & Parameter Experiments
1. **Width Scaling:** Increase boat beam width $w$ from $1.5\text{ cm}$ to $4.0\text{ cm}$. Observe how capillary thrust scales linearly with perimeter width while terminal speed scales with $\sqrt{w/A}$.
2. **Surface Contamination Limit:** Compare pristine water launch with repeated launches in a contaminated pool to verify the Marangoni saturation threshold.
3. **Camphor Self-Oscillation:** Switch to the Camphor Dance preset to observe non-equilibrium steady-state limit cycles driven by asymmetric crystal dissolution.
