;; Many thanks to https://github.com/diamondap/ring-sample
(ns huffpost-hires.models
  (:require [cheshire.core :as json]
            [clojure.java.jdbc :as jdbc]))

(def db (if (System/getenv "DEVELOPMENT")
			(System/getenv "DATABASE_URL")
			(System/getenv "HEROKU_POSTGRESQL_MAROON_URL")))

; Tasks are the glue between Applicants and Interviewers.  
; Each task has a relation to an Interviewer and an Applicant

; 		  Applicant
; 		 /	|	\   \
; 	Task  Task  Task  Task
; 		\   |	 |	/
; 		Interviewer



(defn make-table-applicants
	"Create the Applicants Table in our database."
	[]
	(try (jdbc/with-connection db
		(jdbc/create-table :applicants
						[:id :integer]
						[:name :string]
						[:goalie :integer] ;; id of interviewer -- relationship
						[:email :string]
						[:role :string]
						[:phone :string]
						[:resume :string] ;; for now it can be a hyperlink to a googledoc
						[:asof :string] ;; JSON date created in javascript clientside
						[:pass :integer] ; 0/1 boolean
						[:completed :integer])) ; 0/1 boolean
		(catch Exception e 
			(println (str "EXCEPTION in make-table-applicants: " e)))))

(defn init-table-applicants
  []
  (make-table-applicants)
  (println "Initializing Applicants Table.")
	(jdbc/with-connection db
		(jdbc/insert-records :applicants
			{:id 1 
				:name "Alex Berke"
				:goalie 1
				:phone "12223334444"
				:email "alexandra.berke@huffingtonpost.com"
				:role "Developer"
				:resume "www.google.com"
				:asof "2013-10-22T20:02:02.920Z"
				:pass 1 ; 0/1 boolean
				:completed 0} ; 0/1 boolean
			{:id 2 
				:name "Angelina Jolie"
				:goalie 2
				:phone "12223334444"
				:email "alexandra.berke@huffingtonpost.com"
				:role "Developer"
				:resume "www.google.com"
				:asof "2013-10-22T20:02:02.920Z"
				:pass 1
				:completed 0}
			{:id 3 
				:name "Mila Kunis"
				:goalie 3
				:phone "12223334444"
				:email "alexandra.berke@huffingtonpost.com"
				:role "Developer"
				:resume "www.google.com"
				:asof "2013-10-22T20:02:02.920Z"
				:pass 1 ; 0/1 boolean
				:completed 0}))) ; 0/1 boolean

(defn make-table-interviewers
	"Creating the Interviewers Table in our database."
	[]
	(println "Making the Interviewers Table in database.")
	(try (jdbc/with-connection db
		(jdbc/create-table :interviewers
						[:id :integer]
						[:name :string]
						[:email :string]
						[:phone :string]))
		(catch Exception e 
			(println (str "EXCEPTION in make-table-interviewers: " e)))))

(defn init-table-interviewers
	"Fill Interviewers table with data"
	[]
	(make-table-interviewers)
	(println "Initializing Interviewers table")
	(jdbc/with-connection db
		(jdbc/insert-records :interviewers
			{:id 1 
				:name "Fred Flintstone"
				:phone "12223334444"
				:email "alexandra.berke@huffingtonpost.com"}
			{:id 2 
				:name "Alice Flintstone"
				:phone "12223334444"
				:email "alexandra.berke@huffingtonpost.com"}
			{:id 3 
				:name "Amy Flintstone"
				:phone "12223334444"
				:email "alexandra.berke@huffingtonpost.com"})))

(defn make-table-tasks
	"Create the Tasks Table in our databse"
	[]
	(println "Making Tasks Table in database.")
	(try (jdbc/with-connection db
		(jdbc/create-table :tasks
				[:id :integer]
				[:applicant :integer] ; id of applicant -- relationship
				[:interviewer :integer] ; id of interviewer assigned to task -- relationship
				[:title :string]
				[:feedback :string]
				[:date :string] ; JSON date created in javascript clientside
				[:feedback_due :string] ; JSON date created in javascript clientside
				[:completed :integer] ; 0/1 boolean
				[:pass :integer])))) ; 0/1 boolean

(defn init-table-tasks
	"Fill Tasks table with dummy data"
	[]
	(make-table-tasks)
	(println "Initializing Tasks table")
	(jdbc/with-connection db
		(jdbc/insert-records :tasks
			{:id 1
				:applicant 1
				:interviewer 1
				:title "Resume review"
				:feedback "She is over qualified -- great internship at Huffpost!"
				:date "2013-10-22T20:02:02.920Z"
				:feedback_due "2013-10-22T20:02:02.920Z"
				:completed 1
				:pass 1}
			{:id 2
				:applicant 2
				:interviewer 2
				:title "Resume review"
				:feedback "She is over qualified -- great internship at Huffpost!"
				:date "2013-10-22T20:02:02.920Z"
				:feedback_due "2013-10-22T20:02:02.920Z"
				:completed 1
				:pass 1}
			{:id 3
				:applicant 3
				:interviewer 1
				:title "Resume review"
				:feedback ""
				:date "2013-10-22T20:02:02.920Z"
				:feedback_due "2013-10-22T20:02:02.920Z"
				:completed 0
				:pass 1}
			{:id 4
				:applicant 1
				:interviewer 2
				:title "Phone screen"
				:feedback "She has such a heavy accent."
				:date "2013-10-22T20:02:02.920Z"
				:feedback_due "2013-10-22T20:02:02.920Z"
				:completed 1
				:pass 1}
			{:id 4
				:applicant 2
				:interviewer 2
				:title "Phone screen"
				:feedback "She has such a heavy accent."
				:date "2013-10-22T20:02:02.920Z"
				:feedback_due "2013-10-22T20:02:02.920Z"
				:completed 1
				:pass 1}
			{:id 4
				:applicant 3
				:interviewer 3
				:title "Phone screen"
				:feedback ""
				:date "2013-10-22T20:02:02.920Z"
				:feedback_due "2013-10-22T20:02:02.920Z"
				:completed 0
				:pass 1})))

(defn init-tables
  "Create all of the tables in our database and fill each with dummy data."
  []
  (init-table-applicants)
  (init-table-interviewers)
  (init-table-tasks))

(defn drop-tables
	"Drop ALL of the tables"
	[]
	(try (jdbc/with-connection db
		(println "Dropping the applicants table")
		(jdbc/drop-table :applicants)
		(println "Dropping the interviewers table")	
		(jdbc/drop-table :interviewers)
		(println "Dropping the tasks table")	
		(jdbc/drop-table :tasks))
		(catch Exception e (println (str "EXCEPTION in drop-tables: " e)))))

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
