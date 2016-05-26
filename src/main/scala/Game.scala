import Game._
import akka.actor.{Actor, Props}
import akka.persistence.PersistentActor
import scala.concurrent.{Future, Promise}

/**
  * Created by cosimoranieri on 23/05/2016.
  */

object Game {
  def props(home:String, away:String) = Props(new Game(home, away))
  def name = "gamesManager"

//  sealed trait Command
  case class AddScore(homeScore:String, awayScore:String)
  case object GetScore
  case object GetGame

  case object GameExists

  case class GameScore(homeScore:String = "0", awayScore:String = "0")
}


class Game(home:String, away:String) extends Actor {
  implicit val ec = context.dispatcher

  var score = GameScore()

  def receive = {
    case AddScore(home, away) => {
      println(s"Added score $home$away")
      import akka.pattern.pipe
      def updateScore: Future[GameScore] = {
        val p = Promise[GameScore]()
        Future {
          score = GameScore(home, away)
          p.success(score)
        }
        p.future
      }
      pipe(updateScore) to sender()
    }
    case GetScore => {
      println(" GameActor get score")
      sender() ! score
    }
    case GetGame => sender() ! Some(GamesManager.Game(home, away))
  }
}
