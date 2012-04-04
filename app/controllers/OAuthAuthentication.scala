package controllers

import play.api.libs.oauth._
import play.api.mvc._
import play.api.Logger

/**
 * Basic OAuth authentication implementation
 */
trait OAuthAuthentication { this: Controller =>

  /** Authentication Call */
  def authenticateCall: Call
  /** Call to be redirected to, when authenticated */
  def authenticatedCall: Call
  /** OAuth settings to use */
  def oAuth: OAuth

  /**
   * Perform authentication according to the OAuth protocol
   */
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

  /**
   * Helper to write secured actions.
   * The wrapped action `action` will be called only if the current user is authenticated. Otherwise a redirect to the
   * authentication action will be returned.
   * 
   * Example of use:
   * {{{
   *   def securedAction = Authenticated { token => request =>
   *     Ok(...)
   *   }
   * }}}
   */
  def Authenticated(action: RequestToken => RequestHeader => Result) = Action { implicit request =>
    sessionTokens match {
      case Some(tokens) => action(tokens)(request)
      case None => Redirect(authenticateCall)
    }
  }

  /**
   * Retrieve the request token from the session
   */
  def sessionTokens(implicit request: RequestHeader): Option[RequestToken] = {
    for {
      token <- session.get("token")
      secret <- session.get("secret")
    } yield RequestToken(token, secret)
  }

}