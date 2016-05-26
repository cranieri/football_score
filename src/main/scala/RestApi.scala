import Game.GameScore
import GamesManager._
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout

class RestApi(system: ActorSystem, timeout: Timeout) extends GameMarshalling {
  implicit val requestTimeout = timeout

  implicit def executionContext = system.dispatcher

  def createGamesManager() = system.actorOf(GamesManager.props, GamesManager.name)

  lazy val gamesManager = createGamesManager()

  def routes = gameGetRoute ~ scoreRoute

  def getGames() =
    gamesManager.ask(GetGames).mapTo[Games]

  def createGame(home: String, away: String) =
    gamesManager.ask(CreateGame(home, away)).mapTo[GamesManager.Game]

  def addGame(home: String, away: String) =
    gamesManager.ask(AddGame(home, away)).mapTo[GamesManager.Game]

  def getScore(home: String, away: String) =
    gamesManager.ask(GamesManager.GetScore(home, away)).mapTo[Game.GameScore]

  def addScore(home: String, away: String, homeScore:String, awayScore:String) =
    gamesManager.ask(GamesManager.AddScore(home, away, homeScore, awayScore)).mapTo[Game.GameScore]

  def gameGetRoute =
    pathPrefix("games") {
      pathEndOrSingleSlash {
        get {
          onSuccess(getGames()) {
            case games => complete(games)
          }
        } ~
        post {
          entity(as[GamesManager.Game]) { game =>
            onSuccess(addGame(game.home, game.away)) { game =>
              complete(game)}
          }
        }
      }
    }

  def scoreRoute =
    pathPrefix("scores" / Segment) { game =>
      pathEndOrSingleSlash {
        get {
          val gameParam = game.split('-')
          val home = gameParam(0)
          val away = gameParam(1)
          onSuccess(getScore(home, away)) {
            case score => complete(score)
          }
        } ~
        post {
          val gameParam = game.split('-')
          val home = gameParam(0)
          val away = gameParam(1)
          entity(as[GameScore]) { gameScore =>
            onSuccess(addScore(home, away, gameScore.homeScore, gameScore.awayScore)) { score =>
              complete(score)}
          }
        }
      }
    }
}