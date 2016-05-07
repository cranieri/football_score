import GamesManager.{Game, Games, GetGames}
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout

class RestApi(system: ActorSystem, timeout: Timeout) extends GameMarshalling {
  implicit val requestTimeout = timeout

  implicit def executionContext = system.dispatcher

  def createGamesManager() = system.actorOf(GamesManager.props, GamesManager.name)

  lazy val gamesManager = createGamesManager()


  def routes = eventsRoute

  def getGames() =
    gamesManager.ask(GetGames).mapTo[Games]

  def eventsRoute =
    pathPrefix("games") {
      pathEndOrSingleSlash {
        get {
          // GET /games
          onSuccess(getGames()) {
            case games => complete(games)
          }
        }
      }
    }
}