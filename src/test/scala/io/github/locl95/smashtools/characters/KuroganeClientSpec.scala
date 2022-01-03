package io.github.locl95.smashtools.characters

import cats.effect.{Blocker, ContextShift, IO, Timer}
import io.github.locl95.smashtools.characters.protocol.Kurogane._
import munit.CatsEffectSuite
import org.http4s.client.JavaNetClientBuilder

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global

class KuroganeClientSpec extends CatsEffectSuite {
  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  implicit val timer: Timer[IO] = IO.timer(global)
  private val blockingEC = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(5))
  private val blocker: Blocker = Blocker.liftExecutionContext(blockingEC)

  private val client = JavaNetClientBuilder[IO](blocker).create
  private val kuroganeClient = KuroganeClient.impl[IO](client)
  private val kuroganeClientMock = new KuroganeClientMock[IO]
  private val clients = List(kuroganeClient, kuroganeClientMock)

  private val getCharactersTest = (client: KuroganeClient[IO]) =>
    assertIO(client.getCharacters.map(_.take(2)), TestHelper.characters)

  private val getMovementsTest = (client: KuroganeClient[IO]) =>
    assertIO(client.getMovements("joker").map(_.take(2)), TestHelper.movements)

  clients.foreach { c =>
    test(s"Should be able to retreat characters from Kurogane API with $c") { getCharactersTest(c) }
    test(s"Should be able to retreat movements from Kurogane API with $c") { getMovementsTest(c) }
  }
}
