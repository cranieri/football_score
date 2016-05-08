import GamesManager.{CreateGame, Game, Games, GetGames}
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout

class RestApi(system: ActorSystem, timeout: Timeout) extends GameMarshalling {
  implicit val requestTimeout = timeout

  implicit def executionContext = system.dispatcher

  def createGamesManager() = system.actorOf(GamesManager.props, GamesManager.name)

  lazy val gamesManager = createGamesManager()

  def routes = gameGetRoute

  def getGames() =
    gamesManager.ask(GetGames).mapTo[Games]

  def createGame(home: String, away: String) =
    gamesManager.ask(CreateGame(home, away)).mapTo[Game]

  def gameGetRoute =
    pathPrefix("games") {
      pathEndOrSingleSlash {
        get {
          onSuccess(getGames()) {
            case games => complete(games)
          }
        } ~
        post {
          entity(as[Game]) { game =>
            onSuccess(createGame(game.home, game.away)) { game =>
              complete(game)}
          }
        }
      }
    }
}