# Day 4: Disappearing Glass

> **Week 1: Home Physics • Mind-Bending Home & Kitchen Physics**  
> *Topic Subtitle: Index of Refraction Matching & Optical Invisibility*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Submerge a glass tube into a liquid and watch its contours vanish completely before your eyes.

### Scientific Principles & Mechanism
Transparent glass is normally visible because light reflects and refracts at the interface between the surrounding medium and the glass. This reflection is caused by the sudden change in the speed of light, quantified by the refractive index ($n = c / v$).

Pyrex borosilicate glass has a refractive index of $n_{\text{glass}} = 1.474$. When submerged in water ($n = 1.333$), the mismatch is large, causing noticeable Fresnel reflection and beam bending at the boundary. However, when submerged in vegetable cooking oil or glycerin (which also have $n \approx 1.474$), light travels at the exact same speed across the boundary. With no change in phase velocity, light neither refracts nor reflects ($R = 0$), rendering the submerged glass 100% optically invisible!

### Laboratory / Kitchen Protocol (Try It At Home)
> Place a Pyrex glass stirring rod or small Pyrex test tube into a cup of clear water (it remains clearly visible). Now transfer it into a cup of clear vegetable cooking oil or baby oil/glycerin—the submerged portion vanishes entirely!

---

## 2. Mathematical Foundation & Governing Equations

### Snell's Law of Refraction
At any optical interface between medium 1 and medium 2:

$$
n_1 \sin\theta_1 = n_2 \sin\theta_2
$$

When $n_1 = n_2$, $\sin\theta_1 = \sin\theta_2 \implies \theta_1 = \theta_2$. The light ray passes straight through without deviation.

### Fresnel Reflection Coefficient (Normal Incidence)
The fraction of reflected light intensity $R$ at the boundary is given by the Fresnel reflection formula:

$$
R = \left(\frac{n_1 - n_2}{n_1 + n_2}\right)^2
$$

When the refractive index of the liquid matches that of Pyrex glass ($n_{\text{liquid}} = n_{\text{pyrex}} = 1.474$):

$$
R = \left(\frac{1.474 - 1.474}{1.474 + 1.474}\right)^2 = 0
$$

With zero reflected light ($R = 0$) and zero refracted angle shift ($\theta_1 = \theta_2$), the human eye receives no optical boundary signals, producing total invisibility.

### Physical Meaning & Quantities
- **$n_{\text{pyrex}} = 1.474$:** Refractive index of borosilicate glass.
- **$n_{\text{liquid}}$:** Refractive index of the surrounding fluid ($1.000$ to $1.650$).
- **$\Delta n = |n_{\text{liquid}} - n_{\text{pyrex}}|:$** Absolute index mismatch determining boundary visibility.
- **$R$ (Fresnel Reflectance):** Fraction of incident light reflected back to the observer.

---

## 3. Simulation Architecture & Code Breakdown

### Source Location
- **Primary Composable:** [`RefractionGlassExperiment`](./RefractionGlassExperiment.kt)
- **Package:** `com.geosid.simplephysics.ui.experiments.week1.Day4`
- **Screen Architecture:** Compose Multiplatform with Canvas rendering & interactive ray tracing.

### Reactive State Variables
The interactive state is managed via Compose `mutableStateOf` variables:

| State Variable | Type / Default | Functional Role in Simulation |
| :--- | :--- | :--- |
| `liquidIndex` | `mutableStateOf(1.333f)` | Fluid refractive index $n$ ($1.000 - 1.650$) |
| `liquidName` | `mutableStateOf("Water (n=1.33)")` | Active fluid descriptive label |
| `tubeSubmergedFraction` | `mutableStateOf(0.65f)` | Vertical immersion fraction of the Pyrex tube ($0.15 - 0.90$) |
| `showLaserRays` | `mutableStateOf(true)` | Checkbox toggle for multi-beam laser ray optics |
| `laserPulse` | `Float` | Animated glow pulsing factor for laser beams |

### Optical & Visibility Computation
- **Submerged Visibility:** Evaluates normalized optical contrast:
  $$\text{submergedVisibility} = \text{coerceIn}\left(\frac{|n_{\text{liquid}} - 1.474|}{0.474}, 0, 1\right)$$
- **Index Match Percentage:** Quantified as $(1 - \text{submergedVisibility}) \times 100\%$.
- **Boundary State Classification:** Displays *"✨ 100% INVISIBLE (Perfect Match!)"* when match $> 96\%$, *"Subtle Ghost"* when $> 70\%$, and *"Clearly Visible"* otherwise.

### User Gestures & Interactivity
- **Vertical Drag Gesture:** Touch and drag the Pyrex tube directly on canvas to raise or lower it into the liquid.
- **Liquid Presets:**
  - `Air` ($n = 1.000$): Maximum contrast and shadow.
  - `Water` ($n = 1.333$): Common household baseline (clearly visible).
  - `Glycerin / Oil` ($n = 1.474$): Perfect index match (complete invisibility).
  - `Dense Glass` ($n = 1.620$): Over-matched optical medium (flint fluid).
- **Precision Slider:** Continuous adjustment of $n$ from $1.000$ to $1.650$.
- **Laser Optics Toggle:** Turn laser beams on/off to examine refraction geometry.

### Canvas Graphics Pipeline
- **Liquid Fill in Beaker:** Responsive gradient fill tinted according to fluid optical density (`drawLiquidInBeaker`).
- **Pyrex Tube with Selective Invisibility:** Upper portion above fluid drawn with full glass specular highlights; lower submerged portion dynamically modulates boundary stroke opacity and refractive tint down to zero (`drawPyrexTube`).
- **Graduated Beaker:** Glass beaker walls, lip, and volume graduation ticks (`drawBeakerGlass`).
- **Laser Ray Tracing:** Horizontal green laser beams that refract inward/outward when $n_{\text{liquid}} \neq n_{\text{pyrex}}$, but travel in perfectly undisturbed straight lines when matched (`drawLaserRays`).

---

## 4. Suggested Investigations & Parameter Experiments
1. **The Invisibility Threshold:** Select `Water (n=1.333)`. Notice the distinct glass edges. Now tap `Glycerin / Oil (n=1.474)` and observe the submerged contours completely dissolve into the liquid!
2. **Laser Beam Path Analysis:** Enable `Show Laser Beams`. In Water, watch the laser beams bend at the tube's curved cylindrical surface (acting as a cylindrical lens). In Vegetable Oil ($n=1.474$), notice that the beams pass through the glass with zero refraction angle!
3. **Over-Matching Index:** Select `Dense Glass (n=1.620)`. Notice that because $|1.620 - 1.474| > 0$, the glass boundaries reappear, proving that invisibility requires an exact match, not merely a high index.
