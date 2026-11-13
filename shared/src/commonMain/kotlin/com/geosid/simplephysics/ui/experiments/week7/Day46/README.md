# Day 46: Stirling Cycle Heat Engine

> **Week 7: Thermodynamics • Kinetic Heat Theory, Entropy & Carnot Cycles**  
> *Topic Subtitle: Closed-Cycle Regenerative External Combustion*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Place a small desktop engine atop a steaming mug of hot tea or the palm of your hand. Within seconds, a flick of the brass flywheel sends the engine whirring silently at hundreds of RPM, powered purely by the temperature difference between the tabletop and the ambient room.

### Scientific Principles & Mechanism
Invented in 1816 by the Scottish clergyman Robert Stirling as a safe alternative to explosive high-pressure steam boilers, the Stirling engine operates on a closed thermodynamic cycle with external combustion:

1. **Closed Working Fluid:** The exact same mass of gas (typically air, helium, or hydrogen) is permanently sealed inside the engine. It is never exhausted or replaced.
2. **Thermal Shuttling:** Mechanically linked pistons shuttle the gas cyclically between a hot heat source ($T_H$) and a cold heat sink ($T_C$).
3. **Thermal Regenerator:** The true heart of Stirling's invention is the *regenerator*—a porous thermal sponge (wire mesh or porous ceramic matrix) situated between the hot and cold spaces. As hot gas moves toward the cold cylinder, the regenerator absorbs and stores its thermal energy. When the cold gas returns toward the hot cylinder, it reclaims that stored heat, virtually eliminating the energy cost of reheating.
4. **Volume Phase Offset:** A $90^\circ$ ($\pi/2$) crank phase shift between the expansion and compression pistons ensures that gas expands predominantly while hot (producing high pressure and positive work on the crank) and compresses predominantly while cold (requiring minimal mechanical work). The net area enclosed on the $P\text{--}V$ diagram represents useful shaft work delivered to the flywheel.

### Laboratory / Kitchen Protocol (Try It At Home)
> 1. Procure a Low-Temperature Difference (LTD) tabletop Stirling engine.
> 2. Fill a mug with near-boiling water ($T_H \approx 90^\circ\text{C} = 363\ \text{K}$).
> 3. Rest the flat aluminum base plate of the engine on the rim of the mug in room air ($T_C \approx 20^\circ\text{C} = 293\ \text{K}$).
> 4. Wait 30 seconds for the aluminum heat exchanger to conduct heat, then give the counterweighted flywheel a gentle clockwise flick.
> 5. Watch the engine run continuously for $20\text{--}40$ minutes until the water cools below the stall threshold ($\Delta T_{\text{min}} \approx 8\text{--}12\ \text{K}$).

---

## 2. Mathematical Foundation & Governing Equations

### 2.1 The 3 Theoretical Pillars

#### Pillar 1: The Ideal Stirling 4-Stroke Cycle
The ideal cycle consists of two isothermal processes connected by two isochoric regenerative processes:

1. **State $1 \to 2$: Isothermal Expansion at $T_H$**
   Gas expands at constant high temperature from $V_{\text{min}}$ to $V_{\text{max}}$.
   $$Q_{\text{in}} = W_{1\to 2} = n R T_H \ln\left(\frac{V_{\text{max}}}{V_{\text{min}}}\right)$$
   $$\Delta U_{1\to 2} = 0$$

2. **State $2 \to 3$: Isochoric Regenerative Cooling at $V_{\text{max}}$**
   Gas is shuttled through the regenerator matrix at constant volume. Heat is deposited into the regenerator:
   $$Q_{\text{regen,out}} = n C_v (T_H - T_C)$$
   $$W_{2\to 3} = 0$$

3. **State $3 \to 4$: Isothermal Compression at $T_C$**
   Gas is compressed by flywheel inertia at constant low temperature from $V_{\text{max}}$ to $V_{\text{min}}$:
   $$Q_{\text{out}} = -W_{3\to 4} = n R T_C \ln\left(\frac{V_{\text{max}}}{V_{\text{min}}}\right)$$
   $$\Delta U_{3\to 4} = 0$$

4. **State $4 \to 1$: Isochoric Regenerative Heating at $V_{\text{min}}$**
   Gas returns through the regenerator matrix at constant volume, recovering the stored heat:
   $$Q_{\text{regen,in}} = n C_v (T_H - T_C)$$
   $$W_{4\to 1} = 0$$

Under ideal regeneration ($Q_{\text{regen,in}} = Q_{\text{regen,out}}$), the internal heat transfers cancel out completely.

#### Pillar 2: Thermal Efficiency & Carnot Limit
The net indicated work per complete thermodynamic cycle is:

$$W_{\text{net}} = \oint P \, dV = W_{1\to 2} - |W_{3\to 4}| = n R (T_H - T_C) \ln\left(\frac{V_{\text{max}}}{V_{\text{min}}}\right)$$

The ideal thermal efficiency matches the fundamental Carnot efficiency:

$$\eta_{\text{ideal}} = \frac{W_{\text{net}}}{Q_{\text{in}}} = \frac{n R (T_H - T_C) \ln(r_v)}{n R T_H \ln(r_v)} = 1 - \frac{T_C}{T_H} = \eta_{\text{Carnot}}$$

With imperfect regenerator effectiveness ($\epsilon_{\text{regen}} < 1$), additional heat must be supplied during each cycle to make up for regenerator losses:

$$Q_{\text{in,actual}} = Q_{\text{isothermal}} + (1 - \epsilon_{\text{regen}}) \cdot n C_v (T_H - T_C)$$

$$\eta_{\text{actual}} = \frac{W_{\text{net}}}{Q_{\text{in,actual}}} < \eta_{\text{Carnot}}$$

#### Pillar 3: Schmidt Kinematics & Instantaneous Pressure
In practical sinusoidal-crank Alpha or Beta Stirling engines, cylinder volumes vary continuously:

$$V_H(\theta) = V_{H,\text{cl}} + \frac{V_{\text{sw}}}{2}(1 - \cos\theta)$$
$$V_C(\theta) = V_{C,\text{cl}} + \frac{V_{\text{sw}}}{2}(1 - \cos(\theta - \phi))$$

Where $\phi \approx 90^\circ$ is the kinematic crank phase offset between hot and cold cylinders.

Using Gustav Schmidt's classical closed-system isothermal analysis, total mass conservation ($M_{\text{total}} = \sum \frac{P V_i}{R T_i}$) yields the instantaneous cycle pressure:

$$P(\theta) = \frac{n R}{\dfrac{V_H(\theta)}{T_H} + \dfrac{V_C(\theta)}{T_C} + \dfrac{V_D}{T_D}}$$

Where $V_D$ is the dead volume (ducts + regenerator voids) at effective log-mean temperature $T_D \approx \frac{T_H - T_C}{\ln(T_H / T_C)}$.

The instantaneous gas torque acting on the crankshaft is:

$$\tau_{\text{gas}}(\theta) = [P(\theta) - P_{\text{atm}}] \cdot A_{\text{piston}} \cdot r_{\text{crank}} [\sin\theta + \sin(\theta - \phi)]$$

Governing flywheel angular acceleration $\omega$:

$$I_{\text{flywheel}} \frac{d\omega}{dt} = \tau_{\text{gas}}(\theta) - \tau_{\text{friction}}(\omega) - \tau_{\text{load}}$$

---

## 3. Physical Parameters & SI Units Table

| Parameter Symbol | Description | Typical Laboratory Value | High-Temp Power Value | SI Unit |
| :--- | :--- | :--- | :--- | :--- |
| $T_H$ | Hot Reservoir Temperature | $350\text{--}550$ | $850\text{--}1100$ | $\text{K}$ |
| $T_C$ | Cold Sink Temperature | $293\text{--}300$ | $285\text{--}320$ | $\text{K}$ |
| $r_v = V_{\text{max}} / V_{\text{min}}$ | Volumetric Compression Ratio | $1.2\text{--}1.8$ | $2.0\text{--}3.5$ | Dimensionless |
| $\phi$ | Hot-to-Cold Crank Phase Offset | $90.0^\circ$ ($\pi/2\ \text{rad}$) | $90.0^\circ$ | Degrees / Radians |
| $V_{\text{sw}}$ | Cylinder Swept Volume | $25\text{--}60$ | $200\text{--}800$ | $\text{cm}^3$ |
| $I_{\text{flywheel}}$ | Flywheel Mass Moment of Inertia | $0.015\text{--}0.025$ | $0.10\text{--}0.50$ | $\text{kg}\cdot\text{m}^2$ |
| $\epsilon_{\text{regen}}$ | Regenerator Thermal Effectiveness | $0.80\text{--}0.92$ | $0.92\text{--}0.98$ | Dimensionless ($0\text{--}1$) |
| $\eta_{\text{Carnot}}$ | Theoretical Maximum Carnot Limit | $15\text{--}45\%$ | $60\text{--}72\%$ | Dimensionless |
| $\tau_{\text{load}}$ | Mechanical Generator Load Torque | $0.05\text{--}0.25$ | $1.5\text{--}8.0$ | $\text{N}\cdot\text{m}$ |

---

## 4. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`StirlingEngineExperiment`](./StirlingEngineExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week7.Day46`
- **Framework:** Compose Multiplatform with Canvas rendering and real-time sub-nanosecond physics integrator.

### Key Simulation Engines
1. **Thermodynamic Schmidt Integrator (`withFrameNanos`):**
   - Evaluates continuous hot and cold swept volumes $V_H(\theta), V_C(\theta)$ alongside dead regenerator volume.
   - Computes closed-cycle pressure $P(\theta)$ via the Schmidt formulation.
   - Calculates indicated cycle work $W = \oint P dV$ and mechanical shaft power $\mathcal{P} = \tau \omega$.
2. **Flywheel Rotational Dynamics Engine:**
   - Evaluates instantaneous geometric gas pressure torque $\tau_{\text{gas}}(\theta)$.
   - Solves Newton's rotational law: $I \frac{d\omega}{dt} = \tau_{\text{gas}} - \tau_{\text{friction}} - \tau_{\text{load}}$.
   - Converts angular velocity to engine RPM.
3. **Kinetic Gas Shuttling & Regenerator Mesh:**
   - Tracks 42 microscopic gas molecules shuttling between the hot expansion chamber and cold compression cylinder.
   - Interpolates particle temperature and color (Cyan $\leftrightarrow$ Coral) based on local thermal relaxation.
4. **Real-Time Inset $P\text{--}V$ Indicator Diagram:**
   - Plots the closed 4-stage Stirling loop.
   - Renders a live dynamic operating marker $(V(t), P(t))$ tracing out the cycle perimeter in sync with flywheel rotation.

### Interactive Presets
- **High Performance (850 K):** Extreme temperature difference producing high torque and high RPM.
- **Candle Flame (560 K):** Typical desktop burner model running at medium load.
- **Palm Heat LTD (312 K):** Ultra-sensitive low-temperature difference engine running on body heat.
- **Braked Load Test:** High mechanical braking load evaluating engine stall torque.

---

## 5. Suggested Investigations & Experiments
1. **The Temperature Threshold (Stall Point):** Gradually decrease $T_H$ toward $T_C$. Observe how net indicated work shrinks until mechanical friction and generator load exceed driving torque, causing the engine to stall.
2. **Regenerator Impact on Efficiency:** Adjust the regenerator effectiveness slider $\epsilon$ from $0.95$ down to $0.20$. Notice how real Stirling thermal efficiency collapses even when temperatures remain fixed.
3. **Load vs. RPM Power Curve:** Slowly ramp up the external load torque $\tau_{\text{load}}$ while observing mechanical power output $\mathcal{P} = \tau \omega$. Identify the peak power operating point.
