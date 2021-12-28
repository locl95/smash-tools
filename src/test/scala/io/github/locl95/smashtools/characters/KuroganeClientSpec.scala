package io.github.locl95.smashtools.characters

import cats.effect.IO
import cats.implicits._
import io.github.locl95.smashtools.UriHelper
import io.github.locl95.smashtools.characters.domain.{KuroganeCharacter, KuroganeCharacterMove}
import io.github.locl95.smashtools.characters.protocol.Kurogane._
import munit.CatsEffectSuite
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext.global

class KuroganeClientSpec extends CatsEffectSuite {
  test("Should be able to retreat characters from Kurogane API") {
      val program: fs2.Stream[IO, List[KuroganeCharacter]] = for {
        client <- BlazeClientBuilder[IO](global).stream
        kuroganeClient = KuroganeClient.impl(client)
        uri <- fs2.Stream.eval(UriHelper.fromString[IO]("https://api.kuroganehammer.com/api/characters?game=ultimate"))
        characters <- fs2.Stream.eval(kuroganeClient.get[List[KuroganeCharacter]](uri))
      } yield characters

    assertIO(program.compile.foldMonoid.map(_.take(2)), TestHelper.characters)
  }

  test("Should be able to retreat movements from Kurogane API") {
    val program: fs2.Stream[IO, List[KuroganeCharacterMove]] = for {
      client <- BlazeClientBuilder[IO](global).stream
      kuroganeClient = KuroganeClient.impl(client)
      uri <- fs2.Stream.eval(UriHelper.fromString[IO]("https://api.kuroganehammer.com/api/characters/name/joker/moves?expand=true&game=ultimate"))
      movements <- fs2.Stream.eval(kuroganeClient.get[List[KuroganeCharacterMove]](uri))
    } yield movements

    assertIO(program.compile.foldMonoid.map(_.take(2)), TestHelper.movements)
  }
}
