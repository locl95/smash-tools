package io.github.locl95.smashtools.smashgg.service

import io.github.locl95.smashtools.smashgg.SmashggClient
import io.github.locl95.smashtools.smashgg.domain.Sets
import io.github.locl95.smashtools.smashgg.repository.SetsRepository

final case class SetsService[F[_]](setsRepository: SetsRepository[F],
                                   client: SmashggClient[F]){

  def insert(sets: List[Sets]): F[Int] =
    setsRepository.insert(sets)

  def getSets: F[List[Sets]] =
    setsRepository.getSets
}
