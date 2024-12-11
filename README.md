
# manage-transit-movements-departure-cache

## Endpoints

---

## `GET /user-answers?lrn=[Option[String]]&state=[Option[SubmissionState]]&limit=[Option[Int]]&skip=[Option[Int]]&sortBy=[Option[String]]`

#### Params

* Optional param list for user answers endpoint
  * lrn - Filters documents for partial or matching Local Reference Number of all user answers for given eori number
  * state - Filters documents based on the submission state of the document
  * limit - Sets the maximum number of returned documents 
  * skip - Sets the increment of skipped documents for pagination purposes. The number of documents skipped is worked out as 'skip * limit'
  * sortBy - Sorts the documents being returned. These can be sorted in ascending or descending order using the LRN or date created

### Successful response

#### 200 OK

* A call is made to the `GET` endpoint with:
  * a valid bearer token
  * a valid `HMRC-CTC-ORG` enrolment with `EoriNumber` identifier
* A list of documents are found in the `user-answers` collection for the given EORI number from enrolment
* The response JSON will have a summary list of each user answer with the following fields:
  * `lrn` - The local reference number associated with the departure application
  * `_links` - Links used to retrieve the full data for a specific lrn
  * `createdAt` - The date and time that the application was started. The user has 30 days from this point to complete and submit the application
  * `lastUpdated` - The date and time that the application was last updated
  * `id` - a UUID

#### Sample response

```
{
  "eoriNumber": "1234567",
  "totalMovements": 2,
  "totalMatchingMovements": 2, 
  "userAnswers": [
    {
      "lrn": "AB123",
      "_links": {
        "self": {
          "href": "/manage-transit-movements-departure-cache/user-answers/AB123"
        }
      },
      "createdAt": "2023-01-26T10:32:15.648",
      "lastUpdated": "2023-01-27T08:43:17.064",
      "expiresInDays": 29,
      "_id": "27e687a9-4544-4e22-937e-74e699d855f8",
      "isSubmitted": "notSubmitted"
    },
    {
      "lrn": "CD123",
      "_links": {
        "self": {
          "href": "/manage-transit-movements-departure-cache/user-answers/CD123"
        }
      },
      "createdAt": "2023-01-26T10:32:36.96",
      "lastUpdated": "2023-01-26T10:32:41.377",
      "expiresInDays": 29,
      "_id": "750f1f92-6c61-4a3b-ad3e-95d8c7418eb4",
      "isSubmitted": "notSubmitted"
    }
  ]
}
```

### Unsuccessful responses (with possible causes)

#### 401 UNAUTHORIZED
* A generic authorization error occurred. The likely cause of this is an invalid or missing bearer token.

#### 403 FORBIDDEN
* User has insufficient enrolments

#### 404 NOT_FOUND
* No documents were found for EORI number

#### 500 INTERNAL_SERVER_ERROR
* An error occurred in the mongo client

---

## `GET /user-answers/:lrn`

### Successful response

#### 200 OK

* A call is made to the `GET` endpoint with:
  * a valid bearer token 
  * a valid `HMRC-CTC-ORG` enrolment with `EoriNumber` identifier
  * an `APIVersion` header with either:
    * `2.0` for transition rules
    * `2.1` for final rules
* A document is found in the `user-answers` collection for the given LRN (the EORI number is extracted from the enrolment)
* The response JSON has the following fields:
  * `lrn` - The local reference number associated with the departure application
  * `eoriNumber` - The EORI number linked to the user's enrolment
  * `data` - The user's answers
  * `createdAt` - The date and time that the application was started. The user has 30 days from this point to complete and submit the application
  * `lastUpdated` - The date and time that the application was last updated
  * `id` - a UUID

### Unsuccessful responses (with possible causes)

#### 400 BAD_REQUEST
* `APIVersion` header was missing or did not align with saved answers

#### 401 UNAUTHORIZED
* A generic authorization error occurred. The likely cause of this is an invalid or missing bearer token.

#### 403 FORBIDDEN
* User has insufficient enrolments

#### 404 NOT_FOUND
* No document was found for the given LRN

#### 500 INTERNAL_SERVER_ERROR
* An error occurred in the mongo client

---

## `POST /user-answers/:lrn`

### Successful response

#### 200 OK

* A call is made to the `POST` endpoint with:
    * a valid bearer token
    * a valid `HMRC-CTC-ORG` enrolment with `EoriNumber` identifier
    * a valid `UserAnswers` request body
    * the same EORI number in the enrolment and request body
* Then, for the given LRN and EORI number in the request body, either:
  * A document is found in the `user-answers` collection and the document gets updated with the user answers from the request body
  * A document is not found in the `user-answers` collection and a new document gets created with the user answers from the request body

### Unsuccessful responses (with possible causes)

#### 400 BAD_REQUEST
* Request body could not be validated as a `UserAnswers`

#### 401 UNAUTHORIZED
* A generic authorization error occurred. The likely cause of this is an invalid or missing bearer token.

#### 403 FORBIDDEN
* User has insufficient enrolments
* EORI number in request body does not match the EORI number in the user's enrolment

#### 500 INTERNAL_SERVER_ERROR
* An error occurred in the mongo client

---

## `PATCH /user-answers/:lrn`

### Successful response

#### 200 OK

* A call is made to the `PATCH` endpoint with:
  * a valid bearer token
  * a valid `HMRC-CTC-ORG` enrolment with `EoriNumber` identifier
  * a valid `String` request body containing the departure ID
  * the same EORI number in the enrolment and request body
* Then, the relevant document gets prepared for an amendment with the provided departure ID and a submission status of `amendment`

### Unsuccessful responses (with possible causes)

#### 400 BAD_REQUEST
* Request body could not be validated as a `String`

#### 401 UNAUTHORIZED
* A generic authorization error occurred. The likely cause of this is an invalid or missing bearer token.

#### 403 FORBIDDEN
* User has insufficient enrolments
* EORI number in request body does not match the EORI number in the user's enrolment

#### 404 NOT_FOUND
* No document was found for the given LRN

#### 500 INTERNAL_SERVER_ERROR
* An error occurred in the mongo client

---

## `PUT /user-answers`

### Successful response

#### 200 OK

* A call is made to the `PUT` endpoint with:
  * a valid bearer token
  * a valid `HMRC-CTC-ORG` enrolment with `EoriNumber` identifier
  * a valid `String` request body representing the LRN
  * an `APIVersion` header with either:
    * `2.0` for transition rules
    * `2.1` for final rules
* Then, for the given LRN in the request body and EORI number in the enrolment, a new document gets created with an empty user answers

### Unsuccessful responses (with possible causes)

#### 400 BAD_REQUEST
* Request body could not be validated as a `String`
* `APIVersion` header was missing

#### 401 UNAUTHORIZED
* A generic authorization error occurred. The likely cause of this is an invalid or missing bearer token.

#### 403 FORBIDDEN
* User has insufficient enrolments

#### 500 INTERNAL_SERVER_ERROR
* An error occurred in the mongo client

---

## `DELETE /user-answers/:lrn`

### Successful response

#### 200 OK

* A call is made to the `DELETE` endpoint with:
  * a valid bearer token
  * a valid `HMRC-CTC-ORG` enrolment with `EoriNumber` identifier
* A document is deleted from the `user-answers` collection for the given LRN (the EORI number is extracted from the enrolment)

### Unsuccessful responses (with possible causes)

#### 500 INTERNAL_SERVER_ERROR
* An error occurred in the mongo client

---

## `POST /user-answers/:lrn/amendable`

### Successful response

#### 200 OK

* A call is made to the `POST` endpoint with:
  * a valid bearer token
  * a valid `HMRC-CTC-ORG` enrolment with `EoriNumber` identifier
  * a valid `Rejection`, containing information about the rejection. In the case of an IE056 this should contain a business rejection type and some error pointers.
* Then, we check whether:
  * there is a document in the cache for the given LRN and;
  * in the case of an IE056, there is at least one error pointer that is amendable

### Unsuccessful responses (with possible causes)

#### 400 BAD_REQUEST
* Request body could not be validated as a series of `String` X-paths

#### 401 UNAUTHORIZED
* A generic authorization error occurred. The likely cause of this is an invalid or missing bearer token.

#### 403 FORBIDDEN
* User has insufficient enrolments

#### 500 INTERNAL_SERVER_ERROR
* An error occurred in the mongo client

---

## `POST /user-answers/:lrn/errors`

### Successful response

#### 200 OK

* A call is made to the `POST` endpoint with:
  * a valid bearer token
  * a valid `HMRC-CTC-ORG` enrolment with `EoriNumber` identifier
  * a valid `Rejection`, containing information about the rejection. In the case of an IE056 this should contain a business rejection type and some error pointers.
* Then, we check whether:
  * at least one of the error pointers is amendable AND;
  * there is a document in the cache for the given LRN

### Unsuccessful responses (with possible causes)

#### 400 BAD_REQUEST
* Request body could not be validated as a `Rejection`
* If provided, the error pointers (X-paths) must be non-empty

#### 401 UNAUTHORIZED
* A generic authorization error occurred. The likely cause of this is an invalid or missing bearer token.

#### 403 FORBIDDEN
* User has insufficient enrolments

#### 500 INTERNAL_SERVER_ERROR
* An error occurred in the mongo client

---

## `POST /declaration/submit`

### Successful response

#### 200 OK

* A call is made to the `POST` endpoint with:
  * a valid bearer token
  * a valid `HMRC-CTC-ORG` enrolment with `EoriNumber` identifier
  * a valid `String` request body representing the LRN
  * an `APIVersion` header with either:
    * `2.0` for transition rules
    * `2.1` for final rules
* Then, an IE015 gets successfully submitted to the API

### Unsuccessful responses (with possible causes)

#### 400 BAD_REQUEST
* Request body could not be validated as a `String`
* `APIVersion` header was missing

#### 401 UNAUTHORIZED
* A generic authorization error occurred. The likely cause of this is an invalid or missing bearer token.

#### 403 FORBIDDEN
* User has insufficient enrolments

#### 500 INTERNAL_SERVER_ERROR
* An error occurred

---

## `POST /declaration/submit-amendment`

### Successful response

#### 200 OK

* A call is made to the `POST` endpoint with:
  * a valid bearer token
  * a valid `HMRC-CTC-ORG` enrolment with `EoriNumber` identifier
  * a valid `String` request body representing the LRN
  * an `APIVersion` header with either:
    * `2.0` for transition rules
    * `2.1` for final rules
* Then, an IE013 gets successfully submitted to the API

### Unsuccessful responses (with possible causes)

#### 400 BAD_REQUEST
* Request body could not be validated as a `String`
* `APIVersion` header was missing

#### 401 UNAUTHORIZED
* A generic authorization error occurred. The likely cause of this is an invalid or missing bearer token.

#### 403 FORBIDDEN
* User has insufficient enrolments

#### 500 INTERNAL_SERVER_ERROR
* An error occurred

---

## `GET /messages/:lrn`

### Successful response

#### 200 OK

* A call is made to the `GET` endpoint with:
  * a valid bearer token
  * a valid `HMRC-CTC-ORG` enrolment with `EoriNumber` identifier
  * an `APIVersion` header with either:
    * `2.0` for transition rules
    * `2.1` for final rules
* A departure is found in the API for the given LRN and EORI number (extracted from the enrolment)
* Then, the messages corresponding to this departure ID are retrieved 

### Unsuccessful responses (with possible causes)

#### 204 NO_CONTENT
* The departure was found, but it contained no messages

#### 400 BAD_REQUEST
* `APIVersion` header was missing

#### 401 UNAUTHORIZED
* A generic authorization error occurred. The likely cause of this is an invalid or missing bearer token.

#### 403 FORBIDDEN
* User has insufficient enrolments

#### 404 NOT_FOUND
* The departure was not found

#### 500 INTERNAL_SERVER_ERROR
* An error occurred

---

## `POST /messages/rejection`

### Successful response

#### 200 OK

* A call is made to the `POST` endpoint with:
  * a valid bearer token
  * a valid `HMRC-CTC-ORG` enrolment with `EoriNumber` identifier
  * a valid list of functional errors
* The functional errors are converted to a separate list with the error pointers mapped to a section

### Unsuccessful responses (with possible causes)

#### 400 BAD_REQUEST
* Request body could not be validated as a `Seq[FunctionalError]`

#### 401 UNAUTHORIZED
* A generic authorization error occurred. The likely cause of this is an invalid or missing bearer token.

#### 403 FORBIDDEN
* User has insufficient enrolments

---

## `GET /user-answers/:lrn/expiry`

### Successful response

#### 200 OK

* A call is made to the `GET` endpoint with:
  * a valid bearer token
  * a valid `HMRC-CTC-ORG` enrolment with `EoriNumber` identifier
  * an `APIVersion` header with either:
    * `2.0` for transition rules
    * `2.1` for final rules
* A document is found in the `user-answers` collection for the given LRN (the EORI number is extracted from the enrolment)
* The response JSON provides the number of days until the document expires

### Unsuccessful responses (with possible causes)

#### 400 BAD_REQUEST
* `APIVersion` header was missing or did not align with saved answers

#### 401 UNAUTHORIZED
* A generic authorization error occurred. The likely cause of this is an invalid or missing bearer token.

#### 403 FORBIDDEN
* User has insufficient enrolments

#### 404 NOT_FOUND
* No document was found for the given LRN

#### 500 INTERNAL_SERVER_ERROR
* An error occurred in the mongo client

---

## `POST /user-answers/:lrn/copy`

### Successful response

#### 200 OK

* A call is made to the `POST` endpoint with:
  * a valid bearer token
  * a valid `HMRC-CTC-ORG` enrolment with `EoriNumber` identifier
  * a valid `String` request body containing the new LRN
  * an `APIVersion` header with either:
    * `2.0` for transition rules
    * `2.1` for final rules
* Then, a copy is made of the existing document with the new LRN

### Unsuccessful responses (with possible causes)

#### 400 BAD_REQUEST
* Request body could not be validated as a `String`
* `APIVersion` header was missing or did not align with saved answers

#### 401 UNAUTHORIZED
* A generic authorization error occurred. The likely cause of this is an invalid or missing bearer token.

#### 403 FORBIDDEN
* User has insufficient enrolments

#### 404 NOT_FOUND
* No document was found for the given LRN

#### 500 INTERNAL_SERVER_ERROR
* An error occurred in the mongo client

---

# Running the Service
* Start the service locally with `sbt run` in the root directory
* The service can be started via service manager, depending on the version of service manager you are using, with either:
  * `sm --start MANAGE_TRANSIT_MOVEMENTS_DEPARTURE_CACHE`
  * `sm2 --start MANAGE_TRANSIT_MOVEMENTS_DEPARTURE_CACHE`

# Testing the Service
* Run the unit tests by running `sbt test` in the root directory
* Run the integration tests (requires `MongoDB`) by running `sbt it/test` in the root directory

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
