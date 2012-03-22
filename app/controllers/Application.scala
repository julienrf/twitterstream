package controllers

import play.api._
import play.api.mvc._
import play.api.libs.iteratee._
import play.api.libs.ws._
import play.api.libs.oauth._
import views._

object Application extends Controller with OAuthAuthentication {

  def index = Authenticated { request =>
    Ok(html.index())
  }

  val authenticateCall = routes.Application.authenticate
  val authenticatedCall = routes.Application.index

  val consumerKey = ConsumerKey("UstozDw940RBShjqOhNQ", "DgJXSk2HAuDUaqwDqX8L6wb7Q15EGrzeT7qEJzACLA")
  val oAuth = OAuth(ServiceInfo("https://api.twitter.com/oauth/request_token",
    "https://api.twitter.com/oauth/access_token",
    "https://api.twitter.com/oauth/authorize",
    consumerKey))

  def tweets(keywords: String) = Action { implicit request =>
    val token = Application.sessionTokens(request).get

    val EventSource = Enumeratee.map[Array[Byte]] { bytes =>
      "data:" + new String(bytes) + "\n\n"
    }

    Ok.stream { socket: Socket.Out[String] =>
      WS.url("https://stream.twitter.com/1/statuses/filter.json?track=" + keywords)
        .sign(OAuthCalculator(consumerKey, token))
        .get(tweets => EventSource &> socket)
    } withHeaders CONTENT_TYPE -> "text/event-stream"
  }

}