package models

import play.api.libs.json.OFormat

case class User(
               forename: String,
               surname: String,
               email: String,
               password: String,
               basket: List[Game],
               orders: List[Game]
               )
