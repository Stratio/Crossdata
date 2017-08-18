/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package org.apache.spark.sql.crossdata.test

import com.stratio.common.utils.components.logger.impl.SparkLoggerComponent

import scala.util.Try

trait SharedXDContextWithDataTest extends SharedXDContextTest with SparkLoggerComponent {

  import org.apache.spark.sql.crossdata.test.SharedXDContextWithDataTest._

  //Template settings: Override them

  type ClientParams  /* Abstract type which should be overridden in order to specify the type of
                      * the native client used in the test to insert test data.
                      */


  val runningError: String                             /* Error message shown when a test is running without a propper
                                                        * environment being set
                                                        */

  val provider: String                                 // Datasource class name (fully specified)
  def defaultOptions: Map[String, String] = Map.empty  // Spark options used to register the test table in the catalog

  def sparkRegisterTableSQL: Seq[SparkTable] = Nil     /* Spark CREATE sentence. Without OPTIONS or USING parts since
                                                        * they'll be generated from `provider` and `defaultOptions`
                                                        * attributes.
                                                        * e.g: override def sparkRegisterTableSQL: Seq[SparkTable] =
                                                        *          Seq("CREATE TABLE T", "CREATE TEMPORARY TABLE S")
                                                        */

  lazy val assumeEnvironmentIsUpAndRunning =
    if (!isEnvironmentReady) {
      fail(runningError)
    }


  protected def prepareClient: Option[ClientParams]    // Native client initialization
  protected def terminateClient: Unit                  // Native client finalization
  protected def saveTestData: Unit = ()                // Creation and insertion of test data examples
  protected def cleanTestData: Unit                    /* Erases test data from the data source after the test has
                                                        * finished
                                                        */

  //Template: This is the template implementation and shouldn't be modified in any specific test

  implicit def str2sparkTableDesc(query: String): SparkTable = SparkTable(query, defaultOptions)

  var client: Option[ClientParams] = None
  var isEnvironmentReady = false

  protected override def beforeAll(): Unit = {
    super.beforeAll()

    isEnvironmentReady = Try {
      client = prepareClient
      saveTestData
      sparkRegisterTableSQL.foreach { case SparkTable(s, opts) => sql(Sentence(s, provider, opts).toString) }
      client.isDefined
    } recover { case e: Throwable =>
      logError(e.getMessage, e)
      false
    } get
  }

  protected override def afterAll() = {
    _xdContext.dropAllTables()
    super.afterAll()
    for (_ <- client) cleanEnvironment
  }

  private def cleanEnvironment: Unit = {
    cleanTestData
    terminateClient
  }

}

object SharedXDContextWithDataTest {

  case class Sentence(query: String, provider: String, options: Map[String, String]) {
    override def toString: String = {
      val opt = options.map { case (k, v) => s"$k " + s"'$v'" } mkString ","
      s"$query USING $provider" + options.headOption.fold("")(_ => s" OPTIONS ( $opt ) ")
    }
  }

  case class SparkTable(sql: String, options: Map[String, String])

}