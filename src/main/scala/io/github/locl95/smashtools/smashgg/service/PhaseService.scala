package io.github.locl95.smashtools.smashgg.service

import io.github.locl95.smashtools.smashgg.SmashggClient
import io.github.locl95.smashtools.smashgg.domain.Phase
import io.github.locl95.smashtools.smashgg.repository.PhaseRepository

final case class PhaseService[F[_]](phaseRepository: PhaseRepository[F],
                                    client: SmashggClient[F]) {

  def getPhases: F[List[Phase]] =
    phaseRepository.getPhases
}
