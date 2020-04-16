package models

import play.api.data.Form
import play.api.data.Forms._

case class Search(name: String)

object Search {
  val SearchForm: Form[Search] = Form(
    mapping(
      "name" -> nonEmptyText
    )(Search.apply)(Search.unapply)
  )
}