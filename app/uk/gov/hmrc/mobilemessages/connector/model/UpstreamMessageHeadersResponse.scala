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

package uk.gov.hmrc.mobilemessages.connector.model

import java.time.{LocalDate, LocalDateTime}

import uk.gov.hmrc.mobilemessages.domain.{MessageHeader, MessageId}

final case class UpstreamMessageHeadersResponse(items: Seq[MessageHeader])

object UpstreamMessageHeadersResponse {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  implicit val messageHeaderReads: Reads[MessageHeader] = (
    (__ \ "id").read[String] and
    (__ \ "subject").read[String] and
    (__ \ "validFrom").read[LocalDate] and
    (__ \ "readTime").readNullable[LocalDateTime] and
    (__ \ "sentInError").read[Boolean]
  )((id, subject, validFrom, readTime, sentInError) =>
    MessageHeader(MessageId(id), subject, validFrom, readTime, sentInError)
  )

  implicit val reads: Reads[UpstreamMessageHeadersResponse] = Json.reads[UpstreamMessageHeadersResponse]

}
