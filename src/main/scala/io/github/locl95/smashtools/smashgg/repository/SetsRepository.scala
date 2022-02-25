package io.github.locl95.smashtools.smashgg.repository

import cats.effect.Sync
import cats.implicits._
import doobie.Update
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.github.locl95.smashtools.smashgg.domain.{Score, Sets}

trait SetsRepository [F[_]]{
  def insert(sets: List[Sets]): F[Int]
  def getSets: F[List[Sets]]
}

case class sqlSet(id:Int, idEvent:Int, idWinner:Int, idLoser:Int, scoreWinner:Int, scoreLoser:Int)

final class SetsPostgresRepository[F[_]: Sync](transactor: Transactor[F]) extends SetsRepository[F]{
  override def toString: String = "SetsPostgresRepository"
  override def insert(sets: List[Sets]): F[Int] = {

    val sqlSets = sets.map{
      case Sets(id, idEvent, scores) =>
        if (scores(1).score > scores(2).score)
          sqlSet(id, idEvent, scores(1).idPlayer, scores(2).idPlayer, scores(1).score, scores(2).score)
        else
          sqlSet(id, idEvent, scores(2).idPlayer, scores(1).idPlayer, scores(2).score, scores(1).score)
    }

    val sql = "insert into sets (id, id_event, id_winner, id_loser, score_winner, score_loser) values (?,?,?,?,?,?)"
    Update[sqlSet](sql)
      .updateMany(sqlSets)
      .transact(transactor)
  }

  override def getSets: F[List[Sets]] =
    sql"select id, id_event, id_winner, id_loser, score_winner, score_loser from sets"
      .query[sqlSet]
      .to[List]
      .transact(transactor)
      .map(x => x.map{
        case sqlSet(id, idEvent, idWinner, idLoser, scoreWinner, scoreLoser) =>
          Sets(id, idEvent, List(Score(idWinner, scoreWinner), Score(idLoser, scoreLoser)))
      })
}