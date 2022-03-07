package io.github.locl95.smashtools.smashgg.repository

import cats.effect.Sync
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.github.locl95.smashtools.smashgg.domain.Event

trait EventRepository [F[_]]{
  def insert(event: Event): F[Int]
  def get: F[List[Event]]
  def getEvents(tournament:Int): F[List[Event]]
}

final class EventPostgresRepository[F[_]: Sync](transactor: Transactor[F]) extends EventRepository[F]{
  override def toString: String = "EventPostgresRepository"

  override def insert(event: Event): F[Int] =
    sql"insert into events (id,name,id_tournament) values (${event.id}, ${event.name}, ${event.idTournament})"
      .update.withUniqueGeneratedKeys[Int]("id")
      .transact(transactor)

  override def getEvents(tournament: Int): F[List[Event]] = {
    sql"select id, name, id_tournament from events where id_tournament = ${tournament}"
      .query[Event]
      .to[List]
      .transact(transactor)
  }

  override def get: F[List[Event]] =
    sql"select * from events"
      .query[Event]
      .to[List]
      .transact(transactor)
}
