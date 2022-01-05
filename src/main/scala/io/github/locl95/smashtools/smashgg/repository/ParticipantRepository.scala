package io.github.locl95.smashtools.smashgg.repository


import cats.effect.Sync
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.update.Update
import io.github.locl95.smashtools.smashgg.domain.Participant

trait ParticipantRepository [F[_]]{
  def insert(participants: List[Participant]): F[Int]
  def get: F[List[Participant]]
}

final class ParticipantPostgresRepository[F[_]: Sync](transactor: Transactor[F]) extends ParticipantRepository[F] {
  override def toString: String = "ParticipantPostgresRepository"

  override def insert(participants: List[Participant]): F[Int] = {
    val sql = "insert into participants (id) values (?)"
    Update[Participant](sql)
      .updateMany(participants)
      .transact(transactor)
  }

  override def get: F[List[Participant]] = ???
}
