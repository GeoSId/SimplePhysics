# Day 33: Polarization & The 3-Filter Paradox

> **Week 5: Optics & Light • Refraction, Total Internal Reflection & Diffraction**  
> *Topic Subtitle: Malus's Law & Vector Field Projections*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Cross two 90° polarizing sheets to block 100% of light into complete darkness; then slide a 45° third polarizing filter between them and watch light mysteriously reappear!

### Scientific Principles & Mechanism
Light is a transverse electromagnetic wave consisting of coupled oscillating electric ($\vec{E}$) and magnetic ($\vec{B}$) fields propagating perpendicular to the direction of travel ($\hat{k}$). In natural or unpolarized light (from the Sun or an incandescent filament), the electric field vector fluctuates randomly across all angles perpendicular to the beam axis.

1. **Linear Polarizers & The Wire-Grid Mechanism:**  
   A linear polarizing sheet (such as Polaroid film invented by Edwin Land) contains aligned long-chain polymer molecules doped with iodine. Free electrons can move easily along the length of the molecular chains, absorbing the component of the electric field parallel to the chains via ohmic dissipation. The perpendicular component passes through with minimal attenuation. The transmission axis of the filter is thus perpendicular to the polymer chains.

2. **First Polarizer (Unpolarized to Linear):**  
   When unpolarized light of initial intensity $I_0$ passes through the first linear polarizer (Filter 1 oriented at angle $\theta_1 = 0^\circ$ vertical), exactly half the time-averaged electromagnetic power is transmitted:
   $$I_1 = \frac{1}{2} I_0$$
   The transmitted beam is now 100% linearly polarized with electric field amplitude $E_1 = \sqrt{2 I_1 / (\epsilon_0 c)} = E_0 / \sqrt{2}$ oriented strictly along the vertical axis.

3. **The Crossed 90° Blackout:**  
   If a second polarizer (Filter 3) is oriented at $\theta_3 = 90^\circ$ (horizontal, crossed at $90^\circ$ relative to Filter 1), the vertical electric field has zero component along the horizontal transmission axis:
   $$\vec{E}_1 \cdot \hat{n}_3 = E_1 \cos(90^\circ) = 0 \implies I_3 = I_1 \cos^2(90^\circ) = 0$$
   The two filters together absorb 100% of the light, producing total darkness.

4. **The Three-Filter Paradox:**  
   In classical intuition, inserting an additional absorbing material between two opaque barriers should only absorb *more* light, making the result even darker. However, inserting a third polarizer (Filter 2) angled at $\theta_2 = 45^\circ$ between the two crossed filters causes light to **reappear**:
   - The vertical electric field $\vec{E}_1$ arriving at Filter 2 is projected onto the $45^\circ$ diagonal transmission axis:
     $$E_2 = E_1 \cos(45^\circ - 0^\circ) = \frac{\sqrt{2}}{2} E_1$$
     $$I_2 = I_1 \cos^2(45^\circ) = \frac{1}{2} I_1 = \frac{1}{4} I_0$$
   - Crucially, the light exiting Filter 2 is now **diagonally polarized at $45^\circ$**! Its polarization state has been rotated by the measurement projection.
   - When this diagonal light strikes Filter 3 (at $90^\circ$), it is no longer perpendicular to the transmission axis:
     $$E_3 = E_2 \cos(90^\circ - 45^\circ) = \frac{\sqrt{2}}{2} E_2 = \frac{1}{2} E_1$$
     $$I_3 = I_2 \cos^2(45^\circ) = \frac{1}{2} I_2 = \frac{1}{4} I_1 = \frac{1}{8} I_0 = 12.5\% \ I_0$$
   - Light has reappeared with 12.5% of the original unpolarized intensity!

5. **Quantum Mechanical Interpretation (Projective Measurement):**  
   In quantum optics, each photon carries spin angular momentum $\pm \hbar$. A polarizer acts as a quantum projection operator $\hat{P}_\theta = |\theta\rangle\langle\theta|$. Because non-orthogonal projection operators do not commute ($\hat{P}_{90^\circ} \hat{P}_{45^\circ} \ne \hat{P}_{45^\circ} \hat{P}_{90^\circ}$), measuring the photon state at $45^\circ$ collapses its wave function into $|\nearrow\rangle$, which has a non-zero probability amplitude $\langle\rightarrow|\nearrow\rangle = 1/\sqrt{2}$ of passing the final horizontal detector.

### Laboratory & Kitchen Protocol (Try It At Home)
> 1. Obtain two pairs of polarized sunglasses or two polarizing films from a physics kit or discarded LCD monitor.  
> 2. Look through both lenses held together; rotate one lens by $90^\circ$ until the view turns completely black.  
> 3. Take a third polarized sunglass lens (or a clear plastic CD case which exhibits stress birefringence) and slide it between the two crossed lenses at a $45^\circ$ angle.  
> 4. Notice how the blackened area immediately lights up again!

---

## 2. Mathematical Foundation & The Three Pillars

### Pillar 1: Governing Law & Field Projection (Malus's Law)
Formulated by Étienne-Louis Malus in 1809, Malus's Law states that when completely linearly polarized light of intensity $I_{\text{in}}$ passes through an ideal analyzer whose transmission axis forms angle $\Delta\theta$ with the light's polarization vector, the transmitted intensity is:

$$I_{\text{out}} = I_{\text{in}} \cos^2(\Delta\theta)$$

In terms of the electric field amplitude $\vec{E}$:
$$\vec{E}_{\text{out}} = (\vec{E}_{\text{in}} \cdot \hat{n}_{\text{analyzer}}) \hat{n}_{\text{analyzer}} = E_{\text{in}} \cos(\Delta\theta) \hat{n}_{\text{analyzer}}$$

Because the time-averaged Poynting flux $S = \langle |\vec{E} \times \vec{B}| \rangle$ scales quadratically with field amplitude:
$$I = \frac{1}{2} \epsilon_0 c E^2 \propto E^2 \implies I_{\text{out}} = I_{\text{in}} \cos^2(\Delta\theta)$$

---

### Pillar 2: Kinematics / Motion Constraint (Sequential Filter Transmission)
For three sequential filters positioned along the optical rail with transmission axis angles $\theta_1$, $\theta_2$, and $\theta_3$:

1. **Initial Filter 1 Transmission:**
   $$I_1 = \frac{1}{2} I_0, \quad \vec{E}_1 = E_1 (\cos\theta_1 \hat{x} + \sin\theta_1 \hat{y})$$

2. **Middle Filter 2 Transmission (When Active):**
   $$I_2 = I_1 \cos^2(\theta_2 - \theta_1)$$
   $$\vec{E}_2 = E_1 \cos(\theta_2 - \theta_1) (\cos\theta_2 \hat{x} + \sin\theta_2 \hat{y})$$

3. **Final Filter 3 Transmission:**
   $$I_3 = I_2 \cos^2(\theta_3 - \theta_2) = I_1 \cos^2(\theta_2 - \theta_1) \cos^2(\theta_3 - \theta_2)$$

4. **Special Case ($\theta_1 = 0^\circ, \theta_3 = 90^\circ$):**
   $$I_3(\theta_2) = \frac{1}{2} I_0 \cos^2(\theta_2) \cos^2(90^\circ - \theta_2) = \frac{1}{2} I_0 \cos^2(\theta_2) \sin^2(\theta_2)$$
   Using the double-angle identity $\sin(2\theta) = 2 \sin\theta \cos\theta$:
   $$I_3(\theta_2) = \frac{1}{8} I_0 \sin^2(2\theta_2)$$

   The absolute maximum transmission occurs when $\sin^2(2\theta_2) = 1 \implies 2\theta_2 = 90^\circ \implies \theta_2 = 45^\circ$:
   $$I_{3, \text{max}} = \frac{1}{8} I_0 = 12.5\% \ I_0$$

---

### Pillar 3: Energy / Work Conservation & Quantum Projection
Energy conservation requires that all light not transmitted is absorbed by the conducting micro-wires of the polarizers as Joule heating:

$$P_{\text{absorbed, total}} = I_0 - I_3$$

- **Filter 1 Absorption:** Absorbs the orthogonal polarization component ($50\% I_0$).
- **Filter 2 Absorption:** Absorbs $I_1 - I_2 = I_1 \sin^2(\theta_2 - \theta_1)$. At $45^\circ$, absorbs $25\% I_0$.
- **Filter 3 Absorption:** Absorbs $I_2 - I_3 = I_2 \sin^2(\theta_3 - \theta_2)$. At $45^\circ$, absorbs $12.5\% I_0$.
- **Transmitted Energy to Detector:** Exactly $12.5\% I_0$.

In the Quantum State representation:
$$|\psi_0\rangle = \frac{1}{\sqrt{2}} |V\rangle + \frac{1}{\sqrt{2}} |H\rangle$$
$$|\psi_1\rangle = \hat{P}_V |\psi_0\rangle = \frac{1}{\sqrt{2}} |V\rangle$$
$$|\psi_2\rangle = \hat{P}_{45^\circ} |\psi_1\rangle = \frac{1}{2} |\nearrow\rangle$$
$$|\psi_3\rangle = \hat{P}_H |\psi_2\rangle = \frac{1}{2\sqrt{2}} |H\rangle \implies P(H) = |\langle H | \psi_3 \rangle|^2 = \frac{1}{8}$$

---

## 3. Physical Parameters & SI Units

| Symbol | Parameter Name | Default Simulation Value | SI Unit | Physical Role |
| :---: | :--- | :---: | :---: | :--- |
| $I_0$ | Source Light Intensity | $1.0$ ($100\%$) | $\text{W/m}^2$ | Unpolarized optical radiation power density |
| $\theta_1$ | Polarizer 1 Angle | $0.0^\circ$ (Vertical) | $\text{deg}$ / $\text{rad}$ | Initial polarization selection transmission axis |
| $\theta_2$ | Middle Filter 2 Angle | $45.0^\circ$ (Diagonal) | $\text{deg}$ / $\text{rad}$ | Rotatable paradox filter transmission axis |
| $\theta_3$ | Analyzer Filter 3 Angle | $90.0^\circ$ (Horizontal) | $\text{deg}$ / $\text{rad}$ | Final detection analyzer transmission axis |
| $I_1$ | Post-Filter 1 Intensity | $0.50$ ($50.0\%$) | $\text{W/m}^2$ | Linearly polarized intensity after first filter |
| $I_2$ | Post-Filter 2 Intensity | $0.25$ ($25.0\%$) | $\text{W/m}^2$ | Intermediate projected intensity at $45^\circ$ |
| $I_3$ | Final Transmitted Intensity | $0.125$ ($12.5\%$) | $\text{W/m}^2$ | Final optical intensity reaching the photodetector |
| $E_3 / E_0$ | Normalized E-Field Amplitude | $0.354$ ($35.4\%$) | Dimensionless | Ratio of final electric field amplitude to initial field |

---

## 4. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`Polarization3FilterExperiment`](./Polarization3FilterExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week5.Day33`
- **Container:** `ResponsiveExperimentContainer` with transparent telemetry card and compact control deck.

### Optical Bench Pipeline
1. **Source Visualization:** Emits unpolarized oscillating multi-axis Poynting vectors.
2. **Filter Micro-Gratings:** Renders rotating disks with metallic micro-slits oriented along $\theta_1, \theta_2, \theta_3$.
3. **Dynamic Wave Tracing:** Renders traveling transverse sinusoidal $\vec{E}$-field wave paths between filter stations with amplitude dynamically scaled to $\sqrt{I}$.
4. **Interactive Paradox Toggle:** Allows dynamic insertion or extraction of Filter 2 to directly observe the transition between total crossed blackout ($0\%$) and restored transmission ($12.5\%$).
5. **Photodiode Spot Readout:** Visualizes the illuminated focal spot on the detector phosphor screen.

---

## 5. Suggested Investigations & Parameter Experiments

1. **The 45° Maximum:** Keep $\theta_1 = 0^\circ$ and $\theta_3 = 90^\circ$. Sweep $\theta_2$ from $0^\circ$ to $90^\circ$. Confirm that transmission peaks at exactly $\theta_2 = 45^\circ$ ($12.5\%$) and vanishes at both $0^\circ$ and $90^\circ$.
2. **Remove Filter 2:** Click "Remove Filter 2" in the 45° paradox configuration. Observe the immediate drop from $12.5\%$ to complete $0.0\%$ blackout.
3. **All Parallel Alignment:** Set all three filters to $0^\circ$. Observe maximum possible transmission of $50\% I_0$.
4. **Quantum Zeno Analogy:** Gradually step angles across small increments ($\theta_1 = 0^\circ, \theta_2 = 30^\circ, \theta_3 = 60^\circ$). Observe that smaller intermediate rotations preserve significantly more total light.
