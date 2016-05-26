import Game._
import akka.actor.Props
import akka.persistence.PersistentActor
import scala.concurrent.{Future, Promise}

/**
  * Created by cosimoranieri on 23/05/2016.
  */

object Game {
  def props(home:String, away:String) = Props(new Game(home, away))
  def name = "gamesManager"

  sealed trait Command
  case class AddScore(homeScore:String, awayScore:String) extends Command
  case object GetScore
  case object GetGame

  case object GameExists

  sealed trait Event
  case class ScoreAdded(home: String, away: String) extends Event
  case class GameScore(homeScore:String = "0", awayScore:String = "0")
}


class Game(home:String, away:String) extends PersistentActor {
  override def persistenceId = s"${self.path.name}"
  implicit val ec = context.dispatcher

  var score = GameScore()
  def receiveRecover = {
    case event:Event => {
      updateState(event)
    }
  }

  def receiveCommand = {
    case AddScore(home, away) => {
      println(s"Added score $home$away")
      persist(ScoreAdded(home, away))(updateState)
    }
    case GetScore => {
      println(" GameActor get score")
      sender() ! score
    }
    case GetGame => sender() ! Some(GamesManager.Game(home, away))
  }

  private val updateState: (Event => Unit) = {
    case ScoreAdded(homeScore, awayScore) =>  {
      import akka.pattern.pipe
      def updateScore: Future[GameScore] = {
        val p = Promise[GameScore]()
        Future {
          score = GameScore(homeScore, awayScore)
          p.success(score)
        }
        p.future
      }
      pipe(updateScore) to sender()

    }

  }
}
