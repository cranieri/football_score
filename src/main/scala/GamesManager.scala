/**
  * Created by cosimoranieri on 07/05/2016.
  */
import GamesManager._
import akka.actor._
import akka.persistence._
import akka.util.Timeout

import scala.concurrent.{Future, Promise}

object GamesManager {
  def props(implicit timeout: Timeout) = Props(new GamesManager)
  def name = "gamesManager"

  sealed trait Command
  case class Game(home: String, away: String) extends Command
  case class Games(games: Vector[GamesManager.Game]) extends Command
  case object GetGames
  case class GetGame(name:String)
  case class CreateGame(home: String, away: String) extends Command
  case class AddGame(home: String, away:String)
  case class AddScore(home:String, away:String, homeScore:String, awayScore:String)
  case class GetScore(home:String, away:String)

  case object GameExists

  sealed trait Event
  case class GameAdded(home: String, away: String) extends Event
}

class GamesManager(implicit timeout: Timeout) extends PersistentActor {
  implicit val ec = context.dispatcher

  var games = Vector.empty[GamesManager.Game]

  override def persistenceId = s"${self.path.name}"

  var nrEventsRecovered = 0

  def receiveRecover = {
    case event:Event => {
      updateState(event)
    }
  }

  def receiveCommand = {
    case AddGame(home, away) => {
      val childName = s"$home-$away"
      context.child(childName) match {
          case Some(child) => sender() ! GamesManager.Game(home, away)
          case None => persist(GameAdded(home, away))(updateState)
      }
    }
    case AddScore(home, away, homeScore, awayScore) => {
      context.child(s"$home-$away") match {
        case Some(child) => {
          println(s"Found child $home$away")
          child forward Game.AddScore(homeScore, awayScore)
        }
        case None => println("Child not found")
      }
    }

    case GetScore(home, away) => {
      context.child(s"$home-$away") match {
        case Some(child) => child forward Game.GetScore
        case None =>
      }
    }

    case GetGame(name) => {
      def notFound() = sender() ! None
      def getEvent(child: ActorRef) = child forward Game.GetGame
      context.child(name).fold(notFound())(getEvent)
    }
    case GetGames => {
      import akka.pattern.ask
      import akka.pattern.pipe

      def getGames = context.children.map { child =>
        self.ask(GetGame(child.path.name)).mapTo[Option[GamesManager.Game]]
      }
      def convertToGames(f: Future[Iterable[Option[GamesManager.Game]]]) =
        f.map(_.flatten).map(l=> Games(l.toVector))


      pipe(convertToGames(Future.sequence(getGames))) to sender()
    }
  }

  private val updateState: (Event => Unit) = {
    case GameAdded(home, away) =>  {

      import akka.pattern.pipe
      def createGame: Future[GamesManager.Game] = {
        val p = Promise[GamesManager.Game]()
        Future {
          val gameChild = context.actorOf(Game.props(home, away), s"$home-$away")
          val game = gameChild.path.name.split("-")

          p.success(GamesManager.Game(game(0), game(1)))
        }
        p.future
      }
      pipe(createGame) to sender()

    }

  }
}
