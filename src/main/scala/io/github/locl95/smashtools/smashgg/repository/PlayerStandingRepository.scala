package io.github.locl95.smashtools.smashgg.repository

import cats.effect.Sync
import cats.implicits._
import doobie.Update
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.github.locl95.smashtools.smashgg.domain.PlayerStanding

trait PlayerStandingRepository [F[_]]{
  def insert(playerStandings: List[PlayerStanding]): F[Int]
}

final class PlayerStandingPostgresRepository[F[_]: Sync](transactor: Transactor[F]) extends PlayerStandingRepository[F]{
  override def toString: String = "PlayerStandingPostgresRepository"
  override def insert(playerStandings: List[PlayerStanding]): F[Int] = {
    val sql = "insert into player_standings (placement, id_entrant) values (?,?)"
    Update[PlayerStanding](sql)
      .updateMany(playerStandings)
      .transact(transactor)
  }
}
