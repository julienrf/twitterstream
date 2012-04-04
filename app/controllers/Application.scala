package controllers

import play.api._
import play.api.mvc._
import play.api.libs.iteratee._
import play.api.libs.concurrent.Promise
import play.api.libs.ws._
import play.api.libs.oauth._
import views._
import play.api.libs.json.{Json, JsValue}

object Application extends Controller with OAuthAuthentication with Twitter {

  def index = Authenticated { _ => request =>
    Ok(html.index())
  }

  def tweets(keywords: String) = Authenticated { token => implicit request =>

    val json = Enumeratee.map[Array[Byte]] { message =>
      Json.parse(new String(message))
    }

    val htmlTweet = Enumeratee.map[JsValue] { json =>
      val user = (json \ "user" \ "screen_name").as[String]
      val content = (json \ "text").as[String]
      views.html.tweet(user, content).toString
    }

    val eventSource = Enumeratee.map[String] { str =>
      (for (line <- str.split("\n")) yield {
        "data:" + line + "\n"
      }).mkString + "\n\n"
    }

    val rawTweet = new Enumerator[Array[Byte]] {
      def apply[A](iteratee: Iteratee[Array[Byte], A]) = {
        tweetsStream(token)(keywords) { _ => iteratee }
      }
    }

    Ok.feed(rawTweet &> json ><> htmlTweet ><> eventSource).withHeaders(CONTENT_TYPE -> EVENT_STREAM)
  }

  val authenticateCall = routes.Application.authenticate
  val authenticatedCall = routes.Application.index

  val consumerKey = ConsumerKey("UstozDw940RBShjqOhNQ", "DgJXSk2HAuDUaqwDqX8L6wb7Q15EGrzeT7qEJzACLA")
  val oAuth = OAuth(ServiceInfo("https://api.twitter.com/oauth/request_token",
    "https://api.twitter.com/oauth/access_token",
    "https://api.twitter.com/oauth/authorize",
    consumerKey))

}