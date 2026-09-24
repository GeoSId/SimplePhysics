# Day 18: Gravitational Slingshot

> **Week 3: Celestial Gravity & Orbits • Planetary Orbits & Spaceflight Dynamics**  
> *Topic Subtitle: Orbital Gravity Assist & Oberth Effect*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Fly a spacecraft behind a moving giant planet and catapult into interstellar space with massive kinetic velocity gain.

### Scientific Principles & Mechanism
A **gravitational slingshot** (also known as a **gravity assist** or **flyby maneuver**) is an essential orbital mechanics technique used by interplanetary missions (such as *Voyager 1 & 2*, *Galileo*, *Cassini*, *New Horizons*, and the *Parker Solar Probe*) to dramatically alter a probe's velocity and trajectory without burning propellant.

1. **The Moving Planet Reference Frame Paradox:**
   - In the **planet's rest frame**, the spacecraft's flyby is a purely conservative, elastic hyperbolic scatter around a central gravitational body. The spacecraft enters with asymptotic hyperbolic excess velocity $\mathbf{v}'_{\infty, \text{in}}$, whips through periapsis, and exits with velocity $\mathbf{v}'_{\infty, \text{out}}$. Since gravity is conservative in this frame, energy is conserved:
     $$
     v'_{\text{out}} = v'_{\text{in}}
     $$
     The probe's speed relative to the planet is unchanged; only its direction is deflected by asymptotic scattering angle $\delta$.
   - In the **heliocentric (Sun) reference frame**, the planet itself moves along its orbit with velocity $\mathbf{V}_p$. Adding the frames via Galilean velocity transformation:
     $$
     \mathbf{v}_{\text{heliocentric}} = \mathbf{v}' + \mathbf{V}_p
     $$
2. **Trailing Flyby (Speed Boost / Slingshot):**
   - When the spacecraft passes **behind** the moving planet (trailing hemisphere), the planet's gravitational field pulls the probe along in the direction of planetary motion.
   - In the 1D ideal head-on elastic collision limit (analogous to a ping-pong ball hitting a speeding locomotive):
     $$
     v_{\text{max}} = v_{\text{in}} + 2 V_p
     $$
   - The probe extracts a tiny fraction of the planet's massive orbital kinetic energy and momentum. While the planet decelerates by a negligible $\sim 10^{-24}\text{ m/s}$, the spacecraft gains thousands of kilometers per hour!
3. **Leading Flyby (Gravity Brake):**
   - When the spacecraft passes **in front of** the planet (leading hemisphere), the planet pulls backward against the probe's heliocentric motion, shedding orbital energy. Missions like the *Parker Solar Probe* use multiple Venus gravity brakes to shed heliocentric angular momentum and dive toward the Sun.
4. **The Oberth Effect:**
   - Rocket engines impart kinetic energy much more effectively at high speed. Burning rocket thrusters at **periapsis** (closest approach, where speed $v$ is highest) yields maximum mechanical energy increase:
     $$
     \Delta E_k = \frac{1}{2}m (v + \Delta v_{\text{burn}})^2 - \frac{1}{2}m v^2 = m v \Delta v_{\text{burn}} + \frac{1}{2}m \Delta v_{\text{burn}}^2 \approx m v \Delta v_{\text{burn}}
     $$

### Laboratory / Kitchen Protocol (Try It At Home)
> **The Two-Ball Elastic Slingshot Drop:** Take a tennis ball and place a small, high-rebound bouncy rubber superball directly on top of it. Hold them vertically aligned and drop them simultaneously from shoulder height onto a hard floor. When the tennis ball hits the floor, it rebounds upward into the downward-falling superball. The superball shoots into the ceiling with violent velocity! This 1D elastic collision directly mirrors how a spacecraft acquires up to twice the planet's approach velocity.

---

## 2. Mathematical Foundation & The Three Pillars of Orbital Mechanics

### Pillar 1: Governing Law & Gravitational Force

#### Newtonian Central Gravity
The spacecraft of mass $m$ experiences the gravitational field of a planet of mass $M_p$ centered at moving coordinate $\mathbf{r}_p(t)$:

$$
\mathbf{a}_{\text{grav}} = -\frac{G M_p}{|\mathbf{r} - \mathbf{r}_p|^3} (\mathbf{r} - \mathbf{r}_p)
$$

Where:
- $G = 6.6743 \times 10^{-11}\text{ m}^3\text{ kg}^{-1}\text{ s}^{-2}$ is the universal gravitational constant.
- $\mu = G M_p$ is the gravitational parameter of the planet (for Jupiter, $\mu \approx 1.26686 \times 10^{17}\text{ m}^3/\text{s}^2$).
- $\mathbf{r}' = \mathbf{r} - \mathbf{r}_p$ is the position vector relative to the planet.

#### Planetary Sphere of Influence (SOI)
The boundary where planetary gravity dominates over the Sun's tidal gravitational perturbation is defined by Laplace's sphere of influence:

$$
R_{\text{SOI}} = a_p \left(\frac{M_p}{M_\odot}\right)^{2/5}
$$

Inside $R_{\text{SOI}}$, the trajectory is solved as an unperturbed two-body hyperbolic Keplerian orbit relative to the planet.

---

### Pillar 2: Kinematics & Motion Constraints (Hyperbolic Deflection)

#### Hyperbolic Orbit Geometry & Deflection Angle
In the planet's rest frame, the trajectory is a hyperbola with eccentricity $e > 1$. The semi-major axis $a < 0$ is related to hyperbolic excess speed $v'_\infty$ by:

$$
v'^2_\infty = \frac{\mu}{-a} \implies a = -\frac{\mu}{v'^2_\infty}
$$

The distance of closest approach (periapsis radius $r_p$) relates to the impact parameter $b$ and eccentricity $e$ by:

$$
r_p = a(1 - e) = \frac{\mu}{v'^2_\infty} (e - 1)
$$

$$
b = |a| \sqrt{e^2 - 1} = \frac{\mu}{v'^2_\infty} \sqrt{e^2 - 1}
$$

The asymptotic scattering deflection angle $\delta$ satisfies:

$$
\sin\left(\frac{\delta}{2}\right) = \frac{1}{e} = \frac{1}{1 + \frac{r_p v'^2_\infty}{\mu}}
$$

$$
\tan\left(\frac{\delta}{2}\right) = \frac{\mu}{b \, v'^2_\infty}
$$

#### Vector Heliocentric Frame Transformation
Let the incoming and outgoing asymptotic velocities in the planet frame be $\mathbf{v}'_{\text{in}}$ and $\mathbf{v}'_{\text{out}}$, where $|\mathbf{v}'_{\text{in}}| = |\mathbf{v}'_{\text{out}}| = v'_\infty$. In the heliocentric (Sun) frame:

$$
\mathbf{v}_{\text{in}} = \mathbf{v}'_{\text{in}} + \mathbf{V}_p
$$

$$
\mathbf{v}_{\text{out}} = \mathbf{v}'_{\text{out}} + \mathbf{V}_p
$$

The total velocity vector change $\Delta \mathbf{v}$ imparted to the spacecraft is:

$$
\Delta \mathbf{v} = \mathbf{v}_{\text{out}} - \mathbf{v}_{\text{in}} = \mathbf{v}'_{\text{out}} - \mathbf{v}'_{\text{in}}
$$

With magnitude:

$$
|\Delta \mathbf{v}| = 2 v'_\infty \sin\left(\frac{\delta}{2}\right) = \frac{2 v'_\infty}{e}
$$

---

### Pillar 3: Energy & Work Conservation (Velocity Gain & Oberth Effect)

#### Maximum Heliocentric Velocity Gain
In an optimal trailing coplanar flyby where the spacecraft approaches counter to the planet's motion ($\mathbf{v}_{\text{in}} \uparrow\downarrow \mathbf{V}_p$):
1. Relative approach speed: $v'_\infty = v_{\text{in}} + V_p$.
2. Hyperbolic turnaround deflection ($\delta \to 180^\circ$): $\mathbf{v}'_{\text{out}} = - \mathbf{v}'_{\text{in}} = +(v_{\text{in}} + V_p) \mathbf{\hat{V}}_p$.
3. Heliocentric departure velocity:
   $$
   v_{\text{out}} = v'_\infty + V_p = (v_{\text{in}} + V_p) + V_p = v_{\text{in}} + 2 V_p
   $$

#### Specific Energy Transfer
The change in specific orbital energy $\Delta \mathcal{E} = \frac{1}{2} (v_{\text{out}}^2 - v_{\text{in}}^2)$ relative to the Sun is:

$$
\Delta \mathcal{E} = \mathbf{V}_p \cdot \Delta \mathbf{v} = V_p \Delta v_\parallel
$$

- If $\Delta v_\parallel > 0$ (trailing flyby): $\Delta \mathcal{E} > 0$ (spacecraft gains kinetic energy, planet loses orbital energy).
- If $\Delta v_\parallel < 0$ (leading flyby): $\Delta \mathcal{E} < 0$ (spacecraft decelerates into lower heliocentric orbit).

#### The Oberth Rocket Burn at Periapsis
When a spacecraft with speed $v_p$ at periapsis performs a burn $\Delta v_{\text{burn}}$ parallel to its velocity:

$$
\mathcal{E}_{\text{final}} = \frac{1}{2}(v_p + \Delta v_{\text{burn}})^2 - \frac{\mu}{r_p} = \left(\frac{1}{2}v_p^2 - \frac{\mu}{r_p}\right) + v_p \Delta v_{\text{burn}} + \frac{1}{2} \Delta v_{\text{burn}}^2
$$

$$
\Delta \mathcal{E} \approx v_p \Delta v_{\text{burn}}
$$

Because $v_p = \sqrt{v'^2_\infty + \frac{2\mu}{r_p}}$ is maximized at periapsis, firing the rocket deep in the gravitational potential well multiplies the final hyperbolic excess speed $v_{\infty, \text{new}}$ far beyond what the same fuel burn would produce in empty space.

---

## 3. Physical Parameters & SI Units

| Symbol | Parameter Description | Simulation Value | Typical Real-World Value (Jupiter Flyby) | SI Unit |
| :--- | :--- | :--- | :--- | :--- |
| $V_p$ | Planet Orbital Velocity | $1.4\text{ (norm)}$ | $13.07$ | $\text{km/s}$ |
| $v_{\text{in}}$ | Probe Inward Speed | $1.6\text{ (norm)}$ | $10.0$ | $\text{km/s}$ |
| $v_{\text{max}}$ | Max Theoretical Outward Speed | $4.4\text{ (norm)}$ | $36.14$ | $\text{km/s}$ |
| $M_p$ | Planet Mass (Jupiter) | Normalized $\mu = 850\,000$ | $1.898 \times 10^{27}$ | $\text{kg}$ |
| $R_p$ | Planetary Radius | $30\text{ px}$ | $71\,492$ | $\text{km}$ |
| $b$ | Impact Parameter | $-80\text{ to }+80\text{ px}$ | $1.5 \times 10^5 \text{ to } 8.0 \times 10^5$ | $\text{km}$ |
| $\delta$ | Scattering Deflection Angle | $20^\circ \text{ to } 160^\circ$ | $45^\circ \text{ to } 120^\circ$ | $\text{deg}$ |
| $\Delta v_{\text{Oberth}}$ | Periapsis Rocket Impulse | $75\text{ px/s}$ | $1.5$ | $\text{km/s}$ |

---

## 4. Simulation Architecture & Numerical Integration

### Source Location
- **Primary Composable:** [`GravitationalSlingshotExperiment.kt`](./GravitationalSlingshotExperiment.kt)
- **Registry Mapping:** [`ExperimentScreenRegistry.kt`](../../expirementsRegistry/ExperimentScreenRegistry.kt) (`"gravitational_slingshot"`)

### Numerical Integration Routine
1. **Sub-Stepped Symplectic Step:**
   Due to extreme gravitational acceleration near periapsis, the integration loop subdivides each frame step into $N = 6$ sub-steps:
   ```kotlin
   val subSteps = 6
   val dt = rawDt / subSteps
   for (step in 0 until subSteps) {
       planetX += planetSpeedPxS * dt
       val rVec = probePos - Offset(planetX, 0f)
       val rDistSq = rVec.x * rVec.x + rVec.y * rVec.y
       val aGravMag = gmPlanet / (rDistSq + 120f)
       probeVel += Offset(-rVec.x / rDist * aGravMag * dt, -rVec.y / rDist * aGravMag * dt)
       probePos += probeVel * dt
   }
   ```
2. **Velocity-Dependent Trail Color Mapping:**
   As the probe whips around Jupiter, its color smoothly transitions from cool Cyan (`v_in`) to blazing Gold and Coral Neon at peak slingshot speed.
3. **Reference Frame Switcher:**
   Allows the learner to toggle seamlessly between the **Heliocentric Sun Frame** (where planetary motion and speed gain are evident) and the **Planet-Centric Rest Frame** (where the trajectory is a symmetric hyperbola and energy conservation $v'_{\text{out}} = v'_{\text{in}}$ is visually obvious).

---

## 5. Suggested Investigations & Experiments
1. **Trailing vs Leading Flyby:** Adjust the impact parameter slider $b$ from $+42\text{ px}$ (trailing) to $-42\text{ px}$ (leading) and observe how the probe's heliocentric speed transitions from extreme acceleration to a sharp deceleration brake.
2. **The Oberth Multiplier:** Activate "Enable Oberth Burn" and note how an impulse applied at periapsis generates a much larger final velocity increase than if fired in empty space.
3. **Reference Frame Insight:** Switch to the "Planet-Centric Rest Frame" to confirm that in Jupiter's frame, the incoming speed vector and outgoing speed vector have identical magnitudes!
