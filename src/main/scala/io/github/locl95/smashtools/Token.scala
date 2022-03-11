package io.github.locl95.smashtools

sealed trait Token

case class TournamentToken(id: Int) extends Token
