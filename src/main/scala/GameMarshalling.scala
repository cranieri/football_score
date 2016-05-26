import Game.GameScore
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

trait GameMarshalling  extends DefaultJsonProtocol with SprayJsonSupport {
  import GamesManager._

  implicit val gameFormat = jsonFormat2(GamesManager.Game)
  implicit val gamesFormat = jsonFormat1(Games)
  implicit val scoreFormat = jsonFormat2(GameScore)
}
