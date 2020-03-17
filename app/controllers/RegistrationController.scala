package controllers

import javax.inject.Inject
import play.api.mvc._

class RegistrationController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def registerUser: Action[AnyContent] = Action { implicit request:Request[AnyContent] =>

    Ok
  }

}
