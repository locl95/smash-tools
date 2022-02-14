package io.github.locl95.smashtools.smashgg.service

import cats.effect.Sync
import cats.implicits._
import io.github.locl95.smashtools.smashgg.SmashggClient
import io.github.locl95.smashtools.smashgg.domain.Tournament
import io.github.locl95.smashtools.smashgg.repository.TournamentRepository

final case class TournamentService[F[_]: Sync](tournamentRepository: TournamentRepository[F], client: SmashggClient[F]) {
  def get: F[List[Tournament]] = {
    for {
      tournaments <- tournamentRepository.get
    } yield tournaments
  }
}
