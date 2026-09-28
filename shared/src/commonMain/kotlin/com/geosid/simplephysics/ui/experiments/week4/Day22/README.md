# Day 22: Plucked String (1D Wave Equation)

> **Week 4: Waves, Sound & Acoustic Resonance • Days 22–28**  
> *Topic Subtitle: 1D Hyperbolic Wave Equation, Boundary Reflections & Harmonic Standing Waves*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Touch and pluck a taut musical string anywhere along its length. Watch wave packets reflect, invert at boundaries, and form standing harmonic nodes!

### Scientific Principles & Mechanism
When a taut flexible string is plucked transversely from its equilibrium position, the tension creates an elastic restoring force that accelerates each mass segment back toward baseline. The initial localized triangular or sinusoidal disturbance splits into two identical wave packets propagating in opposite directions at the phase velocity $c = \sqrt{T / \mu}$.

Upon striking the rigidly clamped boundaries at $x = 0$ and $x = L$, the wave packets undergo a **phase inversion** of $\pi$ radians ($180^\circ$) to satisfy the fixed Dirichlet boundary constraint $u(0, t) = u(L, t) = 0$. Superposition of these counter-propagating traveling waves produces stationary **standing wave eigenmodes** featuring stationary **nodes** (points of permanent zero displacement) and **antinodes** (points of maximum oscillating amplitude).

### Laboratory / Kitchen Protocol (Try It At Home)
> Stretch a long rubber band between your fingers. Flick it sharply to see the wave pulse travel to the fixed end, invert upside down, and bounce back! Alternatively, pluck a guitar or violin string lightly while resting a fingertip exactly at the 12th fret (midpoint $L/2$) to hear the crisp octave harmonic node!

---

## 2. Mathematical Foundation & The 3 Pillars

```
                     ┌────────────────────────────────────────┐
                     │          THE 3 PILLARS OF DAY 22       │
                     └────────────────────────────────────────┘
                                         │
         ┌───────────────────────────────┼───────────────────────────────┐
         ▼                               ▼                               ▼
┌─────────────────────────┐ ┌─────────────────────────┐ ┌─────────────────────────┐
│     1. GOVERNING LAW    │ │      2. KINEMATICS      │ │  3. ENERGY CONSERVATION │
│  1D Damped Wave Eq.     │ │ Standing Eigenmodes &   │ │ Kinetic + Elastic Strain│
│  u_tt = c² u_xx - γ u_t │ │ Fixed Boundary Nodes    │ │ Energy Dissipation Rate │
└─────────────────────────┘ └─────────────────────────┘ └─────────────────────────┘
```

### Pillar 1: Governing Law & Dynamic Wave Equation
Consider an infinitesimal string element of length $\Delta x$, tension $T$, and linear mass density $\mu$. Newton's second law applied to transverse net forces yields the classical 1D hyperbolic wave equation with viscous air damping $\gamma$:

$$\mu \frac{\partial^2 u}{\partial t^2} = T \frac{\partial^2 u}{\partial x^2} - \gamma \mu \frac{\partial u}{\partial t}$$

Dividing by mass density $\mu$ defines the wave propagation phase speed $c$:

$$\frac{\partial^2 u}{\partial t^2} = c^2 \frac{\partial^2 u}{\partial x^2} - \gamma \frac{\partial u}{\partial t}, \quad \text{where } c = \sqrt{\frac{T}{\mu}}$$

Here:
- $u(x, t)$ is the transverse displacement from equilibrium at position $x$ and time $t$.
- $c$ is the characteristic speed of transverse mechanical shear waves in the medium $[\text{m/s}]$.
- $\gamma$ is the viscous damping coefficient $[1/\text{s}]$, modeling acoustic radiation and internal friction.

---

### Pillar 2: Kinematics, Motion Constraints & Standing Harmonics
The string is clamped firmly at both extremities, imposing homogeneous **Dirichlet boundary conditions**:

$$u(0, t) = 0, \quad u(L, t) = 0 \quad \forall t \ge 0$$

By separation of variables $u(x, t) = X(x) \cdot \Theta(t)$, the spatial solution yields quantized standing wave eigenmodes:

$$X_n(x) = \sin\left(\frac{n \pi x}{L}\right), \quad n \in \{1, 2, 3, \dots\}$$

The corresponding eigenfrequencies $f_n$ and wavelengths $\lambda_n$ form an integer harmonic series:

$$\lambda_n = \frac{2L}{n}, \quad f_n = \frac{c}{\lambda_n} = n \cdot \frac{c}{2L} = n \cdot f_1$$

- $n = 1$: **Fundamental mode** (first harmonic), $\lambda_1 = 2L$, zero interior nodes.
- $n = 2$: **Second harmonic** (first overtone / octave), node at $x = L/2$.
- $n = 3$: **Third harmonic**, nodes at $x = L/3, 2L/3$.

#### Numerical Stability: The CFL Condition
In discrete finite difference time domain (FDTD) stepping, stability requires the **Courant–Friedrichs–Lewy (CFL)** condition:

$$\mathcal{C} = \frac{c \cdot \Delta t}{\Delta x} \le 1$$

When $\mathcal{C} \le 1$, the numerical domain of dependence covers the physical domain of dependence, preventing divergent high-frequency blow-up.

---

### Pillar 3: Energy Conservation & Viscous Dissipation
The total mechanical energy $\mathcal{E}(t)$ of the vibrating string is the integral sum of kinetic energy density $\mathcal{T}$ and elastic potential strain energy density $\mathcal{V}$:

$$\mathcal{E}(t) = \int_0^L \left[ \underbrace{\frac{1}{2}\mu \left(\frac{\partial u}{\partial t}\right)^2}_{\text{Kinetic Energy Density}} + \underbrace{\frac{1}{2}T \left(\frac{\partial u}{\partial x}\right)^2}_{\text{Potential Strain Energy Density}} \right] dx$$

Taking the time derivative and applying integration by parts with $u(0, t) = u(L, t) = 0$:

$$\frac{d\mathcal{E}}{dt} = \int_0^L \left[ \mu \frac{\partial u}{\partial t}\frac{\partial^2 u}{\partial t^2} + T \frac{\partial u}{\partial x}\frac{\partial^2 u}{\partial x \partial t} \right] dx$$

Substituting the governing wave equation $\mu u_{tt} = T u_{xx} - \gamma \mu u_t$:

$$\frac{d\mathcal{E}}{dt} = -\gamma \mu \int_0^L \left( \frac{\partial u}{\partial t} \right)^2 dx \le 0$$

- In the absence of damping ($\gamma = 0$), $d\mathcal{E}/dt = 0$, and mechanical energy continuously oscillates between purely kinetic energy (as the string passes through the baseline) and purely elastic strain energy (at peak modal displacements).
- When $\gamma > 0$, energy exponentially dissipates into sound pressure waves (acoustic radiation) and microscopic heat.

---

## 3. Physical Parameters & SI Units

| Symbol | Quantity | Default Value | SI Unit | Description |
| :--- | :--- | :--- | :--- | :--- |
| $L$ | String Length | $0.80 \times W$ | $\text{m}$ (scaled) | Distance between fixed instrument bridges |
| $N$ | Node Resolution | $72$ | dimensionless | Number of discrete finite-difference grid points |
| $c$ | Wave Speed Factor | $1.0$ ($0.4 \dots 1.2$) | $\text{m/s}$ | Wave propagation phase speed $\sqrt{T/\mu}$ |
| $\gamma$ | Viscous Damping | $0.015$ ($0.002 \dots 0.05$) | $\text{s}^{-1}$ | Air drag and internal acoustic decay rate |
| $\Delta x$ | Spatial Grid Step | $L / (N - 1)$ | $\text{m}$ | Distance between adjacent string nodes |
| $\Delta t$ | Sub-step Time Delta | $\sim 0.002$ | $\text{s}$ | Sub-stepped integration increment ($8\times$ per frame) |
| $\mathcal{C}$ | Courant Parameter | $0.75 \cdot c \le 0.92$ | dimensionless | CFL stability ratio $c \Delta t / \Delta x$ |
| $y_{\text{base}}$ | Canvas Baseline | $0.40 \cdot H$ | $\text{px}$ | Elevated apparatus center in viewport |

---

## 4. Kotlin Multiplatform Compose Simulation Architecture

```
┌────────────────────────────────────────────────────────────────────────┐
│                      Compose UI Screen Viewport                        │
│                                                                        │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │ 📊 ACOUSTIC WAVE TELEMETRY (Transparent HUD Card)              │   │
│   │ Peak Displacement: 45.2 px  •  Wave Speed (c): 1.00x           │   │
│   │ Mode State: Vibrating Harmonics • Energy: 1420.5 a.u.          │   │
│   └────────────────────────────────────────────────────────────────┘   │
│                                                                        │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │ 🎸 Interactive Instrument Canvas (Elevated cy = 0.40 * h)      │   │
│   │   [Peg] ═══~^~^~^~ Vibrating String ~^~^~^~═══ [Peg]           │   │
│   │         ((((   Central Sound Hole & Ripples   ))))             │   │
│   └────────────────────────────────────────────────────────────────┘   │
│                                                                        │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │ ⚙️ Compact Controls Deck (Bottom 35% Reserved Viewport)         │   │
│   │   [n=1 (Fund.)] [n=2 (2nd Harm.)] [n=3 (3rd Harm.)] [Pluck]   │   │
│   │   [ Wave Speed (c) Slider ]  |  [ Damping (γ) Slider ]         │   │
│   │   "🎸 Drag finger on string to pluck!"          [ Reset ↺ ]    │   │
│   └────────────────────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────────────────────┘
```

### Numerical Discretization (Explicit Finite Difference)
The wave equation is solved on a 1D spatial lattice using central difference approximations:

$$\frac{\partial^2 u}{\partial t^2} \approx \frac{u_i^{n+1} - 2u_i^n + u_i^{n-1}}{\Delta t^2}$$

$$\frac{\partial^2 u}{\partial x^2} \approx \frac{u_{i+1}^n - 2u_i^n + u_{i-1}^n}{\Delta x^2}$$

$$\frac{\partial u}{\partial t} \approx \frac{u_i^n - u_i^{n-1}}{\Delta t}$$

Rearranging for the future state $u_i^{n+1}$:

$$u_i^{n+1} = 2u_i^n - u_i^{n-1} + r^2 \left( u_{i+1}^n - 2u_i^n + u_{i-1}^n \right) - \gamma \Delta t \left( u_i^n - u_i^{n-1} \right)$$

where $r = \mathcal{C} = c \Delta t / \Delta x$.

### Sub-stepped Numerical Loop in Kotlin
```kotlin
val subSteps = 8
val courantR = (0.75f * waveSpeedC).coerceAtMost(0.92f)
val r2 = courantR * courantR

for (s in 0 until subSteps) {
    for (i in 1 until nodeCount - 1) {
        val laplacian = curr[i + 1] - 2f * curr[i] + curr[i - 1]
        val velocity = curr[i] - prev[i]
        next[i] = 2f * curr[i] - prev[i] + r2 * laplacian - dampingFactor * velocity
    }
    // Clamped Dirichlet Boundary Conditions
    next[0] = 0f
    next[nodeCount - 1] = 0f

    for (i in 0 until nodeCount) {
        prev[i] = curr[i]
        curr[i] = next[i]
    }
}
```

---

## 5. Suggested Interactive Investigations
1. **Harmonic Node Exploration:** Tap the **2nd Harmonic ($n=2$)** preset and observe that the exact center of the string never moves, even while both halves oscillate with maximum velocity!
2. **Triangular Pluck vs. Pure Sinusoid:** Tap **Pluck (Triangle)** and observe how the sharp apex splits into two counter-propagating step waves that repeatedly invert upon bouncing off the bridges.
3. **CFL Dispersion Limit:** Increase string tension / wave speed to maximum ($1.2\times$) with minimal damping to observe high-frequency wave packet reflections.
