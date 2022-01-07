package io.github.locl95.smashtools.smashgg.repository

import cats.effect.Sync
import cats.implicits._
import doobie.Update
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.github.locl95.smashtools.smashgg.domain.{Phase}

trait PhaseRepository [F[_]]{
  def insert(phases: List[Phase]): F[Int]
}

final class PhasePostgresRepository[F[_]: Sync](transactor: Transactor[F]) extends PhaseRepository[F]{
  override def toString: String = "PlayerStandingPostgresRepository"
  override def insert(playerStandings: List[Phase]): F[Int] = {
    val sql = "insert into phases (id, name) values (?,?)"
    Update[Phase](sql)
      .updateMany(playerStandings)
      .transact(transactor)
  }
}





