# Day 26: Doppler Effect & Sonic Boom

> **Week 4: Waves, Sound & Acoustic Resonance**  
> *Topic Subtitle: Wavefront Compression, Mach Cones & Supersonic Shockwave Acoustics*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Accelerate a sound emitter past Mach 1.0 and witness how compressed circular wavefronts coalesce into a violent conical sonic boom shockwave!

### Scientific Principles & Mechanism
When a stationary sound source vibrates at frequency $f_0$, it radiates concentric spherical acoustic pressure wavefronts that propagate radially outward through the air at the speed of sound $c \approx 343\text{ m/s}$.

When the source moves with velocity $\mathbf{v}_s$:
1. **Subsonic Regime ($M < 1.0$):**  
   The source chases its own emitted wavefronts. Ahead of the source, successive wave crests are squeezed closer together, shortening the observed wavelength $\lambda' = \lambda_0(1 - M)$ and raising the perceived pitch $f' = f_0 / (1 - M)$ (**Doppler Blue-Shift**). Behind the source, wavefronts are stretched apart, lowering the perceived pitch $f' = f_0 / (1 + M)$ (**Doppler Red-Shift**).

2. **Transonic Regime ($M \approx 1.0$):**  
   As the speed of the aircraft approaches the speed of sound ($M = 1.0$), it travels at the exact same speed as its own acoustic disturbances. Wavefronts can no longer outrun the nose; instead, they pile up on top of one another at the leading edge, forming an intense barrier of constructive interference known as the **Sound Barrier**. Local pressure and temperature drops behind this pressure ridge trigger instant water condensation, producing the spectacular **Prandtl-Glauert vapor cone**.

3. **Supersonic Regime ($M > 1.0$):**  
   Once the aircraft breaches Mach 1, it outruns its own sound. The source leads, and all spherical waves previously emitted expand behind it. By Huygens' principle, the constructive superposition of these spherical shells forms a conical envelope tangent to all spheres: the **Mach Cone**.
   - **Zone of Silence:** Any observer situated outside and ahead of the Mach cone cannot hear the aircraft at all.
   - **Sonic Boom Shock Front:** As the surface of the Mach cone sweeps across an observer, the piled-up acoustic pressure reaches them in a single abrupt pressure step ($\Delta P$), producing a thunderous double sonic boom.
   - **Zone of Hearing:** Behind the shock cone, the continuous engine roar finally reaches the observer.

### Laboratory & Real-World Protocol (Try It At Home)
> Stand safely on the sidewalk as an ambulance or train speeds past you with its siren blaring. Notice how the siren sounds sharply higher in pitch as it approaches, drops instantly in pitch the precise split-second it passes you, and drones at a noticeably lower pitch as it drives away!

---

## 2. Mathematical Formulation & The Three Pillars

### Pillar 1: Governing Law & Acoustic Doppler Frequency Shift
For an observer and sound source in relative motion along line of sight, the apparent frequency $f'$ received by the observer is governed by the classical acoustic Doppler equation:

$$
f' = f_0 \left( \frac{c \pm v_{\text{obs}}}{c \mp v_s} \right)
$$

where $c$ is the speed of sound in air, $v_s$ is source velocity, and $v_{\text{obs}}$ is observer velocity.

In 2D space, when a stationary observer is situated at position $\mathbf{r}_{\text{obs}}$ relative to a source moving at velocity $\mathbf{v}_s$ with angle $\theta$ between the flight path and line of sight:

$$
f'(\theta) = \frac{f_0}{1 - M \cos\theta}, \quad M = \frac{v_s}{c}
$$

- **Directly Ahead ($\theta = 0^\circ$):** $\cos(0) = 1 \implies f'_{\text{ahead}} = \frac{f_0}{1 - M}$
- **Directly Behind ($\theta = 180^\circ$):** $\cos(\pi) = -1 \implies f'_{\text{behind}} = \frac{f_0}{1 + M}$
- **Perpendicular Passage ($\theta = 90^\circ$):** $\cos(\pi/2) = 0 \implies f' = f_0$ (at instantaneous closest approach).

As $M \to 1^-$, the approaching frequency $f'_{\text{ahead}} \to \infty$, signifying the coalescence of infinite wave crests into a shock front.

---

### Pillar 2: Kinematics & Huygens Mach Cone Geometry
Consider a source moving along the $x$-axis at constant supersonic velocity $v_s = M c > c$. At time $t_e \in [0, t]$, the source was at $x_e = v_s t_e$. At current time $t$, the spherical wavefront emitted at $t_e$ has expanded to radius:

$$
R(t, t_e) = c (t - t_e)
$$

The envelope tangent to this family of expanding spheres forms a straight conical surface with apex at the source's instantaneous position $(x_s(t), y_s(t))$. 

From right-triangle trigonometry between the vertex, the center of an emitted sphere, and the point of tangency:

$$
\sin\mu = \frac{R(t, t_e)}{x_s(t) - x_e(t_e)} = \frac{c(t - t_e)}{v_s(t - t_e)} = \frac{c}{v_s} = \frac{1}{M}
$$

Thus, the half-opening angle of the Mach cone (Mach angle $\mu$) is given by:

$$
\mu = \arcsin\left(\frac{1}{M}\right), \quad M \ge 1
$$

- At $M = 1.0$: $\mu = \arcsin(1) = 90^\circ$ (flat vertical shock wave wall).
- At $M = \sqrt{2} \approx 1.414$: $\mu = 45^\circ$.
- At $M = 2.0$: $\mu = \arcsin(0.5) = 30^\circ$.
- As $M \to \infty$: $\mu \to 0^\circ$ (infinitely slender hypersonic needle cone).

---

### Pillar 3: Energy, Pressure Discontinuity & Sonic Boom (Rankine-Hugoniot Jump)
In supersonic flow, fluid information cannot propagate upstream against the supersonic stream. The pressure disturbance is compressed into a non-linear discontinuity of order $10^{-7}\text{ m}$ (mean free path of air molecules). 

Across this oblique shock front, the flow satisfies the Rankine-Hugoniot jump relations for compressible gas dynamics:

$$
\frac{P_2}{P_1} = 1 + \frac{2\gamma}{\gamma + 1}\left( M_n^2 - 1 \right)
$$

where $\gamma \approx 1.4$ is the adiabatic index of diatomic air, and $M_n = M \sin\mu = 1$ is the normal Mach component across the envelope. 

For an observer on the ground at distance $h_{\text{alt}}$ below flight level, the acoustic pressure profile arrives as a characteristic **N-wave** signature:
1. **Bow Shock:** Rapid positive pressure jump $+ \Delta P_{\text{max}}$.
2. **Expansion Trough:** Linear expansion below atmospheric pressure $- \Delta P_{\text{max}}$.
3. **Tail Shock:** Sudden pressure recovery back to ambient pressure.

The peak ground overpressure $\Delta P$ scales with aircraft length $L$, flight altitude $h_{\text{alt}}$, and Mach number:

$$
\Delta P_{\text{ground}} \approx K_s \cdot P_0 \cdot \frac{(M^2 - 1)^{1/8}}{h_{\text{alt}}^{3/4}} \cdot \left(\frac{L}{d}\right)^{1/2}
$$

This steep jump is perceived by human ears as the classic explosive **"Boom-Boom"** double shock pulse.

---

## 3. Physical Parameters & SI Units

| Symbol | Parameter Description | Range / Nominal Value | SI Unit |
| :--- | :--- | :--- | :--- |
| $c$ | Speed of sound in ambient air ($20^\circ\text{C}$) | $343.0$ | $\text{m/s}$ |
| $v_s$ | Aircraft flight speed | $0.0 - 857.5$ ($M \le 2.5$) | $\text{m/s}$ |
| $M$ | Mach number ($v_s / c$) | $0.0 - 2.50$ | Dimensionless |
| $f_0$ | Rest acoustic emission frequency | $1.5 - 5.5$ | $\text{Hz}$ (simulation pulse rate) |
| $f'_{\text{ahead}}$ | Apparent Doppler pitch ahead ($M < 1$) | $1.0 - 10.0 \times f_0$ | $\text{Hz}$ |
| $f'_{\text{behind}}$ | Apparent Doppler pitch behind | $0.28 - 1.0 \times f_0$ | $\text{Hz}$ |
| $\mu$ | Mach cone half-angle | $23.6^\circ - 90.0^\circ$ ($M \ge 1$) | Degrees ($^\circ$) / Rad |
| $\Delta P$ | Ground sonic boom overpressure | $50 - 150$ | $\text{Pa}$ ($\text{N/m}^2$) |
| $\gamma$ | Adiabatic heat capacity ratio of air | $1.40$ | Dimensionless |
| $h_{\text{alt}}$ | Flight altitude above observer | $500 - 15,000$ | $\text{m}$ |

---

## 4. Simulation Architecture & Kotlin Multiplatform Implementation

### Source Location
- **Primary Composable:** [`DopplerMachConesExperiment`](./DopplerMachConesExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week4.Day26`
- **Registry Integration:** `ExperimentScreenRegistry.screens["doppler_mach_cones"]`
- **UI Architecture:** Pure Compose Multiplatform Canvas with high-frequency frame animation clock.

### Numerical Stepping & Frame Loop
The simulation integrates aircraft motion and acoustic wavefront emissions using `withFrameNanos`:

```kotlin
// Numerical stepping inside LaunchedEffect
val jetSpeed = machNumber * soundSpeedPx
jetX += jetSpeed * dt

// Discrete acoustic wavefront emission
if (simTime - lastEmitTime >= 1f / sourceFreq) {
    wavefronts.add(SoundWavefront(emitX = jetX, emitY = 0f, birthTime = simTime))
    lastEmitTime = simTime
}

// Wavefront expansion radius
val radius = (simTime - wf.birthTime) * soundSpeedPx
```

### UI/UX Standards & Layout
- **Transparent HUD (`ExperimentHudCard`):** Uses transparent background with subtle science border (`ScienceBorder.copy(alpha = 0.35f)`), allowing expanding shockwaves to shine through cleanly.
- **Elevated Canvas Origin (`flightY = h * 0.38f`):** Places the supersonic flight trajectory in the upper 40% of the screen, leaving the ground plane and bottom 35% completely clear for the compact controls deck.
- **Compact Controls Deck:** Features side-by-side weight-balanced sliders, 4 quick preset chips (`Subsonic`, `Mach 1`, `Supersonic`, `Hypersonic`), and 34.dp action buttons.
- **Interactive Observer Station:** Ground observer station can be dragged anywhere in the lower canvas. When the supersonic Mach shock cone sweeps across the observer's coordinates, an explosive visual shock ring (`boomFlashAlpha`) triggers in real time!

---

## 5. Suggested Investigations & Experiments

1. **Subsonic Doppler Pitch Shift ($M = 0.65$):**  
   Drag the observer directly along the flight line. Compare the perceived wavelength ahead vs. behind and verify the formula $\lambda'_{\text{ahead}} / \lambda'_{\text{behind}} = (1 - M) / (1 + M)$.
2. **Sound Barrier Coalescence ($M = 1.00$):**  
   Observe how all wavefront circles touch tangentially at the aircraft's nose tip, creating an infinitely dense vertical sound barrier wall accompanied by the Prandtl-Glauert vapor cone.
3. **Supersonic Mach Cone Scaling ($M = 1.45 \to 2.20$):**  
   Increase Mach number from $1.45$ to $2.20$. Observe how the Mach angle $\mu$ narrows from $43.6^\circ$ down to $27.0^\circ$, verifying $\sin\mu = 1/M$.
4. **Observer Shock Wave Timing:**  
   Position the ground observer at the bottom right. As the supersonic jet flies overhead, notice that the observer experiences total silence until the amber Mach cone line hits their station, triggering the sonic boom flash!
