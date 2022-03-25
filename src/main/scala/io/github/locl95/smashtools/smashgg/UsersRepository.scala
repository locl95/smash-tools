package io.github.locl95.smashtools.smashgg

import cats.Monad
import cats.implicits.catsSyntaxApplicativeId

import scala.collection.mutable

trait UsersRepository[F[_]] {
  def insert(user: User): F[Int]
  def get(id: Int): F[Either[String,User]]
}

final class UsersInMemoryRepository[F[_]: Monad] extends UsersRepository[F]{
  private val usersArray: mutable.ArrayDeque[User] = mutable.ArrayDeque.empty

  override def insert(user: User): F[Int] = {
    usersArray.append(user)
    user.id.pure[F]
  }

  override def get(id: Int): F[Either[String,User]] = {
    usersArray.find(_.id == id).toRight("Id invalid").pure[F]
  }
}