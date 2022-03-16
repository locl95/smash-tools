package io.github.locl95.smashtools.smashgg

import cats.Monad
import cats.implicits.catsSyntaxApplicativeId

import scala.collection.mutable

trait Users[F[_]] {
  def insert(user: User): F[Int]
  def get(token: String): F[Either[String,User]]
}

final class UsersInMemoryRepository[F[_]: Monad] extends Users[F]{
  private val usersArray: mutable.ArrayDeque[User] = mutable.ArrayDeque.empty

  override def insert(user: User): F[Int] = {
    usersArray.append(user)
    user.id.pure[F]
  }

  override def get(token: String): F[Either[String,User]] = {
    usersArray.find(_.token == token).toRight("Token invalid").pure[F]
  }
}