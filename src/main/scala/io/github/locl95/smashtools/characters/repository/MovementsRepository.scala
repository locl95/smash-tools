package io.github.locl95.smashtools.characters.repository

import cats.effect.Sync
import cats.implicits._
import doobie.Update
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.github.locl95.smashtools.characters.domain.KuroganeCharacterMove

trait MovementsRepository[F[_]] {
  def insert(movements: List[KuroganeCharacterMove]): F[Int]
  def get: F[List[KuroganeCharacterMove]]
  def cache: F[Int]
  def isCached: F[Boolean]
}

final class MovementPostgresRepository[F[_]: Sync](transactor: Transactor[F]) extends MovementsRepository[F] {

  override def insert(movements: List[KuroganeCharacterMove]): F[Int] = {
    val sql = "insert into movements (name) values (?)"
    Update[KuroganeCharacterMove](sql)
      .updateMany(movements)
      .transact(transactor)
  }

  override def isCached: F[Boolean] =
    sql"""select valid from cache where "table"="movements""""
      .query[Boolean]
      .option
      .transact(transactor)
      .map(_.contains(true))

  override def cache: F[Int] = {
    sql"""insert into cache ("table", valid) values ("movements", true) on conflict ("table") do update set valid=true"""
      .update
      .run
      .transact(transactor)
  }

  override def get: F[List[KuroganeCharacterMove]] =
    sql"select name from movements"
      .query[KuroganeCharacterMove]
      .to[List]
      .transact(transactor)
}
