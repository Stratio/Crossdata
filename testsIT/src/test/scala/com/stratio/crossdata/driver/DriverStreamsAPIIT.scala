package com.stratio.crossdata.driver

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.testkit.scaladsl.TestSink
import com.stratio.crossdata.driver.test.Utils._
import org.apache.spark.sql.Row
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner

import scala.concurrent.duration._
import scala.language.postfixOps


@RunWith(classOf[JUnitRunner])
class DriverStreamsAPIIT extends EndToEndTest with ScalaFutures {

  implicit val aSystem: ActorSystem = ActorSystem()
  implicit val aMater: ActorMaterializer = ActorMaterializer()

  Seq(Driver.http -> "through HTTP") foreach { case (factory, description) =>

    implicit val ctx = DriverTestContext(factory)
    val factoryDesc = s" $description"

    it should "return a SuccessfulQueryResult when executing a select *" + factoryDesc in {
      assumeCrossdataUpAndRunning()
      withDriverDo { driver =>

        driver.sql(s"CREATE TEMPORARY TABLE jsonTable USING org.apache.spark.sql.json OPTIONS (path '${Paths.get(getClass.getResource("/tabletest.json").toURI).toString}')").waitForResult()

        whenReady(driver.sqlStreamedResult("SELECT * FROM jsonTable")) { streamedSQLResult =>
          streamedSQLResult.schema.fieldNames should contain allOf("id", "title")

          streamedSQLResult.rowsSource.runWith(TestSink.probe[Row])
            .requestNext(Row(1, "Crossdata"))
            .requestNext(Row(2, "Fuse"))
            .request(1).expectComplete()

        }(PatienceConfig(timeout = 4 seconds))

      }
    }
  }

}
