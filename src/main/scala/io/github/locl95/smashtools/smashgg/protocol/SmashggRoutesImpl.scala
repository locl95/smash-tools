package io.github.locl95.smashtools.smashgg.protocol

import cats.effect.Async
import io.circe.generic.auto._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import io.github.locl95.smashtools.smashgg.domain._
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

object SmashggRoutesImpl {
  implicit val decoderForTournament: Decoder[Tournament] = deriveDecoder[Tournament]
  implicit def entityDecoderForTournament[F[_]: Async]: EntityDecoder[F, Tournament] = jsonOf
  implicit val encoderForTournament: Encoder[Tournament] = deriveEncoder[Tournament]

  implicit val decoderForEvent: Decoder[Event] = deriveDecoder[Event]
  implicit def entityDecoderForEvent[F[_]: Async]: EntityDecoder[F, Event] = jsonOf
  implicit val encoderForEvent: Encoder[Event] = deriveEncoder[Event]

  implicit val decoderForEntrants: Decoder[List[Entrant]] = Decoder.decodeList[Entrant]
  implicit def entityDecoderForEntrants[F[_]: Async]: EntityDecoder[F, List[Entrant]] = jsonOf
  implicit val encoderForEntrants: Encoder[List[Entrant]] = Encoder.encodeList[Entrant]

  implicit val decoderForPhases: Decoder[List[Phase]] = Decoder.decodeList[Phase]
  implicit def entityDecoderForPhases[F[_]: Async]: EntityDecoder[F, List[Phase]] = jsonOf
  implicit val encoderForPhases: Encoder[List[Phase]] = Encoder.encodeList[Phase]

  implicit val decoderForSets: Decoder[List[Sets]] = Decoder.decodeList[Sets]
  implicit def entityDecoderForSets[F[_]: Async]: EntityDecoder[F, List[Sets]] = jsonOf
  implicit val encoderForSets: Encoder[List[Sets]] = Encoder.encodeList[Sets]

}
