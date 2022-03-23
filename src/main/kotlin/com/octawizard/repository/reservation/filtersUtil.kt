package com.octawizard.repository.reservation

import com.mongodb.client.model.Filters
import com.mongodb.client.model.geojson.Point
import com.octawizard.domain.model.RadiusUnit
import org.bson.conversions.Bson
import org.litote.kmongo.geoWithinCenterSphere
import kotlin.reflect.KProperty1

infix fun Bson.and(filter: Bson?): Bson = Filters.and(this, filter)

fun filterGeoWithinSphere(
    kProperty: KProperty1<*, Point?>,
    longitude: Double,
    latitude: Double,
    radius: Double,
    radiusUnit: RadiusUnit,
): Bson =
    kProperty.geoWithinCenterSphere(longitude, latitude, radius / radiusUnit.earthRadius)
