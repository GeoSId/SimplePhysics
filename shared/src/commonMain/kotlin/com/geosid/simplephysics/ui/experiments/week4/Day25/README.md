# Day 25: Acoustic Resonance (Wine Glass Shatter)

> **Week 4: Waves, Sound & Acoustic Resonance**  
> *Topic Subtitle: Q-Factor, Quadrupole Rim Eigenmodes & Catastrophic Fracture Mechanics*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Tune an acoustic tone to the exact resonant frequency of a crystal wine glass until mechanical stress shatters the rim!

### Scientific Principles & Mechanism
Every physical structure possesses discrete natural vibrational frequencies (eigenmodes) determined by its geometry, elastic modulus ($E$), density ($\rho$), and boundary constraints. When an external sound wave strikes a crystal wine glass, periodic alternating pressure fronts exert a periodic driving force on the glass wall.

If the driving audio frequency $f$ differs from the fundamental resonant frequency $f_0$, each successive sound wave arrives out of phase with the rim's natural oscillation, causing destructive interference that cancels out mechanical motion. 

However, when the acoustic tone precisely matches $f_0$ ($f \approx 556\text{ Hz}$), constructive interference occurs on every acoustic cycle. Because fine lead crystal has an exceptionally high **Quality Factor ($Q \approx 2000$)**, internal damping is tiny. The glass accumulates vibrational energy over hundreds of cycles, magnifying the rim's flexural displacement by a factor of $Q$. 

The circular rim deforms into an oscillating quadrupole ellipse ($n=2$ mode). Eventually, dynamic tensile hoop stresses exceed the ultimate tensile strength of the silica matrix ($\sim 38 - 50\text{ MPa}$), propagating supersonic micro-cracks that catastrophically shatter the glass into flying shards!

### Laboratory / Kitchen Protocol (Try It At Home)
> Wet your index finger and rub it around the rim of a crystal wine glass to make it sing with a ringing resonant tone! Place a straw or paperclip across the rim and watch it dance and bounce violently as acoustic resonance builds up.

---

## 2. Mathematical Formulation & The Three Pillars

### Pillar 1: Governing Law & Forced Damped Harmonic Response
The acoustic interaction between the incident sound pressure field $P(t) = P_0 \cos(2\pi f t)$ and the glass wall is modeled as a forced, damped harmonic oscillator:

$$
m \ddot{x} + \gamma \dot{x} + k x = F_0 \cos(2\pi f t)
$$

where $m$ is the effective modal mass, $\gamma = \frac{\sqrt{k m}}{Q}$ is the viscous internal damping coefficient, and $k = m (2\pi f_0)^2$ is the structural stiffness. The steady-state displacement amplitude follows a Lorentzian frequency response curve:

$$
A(f) = \frac{F_0 / m}{\sqrt{\left( (2\pi f_0)^2 - (2\pi f)^2 \right)^2 + (2\pi \gamma f)^2}} = \frac{A_{\text{static}}}{\sqrt{\left(1 - (f/f_0)^2\right)^2 + \left(\frac{f}{Q f_0}\right)^2}}
$$

At exact resonance ($f = f_0$), the amplitude achieves its maximum resonant amplification:

$$
A(f_0) = Q \cdot A_{\text{static}}
$$

### Pillar 2: Kinematics & Quadrupole Rim Eigenmode ($n=2$)
In cylindrical polar coordinates $(r, \theta)$, the fundamental acoustic eigenmode of an open thin-walled cylindrical shell corresponds to azimuthal wavenumber $n=2$ (quadrupole deformation):

$$
r(\theta, t) = R_0 + \Delta R(t) \cos(2\theta) \cos(2\pi f t)
$$

This standing wave configuration produces:
- **4 Stationary Nodes** at $\theta = \frac{\pi}{4}, \frac{3\pi}{4}, \frac{5\pi}{4}, \frac{7\pi}{4}$ ($45^\circ, 135^\circ, 225^\circ, 315^\circ$), where radial displacement is identically zero ($\Delta r = 0$).
- **4 Antinodes** at $\theta = 0, \frac{\pi}{2}, \pi, \frac{3\pi}{2}$ ($0^\circ, 90^\circ, 180^\circ, 270^\circ$), where radial displacement reaches maximum extension and compression ($\pm \Delta R$).

### Pillar 3: Energy Conservation & Catastrophic Fracture Mechanics
The Quality Factor $Q$ quantifies the ratio of stored mechanical energy to energy dissipated per acoustic cycle:

$$
Q = 2\pi \frac{E_{\text{stored}}}{E_{\text{dissipated per cycle}}} = \frac{f_0}{\Delta f_{\text{FWHM}}}
$$

The characteristic ring-up time constant required for the glass to build up to $63.2\%$ of its steady-state amplitude is:

$$
\tau = \frac{Q}{\pi f_0}
$$

The dynamic circumferential hoop stress induced in the rim during flexure is governed by Hooke's Law:

$$
\sigma_{\text{hoop}}(t) = E \cdot \epsilon_{\text{hoop}} = E \cdot \frac{\Delta R(t)}{R_0}
$$

Catastrophic structural failure occurs when peak dynamic hoop stress meets or exceeds the critical Griffith brittle tensile strength threshold of the glass:

$$
\sigma_{\text{hoop}} \ge \sigma_{\text{tensile}} \implies \text{Catastrophic Shatter}
$$

---

## 3. Physical Parameters & SI Units

| Symbol | Parameter Description | Nominal Value (Lead Crystal) | SI Unit |
| :--- | :--- | :--- | :--- |
| $f_0$ | Natural fundamental eigenfrequency | $556.0$ | $\text{Hz}$ |
| $f$ | Speaker driving audio frequency | $540.0 - 572.0$ | $\text{Hz}$ |
| $\Delta f$ | Audio frequency detuning $|f - f_0|$ | $0.0 - 16.0$ | $\text{Hz}$ |
| $Q$ | Quality factor (Crystal vs Soda-Lime) | $2200\text{ (Crystal)}, 280\text{ (Soda-Lime)}$ | Dimensionless |
| $\text{SPL}$ | Sound Pressure Level at glass | $95 - 135$ | $\text{dB}$ |
| $P_0$ | Acoustic sound pressure amplitude | $1.1 - 112.5$ | $\text{Pa}$ |
| $E$ | Young's Modulus of glass | $65.0 \times 10^9$ | $\text{Pa}$ ($\text{N/m}^2$) |
| $R_0$ | Undistorted rim radius | $0.040$ ($40\text{ mm}$) | $\text{m}$ |
| $\Delta R$ | Dynamic rim deflection amplitude | $0.0 - 2.8$ | $\text{mm}$ |
| $\sigma_{\text{hoop}}$ | Dynamic circumferential hoop stress | $0.0 - 45.0$ | $\text{MPa}$ |
| $\sigma_{\text{tensile}}$ | Critical tensile fracture limit | $38.0$ (Crystal), $70.0$ (Soda-Lime) | $\text{MPa}$ |
| $\tau$ | Acoustic ring-up time constant | $\approx 1.26$ | $\text{s}$ |

---

## 4. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`AcousticResonanceExperiment`](./AcousticResonanceExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week4.Day25`
- **Screen Architecture:** Compose Multiplatform with Canvas rendering & real-time physical numerical stepping.

### Reactive State Variables
The interactive state is managed via Compose `mutableStateOf` variables:

| State Variable | Type / Default | Functional Role in Simulation |
| :--- | :--- | :--- |
| `driveFreq` | `556.0f` ($\text{Hz}$) | Audio generator speaker frequency |
| `soundLevelDb` | `118.0f` ($\text{dB}$) | Acoustic sound pressure level |
| `selectedMaterial`| `GlassMaterial.LEAD_CRYSTAL` | Preset selection (`LEAD_CRYSTAL`, `BOROSILICATE`, `SODA_LIME`) |
| `isSpeakerOn` | `true` | Audio transducer power toggle |
| `isSlowMotion` | `true` | Strobe mode slowing $556\text{ Hz}$ rim vibration to $2.2\text{ Hz}$ for clear visualization |
| `currentRimDispMm`| `0f` ($\text{mm}$) | Instantaneous radial rim deflection |
| `stressMpa` | `0f` ($\text{MPa}$) | Real-time dynamic circumferential hoop stress |
| `isShattered` | `false` | Catastrophic fracture state flag |
| `shards` | `SnapshotStateList<GlassShard>`| 36 polygon shard debris particles simulated with ballistic trajectory & drag |

### Frame Loop & Physics Integration
- **High-Precision Loop:** Driven by `withFrameNanos` inside `LaunchedEffect(isSpeakerOn, driveFreq, soundLevelDb, selectedMaterial, isShattered)`.
- **Lorentzian Steady-State Target:** Computes driving frequency ratio $f / f_0$, detuning $\Delta f$, and resonance denominator $\sqrt{(1 - (f/f_0)^2)^2 + (f/(Q f_0))^2}$.
- **Ring-Up Relaxation:** Propagates exponential energy build-up over time constant $\tau = Q / (\pi f_0)$ via numerical stepping: $\Delta A = (A_{\text{target}} - A) \cdot (dt / \tau)$.
- **Fracture Trigger:** When $\sigma_{\text{hoop}} \ge \sigma_{\text{tensile}}$, spawns 36 polygon shards with radial explosive velocities and initiates Newtonian trajectory updates ($vy += g \cdot dt$, air drag decay).

### User Gestures & Interactivity
- **Material Preset Chips:** Compact 32.dp buttons to test high-$Q$ Lead Crystal ($Q=2200$), Borosilicate ($Q=850$), and low-$Q$ Soda-Lime glass ($Q=280$).
- **Dual Sliders:** Paired in a horizontal Row for speaker frequency ($540 - 572\text{ Hz}$) and volume level ($95 - 135\text{ dB}$).
- **Resonance Lock:** "🎯 Lock 556 Hz" button instantly snaps audio driver to the exact peak eigenmode.
- **Visual Strobe:** "⏱️ Strobe Slow" / "⚡ Real Speed" toggle lets the user witness the quadrupole standing wave flexure in slow motion.
- **Glass Restoration:** Reset icon restores the pristine crystal wine glass.

### Canvas Graphics Pipeline
- **Elevated Canvas Origin:** Center of glass apparatus placed at $cy = 0.38 \cdot h$, leaving the lower 38% clear for controls.
- **Acoustic Transducer:** Left-mounted speaker horn with conical flare, vibrating center dome, and radial compression sound wavefronts.
- **Oblique 3D Crystal Glass:** Base foot, slender stem, translucent bowl body, and 72-segment parametric quadrupole rim ellipse:
  $$r(\theta) = R_0 + \Delta R \cos(2\theta) \cos(\omega t)$$
- **Nodal Indicators:** Emerald green dots highlighting the 4 stationary nodes, coral red dots highlighting the 4 vibrating antinodes.
- **Fracture FX:** Supersonic micro-crack stress lines flashing along high-strain zones, followed by ballistic shard dispersion upon failure.

---

## 5. Suggested Investigations & Parameter Experiments
1. **The Lead Crystal Advantage (Q-Factor Comparison):** Set volume to $118\text{ dB}$ and select Lead Crystal ($Q=2200$). Observe how locking to $556\text{ Hz}$ shatters the glass in under 2 seconds. Switch to Soda-Lime ($Q=280$) under identical volume; note that because of lower $Q$, amplitude remains below the fracture threshold!
2. **Frequency Sensitivity (Resonance Bandwidth):** Shift the speaker tone by just $\pm 1.5\text{ Hz}$ off resonance ($554.5\text{ Hz}$ or $557.5\text{ Hz}$). Observe how rim vibration drops precipitously, proving that acoustic shattering requires surgical frequency precision.
3. **Strobe Slow-Motion Observation:** Toggle the Strobe button to observe the $n=2$ quadrupole mode: notice how the rim continuously oscillates between an east-west ellipse and a north-south ellipse, while the 4 diagonal nodal points remain completely stationary!
