package io.github.locl95.smashtools

import cats.data.Kleisli
import cats.effect.{Async, ConcurrentEffect, ContextShift, Timer}
import fs2.Stream
import cats.implicits.toSemigroupKOps
import io.github.locl95.smashtools.smashgg.{User, UsersInMemoryRepository}
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.AuthMiddleware
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import org.http4s.{Request, Response}

import scala.concurrent.ExecutionContext.global

object SmashtoolsServer {

  def stream[F[_]: ConcurrentEffect: ContextShift](implicit T: Timer[F]): Stream[F, Nothing] = {

    val context = new Context[F]
    val users = new UsersInMemoryRepository[F]

    for {
      database <- Stream.eval(context.databaseProgram)
      _ = database.flyway.migrate()

      authMiddleware: AuthMiddleware[F, User] = SmashggAuth.make[F](users).middleware

      //charactersRoutes <- Stream.resource(context.charactersRoutesProgram)
      smashggRoutes <- Stream.resource(context.smashggRoutesProgram)

      httpApp: Kleisli[F, Request[F], Response[F]] =
        (smashggRoutes.smashggRoutes <+> authMiddleware(smashggRoutes.authedSmashhggRoutes)).orNotFound

      //httpApp = (charactersRoutes.characterRoutes).orNotFound

      // With Middlewares in place
      finalHttpApp = Logger.httpApp(true, true)(httpApp)

      port <- Stream.eval(Async[F].delay(Option(System.getenv("PORT")).getOrElse("8080").toInt))

      exitCode <- BlazeServerBuilder[F](global)
        .bindHttp(port, "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain
}
