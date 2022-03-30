package io.github.locl95.smashtools

import cats.effect.{Async, Blocker, ContextShift, Resource}
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import doobie.ExecutionContexts
import doobie.hikari.HikariTransactor
import doobie.util.transactor.Transactor
import org.flywaydb.core.Flyway

import scala.concurrent.duration.DurationInt


final case class JdbcDatabaseConfiguration(
                                            driverClassName: String,
                                            jdbcUrl: String,
                                            user: String,
                                            password: String,
                                            minConnections: Int,
                                            maxConnections: Int
                                          )

object JdbcTransactor {
  def transactorResource[F[_]: Async: ContextShift](config: JdbcDatabaseConfiguration, beforeAll: Flyway => F[Unit])(implicit blocker: Blocker): Resource[F, HikariTransactor[F]] = for {
    ec <- ExecutionContexts.fixedThreadPool[F](config.maxConnections)
    hikariDs <- Resource.fromAutoCloseable(Async[F].delay(new HikariDataSource(hikariConfiguration(config))))
    transactor <- Resource.eval(Async[F].delay(Transactor.fromDataSource[F](hikariDs,ec,blocker)))
    flyway <- Resource.eval(Async[F].delay(Flyway.configure().dataSource(hikariDs).load()))
    _ <- Resource.eval(beforeAll(flyway))
    _ <- Resource.eval(blocker.delay{
      val flyway = Flyway.configure().dataSource(hikariDs).load()
      flyway.migrate()
      flyway
    })
  } yield transactor

  private def hikariConfiguration(databaseConfiguration: JdbcDatabaseConfiguration): HikariConfig = {
    val hikariConfig = new HikariConfig()

    hikariConfig.setDriverClassName(databaseConfiguration.driverClassName)
    hikariConfig.setJdbcUrl(databaseConfiguration.jdbcUrl)
    hikariConfig.setUsername(databaseConfiguration.user)
    hikariConfig.setPassword(databaseConfiguration.password)
    hikariConfig.setMinimumIdle(databaseConfiguration.minConnections)
    hikariConfig.setMaximumPoolSize(databaseConfiguration.maxConnections)
    hikariConfig.setLeakDetectionThreshold(2000L)
    hikariConfig.setConnectionTimeout(30.seconds.toMillis)
    // TODO: Check if this parameters can improve the performance
    // hikariConfig.setMaxLifetime(30.seconds.toMillis)
    // hikariConfig.setIdleTimeout(0) // Zero means never removed
    hikariConfig
  }
}

object JdbcTestTransactor {
  def transactorResource[F[_]: Async: ContextShift](config: JdbcDatabaseConfiguration)(implicit blocker: Blocker): Resource[F, HikariTransactor[F]] =
    JdbcTransactor.transactorResource(config,
      flyway => blocker.delay{
        flyway.clean()
        ()
      })
}
