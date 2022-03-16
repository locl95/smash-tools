package io.github.locl95.smashtools

import cats.Monad
import cats.data.{EitherT, Kleisli, OptionT}
import io.github.locl95.smashtools.smashgg.{User, Users}
import org.http4s.Status.Forbidden
import org.http4s.headers.Authorization
import org.http4s.server.AuthMiddleware
import org.http4s.{AuthedRequest, AuthedRoutes, Request, Response}

final class SmashggAuth[F[_]: Monad](users: Users[F]) {

  val authUser: Kleisli[F, Request[F], Either[String, User]] = Kleisli({
    request =>
      (for {
        header <- EitherT.fromOption[F](request.headers.get(Authorization), "Auth header is required")
        user <- EitherT(users.get(header.value.split(' ').last))
      } yield user).value
  })

  val onFailure: AuthedRoutes[String, F] = Kleisli[OptionT[F, *], AuthedRequest[F, String], Response[F]](_ => OptionT.pure[F](Response[F](Forbidden)))

  val middleware: AuthMiddleware[F, User] = AuthMiddleware(authUser, onFailure)
}

object SmashggAuth {
  def make[F[_]: Monad](users: Users[F]): SmashggAuth[F] = new SmashggAuth[F](users)
}
