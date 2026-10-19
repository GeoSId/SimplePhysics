# Day 39: Venturi Tube & Pressure Drop

> **Week 6: Fluid Dynamics • Laminar Flow, Aerodynamics & Pressure Gradients**  
> *Topic Subtitle: Mass Continuity, Dynamic Pressure & Bernoulli Suction*

---

## 1. Physical Phenomenon & Intuitive Background

### The Paradox of Fast Fluids
When water or air rushes into a narrow constriction, common intuition might suggest that squeezing fluid into a tighter space increases its pressure. In reality, the exact opposite occurs: **the static pressure plummets dramatically inside the throat.**

This phenomenon is the **Venturi Effect**, named after Italian physicist Giovanni Battista Venturi (1791), who discovered that fluid pressure drops when fluid flows through a constricted section of pipe.

### Scientific Principles & Mechanism
1. **Incompressible Mass Conservation (Continuity):** For an incompressible fluid, the volume flow rate $Q = A \cdot v$ must remain constant everywhere along the pipe. When the pipe diameter narrows from $D_1$ to $D_2$, the cross-sectional area decreases by $(D_1/D_2)^2$. To transport the same volume per second, the fluid must accelerate rapidly through the throat:
   $$v_2 = v_1 \left(\frac{D_1}{D_2}\right)^2$$
2. **Kinetic vs. Static Energy Exchange (Bernoulli's Principle):** As fluid accelerates into the constriction, its kinetic energy density $\frac{1}{2}\rho v^2$ increases sharply. Because the total mechanical energy along a streamline is conserved (in inviscid flow), this gain in kinetic energy must be paid for by an equal drop in static pressure $P$:
   $$P_1 + \frac{1}{2}\rho v_1^2 = P_2 + \frac{1}{2}\rho v_2^2 \implies P_2 = P_1 - \frac{1}{2}\rho (v_2^2 - v_1^2)$$
3. **Hydrostatic Manometer Height ($\Delta h$):** Vertical piezometer tubes attached to the pipe tap into the static pressure field. Because $P_2 < P_1$, the liquid column at the throat drops to a lower level than at the inlet:
   $$\Delta h = h_1 - h_2 = \frac{P_1 - P_2}{\rho g} = \frac{v_2^2 - v_1^2}{2g}$$
4. **Cavitation Threshold & Choked Flow:** If the throat velocity becomes high enough, the static pressure $P_2$ can drop below the saturation vapor pressure of the liquid ($P_{\text{vapor}} \approx 2.34\text{ kPa}$ for water at $20^\circ\text{C}$). When this occurs, water boils at room temperature, spawning transient vapor cavities (cavitation bubbles) that collapse destructively upon entering the high-pressure diffuser.

### Laboratory / Kitchen Protocol (Try It At Home)
1. Take an ordinary drinking straw and cut a horizontal slit halfway through about $1\text{ cm}$ from one end.
2. Bend the straw at a right angle, placing the vertical section into a cup of colored water.
3. Blow air hard and fast through the horizontal end.
4. *Observation:* The high-velocity jet of air over the vertical straw mouth drops the local static pressure below atmospheric pressure. The higher atmospheric pressure on the surface of the water in the cup forces liquid up the vertical straw, where the air jet shears it into a fine mist atomizer spray!

---

## 2. Mathematical Foundation & Governing Equations

### 1. Incompressible Continuity Equation
By conservation of fluid mass in a steady, one-dimensional stream tube:

$$
\dot{m} = \rho A_1 v_1 = \rho A_2 v_2 = \text{const}
$$

For constant density $\rho$:

$$
Q = A_1 v_1 = A_2 v_2 \implies v_2 = v_1 \left(\frac{A_1}{A_2}\right) = v_1 \left(\frac{D_1}{D_2}\right)^2
$$

### 2. Bernoulli's Equation along a Horizontal Streamline
Neglecting viscous dissipation and elevation differences ($\Delta z = 0$):

$$
P_1 + \frac{1}{2}\rho v_1^2 = P_2 + \frac{1}{2}\rho v_2^2
$$

The static pressure differential $\Delta P$ between the inlet and the constriction throat is:

$$
\Delta P = P_1 - P_2 = \frac{1}{2}\rho (v_2^2 - v_1^2) = \frac{1}{2}\rho v_1^2 \left[ \left(\frac{A_1}{A_2}\right)^2 - 1 \right]
$$

### 3. Venturi Flow Meter Discharge Equation
Solving for the inlet velocity $v_1$ in terms of the measured pressure difference $\Delta P$:

$$
v_1 = \sqrt{\frac{2(P_1 - P_2)}{\rho \left[ (A_1 / A_2)^2 - 1 \right]}}
$$

The theoretical volume flow rate $Q_{\text{ideal}}$ is:

$$
Q_{\text{ideal}} = A_1 v_1 = A_2 \sqrt{\frac{2 \Delta P / \rho}{1 - (A_2 / A_1)^2}}
$$

In real industrial meters, viscous boundary layers and non-uniform velocity profiles are accounted for by an empirical discharge coefficient $C_d \approx 0.96\text{--}0.98$:

$$
Q_{\text{actual}} = C_d A_2 \sqrt{\frac{2 \Delta P / \rho}{1 - (A_2 / A_1)^2}}
$$

### 4. Manometer Hydrostatic Differential
For an open vertical piezometer tube exposed to atmospheric pressure $P_{\text{atm}}$:

$$
h_1 = \frac{P_1 - P_{\text{atm}}}{\rho g}, \quad h_2 = \frac{P_2 - P_{\text{atm}}}{\rho g}
$$

$$
\Delta h = h_1 - h_2 = \frac{P_1 - P_2}{\rho g} = \frac{v_2^2 - v_1^2}{2g}
$$

### 5. SI Parameter Reference Table

| Symbol | Parameter Description | Nominal SI Value | Units |
| :--- | :--- | :--- | :--- |
| $\rho_{\text{water}}$ | Pure water density ($20^\circ\text{C}$) | $1000$ | $\text{kg/m}^3$ |
| $\rho_{\text{air}}$ | Sea-level ambient air density | $1.225$ | $\text{kg/m}^3$ |
| $D_1$ | Inlet pipe inner diameter | $0.035\text{--}0.060$ | $\text{m}$ |
| $D_2$ | Constriction throat inner diameter | $0.012\text{--}0.030$ | $\text{m}$ |
| $D_2 / D_1$ | Constriction diameter ratio | $0.25\text{--}0.85$ | dimensionless |
| $A_1 / A_2$ | Area contraction ratio $(D_1/D_2)^2$ | $1.4\text{--}16.0$ | dimensionless |
| $v_1$ | Inlet flow velocity | $0.5\text{--}25$ | $\text{m/s}$ |
| $v_2$ | Throat flow velocity | $1.5\text{--}100$ | $\text{m/s}$ |
| $P_1$ | Inlet absolute static pressure | $100\text{--}220 \times 10^3$ | $\text{Pa}$ ($\text{kPa}$) |
| $P_2$ | Throat absolute static pressure | $2\text{--}180 \times 10^3$ | $\text{Pa}$ ($\text{kPa}$) |
| $\Delta P$ | Static pressure drop $P_1 - P_2$ | $5\text{--}150 \times 10^3$ | $\text{Pa}$ ($\text{kPa}$) |
| $P_{\text{vapor}}$ | Water saturation vapor pressure ($20^\circ\text{C}$) | $2.34 \times 10^3$ | $\text{Pa}$ |
| $g$ | Standard acceleration of gravity | $9.81$ | $\text{m/s}^2$ |

---

## 3. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`VenturiTubeExperiment`](./VenturiTubeExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week6.Day39`
- **Registry ID:** `"venturi_tube"`
- **Screen Architecture:** Compose Multiplatform with Canvas rendering, real-time `withFrameNanos` fluid particle physics, and responsive controls deck.

### Presets Implemented
1. **Laboratory Venturi Flowmeter:** Water flowing through acrylic pipe with three vertical glass piezometer tubes showing measurable liquid height difference $\Delta h$.
2. **Automotive Carburetor:** High-velocity intake air stream dropping static pressure to suction and atomize gasoline from a float bowl reservoir.
3. **Perfume / Spray Atomizer:** High-speed air jet drawing liquid perfume up a vertical riser tube and shearing it into a fine aerosol mist.
4. **Hydrodynamic Cavitation Choke:** High inlet water velocity through a narrow throat where static pressure plunges below $2.34\text{ kPa}$, spawning transient boiling vapor bubbles.
5. **Subsonic Wind Tunnel:** Smooth convergent-divergent duct accelerating air into an aerodynamic test section.

### Reactive State Variables
- `inletVelocityMs`: Upstream fluid velocity $v_1$ ($0.5\text{--}30\text{ m/s}$).
- `throatRatio`: Constriction diameter ratio $D_2/D_1$ ($0.25\text{--}0.85$).
- `inletPressureKPa`: Upstream static pressure $P_1$ ($90\text{--}220\text{ kPa}$).
- `fluidDensityKgM3`: Fluid density $\rho$ ($1.225\text{ kg/m}^3$ air to $1000\text{ kg/m}^3$ water).
- `showStreamlines`: Toggles dynamic dashed flow streamlines crowding in the throat.
- `showManometers`: Toggles vertical glass piezometer columns showing hydrostatic levels.
- `showPressureHeatmap`: Renders continuous chromatic pressure gradient (Cyan $\to$ Purple $\to$ Amber).
- `showVelocityVectors`: Renders dynamic velocity vector arrows at inlet and throat.

### Dynamic Particle Integration
Fluid parcels accelerate through the narrowing nozzle according to local cross-sectional area:
```kotlin
// 1. Mass Continuity
val areaRatio = area1M2 / area2M2
val throatVelocityMs = inletVelocityMs * areaRatio

// 2. Bernoulli Pressure Drop
val deltaPPa = 0.5f * fluidDensityKgM3 * (throatVelocityMs * throatVelocityMs - inletVelocityMs * inletVelocityMs)
val throatPressurePa = max(0f, p1Pa - deltaPPa)

// 3. Hydrostatic Manometer Height
val deltaHM = (p1Pa - throatPressurePa) / (fluidDensityKgM3 * g)
```

---

## 4. Suggested Investigations & Parameter Experiments
1. **Square-Law Pressure Drop:** Double the inlet velocity $v_1$ while keeping the throat diameter fixed. Observe how $\Delta P$ and manometer column difference $\Delta h$ quadruple ($4\times$), verifying $\Delta P \propto v^2$.
2. **Cavitation Inception:** Choose the Cavitation preset and reduce the throat ratio slider to $0.25$. Note how $P_2$ drops below $2.34\text{ kPa}$, triggering cavitation bubble nucleation and diffuser collapse.
3. **Density Contrast:** Switch between Air ($\rho = 1.225\text{ kg/m}^3$) and Water ($\rho = 1000\text{ kg/m}^3$). Notice how the same velocity change produces nearly a thousand times greater pressure drop in water!
