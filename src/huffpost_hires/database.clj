;; Many thanks to https://github.com/diamondap/ring-sample
(ns huffpost-hires.database
  	(:require [cheshire.core :as json]
            [clojure.java.jdbc :as jdbc]

            [huffpost-hires.util :as util]))

(def db (if (System/getenv "DEVELOPMENT")
			(System/getenv "DATABASE_URL")
			(System/getenv "HEROKU_POSTGRESQL_MAROON_URL")))

;; ***************************************************************

(defn execute-sql
  "Executes a sql statement. Param statement is a sql string and the following args
   are seqs of parameters to be bound to the sql statement."
  [statement & params]
  (try
    (jdbc/with-connection db
      (jdbc/do-prepared statement params))
    (catch Throwable t (prn statement) (throw t))))

(defn query
	"Executes a query. Returns a vector of results. Each item in the vector
	is a hash, keyed by column name. Param sql must be a vector. To execute
	a simple SQL string, pass in a vector containing only the string. To
	execute a query or statement with params, the sql string should come first,
	followed by the params to be bound."
	[sql]
	(println (str "Executing sql query: " sql))
	(try
		(jdbc/with-connection db
			(jdbc/with-query-results rs sql
				(into [] rs)))
		(catch Throwable t (prn sql) (throw t))))


(defn query-json
	"Executes a query and returns the result as json. Param sql should be a
	vector with [sql-string params...] or just [sql-string]."
	[statement]
		(json/generate-string (query statement)))
