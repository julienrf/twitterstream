package controllers

import play.api.libs.iteratee.Iteratee
import play.api.libs.ws.{WS, ResponseHeaders}
import play.api.libs.oauth.{OAuthCalculator, ConsumerKey, RequestToken}

trait Twitter {

  def tweetsStream[A](token: RequestToken)(terms: String)(consume: ResponseHeaders => Iteratee[Array[Byte], A]) = {
    WS.url("https://stream.twitter.com/1/statuses/filter.json?track=" + terms)
      .sign(OAuthCalculator(consumerKey, token))
      .get(consume)
  }

  def consumerKey: ConsumerKey

}
