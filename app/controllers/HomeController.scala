package controllers

import javax.inject._
import play.api.mvc._

@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def index: Action[AnyContent] = Action {implicit request:Request[AnyContent] =>
    if (request.session.get("user").isDefined) {
      Ok(views.html.index("Logged in as: " + request.session.data("user")))
    }
    else {
      Ok(views.html.index("Welcome to the site!"))
    }
  }

  def home: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    if (request.session.get("user").isDefined) {
      Ok(views.html.home("You are logged in, good work"))
    }
    else {
      Redirect(routes.LoginController.showLoginForm()).flashing("home" -> "no")
    }
  }

}
