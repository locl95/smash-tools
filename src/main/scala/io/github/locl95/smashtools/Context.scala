package io.github.locl95.smashtools

import cats.effect.{Async, Blocker, ConcurrentEffect, ContextShift, Resource}
import cats.implicits._
import doobie.Transactor
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor.Aux
import io.github.locl95.smashtools.characters.repository.{CharacterPostgresRepository, MovementPostgresRepository}
import io.github.locl95.smashtools.characters.service.{CharactersService, MovementsService}
import io.github.locl95.smashtools.characters.{CharactersRoutes, KuroganeClient}
import io.github.locl95.smashtools.smashgg.repository.{EntrantPostgresRepository, EventPostgresRepository, EventRepository, PhasePostgresRepository, SetsPostgresRepository, TournamentPostgresRepository}
import io.github.locl95.smashtools.smashgg.service.{EntrantService, EventService, PhaseService, SetsService, TournamentService}
import io.github.locl95.smashtools.smashgg.{SmashggClient, SmashggRoutes}
import org.flywaydb.core.Flyway
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext.global

final case class DatabaseProgram[F[_]](transactor: Aux[F, Unit], flyway: Flyway)

final case class Context[F[_]: ConcurrentEffect: ContextShift](transactor: HikariTransactor[F]) {

  // TODO: deixar de fer servir això i fer servir el transactor creat per JdbcTransactor
//  val databaseProgram: F[DatabaseProgram[F]] = for {
//    postgresUrl <- Async[F].delay(Option(System.getenv("JDBC_URL")).getOrElse("jdbc:postgresql:smashtools"))
//    postgresUser <- Async[F].delay(Option(System.getenv("DB_USER")).getOrElse("test"))
//    postgresPassword <- Async[F].delay(Option(System.getenv("DB_PASS")).getOrElse("test"))
//  } yield DatabaseProgram(
//    Transactor.fromDriverManager[F](
//      "org.postgresql.Driver",
//      postgresUrl,
//      postgresUser,
//      postgresPassword,
//      Blocker.liftExecutionContext(ExecutionContexts.synchronous)
//    ),
//    Flyway
//      .configure
//      .dataSource(
//        postgresUrl,
//        postgresUser,
//        postgresPassword
//      )
//      .load()
//  )

  val charactersRoutesProgram: Resource[F, CharactersRoutes[F]] = for {
    client <- BlazeClientBuilder[F](global).resource
    kuroganeClient = KuroganeClient.impl[F](client)
    //database <- Resource.eval(databaseProgram)
  } yield new CharactersRoutes[F](
    new CharactersService[F](new CharacterPostgresRepository[F](transactor), kuroganeClient),
    new MovementsService[F](new MovementPostgresRepository[F](transactor), kuroganeClient)
  )

  val smashggRoutesProgram: Resource[F, SmashggRoutes[F]] =
    for {
      client <- BlazeClientBuilder[F](global).resource
      smashggClient = SmashggClient.impl[F](client)
      //database <- Resource.eval(databaseProgram)
    } yield new SmashggRoutes[F](
      new TournamentService[F](new TournamentPostgresRepository[F](transactor), smashggClient),
      new EventService[F](new EventPostgresRepository[F](transactor), smashggClient),
      new EntrantService[F](new EntrantPostgresRepository[F](transactor), smashggClient),
      new PhaseService[F](new PhasePostgresRepository[F](transactor), smashggClient),
      new SetsService[F](new SetsPostgresRepository[F](transactor), smashggClient)
    )


}

object Context {
  def production[F[_]: ConcurrentEffect: ContextShift](config: JdbcDatabaseConfiguration): Resource[F, Context[F]] = for {
    blocker <- Blocker.apply
    transactor <- {
      implicit val b: Blocker = blocker
      JdbcTransactor.transactorResource(config, _ => Async[F].pure(()))
    }
//    tournamentRepository = new TournamentPostgresRepository[F](transactor)
//    entrantRepository = new EntrantPostgresRepository[F](transactor)
//    eventRepository = new EventPostgresRepository[F](transactor)
//    setsRepository = new SetsPostgresRepository[F](transactor)
//    phaseRepository = new PhasePostgresRepository[F](transactor)
  } yield new Context[F](transactor)

  def test[F[_]: ConcurrentEffect: ContextShift](config: JdbcDatabaseConfiguration): Resource[F, Context[F]] = for {
    blocker <- Blocker.apply
    transactor <- {
      implicit val b: Blocker = blocker
      JdbcTestTransactor.transactorResource(config, )
      //JdbcTransactor.transactorResource(config, _ => Async[F].pure(()))
    }
  } yield new Context[F](transactor)

}
