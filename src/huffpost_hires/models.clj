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
	(println (str "db: " db))
	(try (jdbc/with-connection db
		(println "0")
		(jdbc/create-table :applicants
						[:id :serial "PRIMARY KEY"]
						[:name "varchar(100)"]
						[:goalie :numeric] ;; id of interviewer -- relationship
						[:email "varchar(50)"]
						[:position "varchar(50)"]
						[:phone "varchar(11)"]
						[:resume "varchar(180)"] ;; for now it can be a hyperlink to a googledoc
						[:asof "date not null default CURRENT_DATE"] ;; JSON date created in javascript clientside
						[:pass :numeric] ; 0/1 boolean
						[:completed :numeric])) ; 0/1 boolean
		(catch Exception e 
			(println (str "EXCEPTION in make-table-applicants: " e))
			(.printStackTrace (.getCause e)))))

(defn init-table-applicants
  []
  (make-table-applicants)
  (println "Initializing Applicants Table.")
	(try (jdbc/with-connection db
		(println (jdbc/insert-records :applicants
			{:name "Alex Berke"
				:goalie 1
				:phone "12223334444"
				:email "alexandra.berke@huffingtonpost.com"
				:position "Developer"
				:resume "www.google.com"
				:pass 1 ; 0/1 boolean
				:completed 0} ; 0/1 boolean
			{:name "Angelina Jolie"
				:goalie 2
				:phone "12223334444"
				:email "alexandra.berke@huffingtonpost.com"
				:position "Developer"
				:resume "www.google.com"
				:pass 1
				:completed 0}
			{:name "Mila Kunis"
				:goalie 3
				:phone "12223334444"
				:email "alexandra.berke@huffingtonpost.com"
				:position "Developer"
				:resume "www.google.com"
				:pass 1 ; 0/1 boolean
				:completed 0}))) ; 0/1 boolean
		(catch Exception e
			(println (str "EXCEPTION in init-table-applicants: " e))
			(.printStackTrace (.getCause e))
			false))) ;; error -- return false

(defn make-table-interviewers
	"Creating the Interviewers Table in our database."
	[]
	(println "Making the Interviewers Table in database.")
	(try (jdbc/with-connection db
		(jdbc/create-table :interviewers
						[:id :serial "PRIMARY KEY"]
						[:name "varchar(80)"]
						[:email "varchar(80)"]
						[:phone "varchar(11)"]))
		(catch Exception e 
			(println (str "EXCEPTION in make-table-interviewers: " e))
			(.printStackTrace (.getCause e)))))

(defn init-table-interviewers
	"Fill Interviewers table with data"
	[]
	(make-table-interviewers)
	(println "Initializing Interviewers table")
	(try (jdbc/with-connection db
		(jdbc/insert-records :interviewers
			{:name "Fred Flintstone"
				:phone "12223334444"
				:email "alexandra.berke@huffingtonpost.com"}
			{:name "Alice Flintstone"
				:phone "12223334444"
				:email "alexandra.berke@huffingtonpost.com"}
			{:name "Amy Flintstone"
				:phone "12223334444"
				:email "alexandra.berke@huffingtonpost.com"}))
		(catch Exception e
			(println (str "EXCEPTION in init-table-interviewers: " e))
			(.printStackTrace (.getCause e)))))

(defn make-table-tasks
	"Create the Tasks Table in our databse"
	[]
	(println "Making Tasks Table in database.")
	(try (jdbc/with-connection db
		(jdbc/create-table :tasks
				[:id :serial "PRIMARY KEY"]
				[:applicant :serial "references applicants (id)"] ; id of applicant -- relationship
				[:interviewer :serial "references interviewers (id)"] ; id of interviewer assigned to task -- relationship
				[:title :text]
				[:feedback :text]
				[:date "varchar(180)"] ; JSON date created in javascript clientside
				[:feedback_due "varchar(180)"] ; JSON date created in javascript clientside
				[:completed :numeric] ; 0/1 boolean
				[:pass :numeric]))
		(catch Exception e 
			(println (str "EXCEPTION in make-table-tasks: " e))
			(.printStackTrace (.getCause e))))) ; 0/1 boolean

(defn init-table-tasks
	"Fill Tasks table with dummy data"
	[]
	(make-table-tasks)
	(println "Initializing Tasks table")
	(try (jdbc/with-connection db
		(jdbc/insert-records :tasks
			{:applicant 1
				:interviewer 1
				:title "Resume review"
				:feedback "She is over qualified -- great internship at Huffpost!"
				:date "2013-10-22T20:02:02.920Z"
				:feedback_due "2013-10-22T20:02:02.920Z"
				:completed 1
				:pass 1}
			{:applicant 2
				:interviewer 2
				:title "Resume review"
				:feedback "She is over qualified -- great internship at Huffpost!"
				:date "2013-10-22T20:02:02.920Z"
				:feedback_due "2013-10-22T20:02:02.920Z"
				:completed 1
				:pass 1}
			{:applicant 3
				:interviewer 1
				:title "Resume review"
				:feedback ""
				:date "2013-10-22T20:02:02.920Z"
				:feedback_due "2013-10-22T20:02:02.920Z"
				:completed 0
				:pass 1}
			{:applicant 1
				:interviewer 2
				:title "Phone screen"
				:feedback "She has such a heavy accent."
				:date "2013-10-22T20:02:02.920Z"
				:feedback_due "2013-10-22T20:02:02.920Z"
				:completed 1
				:pass 1}
			{:applicant 2
				:interviewer 2
				:title "Phone screen"
				:feedback "She has such a heavy accent."
				:date "2013-10-22T20:02:02.920Z"
				:feedback_due "2013-10-22T20:02:02.920Z"
				:completed 1
				:pass 1}
			{:applicant 3
				:interviewer 3
				:title "Phone screen"
				:feedback ""
				:date "2013-10-22T20:02:02.920Z"
				:feedback_due "2013-10-22T20:02:02.920Z"
				:completed 0
				:pass 1}))
		(catch Exception e
			(println (str "EXCEPTION in init-table-tasks: " e))
			(.printStackTrace (.getCause e)))))

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
		;; must drop tasks table first because it depends on other tables
		(println "Dropping the tasks table")	
		(jdbc/drop-table :tasks)
		(println "Dropping the interviewers table")	
		(jdbc/drop-table :interviewers)
		(println "Dropping the applicants table")
		(jdbc/drop-table :applicants))
		(catch Exception e (println (str "EXCEPTION in drop-tables: " e
			(println (str "EXCEPTION in init-table-tasks: " e))
			(.printStackTrace (.getCause e)))))))


;; ***************** config above ***************************************

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

; **************** UPDATE BELOW *********************************

(defn update-applicant
	"This method updates an applicant entry"
	[attribute-map]

  	(let [statement (str "UPDATE applicants "
                              "SET name='" (attribute-map :name) 
                              	"', goalie=" (attribute-map :goalie) 
                              	", phone='" (attribute-map :phone)
                              	"', email='" (attribute-map :email)
                              	"', position='" (attribute-map :position)
                              	"', resume='" (attribute-map :resume)
                              	"', completed=" (attribute-map :completed)
                              	", pass=" (attribute-map :pass) " "
                              "WHERE id=" (attribute-map :id))]
    	(try (execute-sql statement)
			(catch Exception e
				(println (str "EXCEPTION in udpate-applicant: " e))
				(.printStackTrace (.getCause e))
				false)))) ;; error -- return false

; **************** DELETE BELOW *********************************

(defn delete-task
	"Deletes and task given id"
	[task-id]
  	(try (jdbc/with-connection db
    	(jdbc/delete-rows :tasks ["id=?" task-id])
    		true) ; success
		(catch Exception e
			(println (str "EXCEPTION in delete-task: " e))
			(.printStackTrace (.getCause e))
			false))) ;; error -- return false

(defn delete-interviewer
	"Deletes interviewer with given id
	Must first delete all their tasks"
	[interviewer-id]
  	(try (jdbc/with-connection db
    	(jdbc/delete-rows :tasks ["interviewer=?" interviewer-id])
    	(jdbc/delete-rows :interviewers ["id=?" interviewer-id])
    		true) ; success
		(catch Exception e
			(println (str "EXCEPTION in delete-interviewer: " e))
			(.printStackTrace (.getCause e))
			false))) ;; error -- return false

(defn delete-applicant
	"Deletes and applicant given id
	Must first delete all their tasks"
	[applicant-id]
  	(try (jdbc/with-connection db
    	(jdbc/delete-rows :tasks ["applicant=?" applicant-id])
    	(jdbc/delete-rows :applicants ["id=?" applicant-id])
    		true) ; success
		(catch Exception e
			(println (str "EXCEPTION in delete-applicant: " e))
			(.printStackTrace (.getCause e))
			false))) ;; error -- return false

; **************** INSERT BELOW *********************************

(defn insert-applicant
	[attribute-map]
	(try (jdbc/with-connection db
		(jdbc/insert-record :applicants
			{:name (attribute-map :name)
				:goalie (attribute-map :goalie)
				:phone (attribute-map :phone)
				:email (attribute-map :email)
				:position (attribute-map :position)
				:resume (attribute-map :resume)
				:pass 1 ; 0/1 boolean
				:completed 0}) ; 0/1 boolean)
				true) ; success
		(catch Exception e
			(println (str "EXCEPTION in insert-applicant: " e))
			(.printStackTrace (.getCause e))
			false))) ;; error -- return false

