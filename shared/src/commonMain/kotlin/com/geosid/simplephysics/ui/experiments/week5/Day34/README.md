# Day 34: Diffraction Grating Spectrometer

> **Week 5: Optics & Light • Refraction, Total Internal Reflection & Diffraction**  
> *Topic Subtitle: Multi-Slit Angular Dispersion & Atomic Emission Lines*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Resolve atomic elemental emission spectra into razor-sharp spectral lines using a precision diffraction grating ruled with 600 to 1,200 lines per millimeter!

### Scientific Principles & Mechanism
While a double-slit aperture produces broad sinusoidal interference fringes, increasing the number of coherent transmitting slits from two to thousands transforms the optical interference pattern. A **diffraction grating** consists of an array of closely spaced, parallel, periodic apertures or reflective grooves separated by sub-micrometer distance $d$.

1. **Huygens Superposition from $N$ Slits:**  
   When monochromatic planar light strikes $N$ identical slits, each slit radiates cylindrical wavelets. In directions where the path difference between adjacent slits is an integer multiple of the light's wavelength ($\Delta r = d \sin\theta = m \lambda$), all $N$ wavelets arrive completely in phase. Their electric field amplitudes add constructively:
   $$E_{\text{total}} = N \cdot E_0 \implies I_{\text{peak}} \propto N^2 I_0$$
   At any intermediate angle where wavelets are even slightly out of phase, destructively interfering pairs cancel the intensity completely across the array. As $N$ approaches thousands, the broad bands sharpen into needle-like spectral lines.

2. **Discrete Atomic Spectral Fingerprints:**  
   When light emitted by excited gases (such as Hydrogen, Sodium, or Mercury) passes through the grating, each distinct quantum electronic transition produces photons at exact quantized wavelengths:
   - **Hydrogen Balmer Series:** $H_\alpha$ ($656.3\text{ nm}$ Red), $H_\beta$ ($486.1\text{ nm}$ Cyan), $H_\gamma$ ($434.0\text{ nm}$ Blue-Violet), $H_\delta$ ($410.2\text{ nm}$ Deep Violet).
   - **Sodium D-Line Doublet:** $589.0\text{ nm}$ and $589.6\text{ nm}$ separated by only $0.6\text{ nm}$.
   - **Mercury Arc Spectrum:** Green ($546.1\text{ nm}$), Yellow doublet ($577.0\text{ nm}, 579.1\text{ nm}$), Violet ($404.7\text{ nm}$).
   Because each wavelength is diffracted to a unique angle $\theta_m$, the grating functions as a high-precision optical spectrometer.

3. **Diffraction Orders ($m$):**  
   The zero-order ($m = 0$) beam travels straight through at $\theta = 0^\circ$ without dispersing, recombining all wavelengths into a central white spot. The first ($m = \pm 1$) and second ($m = \pm 2$) orders disperse light symmetrically on either side into bright discrete spectral peaks.

### Laboratory & Kitchen Protocol (Try It At Home)
> 1. Take an optical storage disc (a blank DVD has track pitch $d \approx 740\text{ nm}$, approx. 1,350 lines/mm; a CD has $d \approx 1.6\text{ }\mu\text{m}$, approx. 625 lines/mm).  
> 2. Stand in a dark room and shine a small LED flashlight or candle at an angle onto the disc's recording surface.  
> 3. Observe the sharp, vibrant rainbow tracks reflecting onto a nearby white wall or ceiling.  
> 4. To see discrete emission lines, point the disc reflection at a compact fluorescent lamp (CFL) or street lamp to observe distinct green, yellow, and blue mercury emission bands!

---

## 2. Mathematical Foundation & The Three Pillars

### Pillar 1: Governing Law & Interference (The Grating Equation)
The fundamental equation governing diffraction gratings is:

$$d \sin\theta_m = m \lambda$$

where:
- $d = \frac{1}{N}$ is the grating groove spacing (pitch in meters or nanometers), where $N$ is ruling density (e.g., 600 lines/mm $\implies d = 1.667 \ \mu\text{m}$).
- $\theta_m$ is the diffraction angle measured from the grating normal.
- $m \in \{0, \pm 1, \pm 2, \dots\}$ is the integer diffraction order.
- $\lambda$ is the optical vacuum wavelength.

Solving for the angular position of the $m$-th spectral line:
$$\theta_m(\lambda) = \arcsin\left(\frac{m \lambda}{d}\right)$$

The condition for diffraction to occur physically without total evanescent cutoff is:
$$\left| \frac{m \lambda}{d} \right| \le 1 \implies m_{\text{max}} = \left\lfloor \frac{d}{\lambda} \right\rfloor$$

---

### Pillar 2: Kinematics / Motion Constraint (Intensity Profile & Resolving Power)
For an array of $N_{\text{slits}}$ equally spaced slits each of aperture width $a$:

1. **Multi-Slit Intensity Distribution:**
   $$I(\theta) = I_0 \left[ \frac{\sin(N_{\text{slits}} \beta)}{\sin\beta} \right]^2 \left[ \frac{\sin\alpha}{\alpha} \right]^2$$
   where the phase parameters are:
   $$\beta = \frac{\pi d \sin\theta}{\lambda}, \quad \alpha = \frac{\pi a \sin\theta}{\lambda}$$

2. **Angular Line Sharpness:**  
   The angular half-width $\Delta\theta_{\text{FWHM}}$ of each principal maximum scales inversely with the total number of illuminated slits $N_{\text{total}}$:
   $$\Delta\theta \approx \frac{\lambda}{N_{\text{total}} d \cos\theta_m}$$

3. **Chromatic Resolving Power ($R$):**  
   According to the Rayleigh criterion, two adjacent spectral lines at $\lambda$ and $\lambda + \Delta\lambda$ are just resolved when the principal maximum of one falls on the first minimum of the other:
   $$R = \frac{\lambda}{\Delta\lambda} = m \cdot N_{\text{total}}$$
   For example, resolving the Sodium doublet ($\Delta\lambda = 0.59\text{ nm}$ at $\lambda = 589\text{ nm}$) requires $R \approx 1,000$. In order $m = 1$, illuminating just $1,000$ grooves (a $1.6\text{ mm}$ beam on a $600\text{ l/mm}$ grating) easily resolves the two lines into separate distinct peaks!

---

### Pillar 3: Energy / Work Conservation & Angular Dispersion
Energy conservation dictates that total radiant flux transmitted through the grating equals the sum of fluxes across all propagating diffraction orders:

$$\Phi_{\text{total}} = \sum_{m = -m_{\text{max}}}^{m_{\text{max}}} \Phi_m$$

1. **Angular Dispersion ($D$):**  
   The rate of angular separation per unit change in wavelength is obtained by differentiating the grating equation:
   $$d \cos\theta_m d\theta = m d\lambda \implies D = \frac{d\theta}{d\lambda} = \frac{m}{d \cos\theta_m}$$
   - **Higher Orders ($m \uparrow$):** Yield proportionally greater angular spread between wavelengths.
   - **Finer Groove Spacing ($d \downarrow$ / Higher lines/mm):** Spreads lines much wider across the screen.

2. **Linear Screen Separation:**  
   On a planar screen placed at distance $L$:
   $$y_m = L \tan\theta_m$$
   $$\frac{dy_m}{d\lambda} = L \sec^2\theta_m \frac{d\theta}{d\lambda} = \frac{m L}{d \cos^3\theta_m}$$

---

## 3. Physical Parameters & SI Units

| Symbol | Parameter Name | Default Simulation Value | SI Unit | Physical Role |
| :---: | :--- | :---: | :---: | :--- |
| $N$ | Ruling Density | $600.0$ | $\text{lines/mm}$ | Number of periodic grating ruling grooves per millimeter |
| $d$ | Grating Pitch | $1.667$ | $\mu\text{m}$ / $\text{nm}$ | Center-to-center distance between adjacent grating slits |
| $m$ | Diffraction Order | $\pm 1, \pm 2$ | Dimensionless | Integer harmonic order of constructive interference |
| $\lambda$ | Spectral Wavelength | $400 - 700$ | $\text{nm}$ | Electromagnetic wavelength of atomic emission line |
| $L$ | Screen Distance | $500.0$ | $\text{mm}$ | Distance from grating aperture to detection phosphor plate |
| $\theta_m$ | Diffraction Angle | $\sim 23.2^\circ$ | $\text{deg}$ / $\text{rad}$ | Angular deflection angle from central optical axis |
| $D$ | Angular Dispersion | $\sim 0.65$ | $\text{rad}/\mu\text{m}$ | Rate of angular separation per unit wavelength change |
| $R$ | Resolving Power | $6,000$ | Dimensionless | Theoretical spectral line resolution limit ($m \cdot N_{\text{total}}$) |

---

## 4. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`DiffractionGratingExperiment`](./DiffractionGratingExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week5.Day34`
- **Container:** `ResponsiveExperimentContainer` with transparent telemetry card and compact control deck.

### Optical Spectrometer Pipeline
1. **Source Visualization:** Emits collimated multi-spectral beams corresponding to atomic elemental transitions.
2. **Grating Micro-Rulings:** Visualizes the periodic vertical grating element with realistic ruling lines.
3. **Ray Angle Tracing:** Calculates exact diffraction angles $\theta_m(\lambda) = \arcsin(m \lambda / d)$ for orders $m = 0, \pm 1, \pm 2$.
4. **Screen Phosphor Lines:** Projects razor-sharp color lines at accurate linear screen coordinates $y = L \tan\theta_m$.
5. **Interactive Goniometer Reticle:** Draggable crosshair probe on the detection plane reading live wavelength and deflection angle.

---

## 5. Suggested Investigations & Parameter Experiments

1. **Resolving the Sodium Doublet:** Select the Sodium preset ($\lambda = 589.0\text{ nm}$ and $589.6\text{ nm}$). At $300\text{ l/mm}$, the lines nearly overlap. Increase ruling density to $1,200\text{ l/mm}$ to see the doublet cleanly split into twin yellow peaks!
2. **Order Comparison ($m = 1$ vs $m = 2$):** Compare the spacing between Balmer lines in order $m = 1$ versus order $m = 2$. Confirm that second-order dispersion is exactly twice as wide ($D \propto m$).
3. **Continuous White Light:** Select the Continuous White preset to observe continuous rainbow ribbons and note how second-order violet ($2 \times 400 = 800\text{ nm}$) approaches first-order red ($1 \times 700 = 700\text{ nm}$).
