package controllers

import play.api._
import play.api.mvc._
import play.api.libs.oauth._

object Application extends Controller with OAuthAuthentication {

  def index = Authenticated { tokens => request =>
    Ok(views.html.index("Your new application is ready."))
  }

  val authenticateCall = routes.Application.authenticate
  val authenticatedCall = routes.Application.index
  val oAuth = OAuth(ServiceInfo("https://api.twitter.com/oauth/request_token",
    "https://api.twitter.com/oauth/access_token",
    "https://api.twitter.com/oauth/authorize",
    ConsumerKey("yzfT2vhgsBjgWEquQHSCsg", "mtVRo5tJ0xJ4ZK8l7pAowaYjMSZRGdYY9NphtWVp5dA")))
    //ConsumerKey("UstozDw940RBShjqOhNQ", "DgJXSk2HAuDUaqwDqX8L6wb7Q15EGrzeT7qEJzACLA")))
}