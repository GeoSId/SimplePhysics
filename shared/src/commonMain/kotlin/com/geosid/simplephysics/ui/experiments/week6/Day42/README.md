# Day 42: Kármán Vortex Street (Laminar to Turbulent Wake Shedding & Strouhal Number)

> **Week 6: Fluid Dynamics • Laminar Flow, Aerodynamics & Pressure Gradients**  
> *Topic Subtitle: Boundary Layer Separation, Vortex-Induced Vibrations (VIV) & The von Kármán Stability Ratio*

---

## 1. Physical Phenomenon & Intuitive Background

When a fluid stream encounters a bluff obstacle—such as a circular cylinder or bridge pier—the fluid accelerates along the upstream shoulders, creating a favorable pressure gradient ($\frac{\partial P}{\partial x} < 0$). However, past the widest diameter ($\theta \approx 82^\circ$), the flow encounters an adverse pressure gradient ($\frac{\partial P}{\partial x} > 0$). Viscous friction slows fluid particles inside the boundary layer to a complete halt, causing the boundary layer to detach from the cylinder surface.

At Reynolds numbers above the critical Hopf bifurcation threshold ($\text{Re}_c \approx 47$), this boundary layer separation becomes unstable. The shear layer curls up into concentrated vortices. As a vortex forms on one side, it draws fluid across the wake centerline, cutting off the supply of vorticity to the opposite shear layer and triggering the release of an oppositely rotating vortex. 

This self-sustaining instability creates a stunning staggered wake of alternating counter-rotating vortices known as the **von Kármán Vortex Street**.

### The 3 Pillars of Discovery

1. **Pillar 1: Boundary Layer Detachment & Reynolds Number Regimes ($\text{Re}$)**  
   The wake topology is governed by the ratio of inertial to viscous forces:
   $$\text{Re} = \frac{\rho \cdot v \cdot D}{\mu}$$
   - $\text{Re} < 5$: Unseparated Stokes creeping flow.
   - $5 \le \text{Re} < 47$: Stable, symmetric twin recirculation bubbles attached behind the cylinder.
   - $47 \le \text{Re} < 180$: Laminar 2D Kármán vortex street shedding at regular periodic intervals.
   - $180 \le \text{Re} < 300$: 3D wake transition with streamwise vortex loops (Mode A and Mode B instabilities).
   - $300 \le \text{Re} < 3 \times 10^5$: Subcritical turbulent wake; vortex street shedding remains coherent and periodic.

2. **Pillar 2: Strouhal Number & Shedding Frequency ($f = \text{St} \frac{v}{D}$)**  
   The non-dimensional frequency of vortex shedding is defined by the Strouhal number $\text{St}$:
   $$\text{St} = \frac{f \cdot D}{v}$$
   Across a wide subcritical range ($10^2 \le \text{Re} \le 10^5$), $\text{St}$ remains remarkably constant ($\text{St} \approx 0.198 \cdot (1 - \frac{19.7}{\text{Re}}) \approx 0.20$).

3. **Pillar 3: The von Kármán Spatial Stability Ratio ($h/l \approx 0.281$)**  
   Theodore von Kármán (1911) proved via potential vortex theory that an infinite double row of staggered point vortices is linearly stable to perturbations if and only if:
   $$\sinh\left(\frac{\pi h}{l}\right) = 1 \implies \frac{h}{l} = \frac{1}{\pi} \operatorname{arsinh}(1) \approx 0.28055$$
   where $h$ is the lateral separation between the vortex rows and $l$ is the longitudinal distance between consecutive same-signed vortices.

---

## 2. Mathematical Formulation & Governing Equations

### 2.1 Navier-Stokes Incompressible Momentum Equation
The fluid flow is governed by the incompressible Navier-Stokes equations:
$$\rho \left( \frac{\partial \mathbf{u}}{\partial t} + (\mathbf{u} \cdot \nabla)\mathbf{u} \right) = -\nabla P + \mu \nabla^2 \mathbf{u}$$
$$\nabla \cdot \mathbf{u} = 0$$

### 2.2 Boundary Layer Separation & Vorticity Generation
Vorticity $\boldsymbol{\omega} = \nabla \times \mathbf{u}$ is generated at the cylinder wall by the no-slip condition:
$$\omega_z = \frac{\partial v_y}{\partial x} - \frac{\partial v_x}{\partial y}$$
Separation occurs when the wall shear stress vanishes:
$$\tau_w = \mu \left. \frac{\partial u_\theta}{\partial r} \right|_{r=R} = 0 \quad (\theta_{\text{sep}} \approx 82^\circ)$$

### 2.3 Vortex-Induced Fluctuating Lift & Drag
The periodic shedding of asymmetric vortices creates an oscillating pressure distribution around the cylinder, inducing dynamic lateral and axial aerodynamic forces:
$$F_L(t) = \frac{1}{2} C_L \rho v^2 A \sin(2\pi f t)$$
$$F_D(t) = \bar{F}_D + \frac{1}{2} C_{D,\text{osc}} \rho v^2 A \cos(4\pi f t)$$

Notice that while the lift oscillates at the fundamental shedding frequency $f$, drag oscillates at twice the shedding frequency ($2f$) because a vortex is shed from either the top or bottom every half cycle ($T/2$).

---

## 3. Physical Parameters & SI Units

| Symbol | Parameter Description | Canonical Value | SI Unit |
| :--- | :--- | :--- | :--- |
| $v$ | Free-Stream Flow Velocity | $0.05 - 15.0$ | $\text{m/s}$ |
| $D$ | Cylinder Diameter | $1.0 - 60.0 \times 10^{-3}$ | $\text{m}$ |
| $\rho$ | Fluid Medium Density | $1.204$ (Air) / $998$ (Water) | $\text{kg/m}^3$ |
| $\mu$ | Fluid Dynamic Viscosity | $1.82 \times 10^{-5}$ (Air) / $1.002 \times 10^{-3}$ (Water) | $\text{Pa}\cdot\text{s}$ |
| $\text{Re}$ | Reynolds Number | $10^0 - 10^5$ | Dimensionless |
| $\text{St}$ | Strouhal Number | $0.16 - 0.22$ | Dimensionless |
| $f$ | Vortex Shedding Frequency | $\text{St} \cdot (v / D)$ | $\text{Hz}$ ($\text{s}^{-1}$) |
| $h/l$ | von Kármán Wake Aspect Ratio | $\approx 0.281$ | Dimensionless |
| $C_L$ | Fluctuation Lift Coefficient | $0.45$ (Peak) | Dimensionless |
| $C_D$ | Mean Drag Coefficient | $1.15 - 1.50$ | Dimensionless |

---

## 4. Compose Multiplatform Simulation Architecture

```
KarmanVortexExperiment (Composable)
 ├── State (flowVelocityMS, cylinderDiameterMm, fluidDensity, fluidViscosity)
 ├── Physical Calculus (Reynolds Re, Strouhal St, Frequency f, Lift F_L)
 ├── Canvas Render Pipeline:
 │    ├── Coordinate Grid with Neon Spatial Markers
 │    ├── Upstream Inflow & Potential Deflection Streamlines
 │    ├── Moving Tracer Streakline Particle Field
 │    ├── Alternating Counter-Rotating Kármán Vortex Cores
 │    │    ├── Top Row: Counter-Clockwise (CCW, CyanNeon)
 │    │    └── Bottom Row: Clockwise (CW, AmberVibrant)
 │    ├── von Kármán Spatial Ratio Guides (h/l ≈ 0.281)
 │    ├── Metallic Cylinder Obstacle with Separation Markers (±82°)
 │    └── Dynamic Oscillating Lift & Drag Force Vectors
 ├── ExperimentHudCard (Transparent Overlay, Live Metrics)
 └── Compact Controls Deck (Preset Chips, Paired Sliders, Mode Toggles)
```

---

## 5. Suggested Explorations

1. **Acoustic Whine of Telephone Wires (Aeolian Harp):**
   Select the `Singing Wire` preset ($D = 1.8\text{ mm}, v = 12\text{ m/s}$). Notice how shedding frequency shoots up to $\approx 1,300\text{ Hz}$, generating audible tones as wind whistles past cables.
2. **Subcritical Hopf Bifurcation:**
   Select `Attached Eddies` ($v = 0.08\text{ m/s}$ in Glycerin). Observe $\text{Re} \approx 1.4 < 47$: the wake remains perfectly symmetric with two stable recirculation bubbles clinging to the rear of the cylinder without shedding.
3. **Structural Resonance & Bridge Oscillations:**
   Observe the oscillating lift vector $F_L(t)$. When the shedding frequency $f$ matches the mechanical resonant frequency of a tall chimney, suspension bridge, or offshore oil spar, catastrophic Lock-In resonance occurs.
