/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.crossdata.driver.util

import java.io.File

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, _}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Source}
import com.stratio.crossdata.common.security.Session
import com.stratio.crossdata.common.serializers.CrossdataCommonSerializer
import com.stratio.crossdata.driver.config.DriverConf
import com.stratio.crossdata.driver.util.HttpClient.HttpClientContext
import org.json4s.jackson

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

//TODO: Integrate this functionality into the current state of Http and ClusterClient drivers

object HttpClient {

  case class HttpClientContext(config: DriverConf, actorSystem: ActorSystem)

  def apply(implicit ctx: HttpClientContext): HttpClient = new HttpClient(ctx)

  def apply(config: DriverConf, actorSystem: ActorSystem): HttpClient =
    new HttpClient(HttpClientContext(config, actorSystem))
}

class HttpClient(ctx: HttpClientContext) extends CrossdataCommonSerializer{

  private implicit val actorSystem = ctx.actorSystem
  private val config = ctx.config
  private implicit val materializer: ActorMaterializer = ActorMaterializer()
  import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
  import akka.http.scaladsl.marshalling._
  implicit val serialization = jackson.Serialization

  private val http = Http(actorSystem)
  private val serverHttp = config.getCrossdataServerHttp
  private val protocol = "http"


  def sendJarToHTTPServer(path: String, session: Session): Future[String] = {
    val sessionUUID = session.id

    for (
      request <- createSendFileRequest(s"$protocol://$serverHttp/upload/$sessionUUID", new File(path));
      response <- http.singleRequest(request) map {
        case resp@HttpResponse(StatusCodes.OK, _, entity, _) => resp
        case HttpResponse(code, _, _, _) => throw new RuntimeException(s"Request failed, response code: $code")
      };
      strictEntity <- response.entity.toStrict(5 seconds)
    ) yield strictEntity.data.decodeString("UTF-8")
  }

  private def createJarEntity(file: File): Future[RequestEntity] = {
    require(file.exists())
    val fileIO = FileIO.fromFile(file)
    val formData =
      Multipart.FormData(
        Source.single(
          Multipart.FormData.BodyPart(
            "fileChunk",
            HttpEntity(ContentTypes.`application/octet-stream`, file.length(), fileIO),
            Map("filename" -> file.getName))))
    Marshal(formData).to[RequestEntity]
  }

  private def createSendFileRequest(target: Uri, file: File): Future[HttpRequest] =
    for {
      e ← createJarEntity(file)
    } yield HttpRequest(HttpMethods.POST, uri = target, entity = e)

}
