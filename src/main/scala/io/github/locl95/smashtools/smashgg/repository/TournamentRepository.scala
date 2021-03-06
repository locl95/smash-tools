package io.github.locl95.smashtools.smashgg.repository

import cats.effect.Sync
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.github.locl95.smashtools.smashgg.domain.Tournament

trait TournamentRepository[F[_]] {
  def insert(tournament: Tournament): F[Int]
  def get: F[List[Tournament]]
  def get(tournamentId: Int): F[Option[Tournament]]
}

final class TournamentPostgresRepository[F[_]: Sync](transactor: Transactor[F]) extends TournamentRepository[F] {
  override def toString: String = "TournamentPostgresRepository"

  override def insert(tournament: Tournament): F[Int] =
    sql"insert into tournaments (id,name) values (${tournament.id}, ${tournament.name})"
      .update
      .withUniqueGeneratedKeys[Int]("id")
      .transact(transactor)

  override def get: F[List[Tournament]] =
    sql"select id,name from tournaments"
      .query[Tournament]
      .to[List]
      .transact(transactor)

  override def get(tournamentId: Int): F[Option[Tournament]] =
    sql"""select id,name from tournaments where id = ${tournamentId}"""
      .query[Tournament]
      .option
      .transact(transactor)

}
