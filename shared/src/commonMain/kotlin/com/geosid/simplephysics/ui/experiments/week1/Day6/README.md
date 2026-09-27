# Day 6: Non-Newtonian Oobleck

> **Week 1: Home Physics • Mind-Bending Home & Kitchen Physics**  
> *Topic Subtitle: Shear-Thickening Fluid & Dilatancy*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** A liquid you can run across, but will swallow your foot if you stand still for a second.

### Scientific Principles & Mechanism
A dense suspension of cornstarch granules in water creates a classic **dilatant (shear-thickening)** non-Newtonian fluid, colloquially known as **Oobleck**.

Unlike Newtonian fluids (like pure water or oil) whose viscosity remains constant regardless of shear rate, Oobleck's apparent viscosity increases dramatically when subjected to mechanical stress:

1. **Low Shear Rate (Gentle Motion):** Water molecules freely lubricate the microscopic starch granules, allowing them to glide smoothly over one another. The fluid behaves like a regular viscous liquid.
2. **High Shear Rate (Rapid Impact / Strike):** Sudden mechanical shear pushes the starch granules into direct contact, squeezing out the thin lubricating water layer. Direct friction causes the irregular granules to interlock into rigid **force chains**, undergoing a discontinuous **jamming transition** that momentarily solidifies the mixture.
3. **Relaxation:** Once the stress is removed, interstitial water flows back between the particles, relaxing the force chains and causing the solid lattice to liquefy back into a puddle.

### Laboratory / Kitchen Protocol (Try It At Home)
> Mix 2 parts cornstarch with 1 part tap water in a bowl. Tap the surface rapidly with your knuckles—it feels like a hard rubber wall! Now slowly rest your fingers on the surface—they sink smoothly to the bottom. Try squeezing a handful into a firm ball, then open your palm: it instantly melts and drips through your fingers!

---

## 2. Mathematical Foundation & The 3 Pillars

### Pillar 1: Governing Law & Shear Stress (Ostwald–de Waele Power Law)
The relationship between shear stress $\tau$ and shear strain rate $\dot{\gamma} = \frac{du}{dy}$ is governed by the Ostwald–de Waele power-law model:

$$\tau = K \cdot \left(\frac{du}{dy}\right)^n = K \cdot \dot{\gamma}^n$$

- For **Newtonian fluids**, $n = 1 \implies \tau = \mu \cdot \dot{\gamma}$ (linear response).
- For **Pseudoplastic fluids** (ketchup, paint), $n < 1$ (shear-thinning).
- For **Dilatant fluids** (Oobleck), $n > 1$ (shear-thickening).

In this simulation, the flow behavior index $n$ scales with starch concentration $C_{\text{starch}}$:

$$n = 1.4 + 1.0 \cdot C_{\text{starch}} \quad (1.95 \le n \le 2.15)$$

The fluid consistency index $K$ represents base viscosity at unit shear:

$$K = 0.08 + 0.15 \cdot C_{\text{starch}} \quad (\text{Pa}\cdot\text{s}^n)$$

### Pillar 2: Kinematics & Granular Jamming Transition
The apparent dynamic viscosity $\eta(\dot{\gamma})$ is the instantaneous ratio of shear stress to shear rate:

$$\eta(\dot{\gamma}) = \frac{\tau}{\dot{\gamma}} = K \cdot \dot{\gamma}^{n - 1}$$

Because $n > 1$, the apparent viscosity $\eta$ diverges non-linearly with increasing velocity gradients. The mechanical jamming fraction $\phi_{\text{jam}}$ models the percolation of rigid contact networks:

$$\phi_{\text{target}} = \text{clamp}\left(\frac{\dot{\gamma}}{\dot{\gamma}_{\text{crit}}}, 0, 1\right), \quad \dot{\gamma}_{\text{crit}} = 25\text{ s}^{-1}$$

$$\frac{d\phi_{\text{jam}}}{dt} = \frac{\phi_{\text{target}} - \phi_{\text{jam}}}{\tau_{\text{relax}}}$$

Under high shear ($\phi_{\text{jam}} > 0.55$), the fluid undergoes a transition into an elastic-plastic solid lattice capable of resisting penetration and fracturing along stress concentration lines.

### Pillar 3: Energy & Viscous Dissipation Rate
Mechanical work done on the fluid by external shear is converted into internal dissipation per unit volume:

$$P_{\text{diss}} = \tau \cdot \dot{\gamma} = \eta(\dot{\gamma}) \cdot \dot{\gamma}^2 = K \cdot \dot{\gamma}^{n + 1}$$

The total dissipated work during an impact of duration $\Delta t$ is:

$$W_{\text{diss}} = \int_0^{\Delta t} \int_V \tau(\mathbf{r}, t) \cdot \dot{\gamma}(\mathbf{r}, t) \, dV \, dt$$

During a high-velocity strike, this extreme dissipation rate absorbs projectile kinetic energy almost entirely within the first few millimeters of depth, generating macroscopic rebound forces.

---

## 3. Physical Parameters & SI Units

| Symbol | Parameter Description | Nominal Range | Default Value | SI Unit |
| :--- | :--- | :--- | :--- | :--- |
| $\dot{\gamma}$ | Shear Strain Rate | $0.0 - 120.0$ | Live ($0.0$) | $\text{s}^{-1}$ |
| $\tau$ | Shear Stress | $0.0 - 50.0$ | Live ($0.0$) | $\text{kPa}$ |
| $\eta$ | Apparent Dynamic Viscosity | $0.10 - 25.0$ | $0.12$ | $\text{Pa}\cdot\text{s}$ |
| $K$ | Fluid Consistency Index | $0.08 - 0.23$ | $0.18$ | $\text{Pa}\cdot\text{s}^n$ |
| $n$ | Flow Behavior Index (Dilatant Exponent) | $1.90 - 2.15$ | $2.05$ | Dimensionless |
| $C_{\text{starch}}$ | Cornstarch Mass Concentration | $0.50 - 0.75$ | $0.65$ ($65\%$) | Dimensionless |
| $\phi_{\text{jam}}$ | Granular Jamming Solid Fraction | $0.0 - 1.0$ | $0.0$ (Liquid) | Dimensionless |
| $v_{\text{impact}}$ | Impact Tool Velocity | $0.05 - 6.0$ | Variable | $\text{m}/\text{s}$ |
| $\tau_{\text{relax}}$ | Viscoelastic Jamming Relaxation Time | $0.08 - 0.15$ | $0.125$ | $\text{s}$ |

---

## 4. Simulation Architecture & Numerical Stepping

### Source Location
- **Primary Composable:** [`OobleckExperiment`](./OobleckExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week1.Day6`
- **UI Architecture:** Jetpack Compose Multiplatform Canvas with elevated origin ($c_y = 0.40 \cdot h$) and transparent telemetry HUD.

### Numerical Integration Loop
1. **Frame Tick:** Driven by `withFrameNanos` in a continuous coroutine with clamped step $\Delta t \in [0.001\text{ s}, 0.033\text{ s}]$.
2. **Shear Rate Evaluation:**
   - In `TOUCH_SWIPE`, $\dot{\gamma} = \frac{\Delta r}{\Delta t} \cdot S_{\text{scale}}$. Idle touch decays at $25\text{ s}^{-2}$.
   - In `FAST_STRIKE`, periodic hammer strike injects $\dot{\gamma} \approx 65 \cdot (1 + C_{\text{starch}})\text{ s}^{-1}$.
   - In `SLOW_DIP`, smooth descent caps shear at $\dot{\gamma} \approx 1.2\text{ s}^{-1}$.
   - In `KNEAD_MELT`, continuous harmonic agitation produces $\dot{\gamma} = 45 + 15\sin(40t)\text{ s}^{-1}$.
3. **Apparent Viscosity Calculation:**
   $$\eta = K \cdot \dot{\gamma}^{n - 1}$$
4. **Stress & Jamming State Update:**
   $$\tau = \frac{\eta \cdot \dot{\gamma}}{1000} \quad (\text{kPa})$$
   $$\phi_{\text{jam}} \mathrel{+}= (\phi_{\text{target}} - \phi_{\text{jam}}) \cdot \min(1, 8 \cdot \Delta t)$$

### Procedural Rendering Pipeline
- **Elevated Canvas Origin:** Center positioned at $(0.50 \cdot w, 0.40 \cdot h)$ with constrained dish radius $\min(0.38 \cdot w, 0.28 \cdot h)$, ensuring the bottom $35\%$ of viewport is unobstructed for the control deck.
- **Surface Color Dynamics:** Interpolates from milky liquid turquoise (`#80DEEA`) to crystalline chalky cyan (`#E0F7FA`) based on $\phi_{\text{jam}}$.
- **Dynamic Fracturing:** High shear strikes trigger radial crystalline crack webs; gentle perturbations produce expanding concentric capillary ripples.
- **Transparent HUD:** `ExperimentHudCard` with transparent background reveals ripples and background grid underneath.
