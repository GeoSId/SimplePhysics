# Day 3: Pencil Through Water Bag

> **Week 1: Home Physics • Mind-Bending Home & Kitchen Physics**  
> *Topic Subtitle: Polymer Elasticity & Self-Sealing*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Poke sharp pencils straight through a water-filled plastic bag without spilling a single drop!

### Scientific Principles & Mechanism
Plastic zip-lock sandwich bags are manufactured from Low-Density Polyethylene (LDPE) or High-Density Polyethylene (HDPE). These materials consist of extremely long, tangled chains of repeating ethylene monomer molecules ($-\text{CH}_2-\text{CH}_2-$).

When a smooth, round pencil pierces the bag:
1. The sharp tip slips between the flexible polymer chains rather than tearing them apart.
2. The long polymer chains separate elastically, stretching tightly around the cylindrical shaft of the pencil like rubber bands.
3. Under the hydrostatic pressure of the contained water, the stretched polymer chains form a hermetic, watertight temporary gasket around the pencil surface, preventing leaks.
4. However, if a pencil is pulled out, the permanently deformed plastic cannot snap shut across the open puncture wound, allowing water to drain out under gravity.

### Laboratory / Kitchen Protocol (Try It At Home)
> Fill a clean zip-lock bag 3/4 full with tap water and seal it tightly. Sharpen 3 to 6 smooth, round pencils. In one confident, continuous motion, poke each pencil through one side and out the opposite side over a sink or basin!

---

## 2. Mathematical Foundation & Governing Equations

### Hydrostatic Head Pressure
At any depth $h$ below the free water surface:

$$
P(h) = \rho_{\text{water}} \cdot g \cdot h
$$

### Hoop Stress in Thin-Walled Polymers
The circumferential (hoop) stress $\sigma_\theta$ exerted around the pencil shaft of radius $r$ through a bag film of thickness $t$:

$$
\sigma_\theta = \frac{P \cdot r}{t}
$$

### Elastic Gasket Sealing Condition
For a watertight seal without leaking, the compressive contact pressure $P_{\text{contact}}$ exerted by the stretched polymer chains against the pencil must exceed the outward hydrostatic water pressure:

$$
P_{\text{contact}} = \frac{E \cdot \Delta r}{r} > P_{\text{water}}(h) = \rho g h
$$

Where $E$ is the Young's modulus of polyethylene and $\Delta r$ is the radial interference fit.

### Physical Meaning & Quantities
- **$\rho_{\text{water}} = 1000\text{ kg/m}^3$:** Density of water.
- **$h$ (Water Depth):** Height of the liquid column above the puncture site.
- **$P(h)$:** Hydrostatic pressure pushing outward against the puncture hole.
- **$E$ (Polymer Elastic Modulus):** Elastic stiffness of LDPE chains resisting puncture expansion.

---

## 3. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`PencilWaterBagExperiment`](./PencilWaterBagExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week1.Day3`
- **Screen Architecture:** Compose Multiplatform with Canvas rendering & dynamic particle physics.

### Reactive State Variables
The interactive state is managed via Compose `mutableStateOf` and `mutableStateListOf` variables:

| State Variable | Type / Default | Functional Role in Simulation |
| :--- | :--- | :--- |
| `waterLevel` | `mutableStateOf(0.78f)` | Normalized water volume inside the bag ($0.05 - 1.00$) |
| `leakActive` | `mutableStateOf(false)` | Flag indicating broken seal and active water drainage |
| `pencils` | `SnapshotStateList<PuncturePencil>` | Active pencils pierced through the bag (position, angle, state) |
| `leakParticles` | `SnapshotStateList<WaterLeakParticle>` | Dynamic water spray droplets expelled from unplugged holes |
| `wavePhase` | `Float` | Animated surface meniscus wave oscillation phase |

### Frame Loop & Dynamic Leak Simulation
- **Water Drain Loop:** Driven by `withFrameNanos` inside `LaunchedEffect(leakActive, waterLevel)`.
- **Drainage Dynamics:** When `leakActive == true`, water level drains continuously ($\Delta \text{water} = -0.0015/\text{frame}$).
- **Particle System:** Unplugged holes continuously spawn ballistic water particles subject to horizontal ejection velocity and downward gravitational acceleration ($g = 0.35$).

### User Gestures & Interactivity
- **Interactive Pencil Manipulation:** Drag on the canvas to tilt the angle ($\pm 25^\circ$) and adjust the vertical position of active pencils.
- **`+ Poke Pencil` Button:** Dynamically inserts additional colored pencils through the bag at randomized tilt angles.
- **`Pull Out Pencil ⚡` Button:** Unplugs the last inserted pencil, permanently tearing the polymer seal and initiating pressurized water jet leaks.
- **Reset Button:** Re-seals the bag, refills the water to $78\%$, and restores the default pencil configuration.

### Canvas Graphics Pipeline
- **Suspension Hanger & Clips:** Metallic crossbar with two hanging binder clips supporting the bag (`drawSuspensionHanger`).
- **Water-Filled Plastic Bag:** Translucent plastic envelope with animated sine wave surface water meniscus and blue depth fill (`drawWaterBag`).
- **Piercing Pencils:** Textured wooden pencil shafts with sharpened graphite tips and colored paint bands spanning across the bag (`drawPiercingPencil`).
- **Polymer Seal Highlight Rings:** Neon cyan stress halos indicating tight, watertight polymer gasket seals.
- **Puncture Leak Jets:** Red puncture holes with blue ballistic water spray droplets.

---

## 4. Suggested Investigations & Parameter Experiments
1. **Multi-Pencil Capacity:** Tap `+ Poke Pencil` repeatedly to pierce 4, 5, or 6 pencils at various heights. Notice how the polymer maintains $100\%$ seal integrity regardless of pencil count.
2. **Pencil Extraction & Catastrophic Drain:** Tap `Pull Out Pencil ⚡`. Observe how removing a pencil leaves an open hole, triggering ballistic water sprays and draining the bag.
3. **Pencil Angle Drag:** Drag an inserted pencil up and down or tilt it at an angle. Notice how the polymer chains accommodate angular tilt while preserving the seal.
