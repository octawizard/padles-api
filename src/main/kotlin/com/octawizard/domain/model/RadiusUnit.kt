package com.octawizard.domain.model

private const val EARTH_RADIUS_IN_MILES = 3963.2

private const val EARTH_RADIUS_IN_KM = 6378.1

enum class RadiusUnit(val earthRadius: Double) {
    Miles(EARTH_RADIUS_IN_MILES),
    Kilometers(EARTH_RADIUS_IN_KM)
}
