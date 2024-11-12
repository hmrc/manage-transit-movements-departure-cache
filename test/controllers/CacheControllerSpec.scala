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
import controllers.actions.{
  AuthenticateActionProvider,
  AuthenticateAndLockActionProvider,
  FakeAuthenticateActionProvider,
  FakeAuthenticateAndLockActionProvider
}
import generators.Generators
import models.AuditType.*
import models.Rejection.IE055Rejection
import models.{Metadata, Phase, SubmissionState, UserAnswersSummary, XPath}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalacheck.Arbitrary.arbitrary
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsArray, JsBoolean, JsString, Json}
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

        val request = FakeRequest(GET, routes.CacheController.get(lrn).url).withHeaders(("APIVersion", "2.0"))
        val result  = route(app, request).value

        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(userAnswers)
        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
      }
    }

    "return 404" when {
      "document not found in mongo for given lrn and eori number" in {
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(None))

        val request = FakeRequest(GET, routes.CacheController.get(lrn).url).withHeaders(("APIVersion", "2.0"))
        val result  = route(app, request).value

        status(result) shouldBe NOT_FOUND
        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
      }
    }

    "return 400" when {
      "request phase doesn't match user answers - transitional" in {
        val transitionalUserAnswers = emptyUserAnswers.copy(isTransitional = true)
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(transitionalUserAnswers)))

        val finalRequest = FakeRequest(GET, routes.CacheController.get(lrn).url).withHeaders(("APIVersion", "2.1"))
        val result       = route(app, finalRequest).value

        status(result) shouldBe BAD_REQUEST
        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
      }

      "request phase doesn't match user answers - final" in {
        val finalUserAnswers = emptyUserAnswers.copy(isTransitional = false)
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(finalUserAnswers)))

        val transitionalRequest = FakeRequest(GET, routes.CacheController.get(lrn).url).withHeaders(("APIVersion", "2.0"))
        val result              = route(app, transitionalRequest).value

        status(result) shouldBe BAD_REQUEST
        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
      }
    }

    "return 500" when {
      "read from mongo fails" in {
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.failed(new Throwable()))

        val request = FakeRequest(GET, routes.CacheController.get(lrn).url).withHeaders(("APIVersion", "2.0"))
        val result  = route(app, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
      }
    }
  }

  "post" should {

    "return 200" when {
      "write to mongo was acknowledged" in {
        val metaData = emptyMetadata
        when(mockCacheRepository.set(any(): Metadata, any(): Option[String], any(): Option[Phase]))
          .thenReturn(Future.successful(true))

        val request = FakeRequest(POST, routes.CacheController.post("AB123").url)
          .withBody(Json.toJson(metaData))
        val result = route(app, request).value

        status(result) shouldBe OK
        verify(mockCacheRepository).set(eqTo(metaData), eqTo(None), eqTo(None))
      }
    }

    "return 400" when {
      "request body is invalid" in {
        val request = FakeRequest(POST, routes.CacheController.post("AB123").url)
          .withBody(JsString("foo"))

        val result = route(app, request).value

        status(result) shouldBe BAD_REQUEST
        verifyNoInteractions(mockCacheRepository)
      }

      "request body is empty" in {
        val request = FakeRequest(POST, routes.CacheController.post("AB123").url)
          .withBody(Json.obj())

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
          .withBody(Json.toJson(userAnswers))

        val result = route(app, request).value

        status(result) shouldBe FORBIDDEN
        verifyNoInteractions(mockCacheRepository)
      }
    }

    "return 500" when {
      "write to mongo was not acknowledged on set" in {
        val metaData = emptyMetadata

        when(mockCacheRepository.set(any(): Metadata, any(): Option[String], any(): Option[Phase]))
          .thenReturn(Future.successful(false))

        val request = FakeRequest(POST, routes.CacheController.post("AB123").url)
          .withBody(Json.toJson(metaData))

        val result = route(app, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
        verify(mockCacheRepository).set(eqTo(metaData), eqTo(None), eqTo(None))
      }

      "write to mongo fails on set" in {
        val metaData = emptyMetadata
        when(mockCacheRepository.set(any(): Metadata, any(): Option[String], any(): Option[Phase]))
          .thenReturn(Future.failed(new Throwable()))

        val request = FakeRequest(POST, routes.CacheController.post("AB123").url)
          .withBody(Json.toJson(metaData))

        val result = route(app, request).value
        status(result) shouldBe INTERNAL_SERVER_ERROR
        verify(mockCacheRepository).set(eqTo(metaData), eqTo(None), eqTo(None))
      }
    }
  }

  "put" should {
    val auditType = DepartureDraftStarted

    "return 200" when {
      "write to mongo was acknowledged" in {
        when(mockCacheRepository.set(any(): Metadata, any(): Option[String], any(): Option[Phase]))
          .thenReturn(Future.successful(true))

        val request = FakeRequest(PUT, routes.CacheController.put().url)
          .withHeaders(("APIVersion", "2.0"))
          .withBody(JsString(lrn))

        val result                                   = route(app, request).value
        val metadataCaptor: ArgumentCaptor[Metadata] = ArgumentCaptor.forClass(classOf[Metadata])

        status(result) shouldBe OK
        verify(mockCacheRepository).set(metadataCaptor.capture(), eqTo(None), eqTo(Some(Phase.Transition)))
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
          .withBody(Json.obj("foo" -> "bar"))

        val result = route(app, request).value

        status(result) shouldBe BAD_REQUEST

        verifyNoInteractions(mockCacheRepository)
        verifyNoInteractions(mockAuditService)
        verifyNoInteractions(mockMetricsService)
      }

      "request body is empty" in {
        val request = FakeRequest(PUT, routes.CacheController.put().url)
          .withHeaders(("APIVersion", "2.0"))
          .withBody(Json.obj())

        val result = route(app, request).value

        status(result) shouldBe BAD_REQUEST

        verifyNoInteractions(mockCacheRepository)
        verifyNoInteractions(mockAuditService)
        verifyNoInteractions(mockMetricsService)
      }
    }

    "return 500" when {
      "write to mongo was not acknowledged" in {
        when(mockCacheRepository.set(any(): Metadata, any(): Option[String], any(): Option[Phase]))
          .thenReturn(Future.successful(false))

        val request = FakeRequest(PUT, routes.CacheController.put().url)
          .withHeaders(("APIVersion", "2.0"))
          .withBody(JsString(lrn))

        val result                                   = route(app, request).value
        val metadataCaptor: ArgumentCaptor[Metadata] = ArgumentCaptor.forClass(classOf[Metadata])

        status(result) shouldBe INTERNAL_SERVER_ERROR

        verify(mockCacheRepository).set(metadataCaptor.capture(), eqTo(None), eqTo(Some(Phase.Transition)))
        verifyNoInteractions(mockAuditService)
        verifyNoInteractions(mockMetricsService)

        metadataCaptor.getValue.lrn shouldBe lrn
        metadataCaptor.getValue.isSubmitted shouldBe SubmissionState.NotSubmitted
      }

      "write to mongo fails" in {
        when(mockCacheRepository.set(any(): Metadata, any(): Option[String], any(): Option[Phase]))
          .thenReturn(Future.failed(new Throwable()))

        val request = FakeRequest(PUT, routes.CacheController.put().url)
          .withHeaders(("APIVersion", "2.0"))
          .withBody(JsString(lrn))

        val result                                   = route(app, request).value
        val metadataCaptor: ArgumentCaptor[Metadata] = ArgumentCaptor.forClass(classOf[Metadata])

        status(result) shouldBe INTERNAL_SERVER_ERROR

        verify(mockCacheRepository).set(metadataCaptor.capture(), eqTo(None), eqTo(Some(Phase.Transition)))
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

        val request = FakeRequest(DELETE, routes.CacheController.delete(lrn).url).withHeaders(("APIVersion", "2.0"))
        val result  = route(app, request).value

        status(result) shouldBe OK

        val auditType = DepartureDraftDeleted
        verify(mockAuditService).audit(eqTo(auditType), eqTo(lrn), eqTo(eoriNumber))(any())
        verify(mockMetricsService).increment(auditType.name)
      }
    }

    "return 500" when {
      "deletion was unsuccessful" in {
        when(mockCacheRepository.remove(any(), any())).thenReturn(Future.failed(new Throwable()))

        val request = FakeRequest(DELETE, routes.CacheController.delete(lrn).url).withHeaders(("APIVersion", "2.0"))
        val result  = route(app, request).value

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

        val request = FakeRequest(GET, routes.CacheController.getAll().url).withHeaders(("APIVersion", "2.0"))
        val result  = route(app, request).value

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

        val request = FakeRequest(GET, routes.CacheController.getAll().url).withHeaders(("APIVersion", "2.0"))
        val result  = route(app, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
        verify(mockCacheRepository).getAll(eqTo(eoriNumber), any(), any(), any(), any(), any())
      }
    }
  }

  "getExpiry" should {

    "return 200" when {
      "read from mongo is successful" in {
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val request = FakeRequest(GET, routes.CacheController.getExpiry(lrn).url).withHeaders(("APIVersion", "2.0"))
        val result  = route(app, request).value

        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(30)
        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
      }
    }

    "return 404" when {
      "document not found in mongo for given lrn and eori number" in {
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(None))

        val request = FakeRequest(GET, routes.CacheController.getExpiry(lrn).url).withHeaders(("APIVersion", "2.0"))
        val result  = route(app, request).value

        status(result) shouldBe NOT_FOUND
        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
      }
    }

    "return 500" when {
      "read from mongo fails" in {
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.failed(new Throwable()))

        val request = FakeRequest(GET, routes.CacheController.getExpiry(lrn).url).withHeaders(("APIVersion", "2.0"))
        val result  = route(app, request).value

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

        when(mockCacheRepository.set(any(): Metadata, any(): Option[String], any(): Option[Phase]))
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
        verify(mockCacheRepository).set(eqTo(userAnswers.metadata), eqTo(userAnswers.departureId), eqTo(None))
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
        verify(mockCacheRepository, never()).set(any(): Metadata, any(): Option[String], any(): Option[Phase])
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

        when(mockCacheRepository.set(any(): Metadata, any(): Option[String], any(): Option[Phase]))
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
        verify(mockCacheRepository).set(eqTo(userAnswers.metadata), eqTo(userAnswers.departureId), eqTo(None))
      }
    }
  }

  "isDeclarationAmendable" should {

    val xPaths = arbitrary[Seq[XPath]].sample.value

    "return 200 with true" when {
      "declaration is amendable" in {
        when(mockXPathService.isDeclarationAmendable(any(), any(), any())).thenReturn(Future.successful(true))

        val request = FakeRequest(POST, routes.CacheController.isDeclarationAmendable(lrn).url)
          .withBody(JsArray(xPaths.map(_.value).map(JsString.apply)))

        val result = route(app, request).value

        status(result) shouldBe OK
        contentAsJson(result) shouldBe JsBoolean(true)
        verify(mockXPathService).isDeclarationAmendable(eqTo(lrn), eqTo(eoriNumber), eqTo(xPaths))
      }
    }

    "return 200 with false" when {
      "declaration is not amendable" in {
        when(mockXPathService.isDeclarationAmendable(any(), any(), any())).thenReturn(Future.successful(false))

        val request = FakeRequest(POST, routes.CacheController.isDeclarationAmendable(lrn).url)
          .withBody(JsArray(xPaths.map(_.value).map(JsString.apply)))

        val result = route(app, request).value

        status(result) shouldBe OK
        contentAsJson(result) shouldBe JsBoolean(false)
        verify(mockXPathService).isDeclarationAmendable(eqTo(lrn), eqTo(eoriNumber), eqTo(xPaths))
      }
    }

    "return 400" when {
      "request body is not an array of xpaths" in {
        val request = FakeRequest(POST, routes.CacheController.isDeclarationAmendable(lrn).url)
          .withBody(Json.obj("foo" -> "bar"))

        val result = route(app, request).value

        status(result) shouldBe BAD_REQUEST
        verify(mockXPathService, never()).isDeclarationAmendable(any(), any(), any())
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

        when(mockCacheRepository.set(any(): Metadata, any(): Option[String], any(): Option[Phase]))
          .thenReturn(Future.successful(true))

        val json = JsString(departureId)

        val request = FakeRequest(PATCH, routes.CacheController.prepareForAmendment(lrn).url)
          .withJsonBody(json)

        val result = route(app, request).value

        status(result) shouldBe OK
        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        verify(mockXPathService).prepareForAmendment(eqTo(userAnswers), eqTo(departureId))
        verify(mockCacheRepository).set(eqTo(userAnswers.metadata), eqTo(userAnswers.departureId), eqTo(None))
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
        verify(mockCacheRepository, never()).set(any(): Metadata, any(): Option[String], any(): Option[Phase])
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

        when(mockCacheRepository.set(any(): Metadata, any(): Option[String], any(): Option[Phase]))
          .thenReturn(Future.successful(false))

        val json = JsString(departureId)

        val request = FakeRequest(PATCH, routes.CacheController.prepareForAmendment(lrn).url)
          .withJsonBody(json)

        val result = route(app, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        verify(mockXPathService).prepareForAmendment(eqTo(userAnswers), eqTo(departureId))
        verify(mockCacheRepository).set(eqTo(userAnswers.metadata), eqTo(userAnswers.departureId), eqTo(None))
      }
    }
  }
}
