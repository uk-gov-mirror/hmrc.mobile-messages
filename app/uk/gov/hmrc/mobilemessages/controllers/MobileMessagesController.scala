/*
 * Copyright 2018 HM Revenue & Customs
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

import com.google.inject.Singleton
import javax.inject.{Inject, Named}
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, BodyParsers}
import play.twirl.api.Html
import uk.gov.hmrc.api.controllers._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.crypto.{CryptoWithKeysFromConfig, Decrypter, Encrypter}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.http.{CoreGet, HeaderCarrier}
import uk.gov.hmrc.mobilemessages.controllers.auth.{AccessControl, Authority}
import uk.gov.hmrc.mobilemessages.controllers.model.{MessageHeaderResponseBody, RenderMessageRequest}
import uk.gov.hmrc.mobilemessages.domain.MessageHeader
import uk.gov.hmrc.mobilemessages.sandbox.DomainGenerator.{nextSaUtr, readMessageHeader, unreadMessageHeader}
import uk.gov.hmrc.mobilemessages.sandbox.MessageContentPartialStubs._
import uk.gov.hmrc.mobilemessages.services.MobileMessagesService
import uk.gov.hmrc.play.HeaderCarrierConverter._
import uk.gov.hmrc.play.bootstrap.controller.BaseController
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext.fromLoggingDetails

import scala.concurrent.Future

@Singleton
class MobileMessagesController @Inject()(val service: MobileMessagesService,
                                         override val authConnector: AuthConnector,
                                         override val http: CoreGet,
                                         @Named("controllers.confidenceLevel") override val confLevel: Int,
                                         @Named("auth") val authUrl: String)
  extends BaseController with HeaderValidator with ErrorHandling with AccessControl {

  val crypto: Encrypter with Decrypter =
    CryptoWithKeysFromConfig(baseConfigKey = "cookie.encryption")

  def getMessages(journeyId: Option[String] = None): Action[AnyContent] =
    validateAcceptWithAuth(acceptHeaderValidationRules).async {
      implicit authenticated =>
        implicit val hc: HeaderCarrier = fromHeadersAndSession(authenticated.request.headers, None)
        errorWrapper(service.readAndUnreadMessages().map(
          (messageHeaders: Seq[MessageHeader]) => {
            Logger.info(s"getMessages: found ${messageHeaders.length} messages" )
            Ok(Json.toJson(MessageHeaderResponseBody.fromAll(messageHeaders)(crypto)))
          }
        ))
    }

  def read(journeyId: Option[String] = None): Action[JsValue] =
    validateAcceptWithAuth(acceptHeaderValidationRules).async(BodyParsers.parse.json) {
      implicit authenticated =>
        implicit val hc: HeaderCarrier = fromHeadersAndSession(authenticated.request.headers, None)

        authenticated.request.body.validate[RenderMessageRequest].fold(
          errors => {
            Logger.warn("Received JSON error with read endpoint: " + errors)
            Future.successful(BadRequest(Json.toJson(ErrorGenericBadRequest(errors))))
          },
          renderMessageRequest => {
            implicit val auth: Option[Authority] = authenticated.authority
            errorWrapper {
              service.readMessageContent(renderMessageRequest.toMessageIdUsing(crypto))
                .map((as: Html) => Ok(as))
            }
          }
        )
    }


}

@Singleton
class SandboxMobileMessagesController @Inject()() extends BaseController with HeaderValidator {

  val crypto: Encrypter with Decrypter =
    CryptoWithKeysFromConfig(baseConfigKey = "cookie.encryption")

  val saUtr: SaUtr = nextSaUtr

  def getMessages(journeyId: Option[String] = None): Action[AnyContent] =
    validateAccept(acceptHeaderValidationRules).async {
      implicit request =>
        Future successful (request.headers.get("SANDBOX-CONTROL") match {
          case Some("ERROR-401") => Unauthorized
          case Some("ERROR-403") => Forbidden
          case Some("ERROR-500") => InternalServerError
          case _ => Ok(Json.toJson(MessageHeaderResponseBody.fromAll(Seq(readMessageHeader(saUtr), unreadMessageHeader(saUtr)))(crypto)))
        })
    }

  def read(journeyId: Option[String] = None): Action[JsValue] =
    validateAccept(acceptHeaderValidationRules).async(BodyParsers.parse.json) {
      implicit request =>
        Future successful (request.headers.get("SANDBOX-CONTROL") match {
          case Some("ANNUAL-TAX-SUMMARY") => Ok(annualTaxSummary)
          case Some("STOPPING-SA") => Ok(stoppingSA)
          case Some("OVERDUE-PAYMENT") => Ok(overduePayment)
          case Some("ERROR-401") => Unauthorized
          case Some("ERROR-403") => Forbidden
          case Some("ERROR-500") => InternalServerError
          case _ => Ok(newTaxStatement)
        })
    }
}