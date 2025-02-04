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

package uk.gov.hmrc.mobilemessages.domain

final case class MessageId(value: String)

trait Message {
  def id:        MessageId
  def renderUrl: String
  def `type`:    Option[String]
  def threadId:  Option[String]
}

final case class ReadMessage(
  override val id:        MessageId,
  override val renderUrl: String,
  override val `type`:    Option[String],
  override val threadId:  Option[String])
    extends Message

final case class UnreadMessage(
  override val id:        MessageId,
  override val renderUrl: String,
  markAsReadUrl:          String,
  override val `type`:    Option[String],
  override val threadId:  Option[String])
    extends Message
