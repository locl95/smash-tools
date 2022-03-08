package io.github.locl95.smashtools.smashgg.repository

import cats.effect.Sync
import cats.implicits._
import doobie.Update
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.github.locl95.smashtools.smashgg.domain.Entrant

trait EntrantRepository[F[_]] {
  def insert(entrants: List[Entrant]): F[Int]
  def getEntrants: F[List[Entrant]]
  def getEntrants(eventID:Int): F[List[Entrant]]
}

final class EntrantPostgresRepository[F[_]: Sync](transactor: Transactor[F]) extends EntrantRepository[F]{
  override def toString: String = "EntrantPostgresRepository"
  override def insert(entrants: List[Entrant]): F[Int] = {
    val sql = "insert into entrants (id, id_event, name) values (?, ?, ?)"
    Update[Entrant](sql)
      .updateMany(entrants)
      .transact(transactor)
  }

  override def getEntrants: F[List[Entrant]] =
    sql"""select id,id_event,name from entrants"""
      .query[Entrant]
      .to[List]
      .transact(transactor)

  override def getEntrants(eventID: Int): F[List[Entrant]] =
    sql"""select id,id_event,name from entrants where id_event = ${eventID}"""
      .query[Entrant]
      .to[List]
      .transact(transactor)

}
