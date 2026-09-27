# Day 21: Chaotic Three-Body Problem

> **Week 3: Celestial Gravity & Orbits • Planetary Orbits & Spaceflight Dynamics**  
> *Topic Subtitle: Gravitational N-Body Chaos, Periodic Figure-8 Choreography & Stellar Ejection*

---

## 1. Physical Phenomenon & Intuitive Background

While the two-body Kepler problem is completely integrable and admits closed-form analytical solutions (conic sections: ellipses, parabolas, and hyperbolas), the introduction of just a **third celestial body** renders the general gravitational system analytically non-integrable. Discovered by Henri Poincaré in 1890 during King Oscar II's mathematics competition, this foundational insight birthed modern **deterministic chaos theory**.

In the general three-body problem:
- Minor variations in initial stellar positions or velocities diverge **exponentially** over time (positive Lyapunov exponent $\lambda > 0$).
- Despite deterministic laws, long-term orbital predictions become fundamentally impossible without high-precision numerical integration.
- Violent close encounters often lead to **gravitational slingshots and stellar ejection**, where one star is flung into deep interstellar space while the remaining two form a tightly bound, highly eccentric binary pair.
- Remarkably, rare periodic solutions exist—such as the famous **Figure-8 choreography** discovered numerically by Cristopher Moore (1993) and proved mathematically by Alain Chenciner and Richard Montgomery (2000), where three equal masses trace a single figure-eight loop in perpetual synchrony.

---

## 2. Mathematical Foundation & The 3 Pillars of Physics

```
┌────────────────────────────────────────────────────────────────────────┐
│                       THE 3 PILLARS OF DAY 21                          │
│                                                                        │
│   1. GOVERNING LAW & FORCE       ──▶ Pairwise Newton Gravity + Plummer │
│   2. KINEMATICS & CHAOS          ──▶ Poincaré Non-Integrability & λ    │
│   3. CONSERVATION PRINCIPLES     ──▶ Hamiltonian Energy & Barycenter   │
└────────────────────────────────────────────────────────────────────────┘
```

### Pillar 1: Governing Law & Force (Pairwise Gravitational Attraction)
Each star $i \in \{1, 2, 3\}$ experiences the vector sum of Newtonian gravitational forces from all other bodies $j 
e i$:

$$
m_i \mathbf{\ddot{r}}_i = \sum_{j 
e i} rac{G m_i m_j}{(|\mathbf{r}_j - \mathbf{r}_i|^2 + \epsilon^2)^{3/2}} (\mathbf{r}_j - \mathbf{r}_i)
$$

Dividing by $m_i$, the acceleration of body $i$ is:

$$
\mathbf{a}_i = rac{d\mathbf{v}_i}{dt} = \sum_{j 
e i} rac{G m_j (\mathbf{r}_j - \mathbf{r}_i)}{(|\mathbf{r}_j - \mathbf{r}_i|^2 + \epsilon^2)^{3/2}}
$$

where $\epsilon$ is the **Plummer softening length** (typically $6	ext{–}12	ext{ px}$ in screen units). Softening models finite stellar cores and suppresses non-physical infinite acceleration spikes during ultra-close numerical encounters.

---

### Pillar 2: Kinematics / Motion Constraint & Lyapunov Chaos
Because the system possesses 18 degrees of freedom (6 position/velocity coordinates per body) but only 10 classical integrals of motion (energy, linear momentum, angular momentum, and center-of-mass motion), Brun's theorem and Poincaré's theorem prove that **no additional algebraic integrals exist**.

The phase-space trajectory divergence between two arbitrarily close initial states $\delta \mathbf{Z}(0)$ obeys:

$$
|\delta \mathbf{Z}(t)| pprox |\delta \mathbf{Z}(0)| e^{\lambda t}
$$

where $\lambda > 0$ is the **maximal Lyapunov exponent**. The simulation continuously tracks a ghost shadow system perturbed by $\delta r(0) = 10^{-4}$ to compute the real-time Lyapunov index:

$$
\Lambda(t) = \ln \left( rac{|\mathbf{r}_3(t) - \mathbf{r}_{3,	ext{ghost}}(t)|}{|\delta \mathbf{r}(0)|} ight)
$$

---

### Pillar 3: Energy / Work & Momentum Conservation
In the absence of external non-conservative torques or drag, the **total mechanical energy** $E$ and **center of mass momentum** $\mathbf{P}$ are strictly conserved:

$$
E = K + U = \sum_{i=1}^3 rac{1}{2} m_i |\mathbf{v}_i|^2 - \sum_{1 \le i < j \le 3} rac{G m_i m_j}{\sqrt{|\mathbf{r}_j - \mathbf{r}_i|^2 + \epsilon^2}} = 	ext{const}
$$

The Center of Mass (Barycenter) position $\mathbf{R}_{	ext{cm}}$ and velocity $\mathbf{V}_{	ext{cm}}$ satisfy:

$$
\mathbf{R}_{	ext{cm}} = rac{\sum_{i=1}^3 m_i \mathbf{r}_i}{\sum_{i=1}^3 m_i}, \quad \mathbf{V}_{	ext{cm}} = rac{\sum_{i=1}^3 m_i \mathbf{v}_i}{\sum_{i=1}^3 m_i} = \mathbf{0} \quad (	ext{in barycentric frame})
$$

---

## 3. Simulation Parameters & Presets

| Symbol | Parameter | Value / Range | SI / App Units | Physical Description |
| :--- | :--- | :--- | :--- | :--- |
| $G$ | Gravitational Constant | $240	ext{–}1500$ | $	ext{px}^3 \cdot (	ext{kg}\cdot	ext{s}^2)^{-1}$ | Scaled mutual gravitational coupling constant |
| $\epsilon$ | Plummer Softening | $4.0	ext{–}28.0$ | $	ext{px}$ | Core radius preventing numerical singularities |
| $m_1, m_2$ | Star $lpha$, $eta$ Masses | $1.0	ext{–}4.0$ | $M_\odot$ (norm) | Masses of the primary two celestial bodies |
| $m_3$ | Star $\gamma$ Mass Scale | $0.2	ext{–}3.0$ | $M_\odot$ (norm) | Tunable mass of the third interacting star |
| $\Delta t$ | Integration Time Step | $0.001	ext{–}0.004$ | $	ext{s}$ | Sub-stepped delta per RK4 iteration (8 substeps/frame) |
| $S$ | Spatial Scale | $0.36 \cdot \min(W, H)$ | $	ext{px}$ | Canvas-fitted apparatus scale |

### Built-in Classical & Chaotic Presets
1. **♾️ Figure-8 Choreography**: $m_1 = m_2 = m_3 = 1.0$. Exact periodic solution where three bodies chase each other along a single self-intersecting lemniscate loop.
2. **⚡ Pythagorean Ejection (Burrau's Problem)**: Masses $3, 4, 5$ placed at the vertices of a 3-4-5 right triangle at rest. Undergoes dense chaotic multi-body scattering before ejecting the lightest star ($3M$) to infinity.
3. **📐 Lagrange Equilateral**: Three stars at vertices of a rotating equilateral triangle with synchronous orbital frequency $\omega = \sqrt{3GM / R^3}$.
4. **🪐 Hierarchical Triple Star**: Close inner binary ($m_1, m_2$) orbited at large semi-major axis by distant companion ($m_3$), showcasing secular Kozai-Lidov resonance.

---

## 4. Compose Architecture & Numerical Stepping

- **Substepped 4th-Order Runge-Kutta (RK4)**:
  To avoid the secular energy drift common in simple Euler methods during high-velocity pericenter passes, the physics loop divides each Compose animation frame into **8 substeps** integrated via classical RK4:
  $$k_1 = f(t_n, y_n), \quad k_2 = f(t_n + rac{h}{2}, y_n + rac{h}{2}k_1)$$
  $$k_3 = f(t_n + rac{h}{2}, y_n + rac{h}{2}k_2), \quad k_4 = f(t_n + h, y_n + h k_3)$$
  $$y_{n+1} = y_n + rac{h}{6}(k_1 + 2k_2 + 2k_3 + k_4)$$
- **Elevated Canvas Origin**: Apparatus centered at $c_y = 0.40 \cdot H$ with scale constrained to $\le 0.32 \cdot H$, keeping the lower $35\%$ of the viewport clear for the compact control deck.
- **Transparent HUD (`ExperimentHudCard`)**: Configured with `backgroundColor = Color.Transparent` and `ScienceBorder.copy(alpha = 0.35f)` so multi-color glowing orbital trails shine through unimpeded.
- **Direct Pointer Manipulation**: Users can drag any star directly on the Canvas to perturb coordinates or fling velocity vectors, triggering immediate real-time chaotic transitions.
