# mobile-messages

[![Build Status](https://travis-ci.org/hmrc/mobile-messages.svg?branch=master)](https://travis-ci.org/hmrc/customer-profile) [ ![Download](https://api.bintray.com/packages/hmrc/releases/mobile-messages/images/download.svg) ](https://bintray.com/hmrc/releases/mobile-messages/_latestVersion)

Digital messages for a UK tax customer that has opted-in to paperless communications


Requirements
------------

The following services are exposed from the micro-service.

Please note it is mandatory to supply an Accept HTTP header to all below services with the value ```application/vnd.hmrc.1.0+json```.


API
---

| *Task* | *Supported Methods* | *Description* |
|--------|----|----|
| ```/messages``` | GET | Returns all the user's digital messages. [More...](docs/messages.md)  |
| ```/messages/read``` | POST | Returns a specific user message as an HTML partial. [More...](docs/read-message.md)  |


# Sandbox
All the above endpoints are accessible on sandbox with `/sandbox` prefix on each endpoint, e.g.
```
    GET /sandbox/messages
```

To trigger the sandbox endpoints locally, use the "X-MOBILE-USER-ID" header with one of the following values:
208606423740 or 167927702220

To test different scenarios, add a header "SANDBOX-CONTROL" with one of the following values:

| *Value* | *Description* |
|--------|----|
| "NEW-TAX-STATEMENT" | Happy path, /messages/read ONLY, New Tax Statement |
| "ANNUAL-TAX-SUMMARY" | Happy path, /messages/read ONLY, Annual Tax Summary |
| "STOPPING-SA" | Happy path, /messages/read ONLY, Stopping Self Assessment |
| "OVERDUE-PAYMENT" | Happy path, /messages/read ONLY, Overdue Payment |
| "ERROR-401" | Unhappy path, trigger a 401 Unauthorized response |
| "ERROR-403" | Unhappy path, trigger a 403 Forbidden response |
| "ERROR-500" | Unhappy path, trigger a 500 Internal Server Error response |
| Not set or any other value | Happy path, non-excluded Tax Credits Users |

# Version
Version of API need to be provided in `Accept` request header
```
Accept: application/vnd.hmrc.v1.0+json
```


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")

