package io.github.locl95.smashtools.smashgg.repository

import cats.effect.Sync
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.github.locl95.smashtools.smashgg.domain.Entrant

trait EntrantRepository[F[_]] {
  def insert(entrant: Entrant): F[Int]
}

final class EntrantPostgresRepository[F[_]: Sync](transactor: Transactor[F]) extends EntrantRepository[F]{
  override def toString: String = "EntrantPostgresRepository"
  override def insert(entrant: Entrant): F[Int] =
    sql"insert into entrants (id, id_event, name) values (${entrant.id}, ${entrant.id}, ${entrant.name})".update.run.transact(transactor)
}
