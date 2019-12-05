package controllers

import javax.inject.Inject
import play.api.Configuration
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

class ApiDocsController @Inject()(cc: ControllerComponents, configuration: Configuration) extends AbstractController(cc) {

  def redirectToDocs: Action[AnyContent] = Action {
    val basePath = configuration.underlying.getString("swagger.api.uri")
    Redirect(
      url = "/assets/lib/swagger-ui/index.html",
      queryString = Map("url" -> Seq(s"$basePath/swagger.json"))
    )
  }

}