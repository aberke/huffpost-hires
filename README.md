huffpost-hires
==============

Hiring is hard ...but is Clojure?

Description
----
Wants to be more than a glorified spreadsheet.

Running Locally
----

* Download jvm and leiningen so that you can call ```lein```
* clone the repo
* Install postgres and create a table called huffpost_hires that is accessible from the url ```postgresql://localhost:5432/huffpost_hires```
* Within the root folder of huffpost-hires setup the environment ```$ source env.sh```
* Start up the REPL ```$ lein repl```
* Build the tables

```
user=> (use 'huffpost-hires.models)
user=> (huffpost-hires.models/init-tables)
```

* Import and run the server locally all in one call ```user=> (use 'huffpost-hires.web)```
* Visit <http://0.0.0.0:5000>