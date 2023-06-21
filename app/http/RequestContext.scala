package lila.app
package http

import play.api.mvc.*
import play.api.i18n.Lang
import play.api.http.Writeable

import lila.user.Me
import lila.api.{ Nonce, PageData, UserContext }
import lila.i18n.I18nLangPicker
import lila.common.{ HTTPRequest }
import lila.security.{ Granter, FingerPrintedUser, AppealUser }
import lila.oauth.OAuthScope
import lila.pref.RequestPref

trait RequestContext(using Executor):

  val env: Env

  def minimalContext(using req: RequestHeader): WebContext =
    WebContext(req, I18nLangPicker(req), UserContext.anon, RequestPref.fromRequest(req))

  def minimalBodyContext[A](using req: Request[A]): WebBodyContext[A] =
    WebBodyContext(req, I18nLangPicker(req), UserContext.anon, RequestPref.fromRequest(req))

  def webContext(using req: RequestHeader): Fu[WebContext] = for
    userCtx <- makeUserContext(req)
    lang = getAndSaveLang(req, userCtx.me)
    pref <- env.pref.api.get(userCtx.me, req)
  yield WebContext(req, lang, userCtx, pref)

  def webBodyContext[A](using req: Request[A]): Fu[WebBodyContext[A]] = for
    userCtx <- makeUserContext(req)
    lang = getAndSaveLang(req, userCtx.me)
    pref <- env.pref.api.get(userCtx.me, req)
  yield WebBodyContext(req, lang, userCtx, pref)

  def oauthContext(scoped: OAuthScope.Scoped)(using req: RequestHeader): Fu[WebContext] =
    val lang    = getAndSaveLang(req, scoped.me.some)
    val userCtx = UserContext(scoped.me.some, false, none, scoped.scopes.some)
    env.pref.api
      .get(scoped.me, req)
      .map:
        WebContext(req, lang, userCtx, _)

  def oauthBodyContext[A](scoped: OAuthScope.Scoped)(using req: Request[A]): Fu[WebBodyContext[A]] =
    val lang    = getAndSaveLang(req, scoped.me.some)
    val userCtx = UserContext(scoped.me.some, false, none, scoped.scopes.some)
    env.pref.api
      .get(scoped.me, req)
      .map:
        WebBodyContext(req, lang, userCtx, _)

  private def getAndSaveLang(req: RequestHeader, me: Option[Me]): Lang =
    val lang = I18nLangPicker(req, me.flatMap(_.lang))
    me.filter(_.lang.fold(true)(_ != lang.code)) foreach { env.user.repo.setLang(_, lang) }
    lang

  private def pageDataBuilder(using ctx: WebContext): Fu[PageData] =
    val isPage = HTTPRequest isSynchronousHttp ctx.req
    val nonce  = isPage option Nonce.random
    ctx.me.foldUse(fuccess(PageData.anon(nonce))): me ?=>
      {
        if isPage then
          env.user.lightUserApi preloadUser me
          val enabledId = me.enabled.yes option me.userId
          enabledId.so(env.team.api.nbRequests) zip
            enabledId.so(env.challenge.api.countInFor.get) zip
            enabledId.so(env.notifyM.api.unreadCount) zip
            env.mod.inquiryApi.forMod
        else
          fuccess:
            (((0, 0), lila.notify.Notification.UnreadCount(0)), none)
      } map { case (((teamNbRequests, nbChallenges), nbNotifications), inquiry) =>
        PageData(
          teamNbRequests,
          nbChallenges,
          nbNotifications,
          hasClas = env.clas.hasClas,
          inquiry = inquiry,
          nonce = nonce
        )
      }

  def pageContext(using ctx: WebContext): Fu[PageContext] =
    pageDataBuilder.dmap(PageContext(ctx, _))

  private def makeUserContext(req: RequestHeader): Fu[UserContext] =
    env.security.api restoreUser req dmap {
      case Some(Left(AppealUser(me))) if HTTPRequest.isClosedLoginPath(req) =>
        FingerPrintedUser(me, true).some
      case Some(Right(d)) if !env.net.isProd =>
        d.copy(me = d.me.map:
          _.addRole(lila.security.Permission.Beta.dbKey)
            .addRole(lila.security.Permission.Prismic.dbKey)
        ).some
      case Some(Right(d)) => d.some
      case _              => none
    } flatMap {
      case None => fuccess(UserContext.anon)
      case Some(d) =>
        env.mod.impersonate.impersonating(d.me) map {
          _.fold(UserContext(d.me.some, !d.hasFingerPrint, none, none)): impersonated =>
            UserContext(Me(impersonated).some, needsFp = false, d.me.some, none)
        }
    }
