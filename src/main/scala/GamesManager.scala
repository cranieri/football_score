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
  case class GetGame(name:String)
  case class CreateGame(home: String, away: String) extends Command
  case class AddGame(home: String, away:String)
  case class AddScore(home:String, away:String, homeScore:String, awayScore:String)
  case class GetScore(home:String, away:String)

  case object GameExists

  sealed trait Event
  case class GameCreated(home: String, away: String) extends Event
  case class GameAdded(home: String, away: String) extends Event
}

class GamesManager(implicit timeout: Timeout) extends PersistentActor {
  implicit val ec = context.dispatcher

  var games = Vector.empty[Game]

  override def persistenceId = s"${self.path.name}"

  var nrEventsRecovered = 0

  def receiveRecover = {
    case event:Event => {
      updateState(event)
    }
  }

  val receiveCommand: Receive = {
    case AddGame(home, away) => {
      val childName = s"$home-$away"
      context.child(childName) match {
          case Some(child) => Game(home, away)
          case None => persist(GameAdded(home, away))(updateState)
      }
    }
    case AddScore(home, away, homeScore, awayScore) => {
      context.child(s"$home-$away") match {
        case Some(child) => {
          println(s"Found child $home$away")
          child forward GameActor.AddScore(homeScore, awayScore)
        }
        case None => println("Child not found")
      }
    }

    case GetScore(home, away) => {
      context.child(s"$home-$away") match {
        case Some(child) => child forward GameActor.GetScore
        case None =>
      }
    }

    case GetGame(name) => {
      def notFound() = sender() ! None
      def getEvent(child: ActorRef) = child forward GameActor.GetGame
      context.child(name).fold(notFound())(getEvent)
    }
    case GetGames => {
      import akka.pattern.ask
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

      def getGames = context.children.map { child =>
        self.ask(GetGame(child.path.name)).mapTo[Option[Game]] //<co id="ch02_ask_event"/>
      }
      def convertToGames(f: Future[Iterable[Option[Game]]]) =
        f.map(_.flatten).map(l=> Games(l.toVector)) //<co id="ch02_flatten_options"/>


      pipe(convertToGames(Future.sequence(getGames))) to sender()
    }
  }

  private val updateState: (Event => Unit) = {
    case GameAdded(home, away) =>  {

      import akka.pattern.pipe
      def createGame: Future[Game] = {
        val p = Promise[Game]()
        Future {
          val gameChild = context.actorOf(GameActor.props(home, away), s"$home-$away")
          val game = gameChild.path.name.split("-")

          p.success(Game(game(0), game(1)))
        }
        p.future
      }
      pipe(createGame) to sender()

    }

  }
}
