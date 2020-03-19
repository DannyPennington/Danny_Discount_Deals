package controllers

import javax.inject._
import play.api.mvc._
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json._
import collection._
import scala.concurrent.{Await, ExecutionContext, Future}
import models.{Game, Login, Registration, User}
import models.JsonFormats._
import play.api.libs.json._
import reactivemongo.api.Cursor

import scala.concurrent.duration.Duration


class LoginController @Inject()(
                                 components: ControllerComponents,
                                 val mongoService: MongoService
                               ) extends AbstractController(components) with play.api.i18n.I18nSupport {

  implicit def ec: ExecutionContext = components.executionContext
  val collection: Future[JSONCollection] = mongoService.userCollection

  def searchHelper(email: String): Future[List[User]] = {
    val cursor: Future[Cursor[User]] = collection.map {
      _.find(Json.obj("email" -> email)).
        sort(Json.obj("email" -> -1)).
        cursor[User]()
    }
    val futureUsersList: Future[List[User]] =
      cursor.flatMap(
        _.collect[List](
          -1,
          Cursor.FailOnError[List[User]]()
        )
      )
    futureUsersList
  }

  def showLoginForm(): Action[AnyContent] = Action {implicit request:Request[AnyContent] =>
    if (request.flash.get("invalid").isDefined) {
      Ok(views.html.login(Login.LoginForm,"Invalid credentials"))
    }
    else {
      Ok(views.html.login(Login.LoginForm,""))
    }
  }

  def loginUser(): Action[AnyContent] = Action {implicit request:Request[AnyContent] =>
    Login.LoginForm.bindFromRequest.fold({ formWithErrors =>
      BadRequest(views.html.login(formWithErrors,""))
    }, { login =>
      val user = Await.result(searchHelper(login.email), Duration.Inf)
      if (user.isEmpty) {
        Redirect(routes.JsonReadersWriters.showRegistration()).flashing("exists" -> "no")
      }
      else if (user.head.password == login.password) {
        Redirect(routes.HomeController.home()).withSession(request.session + ("user" -> login.email))
      }
      else {
        Redirect(routes.LoginController.showLoginForm()).flashing("invalid" -> "yes")
      }
    })
  }

  def logoutUser(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index("Successfully logged out")).withSession("loggedout" -> "true")
  }

}
