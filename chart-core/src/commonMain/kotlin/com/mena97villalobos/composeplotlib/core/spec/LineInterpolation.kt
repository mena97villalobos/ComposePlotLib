package com.mena97villalobos.composeplotlib.core.spec

/**
 * How a line series connects its vertices. Set on [LineStyle.interpolation]. Stepped modes insert corner vertices
 * (stroke and area fill both follow the steps); [MonotoneCubic] draws a shape-preserving smooth curve that does not
 * overshoot the data. All modes keep the original data vertices for markers, selection, and hit-testing.
 */
enum class LineInterpolation {
    /** Straight segments between consecutive points (default). */
    Linear,

    /** Step function: the value changes **before** x (vertical then horizontal). Like d3 `curveStepBefore`. */
    StepBefore,

    /** Step function: the value changes **after** x (horizontal then vertical). Like d3 `curveStepAfter`. */
    StepAfter,

    /** Step function with the transition at the **midpoint** x between samples. Like d3 `curveStep`. */
    StepMiddle,

    /** Fritsch–Carlson monotone cubic Hermite spline — smooth and monotonicity-preserving (no overshoot). */
    MonotoneCubic,
}
