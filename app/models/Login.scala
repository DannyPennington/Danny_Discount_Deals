package models

import play.api.data.Form
import play.api.data.Forms._

case class Login(email:String, password:String)

object Login {
  val LoginForm:Form[Login] = Form(
    mapping(
      "email" -> nonEmptyText,
      "password" -> nonEmptyText
    )(Login.apply)(Login.unapply)
  )
}
