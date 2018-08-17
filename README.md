# sampleapi

A clojure project demonstrating an HTTP API, CLI, and shared code for a simple people database kept in-memory.

## Requirements

SampleAPI should allow input of delimited text files and then sort said files a number of ways:

1. by gender (asc), last name (asc)
2. by birth date (asc)
3. by last name (desc)

Supported delimiters include `,`(comma), `|`(pipe), and ` `(space). Escaping is not explicitly supported. Delimiters should not appear inside data rows except as delimiters. 

Delimited text files must include a header containing the following columns: 

 - `LastName`
 - `FirstName`
 - `Gender`
 - `FavoriteColor`
 - `DateOfBirth`
 
Column order doesn't matter, but must be consistent inside the same file. 

SampleAPI should work both as a command-line application (passing in a delimited file) and as an http service where delimited data is uploaded.

The following routes are supported:

```http
POST /records
GET /records/gender
GET /records/birthdate
GET /records/name
```

See the Swagger API Console at `http://localhost:8080/` (when the application is running) for more details.

## Usage

### CLI

```bash

$ lein run -m sampleapi.cli resources/test.csv

```

### HTTP Service

#### Starting:

```bash

$ SAMPLEAPI_HTTP_HOST=localhost SAMPLEAPI_HTTP_IP=8080 lein run -m sampleapi.web

```

#### Using:

```bash

# Load some data
$ curl -v -XPOST 'http://localhost:8080/records' -H 'Content-Type: text/pipe-separated-values' --data-binary @resources/test.psv 
$ curl -v -XPOST 'http://localhost:8080/records' -H 'Content-Type: text/space-separated-values' --data-binary @resources/test.ssv 
$ curl -v -XPOST 'http://localhost:8080/records' -H 'Content-Type: text/csv' --data-binary @resources/test.csv 
# Query the data
$ curl -v -XGET  'http://localhost:8080/records/gender'
$ curl -v -XGET  'http://localhost:8080/records/birthdate'
$ curl -v -XGET  'http://localhost:8080/records/name'

```

## Design

The data is modeled in spec and found in `sampleapi/models.clj`. `sampleapi/core.clj` handles common data manipulation and loading into the in-memory database (a clojure atom containing a vector). `sampleapi/settings.clj` defines the settings needed for sampleapi along with specs and defaults. `sampleapi/web.clj` defines the HTTP routes and provides a -main method for running a web server. `sampleapi/cli.clj` defines the command line interface with a -main method for loading a delimited text file and then sorting it.

SampleAPI uses clojure.spec.alpha for data specification and compojure-api for an HTTP route dsl. 
clojure.data.csv is used to parse delimited data. JodaTime (clj-time) is used for date manipulation. spec.settings is a library for settings declaration and validation that I wrote and published for this exercise inspired by a proprietary design I previously wrote utilizing Prismatic's Schema library. 

Sample data can be found in `resources/`. It was generated using clojure.spec with code found inside a comment block in `sampleapi/models.clj`. 

Overall, I'm happy with the application as a small sample. CSV uploading with compojure-api feels like a hack, but it's still better than how you'd write similar code in, say, Python+Flask. This was my first experience with clojure.spec after many years of using Schema and it was pleasant. 

If you have any questions, please reach out to me. I'm `bja` on the Clojurians slack, `emidln` on reddit, and my email is `emidln@gmail.com`.

## Future Plans

There are three outstanding areas I'd like to fix. I hope to get some time to flesh these out as this is a demo repo.

1. Write some more tests. The HTTP API could use some exercising. The output formats are currently partials, but some property testing might be in order.
2. Figure out how to convey a CSV body for a POST in Swagger. It wasn't immediately obvious to me how I should document this with Swagger/OpenAPI. 
3. Allow selecting which output a user wishes for the CLI (instead of just showing them all).
4. Handle failures in the API more gracefully and semantically (return 400s when appropriate for POST /records)

## License

Copyright © 2018 Brandon Joseph Adams <emidln@gmail.com>

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
