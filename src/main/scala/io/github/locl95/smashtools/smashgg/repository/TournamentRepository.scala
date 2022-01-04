package io.github.locl95.smashtools.smashgg.repository

import cats.effect.Sync
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.github.locl95.smashtools.smashgg.domain.Tournament

trait TournamentRepository[F[_]] {
  def insert(tournament: Tournament): F[Int]
  //def getTournament(tournamentName: String): F[Option[Tournament]]
}

final class TournamentPostgresRepository[F[_]: Sync](transactor: Transactor[F]) extends TournamentRepository[F] {
  override def toString: String = "TournamentPostgresRepository"

  override def insert(tournament: Tournament): F[Int] =
    sql"insert into tournaments (name) values ($tournament.name)".update.run.transact(transactor)

//
  //override def getTournament(tournamentName: String): F[Option[Tournament]] = ???
}
