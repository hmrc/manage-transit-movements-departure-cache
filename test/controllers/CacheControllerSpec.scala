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

package controllers

import base.{AppWithDefaultMockFixtures, SpecBase}
import controllers.actions.*
import generators.Generators
import models.AuditType.*
import models.Rejection.IE055Rejection
import models.{Metadata, SubmissionState, UserAnswersSummary}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsBoolean, JsString, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.{CacheRepository, DefaultLockRepository}
import services.{AuditService, MetricsService, XPathService}

import scala.concurrent.Future

class CacheControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private lazy val mockAuditService   = mock[AuditService]
  private lazy val mockMetricsService = mock[MetricsService]
  private lazy val mockXPathService   = mock[XPathService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[AuthenticateActionProvider].to[FakeAuthenticateActionProvider],
        bind[AuthenticateAndLockActionProvider].to[FakeAuthenticateAndLockActionProvider],
        bind[CacheRepository].toInstance(mockCacheRepository),
        bind[DefaultLockRepository].toInstance(mockLockRepository),
        bind[AuditService].toInstance(mockAuditService),
        bind[MetricsService].toInstance(mockMetricsService),
        bind[XPathService].toInstance(mockXPathService)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuditService)
    reset(mockMetricsService)
    reset(mockXPathService)
  }

  "get" should {

    "return 200" when {
      "read from mongo is successful" in {
        val userAnswers = emptyUserAnswers
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(userAnswers)))

        val request = FakeRequest(GET, routes.CacheController.get(lrn).url)
          .withHeaders(("APIVersion", "2.0"))
        val result = route(app, request).value

        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(userAnswers)
        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
      }
    }

    "return 404" when {
      "document not found in mongo for given lrn and eori number" in {
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(None))

        val request = FakeRequest(GET, routes.CacheController.get(lrn).url)
          .withHeaders(("APIVersion", "2.0"))
        val result = route(app, request).value

        status(result) shouldBe NOT_FOUND
        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
      }
    }

    "return 500" when {
      "read from mongo fails" in {
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.failed(new Throwable()))

        val request = FakeRequest(GET, routes.CacheController.get(lrn).url)
          .withHeaders(("APIVersion", "2.0"))
        val result = route(app, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
      }
    }
  }

  "post" should {

    "return 200" when {
      "write to mongo was acknowledged" in {
        val metaData = emptyMetadata
        when(mockCacheRepository.set(any(): Metadata, any(): Option[String]))
          .thenReturn(Future.successful(true))

        val request = FakeRequest(POST, routes.CacheController.post("AB123").url)
          .withJsonBody(Json.toJson(metaData))
        val result = route(app, request).value

        status(result) shouldBe OK
        verify(mockCacheRepository).set(eqTo(metaData), eqTo(None))
      }
    }

    "return 400" when {
      "request body is invalid" in {
        val request = FakeRequest(POST, routes.CacheController.post("AB123").url)
          .withJsonBody(JsString("foo"))

        val result = route(app, request).value

        status(result) shouldBe BAD_REQUEST
        verifyNoInteractions(mockCacheRepository)
      }

      "request body is empty" in {
        val request = FakeRequest(POST, routes.CacheController.post("AB123").url)
          .withJsonBody(Json.obj())

        val result = route(app, request).value

        status(result) shouldBe BAD_REQUEST
        verifyNoInteractions(mockCacheRepository)
      }
    }

    "return 403" when {
      "the EORI in the enrolment and the EORI in user answers do not match" in {
        val metadata    = emptyMetadata.copy(eoriNumber = "different eori")
        val userAnswers = emptyUserAnswers.copy(metadata = metadata)

        val request = FakeRequest(POST, routes.CacheController.post("AB123").url)
          .withJsonBody(Json.toJson(userAnswers))

        val result = route(app, request).value

        status(result) shouldBe FORBIDDEN
        verifyNoInteractions(mockCacheRepository)
      }
    }

    "return 500" when {
      "write to mongo was not acknowledged on set" in {
        val metaData = emptyMetadata

        when(mockCacheRepository.set(any(): Metadata, any(): Option[String]))
          .thenReturn(Future.successful(false))

        val request = FakeRequest(POST, routes.CacheController.post("AB123").url)
          .withJsonBody(Json.toJson(metaData))

        val result = route(app, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
        verify(mockCacheRepository).set(eqTo(metaData), eqTo(None))
      }

      "write to mongo fails on set" in {
        val metaData = emptyMetadata
        when(mockCacheRepository.set(any(): Metadata, any(): Option[String]))
          .thenReturn(Future.failed(new Throwable()))

        val request = FakeRequest(POST, routes.CacheController.post("AB123").url)
          .withJsonBody(Json.toJson(metaData))

        val result = route(app, request).value
        status(result) shouldBe INTERNAL_SERVER_ERROR
        verify(mockCacheRepository).set(eqTo(metaData), eqTo(None))
      }
    }
  }

  "put" should {
    val auditType = DepartureDraftStarted

    "return 200" when {
      "write to mongo was acknowledged" in {
        when(mockCacheRepository.set(any(): Metadata, any(): Option[String]))
          .thenReturn(Future.successful(true))

        val request = FakeRequest(PUT, routes.CacheController.put().url)
          .withHeaders(("APIVersion", "2.0"))
          .withJsonBody(JsString(lrn))

        val result                                   = route(app, request).value
        val metadataCaptor: ArgumentCaptor[Metadata] = ArgumentCaptor.forClass(classOf[Metadata])

        status(result) shouldBe OK
        verify(mockCacheRepository).set(metadataCaptor.capture(), eqTo(None))
        metadataCaptor.getValue.lrn shouldBe lrn
        metadataCaptor.getValue.isSubmitted shouldBe SubmissionState.NotSubmitted

        verify(mockAuditService).audit(eqTo(auditType), eqTo(lrn), eqTo(eoriNumber))(any())
        verify(mockMetricsService).increment(auditType.name)
      }
    }

    "return 400" when {
      "request body is invalid" in {
        val request = FakeRequest(PUT, routes.CacheController.put().url)
          .withHeaders(("APIVersion", "2.0"))
          .withJsonBody(Json.obj("foo" -> "bar"))

        val result = route(app, request).value

        status(result) shouldBe BAD_REQUEST

        verifyNoInteractions(mockCacheRepository)
        verifyNoInteractions(mockAuditService)
        verifyNoInteractions(mockMetricsService)
      }

      "request body is empty" in {
        val request = FakeRequest(PUT, routes.CacheController.put().url)
          .withHeaders(("APIVersion", "2.0"))
          .withJsonBody(Json.obj())

        val result = route(app, request).value

        status(result) shouldBe BAD_REQUEST

        verifyNoInteractions(mockCacheRepository)
        verifyNoInteractions(mockAuditService)
        verifyNoInteractions(mockMetricsService)
      }
    }

    "return 500" when {
      "write to mongo was not acknowledged" in {
        when(mockCacheRepository.set(any(): Metadata, any(): Option[String]))
          .thenReturn(Future.successful(false))

        val request = FakeRequest(PUT, routes.CacheController.put().url)
          .withHeaders(("APIVersion", "2.0"))
          .withJsonBody(JsString(lrn))

        val result                                   = route(app, request).value
        val metadataCaptor: ArgumentCaptor[Metadata] = ArgumentCaptor.forClass(classOf[Metadata])

        status(result) shouldBe INTERNAL_SERVER_ERROR

        verify(mockCacheRepository).set(metadataCaptor.capture(), eqTo(None))
        verifyNoInteractions(mockAuditService)
        verifyNoInteractions(mockMetricsService)

        metadataCaptor.getValue.lrn shouldBe lrn
        metadataCaptor.getValue.isSubmitted shouldBe SubmissionState.NotSubmitted
      }

      "write to mongo fails" in {
        when(mockCacheRepository.set(any(): Metadata, any(): Option[String]))
          .thenReturn(Future.failed(new Throwable()))

        val request = FakeRequest(PUT, routes.CacheController.put().url)
          .withHeaders(("APIVersion", "2.0"))
          .withJsonBody(JsString(lrn))

        val result                                   = route(app, request).value
        val metadataCaptor: ArgumentCaptor[Metadata] = ArgumentCaptor.forClass(classOf[Metadata])

        status(result) shouldBe INTERNAL_SERVER_ERROR

        verify(mockCacheRepository).set(metadataCaptor.capture(), eqTo(None))
        verifyNoInteractions(mockAuditService)
        verifyNoInteractions(mockMetricsService)

        metadataCaptor.getValue.lrn shouldBe lrn
        metadataCaptor.getValue.isSubmitted shouldBe SubmissionState.NotSubmitted
      }
    }
  }

  "delete" should {

    "return 200" when {
      "deletion was successful" in {
        when(mockCacheRepository.remove(any(), any())).thenReturn(Future.successful(true))

        val request = FakeRequest(DELETE, routes.CacheController.delete(lrn).url)
          .withHeaders(("APIVersion", "2.0"))
        val result = route(app, request).value

        status(result) shouldBe OK

        val auditType = DepartureDraftDeleted
        verify(mockAuditService).audit(eqTo(auditType), eqTo(lrn), eqTo(eoriNumber))(any())
        verify(mockMetricsService).increment(auditType.name)
      }
    }

    "return 500" when {
      "deletion was unsuccessful" in {
        when(mockCacheRepository.remove(any(), any())).thenReturn(Future.failed(new Throwable()))

        val request = FakeRequest(DELETE, routes.CacheController.delete(lrn).url)
          .withHeaders(("APIVersion", "2.0"))
        val result = route(app, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR

        verifyNoInteractions(mockAuditService)
        verifyNoInteractions(mockMetricsService)
      }
    }
  }

  "getAll" should {

    "return 200" when {

      "read from mongo is successful" in {
        val userAnswer1 = emptyUserAnswers.copy(metadata = Metadata("AB123", eoriNumber, SubmissionState.NotSubmitted))
        val userAnswer2 = emptyUserAnswers.copy(metadata = Metadata("CD123", eoriNumber, SubmissionState.NotSubmitted), isTransitional = false)
        val userAnswers = Seq(userAnswer1, userAnswer2)

        when(mockCacheRepository.getAll(any(), any(), any(), any(), any(), any()))
          .thenReturn(Future.successful(UserAnswersSummary(eoriNumber, userAnswers, 2, 2)))

        val request = FakeRequest(GET, routes.CacheController.getAll().url)
          .withHeaders(("APIVersion", "2.0"))
        val result = route(app, request).value

        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.parse(s"""
            |{
            |  "eoriNumber": "$eoriNumber",
            |  "totalMovements": 2,
            |  "totalMatchingMovements": 2,
            |  "userAnswers": [
            |    {
            |      "lrn": "${userAnswer1.lrn}",
            |      "_links": {
            |        "self": {
            |          "href": "/manage-transit-movements-departure-cache/user-answers/${userAnswer1.lrn}"
            |        }
            |      },
            |      "createdAt": "${userAnswer1.createdAt}",
            |      "lastUpdated": "${userAnswer1.lastUpdated}",
            |      "expiresInDays": 30,
            |      "_id": "${userAnswer1.id}",
            |      "isSubmitted": "${userAnswer1.metadata.isSubmitted.asString}",
            |      "isTransitional": true
            |    },
            |    {
            |      "lrn": "${userAnswer2.lrn}",
            |      "_links": {
            |        "self": {
            |          "href": "/manage-transit-movements-departure-cache/user-answers/${userAnswer2.lrn}"
            |        }
            |      },
            |      "createdAt": "${userAnswer2.createdAt}",
            |      "lastUpdated": "${userAnswer2.lastUpdated}",
            |      "expiresInDays": 30,
            |      "_id": "${userAnswer2.id}",
            |      "isSubmitted": "${userAnswer2.metadata.isSubmitted.asString}",
            |      "isTransitional": false
            |    }
            |  ]
            |}
            |""".stripMargin)
        verify(mockCacheRepository).getAll(eqTo(eoriNumber), any(), any(), any(), any(), any())
      }
    }

    "return 500" when {
      "read from mongo fails" in {
        when(mockCacheRepository.getAll(any(), any(), any(), any(), any(), any())).thenReturn(Future.failed(new Throwable()))

        val request = FakeRequest(GET, routes.CacheController.getAll().url)
          .withHeaders(("APIVersion", "2.0"))
        val result = route(app, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
        verify(mockCacheRepository).getAll(eqTo(eoriNumber), any(), any(), any(), any(), any())
      }
    }
  }

  "getExpiry" should {

    "return 200" when {
      "read from mongo is successful" in {
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val request = FakeRequest(GET, routes.CacheController.getExpiry(lrn).url)
          .withHeaders(("APIVersion", "2.0"))
        val result = route(app, request).value

        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(30)
        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
      }
    }

    "return 404" when {
      "document not found in mongo for given lrn and eori number" in {
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(None))

        val request = FakeRequest(GET, routes.CacheController.getExpiry(lrn).url)
          .withHeaders(("APIVersion", "2.0"))
        val result = route(app, request).value

        status(result) shouldBe NOT_FOUND
        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
      }
    }

    "return 500" when {
      "read from mongo fails" in {
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.failed(new Throwable()))

        val request = FakeRequest(GET, routes.CacheController.getExpiry(lrn).url)
          .withHeaders(("APIVersion", "2.0"))
        val result = route(app, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
      }
    }
  }

  "handleErrors" should {

    "return 200" when {
      "rejection successfully handled" in {
        val userAnswers = emptyUserAnswers

        when(mockCacheRepository.get(any(), any()))
          .thenReturn(Future.successful(Some(userAnswers)))

        when(mockXPathService.handleRejection(any(), any()))
          .thenReturn(userAnswers)

        when(mockCacheRepository.set(any(): Metadata, any(): Option[String]))
          .thenReturn(Future.successful(true))

        val json = Json.parse(s"""
            |{
            |  "departureId" : "$departureId",
            |  "type" : "IE055"
            |}
            |""".stripMargin)

        val request = FakeRequest(POST, routes.CacheController.handleErrors(lrn).url)
          .withHeaders(("APIVersion", "2.0"))
          .withJsonBody(json)

        val result = route(app, request).value

        status(result) shouldBe OK
        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        verify(mockXPathService).handleRejection(eqTo(userAnswers), eqTo(IE055Rejection(departureId)))
        verify(mockCacheRepository).set(eqTo(userAnswers.metadata), eqTo(userAnswers.departureId))
      }
    }

    "return 404" when {
      "user answers not found" in {
        when(mockCacheRepository.get(any(), any()))
          .thenReturn(Future.successful(None))

        val json = Json.parse(s"""
            |{
            |  "departureId" : "$departureId",
            |  "type" : "IE055"
            |}
            |""".stripMargin)

        val request = FakeRequest(POST, routes.CacheController.handleErrors(lrn).url)
          .withHeaders(("APIVersion", "2.0"))
          .withJsonBody(json)

        val result = route(app, request).value

        status(result) shouldBe NOT_FOUND
        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        verifyNoInteractions(mockXPathService)
        verify(mockCacheRepository, never()).set(any(): Metadata, any(): Option[String])
      }
    }

    "return 400" when {
      "request body can't be read as a Rejection" in {
        val json = Json.parse(s"""
            |{
            |  "foo" : "bar"
            |}
            |""".stripMargin)

        val request = FakeRequest(POST, routes.CacheController.handleErrors(lrn).url)
          .withHeaders(("APIVersion", "2.0"))
          .withJsonBody(json)

        val result = route(app, request).value

        status(result) shouldBe BAD_REQUEST
        verifyNoInteractions(mockCacheRepository)
        verifyNoInteractions(mockXPathService)
      }
    }

    "return 500" when {
      "write to mongo is unsuccessful" in {
        val userAnswers = emptyUserAnswers

        when(mockCacheRepository.get(any(), any()))
          .thenReturn(Future.successful(Some(userAnswers)))

        when(mockXPathService.handleRejection(any(), any()))
          .thenReturn(userAnswers)

        when(mockCacheRepository.set(any(): Metadata, any(): Option[String]))
          .thenReturn(Future.successful(false))

        val json = Json.parse(s"""
             |{
             |  "departureId" : "$departureId",
             |  "type" : "IE055"
             |}
             |""".stripMargin)

        val request = FakeRequest(POST, routes.CacheController.handleErrors(lrn).url)
          .withHeaders(("APIVersion", "2.0"))
          .withJsonBody(json)

        val result = route(app, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        verify(mockXPathService).handleRejection(eqTo(userAnswers), eqTo(IE055Rejection(departureId)))
        verify(mockCacheRepository).set(eqTo(userAnswers.metadata), eqTo(userAnswers.departureId))
      }
    }
  }

  "isRejectionAmendable" should {

    val json = Json.parse(s"""
         |{
         |  "departureId" : "$departureId",
         |  "type" : "IE055"
         |}
         |""".stripMargin)

    "return 200 with true" when {

      "rejection is amendable" in {
        when(mockXPathService.isRejectionAmendable(any(), any(), any()))
          .thenReturn(Future.successful(true))

        val request = FakeRequest(POST, routes.CacheController.isRejectionAmendable(lrn).url)
          .withJsonBody(json)

        val result = route(app, request).value

        status(result) shouldBe OK
        contentAsJson(result) shouldBe JsBoolean(true)
        verify(mockXPathService).isRejectionAmendable(eqTo(lrn), eqTo(eoriNumber), eqTo(IE055Rejection(departureId)))
      }
    }

    "return 200 with false" when {
      "rejection is not amendable" in {
        when(mockXPathService.isRejectionAmendable(any(), any(), any()))
          .thenReturn(Future.successful(false))

        val request = FakeRequest(POST, routes.CacheController.isRejectionAmendable(lrn).url)
          .withJsonBody(json)

        val result = route(app, request).value

        status(result) shouldBe OK
        contentAsJson(result) shouldBe JsBoolean(false)
        verify(mockXPathService).isRejectionAmendable(eqTo(lrn), eqTo(eoriNumber), eqTo(IE055Rejection(departureId)))
      }
    }

    "return 400" when {
      "request body is not a rejection" in {
        val request = FakeRequest(POST, routes.CacheController.isRejectionAmendable(lrn).url)
          .withJsonBody(Json.obj("foo" -> "bar"))

        val result = route(app, request).value

        status(result) shouldBe BAD_REQUEST
        verify(mockXPathService, never()).isRejectionAmendable(any(), any(), any())
      }
    }
  }

  "prepareForAmendment" should {

    "return 200" when {
      "rejection successfully handled" in {
        val userAnswers = emptyUserAnswers

        when(mockCacheRepository.get(any(), any()))
          .thenReturn(Future.successful(Some(userAnswers)))

        when(mockXPathService.prepareForAmendment(any(), any()))
          .thenReturn(userAnswers)

        when(mockCacheRepository.set(any(): Metadata, any(): Option[String]))
          .thenReturn(Future.successful(true))

        val json = JsString(departureId)

        val request = FakeRequest(PATCH, routes.CacheController.prepareForAmendment(lrn).url)
          .withJsonBody(json)

        val result = route(app, request).value

        status(result) shouldBe OK
        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        verify(mockXPathService).prepareForAmendment(eqTo(userAnswers), eqTo(departureId))
        verify(mockCacheRepository).set(eqTo(userAnswers.metadata), eqTo(userAnswers.departureId))
      }
    }

    "return 404" when {
      "user answers not found" in {
        when(mockCacheRepository.get(any(), any()))
          .thenReturn(Future.successful(None))

        val json = JsString(departureId)

        val request = FakeRequest(PATCH, routes.CacheController.prepareForAmendment(lrn).url)
          .withJsonBody(json)

        val result = route(app, request).value

        status(result) shouldBe NOT_FOUND
        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        verifyNoInteractions(mockXPathService)
        verify(mockCacheRepository, never()).set(any(): Metadata, any(): Option[String])
      }
    }

    "return 400" when {
      "request body can't be read as a departure ID" in {
        val json = Json.parse(s"""
            |{
            |  "foo" : "bar"
            |}
            |""".stripMargin)

        val request = FakeRequest(PATCH, routes.CacheController.prepareForAmendment(lrn).url)
          .withJsonBody(json)

        val result = route(app, request).value

        status(result) shouldBe BAD_REQUEST
        verifyNoInteractions(mockCacheRepository)
        verifyNoInteractions(mockXPathService)
      }
    }

    "return 500" when {
      "write to mongo is unsuccessful" in {
        val userAnswers = emptyUserAnswers

        when(mockCacheRepository.get(any(), any()))
          .thenReturn(Future.successful(Some(userAnswers)))

        when(mockXPathService.prepareForAmendment(any(), any()))
          .thenReturn(userAnswers)

        when(mockCacheRepository.set(any(): Metadata, any(): Option[String]))
          .thenReturn(Future.successful(false))

        val json = JsString(departureId)

        val request = FakeRequest(PATCH, routes.CacheController.prepareForAmendment(lrn).url)
          .withJsonBody(json)

        val result = route(app, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        verify(mockXPathService).prepareForAmendment(eqTo(userAnswers), eqTo(departureId))
        verify(mockCacheRepository).set(eqTo(userAnswers.metadata), eqTo(userAnswers.departureId))
      }
    }
  }

  "copy" should {

    val oldLrn         = "oldLrn"
    val newLrn         = "newLrn"
    val oldUserAnswers = emptyUserAnswers.updateLrn(oldLrn)
    val newUserAnswers = oldUserAnswers.updateLrn(newLrn)

    "return 200" when {
      "new user answers successfully created" in {
        when(mockCacheRepository.get(any(), any()))
          .thenReturn(Future.successful(Some(oldUserAnswers)))

        when(mockCacheRepository.set(any(): Metadata, any(): Option[String]))
          .thenReturn(Future.successful(true))

        val json = JsString(newLrn)

        val request = FakeRequest(POST, routes.CacheController.copy(oldLrn).url)
          .withHeaders(("APIVersion", "2.1"))
          .withJsonBody(json)

        val result = route(app, request).value

        status(result) shouldBe OK
        verify(mockCacheRepository).get(eqTo(oldLrn), eqTo(eoriNumber))
        verify(mockCacheRepository).set(eqTo(newUserAnswers.metadata), eqTo(None))
      }
    }

    "return 400" when {
      "request body does not contain the new LRN" in {
        val request = FakeRequest(POST, routes.CacheController.copy(oldLrn).url)
          .withHeaders(("APIVersion", "2.1"))
          .withJsonBody(Json.obj("foo" -> "bar"))

        val result = route(app, request).value

        status(result) shouldBe BAD_REQUEST
      }
    }

    "return 500" when {
      "write to mongo is unsuccessful" in {
        when(mockCacheRepository.get(any(), any()))
          .thenReturn(Future.successful(Some(oldUserAnswers)))

        when(mockCacheRepository.set(any(): Metadata, any(): Option[String]))
          .thenReturn(Future.successful(false))

        val json = JsString(newLrn)

        val request = FakeRequest(POST, routes.CacheController.copy(oldLrn).url)
          .withHeaders(("APIVersion", "2.1"))
          .withJsonBody(json)

        val result = route(app, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
        verify(mockCacheRepository).get(eqTo(oldLrn), eqTo(eoriNumber))
        verify(mockCacheRepository).set(eqTo(newUserAnswers.metadata), eqTo(None))
      }
    }
  }
}
