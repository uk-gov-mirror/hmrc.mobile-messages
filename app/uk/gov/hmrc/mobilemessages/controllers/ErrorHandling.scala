/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.mobilemessages.controllers

import play.api.mvc.Result
import uk.gov.hmrc.api.controllers.ErrorResponse
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.controller.BackendBaseController

import scala.concurrent.{ExecutionContext, Future}

class GrantAccessException(message: String) extends HttpException(message, 403)

class NinoNotFoundOnAccount extends GrantAccessException("Unauthorised! NINO not found on account!")

class AccountWithLowCL extends GrantAccessException("Unauthorised! Account with low CL!")

case object ErrorUnauthorizedMicroService extends ErrorResponse(401, "UNAUTHORIZED", "Unauthorized")

case object ErrorForbidden extends ErrorResponse(403, "FORBIDDEN", "Forbidden")

trait ErrorHandling {
  self: BackendBaseController =>

  import play.api.libs.json.Json
  import play.api.{Logger, mvc}
  import uk.gov.hmrc.api.controllers.{ErrorInternalServerError, ErrorNotFound, ErrorUnauthorizedLowCL}

  def errorWrapper(
    func:        => Future[mvc.Result]
  )(implicit hc: HeaderCarrier,
    ec:          ExecutionContext
  ): Future[Result] =
    func.recover {
      case _: NotFoundException => Status(ErrorNotFound.httpStatusCode)(Json.toJson(ErrorNotFound))

      case _: UnauthorizedException => Unauthorized(Json.toJson(ErrorUnauthorizedMicroService))

      case _: ForbiddenException => Unauthorized(Json.toJson(ErrorUnauthorizedLowCL))

      case e: Throwable =>
        Logger.error(s"Internal server error: ${e.getMessage}", e)
        Status(ErrorInternalServerError.httpStatusCode)(Json.toJson(ErrorInternalServerError))
    }
}
