package io.github.locl95.smashtools.dailyruleset

import munit.CatsEffectSuite

class TwitterHelperSpec extends CatsEffectSuite {
  test("I can sign twitter request") {
    val params: List[(String, String)] = List(
      "status" -> "Hello Ladies + Gentlemen, a signed OAuth request!",
      "include_entities" -> "true",
      "oauth_consumer_key" -> "xvz1evFS4wEEPTGEFPHBog",
      "oauth_nonce" -> "kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg",
      "oauth_signature_method" -> "HMAC-SHA1",
      "oauth_timestamp" -> "1318622958",
      "oauth_token" -> "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb",
      "oauth_version" -> "1.0"
    )
    val expectedResult = "hCtSmYh+iHYCEqBWrE7C7hYmtUk="
    assertEquals(
      TwitterHelper.signed(
        params,
        "POST",
        "https://api.twitter.com/1.1/statuses/update.json",
        "kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw",
        "LswwdoUaIvS8ltyTt5jkRh4J50vUPVVHtR2YPi5kE"
      ),
      expectedResult
    )
  }
  test("I can generate oauth authorization header") {
    val params: List[(String, String)] = List(
      "oauth_consumer_key" -> "xvz1evFS4wEEPTGEFPHBog",
      "oauth_nonce" -> "kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg",
      "oauth_signature" -> "tnnArxj06cWHq44gCs1OSKk/jLY=",
      "oauth_signature_method" -> "HMAC-SHA1",
      "oauth_timestamp" -> "1318622958",
      "oauth_token" -> "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb",
      "oauth_version" -> "1.0"
    )
    val expectedResult =
      """OAuth oauth_consumer_key="xvz1evFS4wEEPTGEFPHBog", oauth_nonce="kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg", oauth_signature="tnnArxj06cWHq44gCs1OSKk%2FjLY%3D", oauth_signature_method="HMAC-SHA1", oauth_timestamp="1318622958", oauth_token="370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb", oauth_version="1.0""""
    assertEquals(TwitterHelper.oAuthHeader(params), expectedResult)

  }
}
