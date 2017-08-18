package com.stratio.crossdata.kryo

import com.esotericsoftware.kryo.Kryo
import org.apache.spark.serializer.KryoRegistrator
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types._

class CrossdataRegistrator extends KryoRegistrator{
  override def registerClasses(kryo: Kryo): Unit = {
    kryo.register(Nil.getClass)
    kryo.register(StringType.getClass)
    kryo.register(DoubleType.getClass)
    kryo.register(IntegerType.getClass)
    kryo.register(BooleanType.getClass)
    kryo.register(LongType.getClass)
    kryo.register(classOf[ArrayType])
    kryo.register(classOf[StructType])
    kryo.register(classOf[StructField])
    kryo.register(classOf[Metadata])
    kryo.register(classOf[GenericRowWithSchema])
    kryo.register(classOf[Array[Object]])
    kryo.register(classOf[Array[Row]])
    kryo.register(classOf[scala.collection.immutable.Map$EmptyMap$])
    kryo.register(classOf[scala.collection.immutable.$colon$colon[_]])
    kryo.register(classOf[Array[org.apache.spark.sql.types.StructField]])
  }
}
