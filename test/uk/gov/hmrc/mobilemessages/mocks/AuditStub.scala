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

package uk.gov.hmrc.mobilemessages.mocks

import org.scalamock.matchers.MatcherBase
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success
import uk.gov.hmrc.play.audit.model.DataEvent

import scala.concurrent.{ExecutionContext, Future}

trait AuditStub extends MockFactory {

  def dataEventWith(
    auditSource: String,
    auditType:   String,
    tags:        Map[String, String],
    detail:      Map[String, String]
  ): MatcherBase =
    argThat { (dataEvent: DataEvent) =>
      dataEvent.auditSource.equals(auditSource) &&
      dataEvent.auditType.equals(auditType) &&
      dataEvent.tags.equals(tags) &&
      dataEvent.detail.equals(detail)
    }

  def stubAudit(transactionName: String)(implicit auditConnector: AuditConnector): Unit =
    (auditConnector
      .sendEvent(_: DataEvent)(_: HeaderCarrier, _: ExecutionContext))
      .expects(
        dataEventWith("mobile-messages", "ServiceResponseSent", Map("transactionName" -> transactionName), Map.empty),
        *,
        *
      )
      .returning(Future successful Success)

  def stubAuditReadAndUnreadMessages()(implicit auditConnector: AuditConnector): Unit =
    stubAudit("readAndUnreadMessages")

  def stubAuditReadMessageContent()(implicit auditConnector: AuditConnector): Unit =
    stubAudit("readMessageContent")

}
