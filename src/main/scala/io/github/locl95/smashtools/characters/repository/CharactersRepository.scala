package io.github.locl95.smashtools.characters.repository

import cats.effect.Sync
import cats.implicits._
import doobie.Update
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.github.locl95.smashtools.characters.domain.KuroganeCharacter

trait CharactersRepository[F[_]] {
  def insert(character: List[KuroganeCharacter]): F[Int]
  def get: F[List[KuroganeCharacter]]
  def cache(table: String): F[Int]
  def isCached(table: String): F[Boolean]
}

final class CharacterPostgresRepository[F[_]: Sync](transactor: Transactor[F]) extends CharactersRepository[F] {

  override def insert(characters: List[KuroganeCharacter]): F[Int] = {
    val sql = "insert into characters (name) values (?)"
    Update[KuroganeCharacter](sql)
      .updateMany(characters)
      .transact(transactor)
  }

  override def isCached(table: String): F[Boolean] =
    sql"""select valid from cache where "table"=$table"""
      .query[Boolean]
      .option
      .transact(transactor)
      .map(_.contains(true))

  override def cache(table: String): F[Int] = {
    sql"""insert into cache ("table", valid) values ($table, true) on conflict ("table") do update set valid=true"""
      .update
      .run
      .transact(transactor)
  }

  override def get: F[List[KuroganeCharacter]] =
    sql"select name from characters"
      .query[KuroganeCharacter]
      .to[List]
      .transact(transactor)
}
