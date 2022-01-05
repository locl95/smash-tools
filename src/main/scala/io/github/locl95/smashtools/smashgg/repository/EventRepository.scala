package io.github.locl95.smashtools.smashgg.repository

import cats.effect.Sync
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.github.locl95.smashtools.smashgg.domain.Event

trait EventRepository [F[_]]{
  def insert(event: Event): F[Int]
}

final class EventPostgresRepository[F[_]: Sync](transactor: Transactor[F]) extends EventRepository[F]{
  override def toString: String = "EventPostgresRepository"
  override def insert(event: Event): F[Int] =
    sql"insert into events (name) values ($event)".update.run.transact(transactor)
}
