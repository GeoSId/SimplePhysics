# Day 27: Acoustic Beats & Interference

> **Week 4: Waves, Sound & Acoustic Resonance**  
> *Topic Subtitle: Wave Superposition, Envelope Pulsing & Phase Coherence*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Sound two pure acoustic frequencies just 2 Hz apart and hear a periodic wobbling volume thrum caused by constructive and destructive wave superposition!

### Scientific Principles & Mechanism
When two acoustic sources radiate pure harmonic waves at closely spaced frequencies $f_1$ and $f_2$, the human ear does not perceive two isolated, independent tones. Instead, the ear hears a single fused pitch oscillating at the average carrier frequency:

$$
f_c = \frac{f_1 + f_2}{2}
$$

whose loudness surges and fades rhythmically at the difference frequency:

$$
f_{\text{beat}} = |f_1 - f_2|
$$

This phenomenon, known as **Acoustic Beats**, is a direct physical manifestation of the principle of linear wave superposition:
1. **Constructive Interference (In-Phase):**  
   When the two sound waves align crest-to-crest and trough-to-trough, their pressure amplitudes add constructively, doubling the acoustic displacement ($2A$) and quadrupling the instantaneous sound intensity ($I \propto 4A^2$). The sound swells to maximum loudness.
2. **Destructive Interference (Anti-Phase):**  
   Half a beat period later, one wave has slipped half a cycle ($\pi$ radians) relative to the other. Wave crests perfectly coincide with wave troughs, cancelling each other out ($A - A = 0$) and producing moments of total silence.
3. **Auditory Psychoacoustics & Sensory Roughness:**  
   - $\Delta f < 1\text{ Hz}$: Very slow volume swell; perceived as tremolo.
   - $\Delta f \approx 1 - 5\text{ Hz}$: Audible pulsing beats; universally used by musicians and piano tuners to tune instruments to pure unison ("tuning out the beats").
   - $\Delta f \approx 5 - 15\text{ Hz}$: Rapid throbbing flutter.
   - $\Delta f \approx 15 - 30\text{ Hz}$: Individual beats can no longer be resolved by the human auditory cortex; the beating blurs into a harsh sensory sensation called **Helmholtz Acoustic Roughness**, the biological basis of musical dissonance.
   - $\Delta f > 30\text{ Hz}$: Exceeds the critical bandwidth of the human cochlea; the brain separates the stimulus into two distinct musical pitches.

### Laboratory & Real-World Protocol (Try It At Home)
> Hum a steady vocal note at roughly 440 Hz while plucking a guitar string that is slightly out of tune with your voice. Listen carefully: you will hear an unmistakable, pulsing "wa-wa-wa" volume flutter surging through the room as your vocal cords and the metal string go in and out of phase!

---

## 2. Mathematical Formulation & The Three Pillars

### Pillar 1: Governing Law & Linear Wave Superposition
Consider two monochromatic acoustic pressure waves propagating through air with identical amplitude $A$, zero initial phase, and slightly different angular frequencies $\omega_1 = 2\pi f_1$ and $\omega_2 = 2\pi f_2$:

$$
y_1(t) = A \sin(\omega_1 t)
$$
$$
y_2(t) = A \sin(\omega_2 t)
$$

By the principle of linear wave superposition in linear acoustic media:

$$
y(t) = y_1(t) + y_2(t) = A \left[ \sin(\omega_1 t) + \sin(\omega_2 t) \right]
$$

Applying the prosthaphaeresis trigonometric sum-to-product identity:

$$
\sin\alpha + \sin\beta = 2 \cos\left(\frac{\alpha - \beta}{2}\right) \sin\left(\frac{\alpha + \beta}{2}\right)
$$

Substituting $\alpha = \omega_1 t$ and $\beta = \omega_2 t$ yields the exact analytical form:

$$
y(t) = \underbrace{2 A \cos\left(2\pi \frac{f_1 - f_2}{2} t\right)}_{\text{Modulated Amplitude Envelope } A_{\text{env}}(t)} \cdot \underbrace{\sin\left(2\pi \frac{f_1 + f_2}{2} t\right)}_{\text{High-Frequency Carrier Signal } \omega_c}
$$

When amplitudes are unequal ($A_1 \neq A_2$), phasor addition gives the generalized envelope:

$$
A_{\text{env}}(t) = \sqrt{A_1^2 + A_2^2 + 2 A_1 A_2 \cos\left(2\pi (f_1 - f_2) t\right)}
$$

where the envelope minimum is $|A_1 - A_2| > 0$, explaining why volume never completely vanishes when one speaker or instrument is louder than the other.

---

### Pillar 2: Kinematics & Phase Evolution Geometry
The relative phase difference $\Delta\phi(t)$ between the two oscillators evolves linearly with time:

$$
\Delta\phi(t) = (\omega_1 - \omega_2) t = 2\pi (f_1 - f_2) t
$$

- **Phase Coherence Condition for Constructive Antinodes:**
  $$
  \Delta\phi(t) = 2\pi n \quad (n \in \mathbb{Z}) \implies t = \frac{n}{|f_1 - f_2|}
  $$
- **Phase Anti-Coherence Condition for Destructive Nodes:**
  $$
  \Delta\phi(t) = (2n + 1)\pi \quad (n \in \mathbb{Z}) \implies t = \frac{2n + 1}{2|f_1 - f_2|}
  $$

The envelope modulation frequency is:

$$
f_{\text{env}} = \frac{|f_1 - f_2|}{2}
$$

Because the ear perceives loudness based on acoustic power (which is proportional to the square of amplitude), both the positive peak of $\cos(\dots)$ and its negative trough register as maximum volume. Hence, two loudness peaks occur per envelope period, giving the **Beat Frequency**:

$$
f_{\text{beat}} = 2 f_{\text{env}} = |f_1 - f_2|
$$

The duration between consecutive loudness peaks is the **Beat Period**:

$$
T_{\text{beat}} = \frac{1}{f_{\text{beat}}} = \frac{1}{|f_1 - f_2|}
$$

---

### Pillar 3: Energy, Acoustic Intensity & Power Modulation
The instantaneous acoustic energy density $u(t)$ and acoustic intensity $I(t)$ transmitted through air are proportional to the square of the instantaneous pressure:

$$
I(t) = \frac{y(t)^2}{\rho_0 c} = \frac{4 A^2}{\rho_0 c} \cos^2\left(\pi (f_1 - f_2) t\right) \sin^2\left(\pi (f_1 + f_2) t\right)
$$

Averaging over the rapid carrier oscillation cycle ($T_c = 1 / f_c \ll T_{\text{beat}}$) using $\langle \sin^2(\omega_c t) \rangle = \frac{1}{2}$:

$$
\langle I(t) \rangle_{\text{carrier}} = \frac{2 A^2}{\rho_0 c} \cos^2\left(\pi (f_1 - f_2) t\right) = \frac{A^2}{\rho_0 c} \left[ 1 + \cos\left(2\pi f_{\text{beat}} t\right) \right]
$$

- **Peak Intensity (Constructive):** $I_{\text{max}} = \frac{4 A^2}{\rho_0 c} = 4 I_0$ (four times individual source intensity).
- **Minimum Intensity (Destructive):** $I_{\text{min}} = 0$ (complete silence).
- **Average Power Over Beat Period:** $\langle I \rangle_{T_{\text{beat}}} = \frac{2 A^2}{\rho_0 c} = 2 I_0$, rigorously satisfying the **Law of Conservation of Energy**: the energy is not destroyed; it is spatially and temporally redistributed from the destructive nodes into the constructive antinodes.

---

## 3. Physical Parameters & System Constants

| Parameter | Symbol | Nominal Value | Range | SI Units | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| Primary Frequency (Tone A) | $f_1$ | `440.0` | `420.0 .. 460.0` | $\text{Hz}$ | Base tuning pitch (Concert A4) |
| Secondary Frequency (Tone B) | $f_2$ | `442.0` | `420.0 .. 460.0` | $\text{Hz}$ | Detuned oscillator pitch |
| Primary Amplitude | $A_1$ | `1.0` | `0.2 .. 1.0` | Dimensionless | Relative acoustic pressure amplitude |
| Secondary Amplitude | $A_2$ | `1.0` | `0.2 .. 1.0` | Dimensionless | Relative acoustic pressure amplitude |
| Beat Frequency | $f_{\text{beat}}$ | `2.0` | `0.0 .. 40.0` | $\text{Hz}$ | Perceived envelope modulation frequency |
| Carrier Frequency | $f_c$ | `441.0` | `420.0 .. 460.0` | $\text{Hz}$ | Average heard pitch |
| Beat Period | $T_{\text{beat}}$ | `0.500` | `0.025 .. \infty` | $\text{s}$ | Temporal interval between loudness maxima |
| Air Density | $\rho_0$ | `1.225` | Fixed | $\text{kg/m}^3$ | Standard atmospheric air density at 15°C |
| Speed of Sound | $c$ | `343.0` | Fixed | $\text{m/s}$ | Acoustic phase velocity in air at 20°C |

---

## 4. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`AcousticBeatsExperiment`](./AcousticBeatsExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week4.Day27`
- **Registry Integration:** `ExperimentScreenRegistry.screens["acoustic_beats"]`
- **Model Definition:** `Experiment.kt` (`id = "acoustic_beats"`, `day = 27`, `category = WEEK_4_WAVES_SOUND`)

### Reactive State Variables
The interactive state is managed via Compose `mutableStateOf` variables:

| State Variable | Type | Initialization | Functional Role |
| :--- | :--- | :--- | :--- |
| `f1` | `Float` | `440.0f` | Primary tone frequency (Hz) |
| `f2` | `Float` | `442.0f` | Secondary tone frequency (Hz) |
| `amp1` | `Float` | `1.0f` | Tone 1 relative amplitude |
| `amp2` | `Float` | `1.0f` | Tone 2 relative amplitude |
| `isRunning` | `Boolean` | `true` | Simulation clock play/pause flag |
| `showComponents` | `Boolean` | `true` | Toggles individual tone waveform tracks |
| `showEnvelope` | `Boolean` | `true` | Toggles modulated outer envelope overlay |
| `showPhasor` | `Boolean` | `true` | Toggles rotating vector phasor circle gauge |
| `selectedPreset` | `BeatsPreset?` | `BeatsPreset.SLOW_BEAT` | Active experimental preset chip |
| `simTime` | `Float` | `0f` | Elapsed continuous simulation time |
| `touchCursorNormX` | `Float?` | `null` | Interactive touch scrub position on Canvas |
| `isAudioOn` | `Boolean` | `false` | Toggles live audio synthesizer playback |
| `masterVolume` | `Float` | `0.30f` | Master gain level for pure tone generator |
| `isBinaural` | `Boolean` | `false` | Toggles mono acoustic superposition vs stereo binaural output |

### Frame Loop & Physics Integration
- **High-Precision Clock:** Implemented with `withFrameNanos` inside a coroutine `LaunchedEffect(isRunning)`.
- **Dynamic Waveform Generation:** Samples 300 discrete points along the horizontal axis, mapping local space-time coordinates to exact trigonometric waveforms:
  - Wave 1 trace: CyanNeon line with dynamic phase argument.
  - Wave 2 trace: CoralNeon line with detuned frequency argument.
  - Combined Wave: Thick AmberVibrant wave showing constructive packets and pinch points.
  - Modulated Envelope: Dashed EmeraldNeon upper and lower boundary curves.
- **Phasor Circle Gauge:** Evaluates instantaneous complex phasors $\vec{A}_1(t) = A_1 e^{i \omega_1 t}$ and $\vec{A}_2(t) = A_2 e^{i \omega_2 t}$, rendering their head-to-tail vector addition and resultant amplitude in real-time.
- **Acoustic Power VU Meter:** Tracks instantaneous cycle-averaged acoustic intensity, pulsing smoothly with green and amber illumination during constructive antinodes.

### Cross-Platform Real-Time Audio Synthesizer
- **Native Pure Tone Synthesizer ([`AcousticTonePlayer`](file:///home/geo/AndroidStudioProjects/MyProjects/GitHub/SimplePhysics/shared/src/commonMain/kotlin/com/geosid/simplephysics/audio/AcousticTonePlayer.kt)):**
  - **JVM Desktop:** Utilizes native `javax.sound.sampled.SourceDataLine` streaming 16-bit signed stereo PCM at 44.1 kHz.
  - **Android:** Utilizes `android.media.AudioTrack` in streaming mode with `AudioAttributes.USAGE_MEDIA`.
  - **Continuous Phase Tracking:** Accumulates oscillator phase increments per sample ($\Delta\phi = 2\pi f / 44100$), ensuring zero clicking or phase discontinuities when dragging frequency sliders.
  - **Binaural Headphone Mode:** Routes $f_1$ exclusively to the left ear and $f_2$ to the right ear, allowing headphone users to study neural beat synthesis by the brain's superior olivary complex.
  - **Lifecycle Safety:** Managed via Compose `rememberAcousticTonePlayer()` and `DisposableEffect`, automatically stopping and releasing audio hardware resources when navigating away.

---

## 5. Suggested Investigations & Parameter Experiments

1. **The Musician's Zero-Beat Tuning Test:**  
   Set $f_1 = 440\text{ Hz}$. Gradually nudge $f_2$ from $445\text{ Hz}$ down toward $440.0\text{ Hz}$. Watch how the beat period $T_{\text{beat}}$ stretches from 200 ms to several seconds, until beats completely cease at exact unison.
2. **Unequal Amplitude Modulation:**  
   Reduce $A_2$ to $0.40$ while keeping $A_1 = 1.0$. Observe the waveform: the destructive node no longer pinches to zero amplitude. A residual tone remains audible even at minimum volume!
3. **The Roughness Transition:**  
   Select the `Roughness (15 Hz)` preset. Observe how closely packed the beat packets become, visualizing why the human ear experiences dissonance rather than rhythmic pulsation above 15 Hz.
