package org.apache.spark.sql.crossdata.daos

import com.stratio.common.utils.components.config.impl.{MapConfigComponent, TypesafeConfigComponent}
import com.stratio.common.utils.components.dao.GenericDAOComponent
import com.stratio.common.utils.components.logger.impl.SparkLoggerComponent
import org.apache.spark.sql.crossdata.daos.DAOConstants._
import org.apache.spark.sql.crossdata.models.EphemeralStatusModel
import org.apache.spark.sql.crossdata.serializers.CrossdataSerializer

trait EphemeralTableStatusMapDAO extends GenericDAOComponent[EphemeralStatusModel]
with MapConfigComponent with SparkLoggerComponent with CrossdataSerializer with PrefixedDAO {

override implicit val formats = json4sJacksonFormats

override val dao: DAO = new GenericDAO(Option(s"$BaseZKPath/$prefix$EphemeralTableStatusPath"))

}