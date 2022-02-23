package io.github.locl95.smashtools

import cats.effect.{Async, Blocker, ConcurrentEffect, ContextShift}
import cats.implicits._
import doobie.Transactor
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor.Aux
import io.github.locl95.smashtools.characters.{CharactersRoutes, KuroganeClient}
import io.github.locl95.smashtools.characters.repository.{CharacterPostgresRepository, MovementPostgresRepository}
import io.github.locl95.smashtools.characters.service.{CharactersService, MovementsService}
import io.github.locl95.smashtools.smashgg.repository.{EventPostgresRepository, TournamentPostgresRepository}
import io.github.locl95.smashtools.smashgg.service.{EventService, TournamentService}
import io.github.locl95.smashtools.smashgg.{SmashggClient, SmashggRoutes}
import org.flywaydb.core.Flyway
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext.global

final case class DatabaseProgram[F[_]](transactor: Aux[F, Unit], flyway: Flyway)

final case class Context[F[_]: ContextShift: ConcurrentEffect]() {

  val databaseProgram: F[DatabaseProgram[F]] = for {
    postgresUrl <- Async[F].delay(Option(System.getenv("JDBC_URL")).getOrElse("jdbc:postgresql:smashtools"))
    postgresUser <- Async[F].delay(Option(System.getenv("DB_USER")).getOrElse("test"))
    postgresPassword <- Async[F].delay(Option(System.getenv("DB_PASS")).getOrElse("test"))
  } yield DatabaseProgram(
    Transactor.fromDriverManager[F](
      "org.postgresql.Driver",
      postgresUrl,
      postgresUser,
      postgresPassword,
      Blocker.liftExecutionContext(ExecutionContexts.synchronous)
    ),
    Flyway
      .configure
      .dataSource(
        postgresUrl,
        postgresUser,
        postgresPassword
      )
      .load()
  )

  val charactersRoutesProgram: fs2.Stream[F, CharactersRoutes[F]] = for {
    client <- BlazeClientBuilder[F](global).stream
    kuroganeClient = KuroganeClient.impl[F](client)
    database <- fs2.Stream.eval(databaseProgram)
  } yield new CharactersRoutes[F](
    new CharactersService[F](new CharacterPostgresRepository[F](database.transactor), kuroganeClient),
    new MovementsService[F](new MovementPostgresRepository[F](database.transactor), kuroganeClient)
  )

  val smashggRoutesProgram: fs2.Stream[F, SmashggRoutes[F]] =
    for {
      client <- BlazeClientBuilder[F](global).stream
      smashggClient = SmashggClient.impl[F](client)
      database <- fs2.Stream.eval(databaseProgram)
    } yield new SmashggRoutes[F](
      new TournamentService[F](new TournamentPostgresRepository[F](database.transactor), smashggClient),
      new EventService[F](new EventPostgresRepository[F](database.transactor), smashggClient)
    )

}
