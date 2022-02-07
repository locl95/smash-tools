package io.github.locl95.smashtools.smashgg.repository

import cats.effect.Sync
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.github.locl95.smashtools.smashgg.domain.Tournament

trait TournamentRepository[F[_]] {
  def insert(tournament: Tournament): F[Int]
  def get: F[List[Tournament]]
}

final class TournamentPostgresRepository[F[_]: Sync](transactor: Transactor[F]) extends TournamentRepository[F] {
  override def toString: String = "TournamentPostgresRepository"

  override def insert(tournament: Tournament): F[Int] =
    sql"insert into tournaments (id,name) values ($tournament.id, $tournament.name)".update.run.transact(transactor)

  override def get: F[List[Tournament]] =
    sql"select name from tournaments"
      .query[Tournament]
      .to[List]
      .transact(transactor)

}
