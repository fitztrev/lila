package views.html.mod

import controllers.routes
import play.api.i18n.Lang

import lila.app.templating.Environment.*
import lila.app.ui.ScalatagsTemplate.{ *, given }

object impersonate:

  def apply(user: lila.user.User)(using Lang) =
    div(id := "impersonate")(
      div(cls := "meat")(
        "You are impersonating ",
        userLink(user, withOnline = false)
      ),
      div(cls := "actions")(
        postForm(action := routes.Mod.impersonate("-"))(
          submitButton(cls := "button button-empty")("Quit")
        )
      )
    )
