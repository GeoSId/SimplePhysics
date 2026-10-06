# Day 30: Soap Bubble Thin-Film Interference

> **Week 5: Optics & Light • Refraction, Total Internal Reflection & Diffraction**  
> *Topic Subtitle: Phase Inversion, Optical Path Interference & Spectral Iridescence*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Adjust nanometer soap film thickness and watch shimmering iridescent rainbow hues appear.

### Scientific Principles & Mechanism
When you look at a soap bubble or an oil slick on a puddle of water, you see swirling, vibrant rainbow colors. These colors are not produced by pigments, dyes, or absorption. Instead, they are generated purely by the wave nature of light through **Thin-Film Interference**.

1. **Dual Boundary Reflection:**  
   A soap film is a thin sheet of water encased between two monolayers of soap surfactant molecules, surrounded by air on both sides. When light strikes the film:
   - Part of the light reflects immediately from the outer surface (interface 1: air to film).
   - The remaining light refracts into the film, travels across its nanometer thickness, and reflects from the inner surface (interface 2: film to air).
   - This second ray refracts back out into the air, traveling parallel to the first reflected ray.

2. **Phase Inversion upon Reflection (Stokes Relations):**  
   From electromagnetic boundary conditions (Fresnel equations):
   - When light reflects from an optically denser medium ($n_{\text{air}} = 1.00 < n_{\text{film}} \approx 1.33$), the reflected electric field undergoes an instantaneous **half-wave phase shift of $\pi$ radians** ($\lambda / 2$).
   - When light reflects from an optically rarer medium ($n_{\text{film}} \approx 1.33 > n_{\text{air}} = 1.00$), there is **zero phase shift** ($\Delta\phi = 0$).

3. **Optical Path Difference (OPD):**  
   The second ray travels an extra distance inside the film. Accounting for the film refractive index $n$ and refraction angle $\theta_t$, the geometric extra path is:
   $$\Delta_{\text{geometric}} = 2 n d \cos\theta_t$$
   Including the $\pi$ phase shift ($\lambda / 2$) from interface 1, the total effective path difference is $\Delta_{\text{total}} = 2 n d \cos\theta_t + \lambda / 2$.

4. **Constructive & Destructive Interference:**  
   - **Constructive Interference (Bright Rainbow Fringe):** Occurs when the crests of both rays align, reinforcing that wavelength:
     $$2 n d \cos\theta_t = \left(m + \frac{1}{2}\right)\lambda, \quad m \in \{0, 1, 2, \dots\}$$
   - **Destructive Interference (Dark Cancellation):** Occurs when crest meets trough, extinguishing that wavelength:
     $$2 n d \cos\theta_t = m \lambda, \quad m \in \{0, 1, 2, \dots\}$$

5. **Gravitational Drainage Wedge & Newton's Black Film:**  
   When a soap film is held vertically in a wire loop, gravity pulls the fluid downward while capillary forces pull toward the edges. This forms a vertical thickness wedge: the film is extremely thin at the top and thicker toward the bottom.
   - Near the top, when thickness $d \ll \lambda$ ($d < 30\text{ nm}$), the path difference $2 n d \cos\theta_t \to 0$.
   - The only remaining difference is the $\pi$ reflection phase flip.
   - Consequently, **all visible wavelengths interfere destructively**!
   - The soap film turns completely pitch-black and invisible right before it pops. This is the historic **Newton's Black Film**.

### Laboratory & Kitchen Protocol (Try It At Home)
> 1. Form a wire into a circular or rectangular loop with a handle (about 4 cm across).  
> 2. Dip the loop into a mixture of dish soap, water, and a few drops of glycerin.  
> 3. Hold the loop vertically against a dark background in a well-lit room.  
> 4. Watch as gravity drains the soap downward: horizontal rainbow bands form and slowly descend.  
> 5. Notice the very top turn silvery-gray, then jet-black right before the film ruptures!

---

## 2. Mathematical Formulation & The Three Pillars

### Pillar 1: Governing Law & Dielectric Phase Inversion
The reflection of transverse electromagnetic waves at normal and oblique incidence is governed by the Fresnel reflection coefficients for TE (s-polarization) and TM (p-polarization):

$$
r_s = \frac{n_1 \cos\theta_i - n_2 \cos\theta_t}{n_1 \cos\theta_i + n_2 \cos\theta_t}, \quad r_p = \frac{n_2 \cos\theta_i - n_1 \cos\theta_t}{n_2 \cos\theta_i + n_1 \cos\theta_t}
$$

For reflection from air ($n_1 = 1.000$) into liquid film ($n_2 = 1.333$):
Since $n_1 < n_2$, $r_s < 0$, which corresponds to an intrinsic phase flip:

$$
\Delta\phi_{\text{top}} = \pi \text{ radians} \quad \left(\text{equivalent to } \frac{\lambda}{2}\right)
$$

For reflection at the back interface from film ($n_2 = 1.333$) into air ($n_3 = 1.000$):
Since $n_2 > n_3$, $r_s > 0$, giving:

$$
\Delta\phi_{\text{bottom}} = 0 \text{ radians}
$$

The net intrinsic phase difference between Ray 1 and Ray 2 due strictly to boundary reflections is:

$$
\Delta\phi_{\text{boundary}} = \Delta\phi_{\text{top}} - \Delta\phi_{\text{bottom}} = \pi \text{ radians}
$$

---

### Pillar 2: Kinematics & Optical Path Difference Geometry
Snell's Law determines the internal propagation angle $\theta_t$ inside the film:

$$
n_{\text{air}} \sin\theta_i = n_{\text{film}} \sin\theta_t \implies \theta_t = \arcsin\left(\frac{\sin\theta_i}{n_{\text{film}}}\right)
$$

The second ray traverses two diagonal segments of length $s = d / \cos\theta_t$ within the medium of refractive index $n$. During this time, the first ray travels a distance $x_0 = 2 d \tan\theta_t \sin\theta_i$ in air. The optical path difference (OPD) between the two beams is:

$$
\text{OPD} = 2 n_{\text{film}} \left(\frac{d}{\cos\theta_t}\right) - n_{\text{air}} \left(2 d \tan\theta_t \sin\theta_i\right)
$$

Using $n_{\text{air}} \sin\theta_i = n_{\text{film}} \sin\theta_t$ and trigonometric identity $1 - \sin^2\theta_t = \cos^2\theta_t$:

$$
\text{OPD} = 2 n_{\text{film}} d \cos\theta_t
$$

The total phase difference between the two interfering waves at optical wavelength $\lambda$ is:

$$
\Delta\phi(\lambda) = \frac{2\pi}{\lambda} \cdot \text{OPD} + \pi = \frac{4\pi n d \cos\theta_t}{\lambda} + \pi
$$

Constructive interference requires $\Delta\phi = 2\pi m$ ($m \in \mathbb{Z}$), leading to:

$$
2 n d \cos\theta_t = \left(m + \frac{1}{2}\right)\lambda, \quad m = 0, 1, 2, \dots
$$

Destructive interference requires $\Delta\phi = (2m + 1)\pi$, leading to:

$$
2 n d \cos\theta_t = m \lambda, \quad m = 0, 1, 2, \dots
$$

---

### Pillar 3: Multi-Wavelength Spectral Synthesis & Black Film Asymptote
When illuminated with white light, the reflected intensity for each spectral wavelength $\lambda$ follows the two-beam interference equation:

$$
I(\lambda) = I_0 \cos^2\left(\frac{\Delta\phi(\lambda)}{2}\right) = I_0 \cos^2\left(\frac{2\pi n d \cos\theta_t}{\lambda} + \frac{\pi}{2}\right) = I_0 \sin^2\left(\frac{\pi \cdot \text{OPD}}{\lambda}\right)
$$

#### 1. Newton's Zero-Order Black Film Limit ($d \to 0$):
As gravitational thinning drains the apex of the film such that $d < 30\text{ nm} \ll \lambda_{\text{visible}}$:

$$
\lim_{d \to 0} \text{OPD} = 0 \implies \lim_{d \to 0} I(\lambda) = I_0 \sin^2(0) = 0 \quad \forall \lambda \in [380\text{ nm}, 750\text{ nm}]
$$

Because all visible wavelengths undergo destructive interference simultaneously, the film reflects less than $1\%$ of incident light and appears completely dark.

#### 2. Color Synthesis by Spectral Summation:
The perceived RGB color is obtained by integrating spectral reflectance against standard human tristimulus curves:

$$
R = \int_{380}^{750} I(\lambda) \bar{x}(\lambda) d\lambda, \quad G = \int_{380}^{750} I(\lambda) \bar{y}(\lambda) d\lambda, \quad B = \int_{380}^{750} I(\lambda) \bar{z}(\lambda) d\lambda
$$

In our high-efficiency Compose engine, this is sampled across the primary wavelengths $\lambda_{\text{red}} = 650\text{ nm}$, $\lambda_{\text{green}} = 532\text{ nm}$, and $\lambda_{\text{blue}} = 450\text{ nm}$.

---

## 3. Physical Parameters & SI Constants

| Parameter | Symbol | Nominal Value | SI Unit | Physical Description |
| :--- | :--- | :--- | :--- | :--- |
| Soap Film Refractive Index | $n_{\text{film}}$ | $1.333$ | dimensionless | Optical refractive index of soap water mixture |
| Ambient Air Index | $n_{\text{air}}$ | $1.000$ | dimensionless | Refractive index of surrounding atmosphere |
| Center Film Thickness | $d$ | $380$ | $\text{nm}$ ($10^{-9}\text{ m}$) | Local thickness between outer and inner surfaces |
| Angle of Incidence | $\theta_i$ | $15.0$ | degrees ($^\circ$) | Angle between incident illumination and normal |
| Internal Angle of Refraction | $\theta_t$ | $11.2$ | degrees ($^\circ$) | Angle of transmitted ray inside liquid film |
| Optical Path Difference | $\Delta$ | $993.4$ | $\text{nm}$ | Net optical distance between Ray 1 and Ray 2 |
| Sodium D Reference Wavelength | $\lambda_{\text{Na}}$ | $589.0$ | $\text{nm}$ | Monochromatic benchmark line for sharp fringes |
| Black Film Threshold | $d_{\text{black}}$ | $< 30$ | $\text{nm}$ | Thickness threshold where visible reflection cancels |

---

## 4. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`ThinFilmInterferenceExperiment`](./ThinFilmInterferenceExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week5.Day30`
- **Screen Architecture:** Compose Multiplatform with Canvas rendering and high-frequency fluid tick.

### Reactive State Variables
The interactive simulation manages physical state with Compose `mutableStateOf`:

| State Variable | Type | Default Value | Functional Role in Simulation |
| :--- | :--- | :--- | :--- |
| `filmIndex` | `Float` | `1.333f` | Refractive index of liquid medium (soap, oil, alcohol) |
| `filmThicknessNm` | `Float` | `380f` | Center nanometer thickness of the thin film |
| `incidentAngleDeg` | `Float` | `15.0f` | Illumination angle of incident light |
| `isMonochromatic` | `Boolean` | `false` | Toggles between White Light and Sodium D (589nm) |
| `drainageRate` | `Float` | `1.0f` | Rate of downward gravitational liquid thinning |
| `selectedPreset` | `SoapFilmPreset?` | `SOAP_BUBBLE` | Fast switching between physical presets |
| `showRayDiagram` | `Boolean` | `true` | Toggles detailed optical cross-section inset |
| `rippleAmp` | `Float` | `0.0f` | Surface tension wave amplitude from user touch |

### Verbatim Numerical Stepping Synchronized with Video Cards
The simulation executes three core mathematical steps:

```kotlin
// 1. Optical path difference with reflection phase inversion
val optPathDiff = 2f * filmIndex * filmThicknessNm * cos(refractedAngleRad)
val isConstructive = abs((optPathDiff / wavelengthNm) - (orderM + 0.5f)) < 0.15f

// 2. Multi-wavelength spectral reflectance and interference intensity
val phaseShiftRad = (2f * PI.toFloat() * optPathDiff / wavelengthNm) + PI.toFloat()
val reflectedIntensity = cos(phaseShiftRad * 0.5f).pow(2)

// 3. Gravitational drainage wedge thickness and zero-order black film
val localThicknessNm = minThicknessNm + drainageRate * (coordY / filmHeightPx) * maxThicknessNm
val isBlackFilm = localThicknessNm < 30f && reflectedIntensity < 0.05f
```

---

## 5. Suggested Investigations & Experiments
1. **Explore Newton's Black Film:** Drag the thickness slider down below $30\text{ nm}$. Observe how all colorful bands disappear, turning the film pitch-black and nearly transparent.
2. **Monochromatic Sodium Mode:** Switch the illumination source to `Sodium (589nm)`. Notice how the soft rainbow spectrum transforms into razor-sharp alternating bright and dark fringe bands.
3. **Oblique Incidence Shift:** Increase the incident angle $\theta_i$ from $0^\circ$ to $60^\circ$. Observe how increasing $\theta_i$ decreases $\cos\theta_t$, shifting the fringe pattern toward shorter wavelengths (blue-shift).
4. **Touch Perturbation:** Drag a finger across the film to trigger capillary surface tension ripples that temporarily distort the smooth gravitational wedge!
