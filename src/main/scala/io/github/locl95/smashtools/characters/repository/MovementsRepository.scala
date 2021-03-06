package io.github.locl95.smashtools.characters.repository

import cats.effect.Sync
import cats.implicits._
import doobie.Update
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.github.locl95.smashtools.characters.domain.KuroganeCharacterMove

trait MovementsRepository[F[_]] {
  def insert(movements: List[KuroganeCharacterMove]): F[Int]
  def getMove(moveId: String): F[Option[KuroganeCharacterMove]]
  def getMoves(character: String): F[List[KuroganeCharacterMove]]
  def cache(character: String): F[Int]
  def isCached(character: String): F[Boolean]
}

final class MovementPostgresRepository[F[_]: Sync](transactor: Transactor[F]) extends MovementsRepository[F] {

  private val cacheCharacterMovements = (character: String) => s"${character}_movements"

  override def toString: String = "MovementPostgresRepository"

  override def insert(movements: List[KuroganeCharacterMove]): F[Int] = {
    val sql = "insert into movements (id,character,name,advantage,type,first_frame) values (?,?,?,?,?,?)"
    Update[KuroganeCharacterMove](sql)
      .updateMany(movements)
      .transact(transactor)
  }

  override def isCached(character: String): F[Boolean] = {
    sql"""select valid from cache where lower("table")=${cacheCharacterMovements(character)}"""
      .query[Boolean]
      .option
      .transact(transactor)
      .map(_.contains(true))
  }

  override def cache(character: String): F[Int] = {
    sql"""insert into cache ("table", valid) values (${cacheCharacterMovements(character)}, true) on conflict ("table") do update set valid=true"""
      .update
      .run
      .transact(transactor)
  }

  override def getMoves(character: String): F[List[KuroganeCharacterMove]] =
    sql"""select id,character,name,advantage,type,first_frame from movements where lower(character)=$character"""
      .query[KuroganeCharacterMove]
      .to[List]
      .transact(transactor)

  override def getMove(moveId: String): F[Option[KuroganeCharacterMove]] =
    sql"""select id,character,name,advantage,type,first_frame from movements where id=$moveId"""
      .query[KuroganeCharacterMove]
      .option
      .transact(transactor)
}
