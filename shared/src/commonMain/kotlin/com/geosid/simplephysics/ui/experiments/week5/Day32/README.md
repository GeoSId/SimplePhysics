# Day 32: Prism Dispersion & Newton's Rainbow

> **Week 5: Optics & Light • Refraction, Total Internal Reflection & Diffraction**  
> *Topic Subtitle: Wavelength-Dependent Refraction (Chromatic Dispersion)*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Split pure white light into a rainbow spectrum through an equilateral glass prism, demonstrating how chromatic dispersion separates composite light into its fundamental spectral wavelengths.

### Scientific Principles & Mechanism
In 1666, 23-year-old Isaac Newton carried out a series of groundbreaking optical experiments in a darkened room at Trinity College, Cambridge. Passing a narrow beam of sunlight through a triangular glass prism, he projected an oblong spectrum of seven vibrant colors (red, orange, yellow, green, cyan/blue, indigo, violet) onto the opposite wall. 

Crucially, Newton proved that the prism does **not** "color" or alter white light, but rather **reveals** its pre-existing composite nature. By isolating a single monochromatic hue with an aperture and passing it through a second prism, the color remained unchanged. Furthermore, recombining the dispersed rainbow rays through an inverted second prism reconstructed pure white light.

The fundamental physical principles governing prism dispersion are:

1. **Phase Velocity in Dense Media:**  
   In a vacuum, all electromagnetic waves travel at the universal speed of light $c \approx 3 \times 10^8 \text{ m/s}$. Inside a dielectric medium (such as optical glass), light interacts with bound electrons in the atoms. This atomic polarization slows down the propagation of the electromagnetic wavefront to a lower phase velocity:
   $$v(\lambda) = \frac{c}{n(\lambda)}$$

2. **Wavelength-Dependent Refractive Index (Chromatic Dispersion):**  
   Because the resonance frequencies of optical glass electrons lie in the ultraviolet spectrum, high-frequency, short-wavelength light (violet, $\lambda \approx 400 \text{ nm}$) interacts more strongly with the dielectric medium than low-frequency, long-wavelength light (red, $\lambda \approx 700 \text{ nm}$). Consequently, violet light travels slower through glass than red light, resulting in a higher refractive index:
   $$n_{\text{violet}} \approx 1.531 > n_{\text{red}} \approx 1.513$$

3. **Dual-Interface Refraction:**  
   As light passes across the first non-parallel face of the triangular prism, it bends toward the surface normal according to Snell's law. Because $n(\lambda)$ varies across the spectrum, each color refracts at a slightly different angle. When the rays reach the exit face, they bend away from the normal, amplifying the angular separation into a wide rainbow fan.

4. **Total Internal Reflection (TIR):**  
   At the second glass-to-air interface, if the internal incidence angle $\theta_{i2}$ exceeds the critical angle $\theta_c = \arcsin(1 / n)$, light cannot escape into the air and undergoes 100% reflection internally.

### Laboratory & Kitchen Protocol (Try It At Home)
> 1. Fill a clear rectangular or triangular glass tumbler with water and place it on a white tablecloth next to a sunny window.  
> 2. Position a sheet of white cardstock on the floor opposite the glass to catch the exiting refracted rays.  
> 3. Observe the brilliant rainbow band projected on the paper.  
> 4. Alternatively, shine a white LED smartphone flashlight through a small slit cut into black cardboard directly against the side of the glass to produce sharp, distinct spectral bands.

---

## 2. Mathematical Foundation & The Three Pillars

### Pillar 1: Governing Law & Dispersion Relation (Cauchy's Equation)
In transparent optical media well away from absorption bands, the wavelength dependence of the refractive index is modeled by **Cauchy's Dispersion Equation**:

$$n(\lambda) = A + \frac{B}{\lambda^2} + \frac{C}{\lambda^4}$$

where:
- $\lambda$ is the vacuum wavelength of light (expressed in micrometers, $\mu\text{m}$).
- $A$ is the dimensionless base refractive index of the medium in the infinite wavelength limit.
- $B$ is the dispersion coefficient (in $\mu\text{m}^2$), dictating the primary rate of angular separation.
- $C$ is the higher-order dispersion correction coefficient (in $\mu\text{m}^4$).

For standard borosilicate optical crown glass (N-BK7):
$$A \approx 1.5046, \quad B \approx 0.00420 \ \mu\text{m}^2, \quad C \approx 0.00015 \ \mu\text{m}^4$$

Evaluating at the visible spectral boundaries:
- **Deep Red ($\lambda = 700 \text{ nm} = 0.70 \ \mu\text{m}$):**
  $$n(0.70) = 1.5046 + \frac{0.00420}{0.49} + \frac{0.00015}{0.2401} \approx 1.513$$
- **Deep Violet ($\lambda = 400 \text{ nm} = 0.40 \ \mu\text{m}$):**
  $$n(0.40) = 1.5046 + \frac{0.00420}{0.16} + \frac{0.00015}{0.0256} \approx 1.531$$

---

### Pillar 2: Kinematics / Motion Constraint (Prism Geometry & Snell's Law)
Consider an equilateral or isosceles prism with apex angle $\alpha$ immersed in air ($n_{\text{air}} \approx 1.0$).

1. **Refraction at Face 1 (Air to Glass):**  
   A ray strikes the first face at angle of incidence $\theta_{i1}$ measured relative to the outward normal:
   $$\sin\theta_{i1} = n(\lambda) \sin\theta_{r1}(\lambda) \implies \theta_{r1}(\lambda) = \arcsin\left(\frac{\sin\theta_{i1}}{n(\lambda)}\right)$$

2. **Internal Apex Angle Constraint:**  
   From the geometry of the triangle formed by the prism apex and the refracted ray path:
   $$\theta_{i2}(\lambda) = \alpha - \theta_{r1}(\lambda)$$
   where $\theta_{i2}$ is the angle of incidence at the second (exit) face inside the glass.

3. **Refraction at Face 2 (Glass to Air):**  
   Applying Snell's law at the exit interface:
   $$n(\lambda) \sin\theta_{i2}(\lambda) = \sin\theta_{r2}(\lambda) \implies \theta_{r2}(\lambda) = \arcsin\left(n(\lambda) \sin\theta_{i2}(\lambda)\right)$$

4. **Net Angular Deviation ($\delta$):**  
   The total deviation angle between the incident ray and the emergent ray is:
   $$\delta(\lambda) = \theta_{i1} + \theta_{r2}(\lambda) - \alpha$$

5. **Angle of Minimum Deviation ($\delta_{\text{min}}$):**  
   When light traverses the prism symmetrically such that $\theta_{i1} = \theta_{r2}$ and $\theta_{r1} = \theta_{i2} = \alpha / 2$, the total deviation attains its absolute minimum:
   $$\sin\left(\frac{\delta_{\text{min}} + \alpha}{2}\right) = n \sin\left(\frac{\alpha}{2}\right) \implies \delta_{\text{min}} = 2 \arcsin\left(n \sin\frac{\alpha}{2}\right) - \alpha$$

---

### Pillar 3: Energy / Work Conservation & Total Internal Reflection (TIR)
At the exit boundary, energy transmission across the dielectric interface is governed by Fresnel's equations. As the internal angle of incidence $\theta_{i2}$ increases, transmission drops until reaching the **critical angle**:

$$\theta_c(\lambda) = \arcsin\left(\frac{1}{n(\lambda)}\right)$$

When:
$$\theta_{i2}(\lambda) \ge \theta_c(\lambda) \quad \left(\text{or } n(\lambda) \sin\theta_{i2}(\lambda) \ge 1.0\right)$$

All incident electromagnetic power is reflected back into the prism at angle $\theta_{\text{refl}} = \theta_{i2}$. No refracted wave propagates into the exterior air, creating an evanescent wave localized to the boundary.

---

## 3. Physical Parameters & SI Units

| Symbol | Parameter Name | Default Simulation Value | SI Unit | Physical Role |
| :---: | :--- | :---: | :---: | :--- |
| $\alpha$ | Prism Apex Angle | $60.0^\circ$ | $\text{rad}$ / $\text{deg}$ | Vertex angle between the two active optical refracting faces |
| $\theta_{i1}$ | Incident Angle | $49.5^\circ$ | $\text{rad}$ / $\text{deg}$ | Angle between incoming incident light and the Face 1 normal |
| $A$ | Cauchy Base Constant | $1.5046$ | Dimensionless | Infinite-wavelength refractive index of the dielectric glass |
| $B$ | Cauchy Dispersion Coeff | $0.00420$ | $\mu\text{m}^2$ | Principal chromatic dispersion coefficient governing angular fan |
| $C$ | Higher-Order Cauchy Coeff | $0.00015$ | $\mu\text{m}^4$ | Quadratic correction for short-wavelength dispersion curvature |
| $\lambda$ | Light Wavelength | $380 - 750$ | $\text{nm}$ | Electromagnetic vacuum wavelength of the optical radiation |
| $\delta$ | Total Deviation Angle | $\sim 38.9^\circ$ | $\text{rad}$ / $\text{deg}$ | Net angular deviation between incoming beam and emergent beam |
| $\Delta\delta$ | Angular Dispersion | $\sim 1.6^\circ$ | $\text{rad}$ / $\text{deg}$ | Angular spread between red ($700\text{ nm}$) and violet ($400\text{ nm}$) |
| $\theta_c$ | Critical Angle | $\sim 41.1^\circ$ | $\text{rad}$ / $\text{deg}$ | Boundary threshold for Total Internal Reflection (TIR) |

---

## 4. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`PrismDispersionExperiment`](./PrismDispersionExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week5.Day32`
- **Container:** `ResponsiveExperimentContainer` with transparent HUD and compact control deck.

### Ray Tracing Pipeline
The simulation executes a dual-interface 2D analytic ray tracing algorithm in real time on Compose `Canvas`:
1. **Coordinate Geometry:** Maps the equilateral prism vertices $V_0$ (apex), $V_1$ (left base), and $V_2$ (right base) relative to an elevated apparatus origin ($y = 0.40 h$).
2. **Spectral Sampling:** Traces 8 distinct visible wavelengths ($700\text{ nm}$ down to $405\text{ nm}$) in White Light mode, or a single laser ray in Monochromatic mode.
3. **Boundary Intersections:** Computes line-ray intersections using 2D cross-product determinants to identify the exact hit point on Face 2.
4. **TIR Handling:** Detects when $\sin\theta_{r2} \ge 1.0$, rendering dashed internally reflected bounce vectors toward the prism base.
5. **Phosphor Screen Projection:** Renders an interactive detection screen on the right showing the continuous visible spectrum gradient with diffuse ambient glow.

---

## 5. Suggested Investigations & Parameter Experiments

1. **Minimum Deviation Verification:** Set incident angle $\theta_{i1} \approx 49.5^\circ$ with Crown Glass ($\alpha = 60^\circ$). Observe that the ray inside the prism runs strictly horizontal, parallel to the base, and verify that the deviation angle reaches its minimum.
2. **Dense Flint vs Crown Glass:** Switch presets between Borosilicate Crown and Dense Flint glass. Note how Dense Flint significantly broadens the dispersion fan on the detection screen due to its higher Cauchy $B$ parameter.
3. **Total Internal Reflection Threshold:** Lower the incident angle $\theta_{i1}$ below $33^\circ$ or increase the apex angle $\alpha$. Observe how the light rays reach TIR on Face 2 and cannot escape into the air.
