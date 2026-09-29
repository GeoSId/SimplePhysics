# Day 23: Fourier Series & Epicycles

> **Week 4: Waves, Sound & Acoustic Resonance • Days 22–28**  
> *Topic Subtitle: Harmonic Phasor Synthesis, Geometric Epicycles & Gibbs Phenomenon*

---

## 1. Physical Phenomenon & Intuitive Background

**Visual Hook:** Synthesize square, sawtooth, and triangle waveforms using rotating epicyclic circles. Watch individual Fourier harmonics sum together in real time!

### Scientific Principles & Mechanism
Fourier's theorem asserts that any well-behaved periodic signal $f(t)$ with fundamental period $T = 2\pi / \omega$ can be decomposed into an infinite sum of simple harmonic sinusoidal oscillations whose frequencies are integer multiples of $\omega$. 

In the complex plane, each sinusoidal term corresponds to a constant-speed rotating vector—a **phasor**—described by Euler's identity $e^{i n \omega t}$. When these rotating phasors are chained tip-to-tail, they generate a geometric mechanical gear train of **epicycles** (reminiscent of Ptolemaic celestial orbits). The projection of the outermost tip onto a moving time axis draws the synthesized waveform in real time.

When reconstructing signals with jump discontinuities (such as the abrupt vertical transitions in a square or pulse wave), truncating the infinite Fourier sum to a finite number of harmonics $N$ causes high-frequency ringing and an overshoot of approximately $8.95\%$, an immutable mathematical property known as the **Gibbs Phenomenon**.

### Laboratory / Kitchen Protocol (Try It At Home)
> Download an audio spectrum analyzer app on your phone. Play a pure flute tone (single fundamental sine peak), then play a distorted electric guitar note or square wave, and watch the distinct ladder of odd harmonic spikes appear on the frequency spectrum!

---

## 2. Mathematical Foundation & The 3 Pillars

```
                     ┌────────────────────────────────────────┐
                     │          THE 3 PILLARS OF DAY 23       │
                     └────────────────────────────────────────┘
                                         │
         ┌───────────────────────────────┼───────────────────────────────┐
         ▼                               ▼                               ▼
┌─────────────────────────┐ ┌─────────────────────────┐ ┌─────────────────────────┐
│     1. GOVERNING LAW    │ │      2. KINEMATICS      │ │  3. ENERGY CONSERVATION │
│  Orthogonal Sinusoid    │ │ Epicyclic Phasor Kinematics│ │ Parseval's Power Theorem│
│  Basis Decomposition    │ │ & Gibbs Overshoot Ringing │ │ ∑ |c_n|² = Signal Power │
└─────────────────────────┘ └─────────────────────────┘ └─────────────────────────┘
```

### Pillar 1: Governing Law & Fourier Basis Decomposition
For any piecewise-continuous periodic function $f(t)$ with period $T = 2\pi / \omega$, the real Fourier series expansion is given by:

$$f(t) = \frac{a_0}{2} + \sum_{n=1}^\infty \left[ a_n \cos(n \omega t) + b_n \sin(n \omega t) 
\right]$$

with orthogonal Fourier projection integrals:

$$a_n = \frac{2}{T} \int_{-T/2}^{T/2} f(t) \cos(n \omega t) \, dt, \quad b_n = \frac{2}{T} \int_{-T/2}^{T/2} f(t) \sin(n \omega t) \, dt$$

In complex exponential form:

$$f(t) = \sum_{n=-\infty}^\infty c_n e^{i n \omega t}, \quad c_n = \frac{1}{T} \int_{-T/2}^{T/2} f(t) e^{-i n \omega t} \, dt$$

#### Analytical Waveform Coefficients:
1. **Square Wave (Odd Harmonics with $1/n$ Falloff):**
   $$f_{\text{square}}(t) = \frac{4}{\pi} \sum_{k=1}^N \frac{\sin((2k-1)\omega t)}{2k-1} = \frac{4}{\pi} \left( \sin(\omega t) + \frac{1}{3}\sin(3\omega t) + \frac{1}{5}\sin(5\omega t) + \dots \right)$$

2. **Sawtooth Wave (All Harmonics with Alternating Signs):**
   $$f_{\text{sawtooth}}(t) = \frac{2}{\pi} \sum_{n=1}^N \frac{(-1)^{n+1}}{n} \sin(n \omega t) = \frac{2}{\pi} \left( \sin(\omega t) - \frac{1}{2}\sin(2\omega t) + \frac{1}{3}\sin(3\omega t) - \dots \right)$$

3. **Triangle Wave (Rapid $1/n^2$ Falloff):**
   $$f_{\text{triangle}}(t) = \frac{8}{\pi^2} \sum_{k=1}^N \frac{(-1)^{k-1}}{(2k-1)^2} \sin((2k-1)\omega t) = \frac{8}{\pi^2} \left( \sin(\omega t) - \frac{1}{9}\sin(3\omega t) + \frac{1}{25}\sin(5\omega t) - \dots \right)$$

---

### Pillar 2: Kinematics, Phasor Geometry & Gibbs Phenomenon
Each Fourier harmonic term is represented mechanically as a rotating circle (epicycle) of radius $r_n = |c_n|$ revolving at angular frequency $\omega_n = n \omega$. Chaining $N$ epicycles produces the trajectory:

$$\mathbf{z}_N(t) = \mathbf{z}_0 + \sum_{n=1}^N r_n e^{i n \omega t}$$

The vertical coordinate of the outer tip is projected horizontally across time:

$$y_N(t) = \text{Im}\{\mathbf{z}_N(t)\} = \sum_{n=1}^N r_n \sin(n \omega t)$$

#### The Gibbs Phenomenon:
Near a step discontinuity (such as $t = 0$ for a square wave jumping from $-1$ to $+1$), truncating the series at $N$ terms yields:

$$f_N(t) = \frac{2}{\pi} \int_0^t \frac{\sin((2N+1)\omega \tau / 2)}{\sin(\omega \tau / 2)} d\tau$$

As $N \to \infty$, the peak overshoot does not vanish, but converges asymptotically to the sine integral:

$$\lim_{N \to \infty} f_N\left(\frac{\pi}{2N}\right) = \frac{2}{\pi} \text{Si}(\pi) = \frac{2}{\pi} \int_0^\pi \frac{\sin x}{x} dx \approx 1.17898 \dots \times \frac{\pi}{4} \approx 1.08949$$

This creates an inherent **$8.95\%$ overshoot** at step discontinuities, visually manifested as ripples ringing near the corners.

---

### Pillar 3: Energy Conservation & Parseval's Theorem
Parseval's identity establishes that total signal energy (or average power) is strictly conserved when transforming between time and frequency domains:

$$P_{\text{total}} = \frac{1}{T} \int_0^T |f(t)|^2 dt = \frac{a_0^2}{4} + \frac{1}{2} \sum_{n=1}^\infty \left( a_n^2 + b_n^2 \right) = \sum_{n=-\infty}^\infty |c_n|^2$$

For an $N$-harmonic truncation, Bessel's inequality guarantees that the reconstructed signal captures a monotonically increasing fraction of total power:

$$P_N = \sum_{n=-N}^N |c_n|^2 \le P_{\text{total}}, \quad \lim_{N \to \infty} \left( P_{\text{total}} - P_N \right) = 0$$

The residual mean squared error (MSE) is precisely the energy contained in the discarded higher harmonics:

$$\mathcal{E}_{\text{error}}(N) = \frac{1}{T} \int_0^T |f(t) - f_N(t)|^2 dt = \sum_{|n| > N} |c_n|^2$$

---

## 3. Physical Parameters & SI Units

| Symbol | Quantity | Default Value | SI Unit | Description |
| :--- | :--- | :--- | :--- | :--- |
| $N$ | Harmonic Order | $5$ ($1 \dots 25$) | dimensionless | Number of active Fourier epicycle circles |
| $\omega$ | Base Angular Frequency | $1.6 \cdot \text{speed}$ | $\text{rad/s}$ | Base rotational speed of fundamental circle |
| $R_0$ | Base Epicycle Radius | $\min(0.14W, 0.20H)$ | $\text{px}$ | Spatial radius scaling for the fundamental harmonic |
| $r_n$ | Harmonic Radius | $R_0 \cdot |b_n|$ | $\text{px}$ | Radius of the $n$-th epicyclic sub-circle |
| $\text{speed}$ | Speed Multiplier | $1.0\times$ ($0.2\dots 2.5\times$) | dimensionless | Time-dilation factor for interactive playback |
| $y_{\text{base}}$ | Canvas Center ($c_y$) | $0.38 \cdot H$ | $\text{px}$ | Elevated apparatus baseline in viewport |

---

## 4. Kotlin Multiplatform Compose Simulation Architecture

```
┌────────────────────────────────────────────────────────────────────────┐
│                      Compose UI Screen Viewport                        │
│                                                                        │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │ 📊 FOURIER SYNTHESIS TELEMETRY (Transparent HUD Card)          │   │
│   │ Target Wave: Square Wave  •  Harmonics (N): 5                  │   │
│   │ Speed: 1.0x  •  Gibbs Ringing: Visible (~9% overshoot)         │   │
│   └────────────────────────────────────────────────────────────────┘   │
│                                                                        │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │ 🎨 Interactive Epicycle & Wave Canvas (Elevated cy = 0.38 * h) │   │
│   │   [Epicycles] ──(Laser)──> [ Synthesized Waveform Curve ]      │   │
│   │     (⟲) (⟲)                  ~^~^~ Gibbs Overshoot Ripples     │   │
│   │                                [ ▂▃▅█ FFT Harmonic Bars ]      │   │
│   └────────────────────────────────────────────────────────────────┘   │
│                                                                        │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │ ⚙️ Compact Controls Deck (Bottom 35% Reserved Viewport)         │   │
│   │   [Square] [Sawtooth] [Triangle] [Pulse] (32dp compact chips)  │   │
│   │   [ Harmonics (N) Slider ]   |   [ Speed (ω) Slider ]          │   │
│   │   [ ⏸ Pause / ▶ Resume ]                     [ Reset ↺ ]       │   │
│   └────────────────────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────────────────────┘
```

### Harmonic Coefficient Calculation in Kotlin
```kotlin
private fun getHarmonicParams(
    i: Int,
    baseRadius: Float,
    waveType: FourierWaveType
): Pair<Int, Float> {
    return when (waveType) {
        FourierWaveType.SQUARE -> {
            val n = 2 * i - 1
            val r = (baseRadius * 4f / (n * PI.toFloat()))
            n to r
        }
        FourierWaveType.SAWTOOTH -> {
            val n = i
            val r = (baseRadius * 2f / (n * PI.toFloat()))
            n to r
        }
        FourierWaveType.TRIANGLE -> {
            val n = 2 * i - 1
            val r = (baseRadius * 8f / (n * n * PI.toFloat() * PI.toFloat()))
            n to r
        }
        FourierWaveType.PULSE -> {
            val n = i
            val duty = 0.25f
            val r = (baseRadius * 2f * sin(n * PI.toFloat() * duty) / (n * PI.toFloat())).coerceAtLeast(0f)
            n to r
        }
    }
}
```

---

## 5. Suggested Interactive Investigations
1. **Gibbs Ringing Observation:** Select **Square Wave** and dial $N$ from $1$ up to $25$. Notice how the wave becomes sharper, but the percentage overshoot at the transition edge remains constant!
2. **Convergence Speed Comparison:** Compare **Square Wave** ($1/n$ decay) against **Triangle Wave** ($1/n^2$ decay). Notice that Triangle requires only $N = 3$ to look almost like a perfect straight triangular wave.
3. **Harmonic Spectrum Reading:** Observe the mini-FFT spectrum below the graph to confirm that Square and Triangle only have odd harmonic spikes, whereas Sawtooth activates all integer frequencies.
