import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}
import akka.http.scaladsl.model.HttpEntity

import scala.concurrent.duration.{Duration, FiniteDuration}
import spray.json._

/**
  * Created by cosimoranieri on 07/05/2016.
  */
class RestApiIntegrationSpec extends WordSpec with Matchers with ScalatestRouteTest with GameMarshalling{
  //  implicit val requestTimeout = scala.concurrent.duration.Duration("100")
  //  implicit def executionContext = system.dispatcher
  import GamesManager._
  val t = "100s"
  val d = Duration(t)
  val timeout = FiniteDuration(d.length, d.unit)
  val restApi = new RestApi(system, timeout)

  "The football score API" must {
    "return games" in  {
      restApi.getGames()
      val expectedGames = Games(Vector.empty :+ Game("Juventus", "Sampdoria"))
      Get("/games") ~> restApi.routes ~> check {
        responseEntity shouldEqual HttpEntity(`application/json`, expectedGames.toJson.prettyPrint)
        status.isSuccess() shouldEqual true
      }

    }
  }


}
