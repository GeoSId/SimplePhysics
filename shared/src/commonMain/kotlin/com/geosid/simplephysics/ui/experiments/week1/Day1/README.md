# Day 1: Static Straw Levitation

> **Week 1: Home Physics • Mind-Bending Home & Kitchen Physics**  
> *Topic Subtitle: Electrostatic Induction & Dipole Torque*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Spin and steer a balanced plastic straw through empty air without ever touching it with your hands.

### Scientific Principles & Mechanism
Rubbing a plastic wand with wool or hair causes negative electric charge (electrons) to transfer via the **triboelectric effect**, leaving the wand with net charge $-Q$.

When the charged wand is brought near an uncharged plastic straw delicately balanced on the pivot of a glass bottle:
1. **Electrostatic Induction:** The electric field of the negative wand repels mobile electrons in the plastic straw toward the far end, leaving a net positive charge induced on the near end.
2. **Net Attractive Force (Coulomb's Law):** Because the positive induced charges are closer to the wand than the repelled negative charges ($r_{\text{near}} < r_{\text{far}}$), the attractive force strictly exceeds the repulsive force ($F \propto 1/r^2$).
3. **Induced Dipole Torque:** This net electrostatic attraction generates an angular torque ($\tau = r \times F$) around the bottle's pivot point, causing the balanced straw to rotate and track the wand's motion through empty air!

### Laboratory / Kitchen Protocol (Try It At Home)
> Balance a lightweight drinking straw horizontally across the dome cap of a dry glass bottle. Rub another plastic straw or ruler vigorously against a wool sweater or dry hair for 10 seconds. Bring the charged end close to (but not touching) the balanced straw, and watch it swing toward the wand like a compass needle!

---

## 2. Mathematical Foundation & Governing Equations

### Coulomb's Law of Electrostatics
The electrostatic force between two point charges $q_1$ and $q_2$ separated by distance $r$:

$$
F = \frac{1}{4\pi \varepsilon_0} \frac{q_1 q_2}{r^2}
$$

### Induced Dipole Torque & Angular Equation of Motion
The straw experiences a rotational restoring torque $\tau(\theta)$ directed toward the charged wand:

$$
\tau(\theta) = k \cdot \frac{|Q_{\text{wand}}|}{d^2} \cdot \Delta\theta
$$

Under rotational inertia $I$ and air drag damping $b$, the angular acceleration $\alpha = \frac{d^2\theta}{dt^2}$ satisfies:

$$
I \frac{d^2\theta}{dt^2} = \tau(\theta) - b \frac{d\theta}{dt}
$$

### Physical Meaning & Quantities
- **$Q_{\text{wand}}$ (Wand Charge):** Static charge on the wand ($-1.0$ negative electrons to $+1.0$ positive ions).
- **$d$ (Distance):** Separation distance between wand tip and straw pivot.
- **$\Delta\theta$:** Angular misalignment between the straw's orientation and the wand position vector.
- **$I$ (Moment of Inertia):** Rotational inertia of the straw spinning on its center pivot ($I = \frac{1}{12} m L^2$).
- **$b$ (Damping Coefficient):** Aerodynamic resistance slowing the straw's rotation down to rest.

---

## 3. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`StaticStrawExperiment`](./StaticStrawExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week1.Day1`
- **Screen Architecture:** Compose Multiplatform with Canvas rendering & high-frequency physics tick.

### Reactive State Variables
The interactive state is managed via Compose `mutableStateOf` variables:

| State Variable | Type / Default | Functional Role in Simulation |
| :--- | :--- | :--- |
| `wandPos` | `mutableStateOf(Offset(0.72f, 0.38f))` | Normalized 2D spatial coordinate of the charged wand |
| `wandCharge` | `mutableStateOf(-0.85f)` | Wand static charge level ($-100\%$ negative to $+100\%$ positive) |
| `strawAngleRad` | `mutableStateOf(0.3f)` | Instantaneous orientation angle $\theta$ of the balanced straw |
| `strawAngularVelocity` | `mutableStateOf(0f)` | Angular velocity $\omega$ of the spinning straw |
| `isRubbing` | `mutableStateOf(false)` | Wool friction charging animation state |
| `sparkPhase` | `Float` | Continuous cyclic phase driving electric field spark animations |

### Frame Loop & Rotational Physics
- **High-Precision Physics Loop:** Driven by `withFrameNanos` inside `LaunchedEffect(wandPos, wandCharge)`.
- **Symmetric Dipole Tracking:** Evaluates angular misalignment $\Delta\theta$ relative to the closest tip of the straw (accounting for the straw's $180^\circ$ rotational symmetry).
- **Inverse-Square Torque Coupling:** Computes dynamic torque strength $\tau \propto \frac{|Q|}{d^2}$ and applies rotational damping ($b = 2.8$) to simulate realistic air friction and over-swing oscillation.

### User Gestures & Interactivity
- **2D Wand Dragging:** Touch and drag the charged wand anywhere around the bottle to steer the straw in full $360^\circ$ rotation.
- **`⚡ Rub Wool` Button:** Friction charges the wand to $-100\%$ negative charge.
- **`Ground` Button:** Discharges the wand to $0\%$ neutral, immediately cutting electrostatic coupling.
- **Reset Button:** Restores default wand position, charge ($-85\%$), and straw angle.

### Canvas Graphics Pipeline
- **Glass Bottle Stand & Pivot:** Semi-transparent glass bottle with neck, shoulder, and domed cap supporting the pivot (`drawGlassBottlePivot`).
- **Balanced Plastic Straw:** Horizontal drinking straw with center pivot pin that rotates with angle $\theta$ (`drawBalancedStraw`).
- **Draggable Charged Wand:** Wand with glowing charge tip, wool friction sparks, and polarity indicators (`drawChargedWand`).
- **Electric Force Field Vectors:** Animated neon spark lines connecting the charged wand tip to the induced straw end (`drawElectrostaticField`).

---

## 4. Suggested Investigations & Parameter Experiments
1. **Non-Contact Tracking:** Drag the wand in a slow circular path around the bottle. Observe how the straw continuously tracks the wand's tip without physical contact.
2. **Grounding & Discharge:** While the straw is actively spinning toward the wand, press `Ground`. Notice how the electric field collapses immediately, and the straw coasts freely under friction until coming to rest.
3. **Distance & Inverse-Square Falloff:** Move the wand close to the straw versus far across the screen. Notice that electrostatic torque drops precipitously as distance increases ($\tau \propto 1/d^2$).
