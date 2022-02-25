package io.github.locl95.smashtools.smashgg.protocol

import cats.effect.Async
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import io.github.locl95.smashtools.smashgg.domain.{Entrant, Event, Phase, Score, Sets, Tournament}
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

object SmashggRoutesImpl {
  implicit val decoderForTournament: Decoder[Tournament] = deriveDecoder[Tournament]
  implicit def entityDecoderForTournament[F[_]: Async]: EntityDecoder[F, Tournament] = jsonOf
  implicit val encoderForTournament: Encoder[Tournament] = deriveEncoder[Tournament]

  implicit val decoderForEvent: Decoder[Event] = deriveDecoder[Event]
  implicit def entityDecoderForEvent[F[_]: Async]: EntityDecoder[F, Event] = jsonOf
  implicit val encoderForEvent: Encoder[Event] = deriveEncoder[Event]

  implicit val decoderForEntrant: Decoder[Entrant] = deriveDecoder[Entrant]
  implicit val decoderForEntrants: Decoder[List[Entrant]] = Decoder.decodeList[Entrant](decoderForEntrant)
  implicit def entityDecoderForEntrants[F[_]: Async]: EntityDecoder[F, List[Entrant]] = jsonOf
  implicit val encoderForEntrant: Encoder[Entrant] = deriveEncoder[Entrant]
  implicit val encoderForEntrants: Encoder[List[Entrant]] = Encoder.encodeList[Entrant](encoderForEntrant)

  implicit val decoderForPhase: Decoder[Phase] = deriveDecoder[Phase]
  implicit val decoderForPhases: Decoder[List[Phase]] = Decoder.decodeList[Phase]
  implicit def entityDecoderForPhases[F[_]: Async]: EntityDecoder[F, List[Phase]] = jsonOf
  implicit val encoderForPhase: Encoder[Phase] = deriveEncoder[Phase]
  implicit val encoderForPhases: Encoder[List[Phase]] = Encoder.encodeList[Phase](encoderForPhase)

  implicit val encoderForScore: Encoder[Score] = deriveEncoder[Score]
  implicit val encoderForScores: Encoder[List[Score]] = Encoder.encodeList[Score](encoderForScore)
  implicit val encoderForSet: Encoder[Sets] = deriveEncoder[Sets]
  implicit val encoderForSets: Encoder[List[Sets]] = Encoder.encodeList[Sets](encoderForSet)

}
