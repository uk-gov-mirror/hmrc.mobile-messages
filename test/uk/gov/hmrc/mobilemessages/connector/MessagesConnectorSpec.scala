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

package uk.gov.hmrc.mobilemessages.connector

import play.api.libs.json.Json.toJson
import play.api.libs.json.{Json, OFormat}
import play.api.test.Helpers.SERVICE_UNAVAILABLE
import uk.gov.hmrc.auth.core.ConfidenceLevel.L200
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http._
import uk.gov.hmrc.mobilemessages.connector.model.{ResourceActionLocation, UpstreamMessageHeadersResponse, UpstreamMessageResponse}
import uk.gov.hmrc.mobilemessages.controllers.auth.Authority
import uk.gov.hmrc.mobilemessages.domain._
import uk.gov.hmrc.mobilemessages.utils.Setup
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MessagesConnectorSpec extends UnitSpec with Setup {

  def testBaseUrl(serviceName: String): String = "http://localhost:8089"

  def mockBaseUrl: String => String = testBaseUrl

  implicit val formats: OFormat[RenderMessageLocation] = Json.format[RenderMessageLocation]
  implicit val authUser: Option[Authority] = Some(Authority(Nino("CS700100A"), L200, "someId"))

  val responseRenderer = RenderMessageLocation("sa-message-renderer", "http://somelocation")
  val renderPath = "/some/render/path"
  val messageBodyToRender: UpstreamMessageResponse =
    message.bodyWith(id = "id1", renderUrl = ResourceActionLocation("test-renderer-service", renderPath))
  val messageToBeMarkedAsReadBody: UpstreamMessageResponse = message.bodyToBeMarkedAsReadWith(id = "id48")
  val messageToBeMarkedAsRead: UnreadMessage = UnreadMessage(MessageId(messageToBeMarkedAsReadBody.id),
    messageToBeMarkedAsReadBody.renderUrl.url, "markAsReadUrl")

  lazy val ReadSuccessEmptyResult: Future[AnyRef with HttpResponse] = Future.successful(HttpResponse(200, None, Map.empty, None))
  lazy val PostSuccessResult: Future[AnyRef with HttpResponse] = Future.successful(HttpResponse(200, Some(toJson(html.body))))
  lazy val PostSuccessRendererResult: Future[AnyRef with HttpResponse] = Future.successful(HttpResponse(200, Some(toJson(responseRenderer))))

  val connector: MessageConnector = new MessageConnector("messagesBaseUrl", mockHttp, mockBaseUrl)

  private val upstream5xxResponse = Upstream5xxResponse("", SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE)
  private val badRequestException = new BadRequestException("")

  "messages()" should {

    "return a list of items when a 200 response is received with a payload" in {
      val messagesHeaders = Seq(
        message.headerWith(id = "someId1"),
        message.headerWith(id = "someId2"),
        message.headerWith(id = "someId3"))

      messagesGetSuccess(UpstreamMessageHeadersResponse(messagesHeaders))

      await(connector.messages()) shouldBe messagesHeaders
    }

    "throw BadRequestException when a 400 response is returned" in {
      messagesGetFailure(badRequestException)

      intercept[BadRequestException] {
        await(connector.messages())
      }
    }

    "throw Upstream5xxResponse when a 500 response is returned" in {
      messagesGetFailure(upstream5xxResponse)

      intercept[Upstream5xxResponse] {
        await(connector.messages())
      }
    }

    "return empty response when a 200 response is received with an empty payload" in {
      messagesGetSuccess(UpstreamMessageHeadersResponse(Seq.empty))

      await(connector.messages()) shouldBe Seq.empty
    }
  }

  "getMessageBy(messageId)" should {

    "return a message when a 200 response is received with a payload" in {
      messageByGetSuccess(message.bodyWith(id = messageId.value))

      await(connector.getMessageBy(messageId)) shouldBe message.convertedFrom(
        message.bodyWith(id = messageId.value)
      )
    }

    "throw BadRequestException when a 400 response is returned" in {
      messageByGetFailure(badRequestException)

      intercept[BadRequestException] {
        await(connector.getMessageBy(messageId))
      }
    }

    "throw Upstream5xxResponse when a 500 response is returned" in {
      messageByGetFailure(upstream5xxResponse)

      intercept[Upstream5xxResponse] {
        await(connector.getMessageBy(messageId))
      }
    }
  }

  "render()" should {

    "return empty response when a 200 response is received with an empty payload" in {
      renderGetSuccess(ReadSuccessEmptyResult)

      await(connector.render(message.convertedFrom(messageBodyToRender), hc)).body shouldBe ""
    }

    "return a rendered message when a 200 response is received with a payload" in {
      renderGetSuccess(PostSuccessResult)

      await(connector.render(message.convertedFrom(messageBodyToRender), hc)).body should include(s"${html.body}")
    }

    "throw BadRequestException when a 400 response is returned" in {
      renderGetFailure(badRequestException)

      intercept[BadRequestException] {
        await(connector.render(message.convertedFrom(messageBodyToRender), hc))
      }
    }

    "throw Upstream5xxResponse when a 500 response is returned" in {
      renderGetFailure(upstream5xxResponse)

      intercept[Upstream5xxResponse] {
        await(connector.render(message.convertedFrom(messageBodyToRender), hc))
      }
    }
  }

  "markAsRead()" should {

    "return a message when a 200 response is received with a payload" in {
      markAsReadPostSuccess(PostSuccessRendererResult)

      await(connector.markAsRead(messageToBeMarkedAsRead)).status shouldBe 200
    }

    "throw BadRequestException when a 400 response is returned" in {
      markAsReadPostFailure(badRequestException)

      intercept[BadRequestException] {
        await(connector.markAsRead(messageToBeMarkedAsRead))
      }
    }

    "throw Upstream5xxResponse when a 500 response is returned" in {
      markAsReadPostFailure(upstream5xxResponse)

      intercept[Upstream5xxResponse] {
        await(connector.markAsRead(messageToBeMarkedAsRead))
      }
    }
  }
}