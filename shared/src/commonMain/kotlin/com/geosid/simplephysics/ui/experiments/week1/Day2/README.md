# Day 2: Citrus Peel vs. Balloon

> **Week 1: Home Physics • Mind-Bending Home & Kitchen Physics**  
> *Topic Subtitle: Polymer Dissolution & Tension Burst*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Squeeze an orange peel near an inflated balloon and watch it pop instantaneously without touching.

### Scientific Principles & Mechanism
The outer rind (flavedo) of citrus fruits (oranges, lemons, limes, grapefruits) contains microscopic oil pockets packed with **d-limonene** ($C_{10}H_{16}$), a volatile, non-polar cyclic terpene hydrocarbon.

Common party balloons are manufactured from vulcanized natural latex rubber (**polyisoprene**). When inflated, the cross-linked polymer chains are stretched near their elastic breaking limit under high mechanical tensile stress.

Under the chemical principle of *"like dissolves like"*, non-polar limonene instantly dissolves non-polar polyisoprene rubber upon contact:
1. Squeezing the citrus peel squirts an aerosol spray of tiny limonene droplets toward the balloon.
2. Upon landing, the oil immediately dissolves and breaks the polymer backbone bonds at the contact point.
3. This creates a microscopic defect (crack). The enormous stored elastic strain energy converts into crack surface energy, driving catastrophic crack propagation at the speed of sound in stretched rubber ($\sim 50\text{ m/s}$), causing the balloon to violently burst!

### Laboratory / Kitchen Protocol (Try It At Home)
> Inflate a rubber latex balloon until it is firm and taut. Cut a fresh strip of orange or lemon peel. Point the colored outer surface toward the balloon and pinch the peel sharply between your fingers to eject the citrus mist—the balloon bursts instantly!

---

## 2. Mathematical Foundation & Governing Equations

### Membrane Tensile Stress (Laplace's Law)
In a thin-walled spherical balloon of radius $r$, membrane thickness $t$, and internal overpressure $P$:

$$
\sigma = \frac{P \cdot r}{2t}
$$

As the balloon is inflated to larger scale, the wall thickness $t$ decreases ($t \propto 1/r^2$), causing tensile stress $\sigma$ to skyrocket:

$$
\sigma \propto P \cdot r^3
$$

### Griffith Fracture Criterion & Stress Intensity ($K_I$)
When limonene dissolves a localized patch of rubber, it creates a surface crack of initial length $a$. The stress intensity factor at the crack tip is:

$$
K_I = \sigma \sqrt{\pi a}
$$

When the localized stress intensity exceeds the critical fracture toughness of latex rubber ($K_I \ge K_{Ic}$), the crack propagates unstably at the acoustic shear wave speed:

$$
v_{\text{crack}} \approx \sqrt{\frac{E}{\rho_{\text{rubber}}}} \approx 50\text{ m/s}
$$

### Physical Meaning & Quantities
- **$\sigma$ (Membrane Stress):** Enormous biaxial tension held by stretched polyisoprene chains.
- **$P$ (Internal Pressure):** Gauge pressure of air trapped inside the balloon.
- **$t$ (Latex Thickness):** Wall thickness of the rubber membrane ($\approx 0.1 - 0.3\text{ mm}$).
- **$K_{Ic}$ (Fracture Toughness):** Material resistance to rapid crack extension.

---

## 3. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`CitrusBalloonExperiment`](./CitrusBalloonExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week1.Day2`
- **Screen Architecture:** Compose Multiplatform with Canvas rendering & particle burst dynamics.

### Reactive State Variables
The interactive state is managed via Compose `mutableStateOf` and `mutableStateListOf` variables:

| State Variable | Type / Default | Functional Role in Simulation |
| :--- | :--- | :--- |
| `isPopped` | `mutableStateOf(false)` | Flag indicating whether the balloon has burst |
| `balloonColorIndex` | `mutableStateOf(0)` | Active balloon color preset (Orange, Cyan, Purple, Red) |
| `inflationScale` | `mutableStateOf(1f)` | Balloon inflation scale driving membrane radius and tension |
| `peelPos` | `mutableStateOf(Offset(0.78f, 0.45f))` | Normalized 2D coordinate of the citrus peel |
| `isSqueezing` | `mutableStateOf(false)` | Flag indicating active peel squeeze ejection |
| `droplets` | `SnapshotStateList<LimoneneDroplet>` | Active limonene oil spray droplets traveling toward the balloon |
| `shards` | `SnapshotStateList<RubberShard>` | Exploded rubber shards flying outward with ballistic gravity and spin |
| `shockwaveRadius`, `shockwaveAlpha` | `Float` | Rapidly expanding white circular shockwave ring upon burst |

### Frame Loop & Physics Integration
- **High-Precision Physics Loop:** Driven by `withFrameNanos` inside `LaunchedEffect(isPopped, droplets.size, isSqueezing)`.
- **Droplet Collision Detection:** Traces each limonene droplet's trajectory. When distance to balloon center falls below the inflated boundary, dissolution is triggered, immediately bursting the balloon.
- **Explosion Shard Physics:** Generates 28 independent rubber shards with randomized linear velocities ($150 - 600\text{ px/s}$), rotational velocities ($\pm 360^\circ/\text{s}$), and gravity acceleration ($g = 450$).

### User Gestures & Interactivity
- **Peel Dragging:** Touch and drag the orange peel anywhere on screen to adjust distance and angle relative to the balloon.
- **`🍊 Squeeze Peel` Button:** Ejects a high-velocity burst of limonene oil droplets toward the balloon.
- **Balloon Color Switcher:** Cycles between vibrant latex colors (Orange, Cyan, Purple, Red).
- **Reset Button:** Inflates a new, taut balloon and clears active particles.

### Canvas Graphics Pipeline
- **Floating Balloon:** Glossy, taut rubber balloon with realistic 3D specular highlight and floating harmonic bobbing (`drawTautBalloon`).
- **Citrus Peel:** Rendered orange crescent with outer orange rind, white pith, and juicy zest texture (`drawCitrusPeel`).
- **Limonene Droplet Stream:** Bright yellow/orange aerosol particles flying from the peel.
- **Shockwave & Rubber Shards:** High-speed expanding white circular shockwave ring and tumbling, spinning rubber fragments.

---

## 4. Suggested Investigations & Parameter Experiments
1. **Distance vs. Dispersion:** Squeeze the peel from far away versus right beside the balloon. Notice how droplet dispersion makes hits harder from afar, while close proximity causes instantaneous detonation.
2. **Dissolution Mechanism:** Observe that the peel itself never touches the balloon; the explosion is entirely triggered by the airborne chemical solvent droplets landing on the stressed rubber.
3. **Explosion Fragmentation Dynamics:** Trigger the burst and observe the high-speed tumbling fragments and expanding acoustic shockwave simulating the rapid release of stored strain energy.
