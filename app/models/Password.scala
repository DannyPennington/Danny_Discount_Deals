package models

import play.api.data.Form
import play.api.data.Forms._

case class Password(c_password:String, n_password:String)

object Password {
  val PasswordForm:Form[Password] = Form(
    mapping(
      "c_password" -> nonEmptyText,
      "n_password" -> nonEmptyText
    )(Password.apply)(Password.unapply)
  )
}

