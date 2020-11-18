//package com.octawizard.repository.reservation
//
//import com.mongodb.ConnectionString
//import com.mongodb.client.MongoClient
//import com.mongodb.client.MongoCollection
//import com.mongodb.client.model.geojson.Geometry
//import com.mongodb.client.model.geojson.Point
//import com.mongodb.client.model.geojson.Position
//import com.octawizard.domain.model.Field
//import com.octawizard.domain.model.WallsMaterial
//import com.octawizard.repository.reservation.model.ClubReservationInfoDTO
//import org.junit.Rule
//import org.junit.Test
//import org.junit.experimental.categories.Category
//import org.litote.kmongo.KFlapdoodleRule
//import org.litote.kmongo.KMongoBaseTest
//import org.litote.kmongo.NativeMappingCategory
//import org.litote.kmongo.findOne
//import org.litote.kmongo.service.MongoClientProvider
//import java.lang.reflect.ParameterizedType
//import java.util.*
//import kotlin.reflect.KClass
//import kotlin.test.assertEquals
//
//class GeometryTest : KMongoBaseTest<ClubReservationInfoDTO>() {
//
//    @Category(NativeMappingCategory::class)
//    @Test
//    fun `deserializing is ok`() {
//        val fieldId = UUID.randomUUID()
//        val field = Field(fieldId, false, WallsMaterial.Glass, "field")
//        val data = ClubReservationInfoDTO(
//                UUID.randomUUID(),
//                "club name",
//                field,
//                Point(Position(20.0, 20.0))
//        )
//        col.insertOne(data)
//        assertEquals(data, col.findOne())
//    }
//
//}
//
//class Issue111MongoPoint : KMongoBaseTest<Issue111MongoPoint.MyData>() {
//
//    data class MyData(val location: Point)
//
//    @Category(NativeMappingCategory::class)
//    @Test
//    fun `deserializing is ok`() {
//        val data = MyData(Point(Position(20.0, 20.0)))
//        col.insertOne(data)
//        assertEquals(data, col.findOne())
//    }
//
//}
