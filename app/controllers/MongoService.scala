package controllers

import javax.inject.Inject
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import reactivemongo.play.json.collection.JSONCollection
import scala.concurrent.{ExecutionContext, Future}
import reactivemongo.play.json._
import collection._
import models.{Game, User}
import models.JsonFormats._
import play.api.libs.json.{JsValue, Json}
import reactivemongo.api.Cursor
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.commands.WriteResult
import scala.concurrent.ExecutionContext.Implicits.global

class MongoService @Inject()(
                              val reactiveMongoApi: ReactiveMongoApi
                            ) extends ReactiveMongoComponents {

  def userCollection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection]("users"))
  def gameCollection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection]("games"))

  def findAllUsers(): Future[List[User]] = {
    userCollection.map {
      _.find(Json.obj())
        .sort(Json.obj("surname" -> -1))
        .cursor[User]()
    }.flatMap(
      _.collect[List](
        -1,
        Cursor.FailOnError[List[User]]()
      )
    )
  }

  def findAllGames(): Future[List[Game]] = {
    gameCollection.map {
      _.find(Json.obj())
        .sort(Json.obj("name" -> -1))
        .cursor[Game]()
    }.flatMap(
      _.collect[List](
        -1,
        Cursor.FailOnError[List[Game]]()
      )
    )
  }

}