package com.ironmangym.toolbar

import com.ironmangym.common._
import com.ironmangym.domain.{ Credentials, LogIn, Users }
import com.pangwarta.sjrmui.{ Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, FormControl, FormHelperText, Hidden, ReactHandler1, TextField }
import diode.react.{ ModelProxy, ReactConnectProxy }
import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Scala.BackendScope
import japgolly.scalajs.react.vdom.html_<^._

import scala.scalajs.js

object LoginDialog {

  case class Props(proxy: ModelProxy[Users], open: Boolean, onClose: ReactHandler1[ReactEvent])

  case class State(
      usersWrapper: ReactConnectProxy[Users],
      username:     Option[String]           = None,
      password:     Option[String]           = None,
      formValid:    Boolean                  = false,
      loginValid:   Option[Boolean]          = None
  ) extends FormState {
    def validate(): State =
      copy(formValid = username.exists(_.nonEmpty) && password.exists(_.nonEmpty))

    def reset(): State =
      copy(username   = None, password = None, formValid = false, loginValid = None)
  }

  private class Backend($: BackendScope[Props, State]) {

    def render(p: Props, s: State): VdomElement =
      Dialog(
        open    = p.open,
        onClose = resetAndClose(_)
      )()(
          DialogTitle()()("Sign In"),
          DialogContent()()(
            DialogContentText()()("Please sign in to access your profile."),
            FormControl()()(
              TextField(
                label      = "Username",
                autoFocus  = true,
                required   = true,
                onChange   = usernameChanged($)(_),
                error      = s.wasMadeEmpty[State](_.username),
                helperText = if (s.wasMadeEmpty[State](_.username)) "Please enter your username." else js.undefined
              )()(),
              TextField(
                label      = "Password",
                typ        = "password",
                required   = true,
                onChange   = passwordChanged($)(_),
                error      = s.wasMadeEmpty[State](_.password),
                helperText = if (s.wasMadeEmpty[State](_.password)) "Please enter your password" else js.undefined
              )()(),
              Hidden(xsUp = s.loginValid.forall(_ == true))()(
                FormHelperText(error = true)()("Invalid username and password")
              )
            ),
            DialogActions()()(
              Button(
                onClick  = login(_),
                variant  = Button.Variant.raised,
                disabled = !s.formValid
              )()("Go")
            )
          )
        )

    def usernameChanged($: BackendScope[Props, State])(e: ReactEvent): Callback = {
      val username = getValue(e)
      fieldChanged[Props, State]($, _.copy(username = Some(username)).validate())
    }

    def passwordChanged($: BackendScope[Props, State])(e: ReactEvent): Callback = {
      val password = getValue(e)
      fieldChanged[Props, State]($, _.copy(password = Some(password)).validate())
    }

    def resetAndClose(e: ReactEvent): Callback =
      $.modState(_.reset()) >> $.props >>= { p => p.onClose.getOrElse((_: ReactEvent) => Callback.empty)(e) }

    def login(e: ReactEvent): Callback = {
      $.props >>= (p => $.state >>= (s => {
        val formCredentials = Credentials(s.username.get, s.password.get)
        val users = p.proxy()
        val trainer = users.trainers.find(_.credentials == formCredentials)
        val trainee = users.trainees.find(_.credentials == formCredentials)
        if (trainer.nonEmpty) p.proxy.dispatchCB(LogIn(trainer.get))
        else if (trainee.nonEmpty) p.proxy.dispatchCB(LogIn(trainee.get))
        else $.modState(_.copy(loginValid = Some(false)))
      }))
    }
  }

  private val component = ScalaComponent.builder[Props]("Login Form")
    .initialStateFromProps(p => State(p.proxy.connect(identity)))
    .renderBackend[Backend]
    .build

  def apply(
      proxy:   ModelProxy[Users],
      open:    Boolean,
      onClose: ReactHandler1[ReactEvent]
  ): VdomElement = component(Props(proxy, open, onClose))
}
