# Day 24: Chladni Plates & Cymatics

> **Week 4: Waves & Acoustics • Sound Resonance, Doppler & Wave Interference**  
> *Topic Subtitle: Acoustic 2D Standing Nodal Geometry*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Vibrate a square metal plate at resonant acoustic frequencies and watch thousands of grains of fine sand spontaneously organize into breathtaking geometric mandalas and star-like nodal patterns.

### Scientific Principles & Mechanism
When a thin, elastic solid plate is driven by a periodic vertical oscillation at its center, transverse flexural waves propagate radially outward toward the edges and reflect back inward. At specific discrete eigenfrequencies, the incident and reflected waves interfere constructively to form **two-dimensional standing waves**.

Certain contour lines on the plate surface experience exactly zero vertical displacement throughout the entire vibrational cycle. These stationary curves are known as **nodal lines**. Grains of sand sprinkled across the plate are violently tossed into the air whenever they land on vibrating antinodal regions. When grains land near a nodal line, the plate displacement and acceleration vanish ($w(x, y) \approx 0$), allowing friction to capture the grains and hold them at rest. This process reveals the plate's hidden acoustic vibration geometry - a discipline known as **cymatics**, pioneered by Ernst Chladni in 1787.

### Laboratory / Kitchen Protocol (Try It At Home)
> Sprinkle fine dry salt or sand onto plastic wrap stretched tightly over a portable speaker cone. Use an online frequency generator app to sweep tones from 200 Hz to 1200 Hz. Watch concentric rings, diamonds, and intricate crosses emerge at distinct resonant frequencies!

---

## 2. Mathematical Foundation: The 3 Pillars

### Pillar 1: Governing Law & Biharmonic Flexural Wave Equation
The out-of-plane transverse displacement $w(x, y, t)$ of a thin, isotropic, elastic plate is governed by the **Kirchhoff-Love biharmonic plate equation**:

$$
D \nabla^4 w + \rho h \frac{\partial^2 w}{\partial t^2} = q(x, y, t)
$$

where:
- $\nabla^4 = \nabla^2(\nabla^2) = \frac{\partial^4}{\partial x^4} + 2\frac{\partial^4}{\partial x^2 \partial y^2} + \frac{\partial^4}{\partial y^4}$ is the two-dimensional biharmonic differential operator,
- $\rho$ is the volumetric mass density ($\text{kg/m}^3$),
- $h$ is the plate thickness ($\text{m}$),
- $D = \frac{E h^3}{12(1-\nu^2)}$ is the **flexural rigidity** ($\text{N}\cdot\text{m}$), where $E$ is Young's modulus and $\nu$ is Poisson's ratio,
- $q(x, y, t)$ represents external transverse driving loads (the central electromechanical exciter post).

### Pillar 2: Kinematics & Resonant Standing Nodal Eigenmodes
For steady-state harmonic excitation at angular eigenfrequency $\omega = 2\pi f$, the displacement factors into spatial and temporal components:

$$
w(x, y, t) = W(x, y) \cos(\omega t + \phi)
$$

Substituting into the biharmonic equation in the unforced interior yields the spatial Helmholtz-like biharmonic eigenvalue problem:

$$
\nabla^4 W(x, y) - k^4 W(x, y) = 0, \quad k^4 = \frac{\rho h \omega^2}{D}
$$

For a square plate with free boundaries driven at the center, Chladni observed that degenerate vibration modes with modal indices $(m, n)$ superpose linearly. In normalized spatial coordinates $(x, y) \in [-1, 1]^2$:

$$
W_{m,n}(x, y) = a \cos\left(\frac{m \pi x}{2}\right) \cos\left(\frac{n \pi y}{2}\right) - \alpha b \cos\left(\frac{n \pi x}{2}\right) \cos\left(\frac{m \pi y}{2}\right)
$$

where $\alpha$ is a modal coupling parameter determined by the boundary conditions and exciter alignment.

The **nodal lines** are the set of stationary points where displacement is identically zero:

$$
\mathcal{N} = \left\{ (x, y) \in [-1, 1]^2 \;\middle|\; W_{m,n}(x, y) = 0 \right\}
$$

The resonant frequencies follow **Chladni's Law**:

$$
f_{m,n} \propto (m + 2n)^p \quad \text{with } p \approx 2
$$

### Pillar 3: Energy Conservation & Cymatic Particle Drift
The total mechanical energy $\mathcal{E}$ stored in the vibrating plate is the integral sum of the kinetic energy and elastic bending strain energy:

$$
\mathcal{E} = \frac{1}{2} \iint_A \left\{ \rho h \left(\frac{\partial w}{\partial t}\right)^2 + D \left[ (\nabla^2 w)^2 - 2(1-\nu) \left( \frac{\partial^2 w}{\partial x^2}\frac{\partial^2 w}{\partial y^2} - \left(\frac{\partial^2 w}{\partial x \partial y}\right)^2 \right) \right] \right\} dA
$$

The time-averaged kinetic energy density on the plate surface is:

$$
\langle \mathcal{T}(x, y) \rangle = \frac{1}{4} \rho h \omega^2 |W(x, y)|^2
$$

When the plate's vertical acceleration exceeds gravity ($a_z = \omega^2 |W(x, y)| > g$), particles lose contact with the surface and experience bouncing impacts. The gradient of the local vibration intensity exerts an effective acoustic radiation drift force directed away from antinodes toward nodal lines:

$$
\vec{F}_{\text{drift}} = -\nabla \langle \mathcal{T}(x, y) \rangle \propto -\nabla \left( |W(x, y)|^2 \right) = -2 W(x, y) \nabla W(x, y)
$$

At the nodal lines ($W(x, y) = 0$), $\vec{F}_{\text{drift}} = \vec{0}$ and vertical vibration ceases, allowing sand grains to come to permanent rest under kinetic friction.

---

## 3. Physical Parameters & SI Units Table

| Symbol | Parameter Description | Nominal Value (App) | Standard SI Units |
| :--- | :--- | :--- | :--- |
| $L$ | Square Plate Edge Length | $0.30$ | $\text{m}$ |
| $h$ | Plate Thickness | $1.5 \times 10^{-3}$ | $\text{m}$ ($1.5\text{ mm}$) |
| $\rho$ | Mass Density (Steel) | $7850$ | $\text{kg/m}^3$ |
| $E$ | Young's Elastic Modulus | $2.1 \times 10^{11}$ | $\text{Pa}$ ($210\text{ GPa}$) |
| $\nu$ | Poisson's Ratio | $0.28$ | dimensionless |
| $D$ | Flexural Rigidity | $63.8$ | $\text{N}\cdot\text{m}$ |
| $m, n$ | Resonant Mode Indices | $1 \le m, n \le 6$ | integer (pure) |
| $\alpha$ | Modal Coupling Factor | $-1.0 \dots +1.0$ | dimensionless |
| $f_{m,n}$ | Resonant Acoustic Eigenfrequency | $108 \dots 1944$ | $\text{Hz}$ |
| $N_{\text{sand}}$ | Simulated Sand Grains | $800$ | count |

---

## 4. Simulation Architecture & Kotlin Compose Stepping

### Numerical Integration Loop
1. **Kinematic Gradient Evaluation**: In each 60 FPS frame tick (`withFrameNanos`), the application evaluates the analytical modal function $W(x, y)$ and its directional derivatives $\frac{\partial W}{\partial x}, \frac{\partial W}{\partial y}$ for all 800 sand particles.
2. **Cymatic Drift Impulse**:
   $$v_x \leftarrow v_x - 2 W \frac{\partial W}{\partial x} \cdot \beta + \xi_x, \quad v_y \leftarrow v_y - 2 W \frac{\partial W}{\partial y} \cdot \beta + \xi_y$$
   where $\beta$ is the drive amplitude factor and $\xi$ is stochastic bouncing jitter proportional to local vibration amplitude $|W|$.
3. **Frictional Dissipation & Inelastic Boundaries**: Damping factor $0.88$ models solid surface friction; particles hitting the plate edge reflect with restitution coefficient $0.50$.
4. **Real-Time Clustered Metric**: Particles within $|W| < 0.12$ are classified as clustered on nodal lines, providing a live percentage metric in the transparent HUD card.
5. **Interactive Touch Perturbation**: Direct tap and drag gestures perturb local sand grains, demonstrating dynamic re-assembly into standing nodal patterns.
