# Day 37: Toroidal Vortex Rings

> **Week 6: Fluid Dynamics • Aerodynamic Flow, Circulation & Turbulence**  
> *Topic Subtitle: Kelvin's Circulation Theorem & Vortex Cores*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Blast an invisible air vortex ring from a simple paper cup across an entire room to extinguish a burning candle 2 meters away, or watch two toroidal smoke rings leapfrog through each other in a mesmerizing periodic dance!

### Scientific Principles & Mechanism
A **vortex ring** (toroidal vortex) is one of nature's most robust and coherent coherent structures. While a straight turbulent jet rapidly mixes with ambient air and disperses within a few orifice diameters, a toroidal vortex ring rolls up into a self-contained, closed-loop fluid donut that travels vast distances carrying its own fluid mass, momentum, and energy:

1. **Roll-up at Sharp Orifice Lips:**  
   When a slug of fluid is impulsively ejected through a circular orifice of radius $R_0$, the boundary layer along the orifice wall experiences intense viscous shear stress. As the fluid emerges into the quiescent ambient medium, this cylindrical vortex sheet separates from the sharp rim and curls into a spiral, rolling into a closed toroidal core.

2. **Self-Induced Forward Translation:**  
   A straight vortex line induces circular streamlines around itself via the Biot-Savart law. When bent into a closed circular loop of radius $R$, opposite segments of the ring induce fluid velocity on each other. The upper segment (rotating counter-clockwise) induces a forward velocity on the lower segment, and the lower segment (rotating clockwise) induces an identical forward velocity on the upper segment. As a result, the entire torus propels itself autonomously forward along its axis of rotational symmetry!

3. **Helmholtz Vortex Leapfrogging:**  
   When two coaxial vortex rings of identical circulation are launched in sequence:
   - The leading ring induces an outward radial flow on the trailing ring, while the trailing ring induces a forward and inward radial flow on the leading ring.
   - The rear ring contracts in radius ($R_2 \downarrow$) and accelerates forward ($U \propto 1/R$).
   - The front ring expands ($R_1 \uparrow$) and decelerates.
   - The contracted rear ring shoots right through the center of the expanded front ring, taking the lead!
   - Once ahead, the roles reverse, creating an endless, graceful **leapfrogging dance**.

4. **Natural and Technological Occurrences:**  
   - **Volcanic Eruptions:** Mt. Etna frequently puffs giant steam vortex rings hundreds of meters wide that float kilometers into the sky.  
   - **Marine Biology:** Dolphins and whales blow underwater bubble rings and play with them.  
   - **Heart Physiology:** During diastole, blood entering the left ventricle forms a toroidal vortex ring that optimizes ventricular filling and valve closure.  
   - **Combustion & Propulsion:** Pulse detonation engines and volcanic pyroclastic plumes.

### Laboratory & Kitchen Protocol (Try It At Home)
> 1. Take a 16 oz disposable plastic or paper drinking cup.  
> 2. Cut a neat circular hole ($2\text{ cm}$ diameter) in the center of the bottom base.  
> 3. Stretch a sheet of plastic wrap (or a cut piece of a balloon) over the wide top opening of the cup and secure it tightly with a rubber band to form a flexible drum membrane.  
> 4. Light a candle and place it on a table 1 to 2 meters away.  
> 5. Aim the bottom hole at the candle and tap the plastic membrane firmly with your finger:  
> 6. Watch the candle flame instantly flicker and extinguish from across the room! (Fill the cup with vapor from an incense stick to see the flying smoke donut in full glory!)

---

## 2. Mathematical Foundation & The Three Pillars

### Pillar 1: Governing Law & Bound Circulation (Kelvin's Circulation Theorem)
For an inviscid, barotropic fluid subject to conservative body forces, **Kelvin's Circulation Theorem** states that the circulation $\Gamma$ around any closed material contour moving with the fluid remains constant in time:

$$\frac{D\Gamma}{Dt} = 0$$

where circulation $\Gamma$ is defined as the line integral of fluid velocity:

$$\Gamma = \oint_{C} \mathbf{v} \cdot d\mathbf{r} = \iint_{S} \boldsymbol{\omega} \cdot d\mathbf{A}$$

By **Stokes' theorem**, circulation measures the total flux of vorticity $\boldsymbol{\omega} = \nabla \times \mathbf{v}$ trapped inside the toroidal core. In the roll-up of a piston jet of velocity $v_{\text{jet}}$ over stroke time $\Delta t$, the generated circulation is:

$$\Gamma \approx \frac{1}{2} \int_0^{\Delta t} v_{\text{jet}}^2(t) \, dt \approx 2\pi R_0 v_{\text{jet}}$$

Once detached, **Helmholtz's First Vortex Law** ensures that vortex lines move with the fluid particles, locking the circulation inside the toroidal ring.

---

### Pillar 2: Kinematics / Motion Constraint (Kelvin-Hicks Translation Speed)
For a circular vortex ring of major radius $R$ and small core radius $a$ ($a \ll R$) with uniform core vorticity, the self-induced translational speed along the $z$-axis of symmetry was derived by Lord Kelvin and W. M. Hicks:

$$U = \frac{\Gamma}{4\pi R} \left[ \ln\left(\frac{8R}{a}\right) - \frac{1}{4} \right]$$

For a Gaussian vortex core with viscous diffusion (Lamb-Oseen distribution), the constant becomes $\approx -0.558$.

#### Biot-Savart Induced Fluid Velocity Field:
At any point $(r, z)$ in cylindrical coordinates, the velocity field induced by the circular vortex filament is given by the Biot-Savart law:

$$\mathbf{v}(\mathbf{x}) = \frac{\Gamma}{4\pi} \oint \frac{d\mathbf{s} \times (\mathbf{x} - \mathbf{s})}{|\mathbf{x} - \mathbf{s}|^3}$$

In the center of the ring ($r = 0, z = 0$), the induced fluid velocity reaches its maximum along the forward axis:

$$v_z(0, 0) = \frac{\Gamma}{2R}$$

This powerful central jet streams fluid through the donut hole, maintaining the ring's forward momentum.

---

### Pillar 3: Energy / Work Conservation & Hydrodynamic Impulse
In unbounded fluid without external forces, the total hydrodynamic impulse $\mathbf{P}$ and kinetic energy $E$ of the vortex ring are strictly conserved:

1. **Hydrodynamic Impulse (Linear Momentum):**  
   The momentum carried by a circular vortex ring of radius $R$ in a fluid of density $\rho$ is:
   $$P = \pi \rho \Gamma R^2$$
   Because $P$ is conserved, as viscous diffusion causes the ring core to entrain ambient fluid and expand in radius ($R \uparrow$), the propagation speed must gradually decrease ($U \propto 1/R$).

2. **Kinetic Energy:**  
   The total kinetic energy of the circulating fluid is:
   $$E = \frac{1}{2} \rho \Gamma^2 R \left[ \ln\left(\frac{8R}{a}\right) - \frac{7}{4} \right]$$

3. **Leapfrogging Conservation:**  
   In coaxial dual-ring leapfrogging, the sum of individual impulses $P_{\text{total}} = P_1 + P_2$ remains invariant:
   $$\pi \rho \Gamma (R_1^2 + R_2^2) = \text{constant}$$
   As one ring contracts ($R_2 \downarrow$), the other must expand ($R_1 \uparrow$), ensuring exact momentum conservation during the periodic dance.

---

## 3. Physical Parameters & SI Units

| Symbol | Parameter Name | Default Simulation Value | SI Unit | Physical Role |
| :---: | :--- | :---: | :---: | :--- |
| $R_0$ | Orifice Radius | $6.0$ | $\text{cm}$ ($0.06\text{ m}$) | Inner radius of cannon nozzle generating the shear layer |
| $v_{\text{jet}}$ | Jet Pulse Speed | $12.0$ | $\text{m/s}$ | Piston stroke ejection velocity |
| $R$ | Ring Major Radius | $\approx 6.9$ | $\text{cm}$ ($0.069\text{ m}$) | Center-to-core radius of the toroidal smoke ring |
| $a$ | Vortex Core Radius | $\approx 1.5$ | $\text{cm}$ ($0.015\text{ m}$) | Cross-sectional radius of the rotating vorticity core |
| $\Gamma$ | Bound Circulation | $\approx 4.52$ | $\text{m}^2/\text{s}$ | Conserved line integral of fluid velocity around core |
| $U$ | Self-Induced Speed | $\approx 14.8$ | $\text{m/s}$ | Autonomous forward translational velocity |
| $\rho$ | Fluid Density | $1.225$ | $\text{kg/m}^3$ | Ambient air density at sea level ($15^\circ\text{C}$) |
| $P$ | Hydrodynamic Impulse | $\approx 26.2$ | $\text{mN}\cdot\text{s}$ | Conserved fluid momentum carried by the vortex bullet |
| $E$ | Ring Kinetic Energy | $\approx 31.4$ | $\text{mJ}$ | Total fluid kinetic energy contained within the torus |
| $\nu$ | Kinematic Viscosity | $0.18$ | Dimensionless factor | Viscous dissipation and core diffusion rate |

---

## 4. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`VortexRingsExperiment`](./VortexRingsExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week6.Day37`
- **Container:** `ResponsiveExperimentContainer` with transparent telemetry card and compact control deck.

### Aerodynamic Simulation Pipeline
1. **Toroidal Roll-Up & Translation:** Evaluates Kelvin-Hicks self-induced speed $U = \frac{\Gamma}{4\pi R}[\ln(8R/a) - 1/4]$, stepping ring coordinates across the domain.
2. **Dual-Ring Leapfrogging:** Simulates mutual Biot-Savart induction between coaxial rings, oscillating radii $R_1(t)$ and $R_2(t)$ in a periodic phase-locked dance.
3. **Candle Extinguisher Target:** Detects arrival of the ring at the target station, triggering flame deflection and smoke wisps.
4. **Interactive Controls:** Presets for Vortex Cannon, Helmholtz Leapfrogging, Solid Wall Impact, and High-Speed Micro Vortex.
