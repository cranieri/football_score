import GameActor._
import akka.actor.Props
import akka.persistence.PersistentActor
import scala.concurrent.{Future, Promise}

/**
  * Created by cosimoranieri on 23/05/2016.
  */

object GameActor {
  def props(home:String, away:String) = Props(new GameActor(home, away))
  def name = "gamesManager"

  sealed trait Command
  case class AddScore(homeScore:String, awayScore:String) extends Command
  case object GetScore

  case object GameExists

  sealed trait Event
  case class ScoreAdded(home: String, away: String) extends Event
  case class GameScore(homeScore:String = "0", awayScore:String = "0")
}


class GameActor(home:String, away:String) extends PersistentActor {
  override def persistenceId = s"${self.path.name}"
  implicit val ec = context.dispatcher

  var score = GameScore()
  def receiveRecover = {
    case event:Event => {
      //      nrEventsRecovered = nrEventsRecovered + 1
      updateState(event)
    }
  }

  val receiveCommand: Receive = {
    case AddScore(home, away) => {
      println(s"Added score $home$away")
      persist(ScoreAdded(home, away))(updateState)
    }
    case GetScore => {
      println(" GameActor get score")
      sender() ! score
    }
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
