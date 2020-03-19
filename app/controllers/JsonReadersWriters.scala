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


class JsonReadersWriters @Inject()(
                                                    components: ControllerComponents,
                                                    val mongoService: MongoService
                                                  ) extends AbstractController(components) with play.api.i18n.I18nSupport {

  implicit def ec: ExecutionContext = components.executionContext
  val collection: Future[JSONCollection] = mongoService.userCollection

  def create: Action[AnyContent] = Action.async {
    val user = User("Test","User","test@user.com","password",List.empty[Game], List.empty[Game])
    val futureResult = collection.flatMap(_.insert.one(user))
    futureResult.map(_ => Ok(views.html.index("User added")))
  }

  def addUser(forename :String, surname :String, email :String, password :String): Action[AnyContent] = Action.async { implicit request:Request[AnyContent] =>
    val user = User(forename, surname, email, password, List.empty[Game], List.empty[Game])
    val exists = Await.result(userExists(email), Duration.Inf)
    if (exists) {
     Future(Redirect(routes.JsonReadersWriters.showRegistration()).flashing("new" -> "no"))
    }
    else {
      val futureResult = collection.flatMap(_.insert.one(user))
      futureResult.map(_ => Redirect("/").withSession(request.session + ("user" -> user.email)))
    }
  }

  def showRegistration: Action[AnyContent] = Action {implicit request:Request[AnyContent] =>
    if (request.flash.get("new").isDefined) {
      Ok(views.html.registration(Registration.RegistrationForm, "Email address already registered with account!"))
    }
    else if (request.flash.get("exists").isDefined) {
      Ok(views.html.registration(Registration.RegistrationForm, "Please create an account!"))
    }
    else{
      Ok(views.html.registration(Registration.RegistrationForm,""))
    }
  }

  def registerUser: Action[AnyContent] = Action { implicit request:Request[AnyContent] =>
    Registration.RegistrationForm.bindFromRequest.fold({ formWithErrors =>
      BadRequest(views.html.registration(formWithErrors,""))
    }, { register =>
      Redirect(routes.JsonReadersWriters.addUser(register.forename, register.surname, register.email, register.password)).flashing("new" -> "yes")
      //Redirect(routes.HomeController.index()).withSession(request.session + ("user" -> user.email))
    })
  }

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

  def findByEmail(email: String): Action[AnyContent] = Action.async {
    val futureUsersList = searchHelper(email)
    futureUsersList.map { persons =>
      Ok(persons.toString)
    }
  }

  def userExists(email: String): Future[Boolean] = {
    val futureUsersList = searchHelper(email)
    futureUsersList.map { person =>
      if (person.isEmpty) {
        false
      }
      else {
        true
      }
    }
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
        Redirect(routes.HomeController.index()).withSession(request.session + ("user" -> login.email))
      }
      else {
        Redirect(routes.JsonReadersWriters.showLoginForm()).flashing("invalid" -> "yes")
      }
    })
  }


}