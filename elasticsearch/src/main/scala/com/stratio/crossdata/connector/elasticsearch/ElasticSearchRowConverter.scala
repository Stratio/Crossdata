/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.crossdata.connector.elasticsearch

import java.sql.Timestamp
import java.sql.{Date => SQLDate}
import java.text.SimpleDateFormat
import java.util
import java.util.Date

import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.{Attribute, GenericRowWithSchema}
import org.apache.spark.sql.types._
import org.elasticsearch.search.SearchHit
import org.elasticsearch.search.SearchHitField
import org.joda.time.DateTime

import scala.collection.JavaConverters._

object ElasticSearchRowConverter {


  def asRows(schema: StructType, array: Array[SearchHit], requiredFields: Seq[Attribute]): Array[Row] = {

    array map { hit =>
      hitAsRow(
        hit.fields().asScala.toMap,
        Option(hit.getSource).map(_.asScala.toMap).getOrElse(Map.empty),
        schema,
        requiredFields.map(_.name)
      )
    }
  }

  def hitAsRow(
                hitFields: Map[String, SearchHitField],
                subDocuments: Map[String, AnyRef],
                schema: StructType,
                requiredFields: Seq[String]): Row = {

    val schemaMap = schema.map(field => field.name -> field.dataType).toMap

    val values: Seq[Any] = requiredFields.map { name =>

      // TODO: Note that if a nested subdocument is targeted, it won't work and this algorithm should be made recursive.
      (hitFields.get(name) orElse subDocuments.get(name)).flatMap(Option(_)) map {
        ((value: Any) => enforceCorrectType(value, schemaMap(name))) compose {
          case hitField: SearchHitField =>
            if(hitField.getValues.size()>1) hitField.getValues
            else hitField.getValue
          case other => other
        }
      } orNull

    }
    new GenericRowWithSchema(values.toArray, schema)
  }

  protected def enforceCorrectType(value: Any, desiredType: DataType): Any =
      // TODO check if value==null
      desiredType match {
        case StringType => value.toString
        case _ if value == "" => null // guard the non string type
        case ByteType => toByte(value)
        case ShortType => toShort(value)
        case IntegerType => toInt(value)
        case LongType => toLong(value)
        case DoubleType => toDouble(value)
        case FloatType => toFloat(value)
        case DecimalType() => toDecimal(value)
        case BooleanType => value.asInstanceOf[Boolean]
        case TimestampType => toTimestamp(value)
        case NullType => null
        case DateType => toDate(value)
        case BinaryType => toBinary(value)
        case schema: StructType => toRow(value, schema)
        case ArrayType(elementType: DataType, _) => toArray(value, elementType)
        case _ =>
          sys.error(s"Unsupported datatype conversion [${value.getClass}},$desiredType]")
          value
      }

  private def toByte(value: Any): Byte = value match {
    case value: Byte => value
    case value: Int => value.toByte
    case value: Long => value.toByte
  }

  private def toShort(value: Any): Short = value match {
    case value: Int => value.toShort
    case value: Long => value.toShort
  }

  private def toInt(value: Any): Int = {
    import scala.language.reflectiveCalls
    value match {
      case value: String => value.toInt
      case _ => value.asInstanceOf[ {def toInt: Int}].toInt
    }
  }

  private def toLong(value: Any): Long = {
    value match {
      case value: Int => value.toLong
      case value: Long => value
    }
  }

  private def toDouble(value: Any): Double = {
    value match {
      case value: Int => value.toDouble
      case value: Long => value.toDouble
      case value: Double => value
    }
  }

  private def toFloat(value: Any): Float = {
    value match {
      case value: Int => value.toFloat
      case value: Long => value.toFloat
      case value: Float => value
      case value: Double => value.toFloat
    }
  }

  private def toDecimal(value: Any): Decimal = {
    value match {
      case value: Int => Decimal(value)
      case value: Long => Decimal(value)
      case value: java.math.BigInteger => Decimal(new java.math.BigDecimal(value))
      case value: Double => Decimal(value)
      case value: java.math.BigDecimal => Decimal(value)
    }
  }

  private def toTimestamp(value: Any): Timestamp = {
    value match {
      case value : String =>
        val dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS")
        val parsedDate = dateFormat.parse(value)
        new java.sql.Timestamp(parsedDate.getTime)
      case value: java.util.Date => new Timestamp(value.getTime)
      case _ => sys.error(s"Unsupported datatype conversion [${value.getClass}},Timestamp]")
    }
  }

  def toDate(value: Any): Date = {
    value match {
      case value: String => new SQLDate(DateTime.parse(value).getMillis)
    }
  }

  def toBinary(value: Any): Array[Byte] = value match {
    case str: String => str.getBytes
    case arr: Array[Byte @unchecked] if arr.headOption.collect { case _: Byte => true } getOrElse false => arr
    case _ => sys.error(s"Unsupported datatype conversion [${value.getClass}},Array[Byte]")
  }


  def toRow(value: Any, schema: StructType): Row = value match {
    case m: util.HashMap[String @ unchecked, _] =>
      val rowValues = schema.fields map (field => enforceCorrectType(m.get(field.name), field.dataType))
      new GenericRowWithSchema(rowValues, schema)
    case _ => sys.error(s"Unsupported datatype conversion [${value.getClass}},Row")
  }

  def toArray(value: Any, elementType: DataType): Seq[Any] = value match {
    case arr: util.ArrayList[Any] =>
      arr.toArray.map(enforceCorrectType(_, elementType))
  }

}
