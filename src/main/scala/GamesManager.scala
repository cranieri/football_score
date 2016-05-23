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
  case class Games(games: Vector[Game]) extends Command
  case object GetGames
  case class CreateGame(home: String, away: String) extends Command
  case class AddGame(home: String, away:String)
  case class AddScore(home:String, away:String, homeScore:String, awayScore:String)
  case class GetScore(home:String, away:String)

  case object GameExists

  sealed trait Event
  case class GameCreated(home: String, away: String) extends Event
}

class GamesManager(implicit timeout: Timeout) extends PersistentActor {

  implicit val ec = context.dispatcher

  var games = Vector.empty[Game]

  override def persistenceId = s"${self.path.name}"

  var nrEventsRecovered = 0

  def receiveRecover = {
    case event:Event => {
//      nrEventsRecovered = nrEventsRecovered + 1
      updateState(event)
    }
  }

  val receiveCommand: Receive = {
    case CreateGame(home, away) => persist(GameCreated(home, away))(updateState)
    case AddGame(home, away) => {
      val childName = s"$home$away"
      context.child(childName) match {
          case Some(child) =>
          case None => context.actorOf(GameActor.props(home, away), childName)
      }
      sender() ! Game(home, away)
    }
    case AddScore(home, away, homeScore, awayScore) => {
      context.child(s"$home$away") match {
        case Some(child) => {
          println(s"Found child $home$away")
          child forward GameActor.AddScore(homeScore, awayScore)
        }
        case None => println("Child not found")
      }
    }

    case GetScore(home, away) => {
      context.child(s"$home$away") match {
        case Some(child) => child forward GameActor.GetScore
        case None =>
      }
    }
    case GetGames => {
      import akka.pattern.pipe

      def gamesFuture: Future[Games] = {
        val p = Promise[Games]()
        Future {
          println("Fetching games")
          p.success(Games(games))
          println("Returned games")
        }
        println("outside future")
        p.future
      }
      pipe(gamesFuture) to sender()
    }
  }

  private val updateState: (Event => Unit) = {
    case GameCreated(home, away) =>  {
      import akka.pattern.pipe
      def game: Future[Game] = {
        val p = Promise[Game]()
        Future {
          val game = Game(home, away)
          games = games :+ game
          p.success(game)
        }
        p.future
      }
      pipe(game) to sender()
    }

  }
}
