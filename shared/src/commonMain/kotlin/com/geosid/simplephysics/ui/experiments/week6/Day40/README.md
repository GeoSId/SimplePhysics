# Day 40: Hydraulic Press Force Multiplier

> **Week 6: Fluid Dynamics • Laminar Flow, Aerodynamics & Pressure Gradients**  
> *Topic Subtitle: Pascal's Principle, Hydraulic Multiplication & Work Invariance*

---

## 1. Physical Phenomenon & Intuitive Background

### The Mechanical Wonder of Fluids
If you push a solid rod with a force of $10\text{ N}$, the other end transmits exactly $10\text{ N}$. But if you push on an enclosed volume of liquid through a narrow cylinder, the opposite end can lift an automobile weighing $15,000\text{ N}$ with effortless ease.

This is the **Hydraulic Press**, invented by Joseph Bramah in 1795 based on the principle discovered by French mathematician and physicist Blaise Pascal in 1653.

### Scientific Principles & Mechanism
1. **Pascal's Principle:** Any change in pressure applied to a completely enclosed, incompressible fluid is transmitted undiminished and instantaneously to every portion of the fluid and to the walls of its containing vessel:
   $$P_1 = P_2 = \frac{F_1}{A_1} = \frac{F_2}{A_2}$$
2. **Force Amplification (Mechanical Advantage):** Because pressure is force per unit area ($P = F/A$), equating the static pressures yields:
   $$F_2 = F_1 \left(\frac{A_2}{A_1}\right) = F_1 \left(\frac{D_2}{D_1}\right)^2$$
   If the output ram diameter $D_2$ is $10$ times wider than the input plunger diameter $D_1$, the cross-sectional area is $100$ times larger. A modest hand push of $50\text{ N}$ translates into a crushing output force of $5,000\text{ N}$ ($500\text{ kg}$)!
3. **Conservation of Energy & Volume (Work Invariance):** Physics offers no free energy. Because liquids are virtually incompressible, the fluid volume displaced down by the input plunger $\Delta V_1 = A_1 \cdot d_1$ must equal the fluid volume entering the output cylinder $\Delta V_2 = A_2 \cdot d_2$:
   $$d_2 = d_1 \left(\frac{A_1}{A_2}\right) = d_1 \left(\frac{D_1}{D_2}\right)^2$$
   The work done by the input force equals the work delivered to the crushed load (neglecting friction):
   $$W = F_1 \cdot d_1 = \left[F_2 \left(\frac{A_1}{A_2}\right)\right] \cdot \left[d_2 \left(\frac{A_2}{A_1}\right)\right] = F_2 \cdot d_2$$
   To raise the crushing ram by $1\text{ mm}$, the narrow input plunger must plunge downward by $100\text{ mm}$ ($10\text{ cm}$).

### Laboratory / Kitchen Protocol (Try It At Home)
1. Obtain two plastic medical syringes of different diameters: a narrow $5\text{ mL}$ syringe and a wide $50\text{ mL}$ syringe.
2. Fill both completely with water, removing all trapped air bubbles.
3. Connect their nozzles tightly with a flexible vinyl tube.
4. Push the narrow syringe with your thumb while holding the wide plunger against your palm:
5. *Observation:* The narrow plunger is remarkably easy to push and overpowers the wide syringe, effortlessly lifting heavy books placed on the wide plunger!

---

## 2. Mathematical Foundation & Governing Equations

### 1. Pascal's Law of Hydrostatics
In an enclosed equilibrium fluid under negligible gravitational head ($\rho g h \ll P$):

$$
P_1 = P_2 = \text{const} \iff \frac{F_1}{A_1} = \frac{F_2}{A_2}
$$

### 2. Output Force & Ideal Mechanical Advantage
The output force $F_2$ delivered by the hydraulic ram is:

$$
F_2 = F_1 \cdot \text{MA} = F_1 \left(\frac{A_2}{A_1}\right) = F_1 \left(\frac{D_2}{D_1}\right)^2
$$

Where:
- $F_1$ is the input effort force ($N$)
- $D_1$ is the input plunger diameter ($m$)
- $A_1 = \frac{\pi}{4} D_1^2$ is the input piston area ($m^2$)
- $D_2$ is the output ram diameter ($m$)
- $A_2 = \frac{\pi}{4} D_2^2$ is the output piston area ($m^2$)
- $\text{MA} = \frac{A_2}{A_1}$ is the ideal mechanical advantage

### 3. Incompressible Volume Conservation & Stroke Kinematics
By conservation of fluid volume:

$$
\Delta V = A_1 \cdot d_1 = A_2 \cdot d_2 \implies d_2 = d_1 \left(\frac{A_1}{A_2}\right)
$$

### 4. Conservation of Mechanical Work
Assuming zero frictional seal losses and ideal laminar oil flow:

$$
W_{\text{in}} = F_1 \cdot d_1 = P \cdot \Delta V = F_2 \cdot d_2 = W_{\text{out}}
$$

### 5. Crush Resistance & Material Yield Stress
When the output ram contacts a solid specimen, deformation begins when the compressive stress exceeds the material's yield strength $\sigma_y$:

$$
\sigma = \frac{F_2}{A_{\text{contact}}} \ge \sigma_y
$$

For structural materials:
- Aluminum beverage can buckling threshold: $F_{\text{crush}} \approx 250\text{--}400\text{ N}$
- Structural steel plastic yield: $\sigma_y \approx 250\text{--}400\text{ MPa}$ ($F_{\text{crush}} > 10\text{ kN}$)

### 6. SI Parameter Reference Table

| Symbol | Parameter Description | Nominal SI Value | Units |
| :--- | :--- | :--- | :--- |
| $D_1$ | Input plunger inner diameter | $0.010\text{--}0.030$ | $\text{m}$ |
| $D_2$ | Output ram inner diameter | $0.060\text{--}0.200$ | $\text{m}$ |
| $A_2 / A_1$ | Hydraulic area multiplication ratio | $4.0\text{--}256.0$ | dimensionless |
| $F_1$ | Input effort force | $10\text{--}250$ | $\text{N}$ |
| $F_2$ | Output crush load force | $200\text{--}40,000$ | $\text{N}$ |
| $P$ | Internal system fluid pressure | $0.5\text{--}50 \times 10^5$ | $\text{Pa}$ ($\text{bar}$) |
| $d_1$ | Input plunger displacement | $0.02\text{--}0.15$ | $\text{m}$ |
| $d_2$ | Output ram displacement | $0.0002\text{--}0.010$ | $\text{m}$ |
| $W$ | Conserved mechanical work | $1\text{--}35$ | $\text{J}$ |
| $\rho_{\text{oil}}$ | Hydraulic oil density | $870$ | $\text{kg/m}^3$ |

---

## 3. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`HydraulicPressExperiment`](./HydraulicPressExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week6.Day40`
- **Registry ID:** `"hydraulic_press"`
- **Architecture:** Compose Multiplatform with Canvas rendering, real-time piston kinematics, crush deformation dynamics, and responsive controls deck.

### Presets Implemented
1. **Soda Can:** Thin-walled aluminum beverage can buckling and crimping into an accordion disc under $1\text{ kN}$ load.
2. **Rubber Ball:** Elastic solid sphere squishing into a wide bulging oval, storing strain energy.
3. **Steel Block:** Heavy forged industrial steel block yielding under $12\text{ kN}$ with glowing stress fracture lines.
4. **Car Jack:** Automotive bottle jack hoisting a $1.5\text{ metric ton}$ vehicle axle.
5. **Diamond Anvil:** Ultra-high amplification ($256\times$) micro-anvil generating extreme gigapascal pressure.

### Reactive State Variables
- `inputForceN`: Input manual effort force ($10\text{--}250\text{ N}$).
- `inputDiameterMm`: Plunger diameter ($10\text{--}40\text{ mm}$).
- `outputDiameterMm`: Ram diameter ($40\text{--}200\text{ mm}$).
- `inputStrokeFrac`: Normalized plunger stroke ($0.05\text{--}0.95$).
- `isAutoCycling`: Toggles automated sinusoidal pumping cycle.
- `showPressureField`: Toggles chromatic pressure heatmap inside hydraulic oil.
- `showForceVectors`: Renders downward $F_1$ and massive upward $F_2$ vector arrows.

---

## 4. Suggested Investigations & Parameter Experiments
1. **Diameter Ratio Quadratic Scaling:** Double the output ram diameter $D_2$ while keeping $D_1$ and $F_1$ fixed. Verify that the output force $F_2$ quadruples ($4\times$), demonstrating the $(D_2/D_1)^2$ scaling law.
2. **Stroke vs. Force Trade-Off:** Increase mechanical advantage to $100\times$. Notice that while $F_2$ is massive, the ram movement $d_2$ becomes microscopic, confirming work invariance $F_1 d_1 = F_2 d_2$.
3. **Yield Stress Limit:** Test the Steel Block preset with small input force vs. maximum input force. Observe how deformation only initiates when $F_2$ surpasses the material yield threshold.
