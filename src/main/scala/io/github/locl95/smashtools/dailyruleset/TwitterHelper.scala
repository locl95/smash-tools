package io.github.locl95.smashtools.dailyruleset

import org.apache.commons.codec.binary.{Base64, Hex}

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object TwitterHelper {

  private val percentEncodeSubstitutions: List[(String, String)] =
    List(
      "%" -> "%25",
      "!" -> "%21",
      "#" -> "%23",
      "$" -> "%24",
      "&" -> "%26",
      "'" -> "%27",
      "(" -> "%28",
      ")" -> "%29",
      "*" -> "%2A",
      "+" -> "%2B",
      "," -> "%2C",
      "/" -> "%2F",
      ":" -> "%3A",
      ";" -> "%3B",
      "=" -> "%3D",
      "?" -> "%3F",
      "@" -> "%40",
      "[" -> "%5B",
      "]" -> "%5D",
      " " -> "%20"
    )

  private def percentEncodeString(stringToEncode: String) = {
    percentEncodeSubstitutions.foldLeft(stringToEncode) { case (current, (from, to)) => current.replace(from, to) }
  }

  def signed(
      params: List[(String, String)],
      method: String,
      baseUrl: String,
      consumerSecret: String,
      oAuthSecret: String
    ): String = {

    val encodedParams: String = params.map {
      case (key, value) =>
        percentEncodeSubstitutions.foldLeft((key, value)) {
          case ((key, value), (from, to)) => key.replace(from, to) -> value.replace(from, to)
        }
    }.sortBy(_._1)
      .map {
        case (key, value) => s"$key=$value"
      }
      .mkString("&")

    val encodedBaseString: String = List(method, percentEncodeString(baseUrl)).mkString("&")

    val baseString = percentEncodeString(encodedParams).prepended('&').prependedAll(encodedBaseString)
    val signingKey = percentEncodeString(consumerSecret).appended('&').appendedAll(percentEncodeString(oAuthSecret))

    val mac = Mac.getInstance("HmacSHA1")
    mac.init(new SecretKeySpec(signingKey.getBytes(), "HmacSHA1"))
    val hashedString = mac
      .doFinal(baseString.getBytes)
      .map("%02X" format _)
      .mkString

    Base64.encodeBase64(Hex.decodeHex(hashedString)).map(_.toChar).mkString

  }

  def oAuthHeader(params: List[(String, String)]): String =
    params.map { case (key, value) => s"""${percentEncodeString(key)}="${percentEncodeString(value)}"""" }
      .mkString(", ")
      .prependedAll("OAuth ")
}
