package controllers

import play.api.libs.oauth._
import play.api.mvc._
import play.api.Logger

trait OAuthAuthentication extends Controller {

  def authenticateCall: Call
  def authenticatedCall: Call
  def oAuth: OAuth

  def authenticate = Action { implicit request =>

    val verifierAndTokens = for {
      values <- request.queryString.get("oauth_verifier")
      verifier <- values.headOption
      tokens <- sessionTokens
    } yield (verifier, tokens)

    val result = verifierAndTokens match {
      case None => {
        // First access: try to retrieve request tokens
        for (tokens <- oAuth.retrieveRequestToken(authenticateCall.absoluteURL()).right) yield {
          Redirect(oAuth.redirectUrl(tokens.token))
              .withSession("token"->tokens.token, "secret"->tokens.secret)
        }
      }
      case Some((verifier, tokens)) => {
        // Second time: the user has been authenticated, retrieve the access tokens
        for (tokens <- oAuth.retrieveAccessToken(tokens, verifier).right) yield {
          Redirect(authenticatedCall)
              .withSession("token"->tokens.token, "secret"->tokens.secret)
        }
      }
    }
    result.right.getOrElse(InternalServerError("Authentication failed!"))
  }

  def Authenticated(action: RequestHeader => Result) = Action { implicit request =>
    sessionTokens match {
      case Some(tokens) => action(request)
      case None => Redirect(authenticateCall)
    }
  }

  def sessionTokens(implicit request: RequestHeader) = {
    for {
      token <- session.get("token")
      secret <- session.get("secret")
    } yield RequestToken(token, secret)
  }

}