# callerid

Caller ID API Service.

This service responds to requests for caller ID information. Presently, adding or retrieving Caller ID info is supported.
The API listens on port 8080 by default, and can be overridden with the `PORT` environment variable.


**Note that from the seed data, numbers in (XXX) XXX XXXX get converted to E.164 format of +1XXXXXXXXXX.**

## Endpoints

### GET /query

Fetches caller id information.

**Params:**
*number* - the number we want caller id information for.

**Example:**
GET http://localhost:8080/query?number=%2B15556789090

**Curl Example:**

    curl -H 'Content-Type: application/json; charset=utf-8' http://localhost:8080/query?number=%2B15556789090

**Response:**

```
{results: [{ “name”: “Bob Barker”, “number”: “+15556789090”, “context”: “personal”}]}
```

**Reponse Codes:**
* *200* - Successfull query
* *404* - Phone Number Not Found


### POST /number

Adds caller id data to the service.

**POST Body:**
* *name*    - contact name
* *number*  - the number in E.164 format
* *context* - the context for the phone number.

Note: A phone number may be present multiple times, but can only appear once per context. In other words you can think of a <number,context> pair as unique.

The project spec didn't specify that the POST body must be a JSON string so we're doing a standard HTTP POST body here.

**Curl Example:**

    curl -d "name=Donald%20Knuth&number=+15555551212&context=personal" http://localhost:8080/number

**Response Codes:**
* *200* - Success: The caller ID info was successfully entered
* *409* - Conflict: A record with this number and context already exists
* *400* - Bad Request: A required parameter is missing 

## Running Tests

In order to run tests, leiningen must be installed, then run:

    lein test

## Running

### With leiningen

To start a web server for the application, run:

    lein run
    
To force the application to listen on an alternate port:

    PORT=8081 lein run

### Standalone

From the root of the project, run:

    lein uberjar
    ./bin/startup.sh

With an alternate port:

    PORT=8081 ./bin/startup.sh

## License

Copyright © 2017 Jason Stewart
