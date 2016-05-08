/**
  * Created by cosimoranieri on 07/05/2016.
  */
import GamesManager.{CreateGame, Game, Games, GetGames}
import akka.actor._
import akka.util.Timeout

import scala.concurrent.{Future, Promise}

object GamesManager {
  def props(implicit timeout: Timeout) = Props(new GamesManager)
  def name = "gamesManager"
  case class Game(home: String, away: String)
  case class Games(games: Vector[Game])
  case object GetGames
  case class CreateGame(home: String, away: String)
}

class GamesManager(implicit timeout: Timeout) extends Actor {

  implicit val ec = context.dispatcher

  def receive = {
    case GetGames => {
      import akka.pattern.pipe

      def games: Future[Games] = {
        val p = Promise[Games]()
        Future {
          println("Fetching games")
          val games: Games = Games(Vector.empty :+ Game("Juventus", "Sampdoria"))
          p.success(games)
          println("Returned games")
        }
        println("outside future")
        p.future
      }
      pipe(games) to sender()
    }

    case CreateGame(home, away) => {
      import akka.pattern.pipe
      def game: Future[Game] = {
        val p = Promise[Game]()
        Future {
          val game = Game(home, away)
          p.success(game)
        }
        p.future
      }
      pipe(game) to sender()
    }
  }
}
