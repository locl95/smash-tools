package io.github.locl95.smashtools.characters.domain

//TODO: Implement Kurogane Domain: Characters & Movements. Can be basic info for now.

case class KuroganeCharacter(Name: String)
case class KuroganeCharacterMove(name: String, advantage: Option[Int], `type`: String, firstFrame: Option[Int])
