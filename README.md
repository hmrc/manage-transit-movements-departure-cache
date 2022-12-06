
# manage-transit-movements-departure-cache

## Endpoints

---

## `GET /user-answers/:lrn`

### Successful response

#### 200 OK

* A call is made to the `GET` endpoint with:
  * a valid bearer token 
  * a valid `HMRC-CTC-ORG` enrolment with `EoriNumber` identifier
* A document is found in the `user-answers` collection for the given LRN (the EORI number is extracted from the enrolment)
* The response JSON has the following fields:
  * `lrn` - The local reference number associated with the departure application
  * `eoriNumber` - The EORI number linked to the user's enrolment
  * `data` - The user's answers
  * `createdAt` - The date and time that the application was started. The user has 30 days from this point to complete and submit the application
  * `lastUpdated` - The date and time that the application was last updated
  * `id` - a UUID

### Unsuccessful responses (with possible causes)

#### 401 UNAUTHORIZED
* A generic authorization error occurred. The likely cause of this is an invalid or missing bearer token.

#### 403 FORBIDDEN
* User has insufficient enrolments

#### 404 NOT_FOUND
* No documents were found for the given LRN

#### 500 INTERNAL_SERVER_ERROR
* An error occurred in the mongo client

---

##  `POST /user-answers`

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
* Request body could not be validated as a `UserAnswers'

#### 401 UNAUTHORIZED
* A generic authorization error occurred. The likely cause of this is an invalid or missing bearer token.

#### 403 FORBIDDEN
* User has insufficient enrolments
* EORI number in request body does not match the EORI number in the user's enrolment

#### 500 INTERNAL_SERVER_ERROR
* An error occurred in the mongo client

---

##  `DELETE /user-answers/:lrn`

### Successful response

#### 200 OK

* A call is made to the `POST` endpoint with:
  * a valid bearer token
  * a valid `HMRC-CTC-ORG` enrolment with `EoriNumber` identifier
* A document is deleted from the `user-answers` collection for the given LRN (the EORI number is extracted from the enrolment)

### Unsuccessful responses (with possible causes)

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
* Run the integration tests (requires `MongoDB`) by running `sbt IntegrationTest/test` in the root directory

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").