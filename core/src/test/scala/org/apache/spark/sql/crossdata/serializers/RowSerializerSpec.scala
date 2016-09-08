package org.apache.spark.sql.crossdata.serializers

import com.stratio.crossdata.test.BaseXDTest
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types._

import org.json4s._
import org.json4s.jackson.JsonMethods._

class RowSerializerSpec extends BaseXDTest {

  val schema = StructType(List(
    StructField("int",IntegerType,true),
    StructField("bigint",LongType,true),
    StructField("long",LongType,true),
    StructField("string",StringType,true),
    StructField("boolean",BooleanType,true),
    StructField("double",DoubleType,true),
    StructField("float",FloatType,true),
    StructField("decimalint",DecimalType(10,0),true),
    StructField("decimallong",DecimalType(10,0),true),
    StructField("decimaldouble",DecimalType(10,0),true),
    StructField("decimalfloat",DecimalType(10,0),true),
    StructField("date", DateType,true),
    StructField("timestamp",TimestampType,true),
    StructField("smallint", ShortType, true),
    StructField("binary",BinaryType,true),
    StructField("arrayint",ArrayType(IntegerType,true),true),
    StructField("arraystring",ArrayType(StringType,true),true),
    StructField("mapstringint",MapType(StringType,IntegerType,true),true),
    StructField("mapstringstring",MapType(StringType,StringType,true),true),
    StructField("struct",StructType(StructField("field1",IntegerType,true)::StructField("field2",IntegerType,true) ::Nil), true),
    StructField("arraystruct",ArrayType(StructType(StructField("field1",IntegerType,true)::StructField("field2", IntegerType,true)::Nil),true),true),
    StructField("structofstruct",StructType(StructField("field1",TimestampType,true)::StructField("field2", IntegerType, true)::StructField("struct1",StructType(StructField("structField1",StringType,true)::StructField("structField2",IntegerType,true)::Nil),true)::Nil),true)
  ))

  val values: Array[Any] =  Array(
    2147483647,
    9223372036854775807L,
    9223372036854775807L,
    "string",
    true,
    3.3,
    3.3F,
    Decimal(12),
    Decimal(22),
    Decimal(32.0),
    Decimal(42.0),
    java.sql.Date.valueOf("2015-11-30"),
    java.sql.Timestamp.valueOf("2015-11-30 10:00:00.0"),
    12.toShort,
    "abcde".getBytes,
    new GenericArrayData(Array(4, 42)),
    new GenericArrayData(Array("hello", "world")),
    ArrayBasedMapData(Map("b" -> 2)),
    ArrayBasedMapData(Map("a" -> "A", "b" -> "B")),
    new GenericRowWithSchema(Array(99,98), StructType(StructField("field1", IntegerType)::StructField("field2", IntegerType)::Nil)),
    new GenericArrayData(
      Array(
      new GenericRowWithSchema(Array(1,2), StructType(StructField("field1", IntegerType)::StructField("field2", IntegerType)::Nil)),
      new GenericRowWithSchema(Array(3,4), StructType(StructField("field1", IntegerType)::StructField("field2", IntegerType)::Nil))
      )
    ),
    new GenericRowWithSchema(
      Array(
        java.sql.Timestamp.valueOf("2015-11-30 10:00:00.0"),
        42,
        new GenericRowWithSchema(
          Array("a glass of wine a day keeps the doctor away", 1138),
          StructType(StructField("structField1",StringType,true)::StructField("structField2",IntegerType,true)::Nil)
        )
      ),
      StructType(
        List(
          StructField("field1",TimestampType,true),
          StructField("field2", IntegerType, true),
          StructField("struct1",StructType(StructField("structField1",StringType,true)::StructField("structField2",IntegerType,true)::Nil),true)
        )
      )
    )
  )

  val rowWithNoSchema = Row.fromSeq(values)
  val rowWithSchema = new GenericRowWithSchema(values, schema)

  implicit val formats = DefaultFormats +  StructTypeSerializer + new RowSerializer(schema)

  "A RowSerializer" should "marshall & unmarshall a row with no schema" in {

    val serialized = pretty(render(Extraction.decompose(rowWithNoSchema)))
    val deserialized = parse(serialized).extract[Row]

    deserialized shouldEqual rowWithNoSchema

  }

  it should "marshall & unmarshall a row with schema" in {

    val serialized = pretty(render(Extraction.decompose(rowWithSchema)))
    val deserialized = parse(serialized).extract[Row]

    deserialized shouldEqual rowWithSchema
  }



}
