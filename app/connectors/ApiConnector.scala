/*
 * Copyright 2023 HM Revenue & Customs
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

package connectors

import api.Conversions
import config.AppConfig
import generated.TransitOperationType06
import models.UserAnswers
import play.api.Logging
import play.api.http.HeaderNames
import uk.gov.hmrc.http.HttpReads.Implicits._
import scalaxb.`package`.toXML
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, HttpClient, HttpErrorFunctions, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ApiConnector @Inject() (httpClient: HttpClient, appConfig: AppConfig)(implicit ec: ExecutionContext) extends HttpErrorFunctions with Logging {

  private val requestHeaders = Seq(
    HeaderNames.ACCEPT       -> "application/vnd.hmrc.2.0+json",
    HeaderNames.CONTENT_TYPE -> "application/xml"
  )

  // TODO - build out for remaining sections
  def createSubmission(userAnswers: UserAnswers): Either[String, String] =
    for {
      transitOperation <- Conversions.transitOperation(userAnswers)
    } yield payloadXml(transitOperation)

  def payloadXml(transitOperation: TransitOperationType06): String =
    (<ncts:CC015C PhaseID="NCTS5.0" xmlns:ncts="http://ncts.dgtaxud.ec">
      <messageRecipient>3pekcCFaMGmCMz1CPGUlhyml9gJCV6</messageRecipient>
      <preparationDateAndTime>2022-07-02T03:11:04</preparationDateAndTime>
      <messageIdentification>wrxe</messageIdentification>
      <messageType>CC015C</messageType>
      {toXML[TransitOperationType06](transitOperation, "TransitOperation", generated.defaultScope)}
      <CustomsOfficeOfDeparture>
        <referenceNumber>GB000218</referenceNumber>
      </CustomsOfficeOfDeparture>
      <CustomsOfficeOfDestinationDeclared>
        <referenceNumber>GB000218</referenceNumber>
      </CustomsOfficeOfDestinationDeclared>
      <HolderOfTheTransitProcedure>
        <identificationNumber>ezv3Z</identificationNumber>
      </HolderOfTheTransitProcedure>
      <Guarantee>
        <sequenceNumber>66710</sequenceNumber>
        <guaranteeType>P</guaranteeType>
        <otherGuaranteeReference>iNkM2E</otherGuaranteeReference>
      </Guarantee>
      <Consignment>
        <grossMass>4380979244.527545</grossMass>
        <HouseConsignment>
          <sequenceNumber>66710</sequenceNumber>
          <grossMass>4380979244.527545</grossMass>
          <ConsignmentItem>
            <goodsItemNumber>34564</goodsItemNumber>
            <declarationGoodsItemNumber>25</declarationGoodsItemNumber>
            <Commodity>
              <descriptionOfGoods>fds9YFrlk6DX7pnwQNgJmksfZ4z9uGjDy6Kaucb13r3kEleTuLHD5zKtbAKUU005AaZeVdTgdAnJKzuGliZGRb1E83Y0Z8IuyeFfnXgT7NwX81eGFb3vRXAWUFswwwprqZBcffnBLwLObF45W7evl7C6J4Tihj1d1a2ZKcAU6ttLNy</descriptionOfGoods>
            </Commodity>
            <Packaging>
              <sequenceNumber>66710</sequenceNumber>
              <typeOfPackages>Nu</typeOfPackages>
            </Packaging>
          </ConsignmentItem>
        </HouseConsignment>
      </Consignment>
    </ncts:CC015C>).mkString

  def submitDeclaration(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[HttpResponse] = {

    val declarationUrl = s"${appConfig.apiUrl}/movements/departures"

    createSubmission(userAnswers) match {
      case Left(msg) => throw new BadRequestException(msg)
      case Right(value) =>
        println(s"ACHI: $value")
        httpClient.POSTString(declarationUrl, value, requestHeaders)
    }

  }

}
