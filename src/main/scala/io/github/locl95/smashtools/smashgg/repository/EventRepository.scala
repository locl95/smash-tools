package io.github.locl95.smashtools.smashgg.repository

import cats.effect.Sync
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.github.locl95.smashtools.smashgg.domain.Event

trait EventRepository [F[_]]{
  def insert(event: Event): F[Int]
  def getEvents: F[List[Event]]
}

final class EventPostgresRepository[F[_]: Sync](transactor: Transactor[F]) extends EventRepository[F]{
  override def toString: String = "EventPostgresRepository"
  override def insert(event: Event): F[Int] =
    sql"insert into events (id,name) values (${event.id}, ${event.name})".update.run.transact(transactor)

  override def getEvents: F[List[Event]] = {
    sql"select id, name from events"
      .query[Event]
      .to[List]
      .transact(transactor)
  }
}