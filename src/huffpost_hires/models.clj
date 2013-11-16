;; Many thanks to https://github.com/diamondap/ring-sample
(ns huffpost-hires.models
  	(:require [clojure.java.jdbc :as jdbc]

            [huffpost-hires.database :as database]
            [huffpost-hires.util :as util]))

		; responsibility	responsibility responsibility
		; 			\			|		/
		; 					listing
		; 				/		|	  \
		; 			requirement	   requirement		requirement






; Tasks are the glue between Applicants and Interviewers.  
; Each task has a relation to an Interviewer and an Applicant

;			 Stage
;			   |
; 		  Applicant ------------ Homework
; 		 /	|	\   \
; 	Task  Task  Task  Task
; 		\   |	 |	/
; 		Interviewer

; Stage:
; 	id
; 	number
; 	name

; Homework:
; 	id
; 	applicant (refers to applicant id)
; 	prompt (text)
; 	text_answer
; 	attachment_url
; 	feedback
; 	asof

; Applicant
; 	id
; 	name
; 	stage
; 	stage_last_changed (date)
; 	goalie (refers to interviewer id)
; 	position
; 	email
; 	referral
; 	notes
; 	phone
; 	resume_url
; 	asof
; 	pass
; 	completed


(defn make-table-listings
	"Create the listings table -- to appear on jobs page"
	[]
	(try (jdbc/with-connection database/db
		(jdbc/create-table :listings
						[:id :serial "PRIMARY KEY"]
						[:hiring_manager :numeric]
						[:title "varchar(200)"]
						[:description :text]
						[:homework_required :numeric] ; 0/1 boolean
						[:homework_question :text]))
	(catch Exception e (util/handle-exception "make-table-listings" e))))
(defn make-table-requirements
	"Create the requirements table -- each requirement belongs to a listing"
	[]
	(try (jdbc/with-connection database/db
		(jdbc/create-table :requirements
						[:id :serial "PRIMARY KEY"]
						[:listing :serial "references listings (id)"]
						[:title :text]))
	(catch Exception e (util/handle-exception "make-table-requirements" e))))
(defn make-table-resposibilities
	"Create the responsibilities table -- each requirement belongs to a listing"
	[]
	(try (jdbc/with-connection database/db
		(jdbc/create-table :responsibilities
						[:id :serial "PRIMARY KEY"]
						[:listing :serial "references listings (id)"]
						[:title :text]))
	(catch Exception e (util/handle-exception "make-table-resposibilities" e))))


(defn make-table-stages
	"Create the stages table -- an applicant goes through stages"
	[]
	(try (jdbc/with-connection database/db
		(jdbc/create-table :stages
						[:id :serial "PRIMARY KEY"]
						[:number :numeric]
						[:name "varchar(50)"]))
	(catch Exception e (util/handle-exception "make-table-stages" e))))

(defn init-table-stages
	[]
	(make-table-stages)
	(println "Initializing Stages Table")
		(try (jdbc/with-connection database/db
			(jdbc/insert-records :stages
				{:number 0
					:name "Pending"
				} {:number 1
					:name "Homework Sent"
				} {:number 2 
					:name "Homework Recieved"
				} {:number 3
					:name "Phone Screen Scheduled"
				} {:number 4
					:name "Phone Screen Completed"
				} {:number 5
					:name "In Person Scheduled"
				} {:number 6
					:name "In Person Completed"
				} {:number 100
					:name "Hired"}))
			(catch Exception e (util/handle-exception "init-table-stages" e))))

(defn make-table-homeworks
	"Create the Homeworks Table in database"
	[]
	(try (jdbc/with-connection database/db
		(jdbc/create-table :homeworks
			[:id :serial "PRIMARY KEY"]
			[:applicant :serial "references applicants (id)"]
			[:prompt :text]
			[:text_answer :text]
			[:attachment_url "VARCHAR(200)"]
			[:reviewer :numeric] ;; references interviewer (can't do serial because then can't be null)
			[:feedback :text]
			[:asof "date not null default CURRENT_DATE"]))
	(catch Exception e (util/handle-exception "make-table-homeworks" e))))

(defn init-table-homeworks
	[]
	(make-table-homeworks)
	(println "Initializing Homeworks Table -- BUT its empty"))

(defn make-table-applicants
	"Create the Applicants Table in our database."
	[]
	(try (jdbc/with-connection database/db
		(jdbc/create-table :applicants
						[:id :serial "PRIMARY KEY"]
						[:name "varchar(100)"]
						[:stage :numeric "default 0"]
						[:stage_last_changed "date not null default CURRENT_DATE"]
						[:goalie :numeric] ;; id of interviewer -- relationship
						[:email "varchar(50)"]
						[:position "varchar(50)"]
						[:referral "varchar(50)"]
						[:notes :text]
						[:phone "varchar(11)"]
						[:resume_url "varchar(200)"] ;; link to s3 bucket object. for now it can be a hyperlink to a googledoc
						[:asof "date not null default CURRENT_DATE"] ;; JSON date created in javascript clientside
						[:pass :numeric] ; 0/1 boolean
						[:completed :numeric])) ; 0/1 boolean
		(catch Exception e (util/handle-exception "make-table-applicants" e)))) ;; error -- return false


(defn init-table-applicants
  []
  (make-table-applicants)
  (println "Initializing Applicants Table.")
	(try (jdbc/with-connection database/db
		(jdbc/insert-records :applicants
			{:name "Alex Berke"
				:goalie 1
				:phone "12223334444"
				:email "alexandra.berke@huffingtonpost.com"
				:position "Developer"
				:referral "Alexandra Berke"
				:notes "Recent graduate"
				:resume_url ""
				:pass 1 ; 0/1 boolean
				:completed 0} ; 0/1 boolean
			{:name "Angelina Jolie"
				:goalie 2
				:phone "12223334444"
				:email "angie.jj@gmail.com"
				:position "Developer"
				:notes "Junior developer -- previously worked as a designer.  She also has a busy home life."
				:referral "Alexandra Berke"
				:resume_url ""
				:stage 4
				:pass 1
				:completed 0}
			{:name "Mila Kunis"
				:goalie 3
				:phone "12223334444"
				:email "mila.kunis@yahoo.com"
				:position "Developer"
				:notes "Recent graduate"
				:referral "Alexandra Berke"
				:resume_url ""
				:pass 1 ; 0/1 boolean
				:completed 0})) ; 0/1 boolean
		(catch Exception e (util/handle-exception "init-table-applicants" e)))) ;; error -- return false


(defn make-table-interviewers
	"Creating the Interviewers Table in our database."
	[]
	(println "Making the Interviewers Table in database.")
	(try (jdbc/with-connection database/db
		(jdbc/create-table :interviewers
						[:id :serial "PRIMARY KEY"]
						[:name "varchar(80)"]
						[:email "varchar(80)"]
						[:phone "varchar(11)"]
						[:pic_url "varchar(200)"])) ;; link to s3 bucket object. 
		(catch Exception e (util/handle-exception "make-table-interviewers" e)))) ;; error -- return false


(defn init-table-interviewers
	"Fill Interviewers table with data"
	[]
	(make-table-interviewers)
	(println "Initializing Interviewers table")
	(try (jdbc/with-connection database/db
		(jdbc/insert-records :interviewers 
			{:name "John Siragussa"
				:phone "16178348458"
				:email "alexandra.berke@huffingtonpost.com"
				:pic_url "/img/default_pic.jpg"
			} {:name "Manachem"
				:phone "16178348458"
				:email "alexandra.berke@huffingtonpost.com"
				:pic_url "/img/default_pic.jpg"
			}{:name "Amy Flintstone"
				:phone "16178348458"
				:email "alexandra.berke@huffingtonpost.com"
				:pic_url "/img/default_pic.jpg"}))
		(catch Exception e (util/handle-exception "init-table-interviewers" e)))) ;; error -- return false


(defn make-table-tasks
	"Create the Tasks Table in our databse"
	[]
	(println "Making Tasks Table in database.")
	(try (jdbc/with-connection database/db
		(jdbc/create-table :tasks
				[:id :serial "PRIMARY KEY"]
				[:applicant :serial "references applicants (id)"] ; id of applicant -- relationship
				[:interviewer :serial "references interviewers (id)"] ; id of interviewer assigned to task -- relationship
				[:title :text]
				[:description :text]
				[:feedback :text]
				[:date "varchar(180)"] ; JSON date created in javascript clientside
				[:feedback_due "varchar(180)"] ; JSON date created in javascript clientside
				[:completed :numeric] ; 0/1 boolean
				[:pass :numeric]))
		(catch Exception e (util/handle-exception "make-table-tasks" e)))) ;; error -- return false


(defn init-table-tasks
	"Fill Tasks table with dummy data"
	[]
	(make-table-tasks)
	(println "Initializing Tasks table")
	(try (jdbc/with-connection database/db
		(jdbc/insert-records :tasks
			{:applicant 1
				:interviewer 1
				:title "Resume review"
				:description "Double check she'd fit in with the operations team based on her Ruby background."
				:feedback "She is over qualified -- great internship at Huffpost!"
				:date "2013-10-22T20:02:02.920Z"
				:feedback_due "2013-10-22T20:02:02.920Z"
				:completed 1
				:pass 1}
			{:applicant 2
				:interviewer 2
				:title "Resume review"
				:description "Double check she'd fit in with the operations team based on her Ruby background."
				:feedback "She is over qualified -- great internship at Huffpost!"
				:date "2013-10-22T20:02:02.920Z"
				:feedback_due "2013-10-22T20:02:02.920Z"
				:completed 1
				:pass 1}
			{:applicant 3
				:interviewer 1
				:title "Resume review"
				:description "Double check she'd fit in with the operations team based on her Ruby background."
				:feedback ""
				:date "2013-10-22T20:02:02.920Z"
				:feedback_due "2013-10-22T20:02:02.920Z"
				:completed 0
				:pass 1}
			{:applicant 1
				:interviewer 2
				:title "Phone screen"
				:description "Double check she'd fit in with the operations team based on her Ruby background."
				:feedback "She has such a heavy accent."
				:date "2013-10-22T20:02:02.920Z"
				:feedback_due "2013-10-22T20:02:02.920Z"
				:completed 1
				:pass 1}
			{:applicant 2
				:interviewer 2
				:title "Phone screen"
				:description "Double check she'd fit in with the operations team based on her Ruby background."
				:feedback "She has such a heavy accent."
				:date "2013-10-22T20:02:02.920Z"
				:feedback_due "2013-10-22T20:02:02.920Z"
				:completed 1
				:pass 1}
			{:applicant 3
				:interviewer 3
				:title "Phone screen"
				:description "Double check she'd fit in with the operations team based on her Ruby background."
				:feedback ""
				:date "2013-10-22T20:02:02.920Z"
				:feedback_due "2013-10-22T20:02:02.920Z"
				:completed 0
				:pass 1}))
		(catch Exception e (util/handle-exception "init-table-tasks" e)))) ;; error -- return false


(defn init-tables
  "Create all of the tables in our database and fill each with dummy data."
  []
  (make-table-listings)
  (make-table-resposibilities)
  (make-table-requirements)

  (init-table-stages)
  (init-table-applicants)
  (init-table-interviewers)
  (init-table-tasks)
  (init-table-homeworks))

(defn drop-tables
	"Drop ALL of the tables"
	[]
	(try (jdbc/with-connection database/db

		(println "Dropping responsibilities table")
		(jdbc/drop-table :responsibilities)
		(println "Dropping requirements table")
		(jdbc/drop-table :requirements)
		(println "Dropping Listings table")
		(jdbc/drop-table :listings)

		;; must drop tasks table first because it depends on other tables
		(println "Dropping the stages table")	
		(jdbc/drop-table :stages)
		(println "Dropping the homeworks table")
		(jdbc/drop-table :homeworks)
		(println "Dropping the tasks table")	
		(jdbc/drop-table :tasks)
		(println "Dropping the interviewers table")	
		(jdbc/drop-table :interviewers)
		(println "Dropping the applicants table")
		(jdbc/drop-table :applicants))
		(catch Exception e (util/handle-exception "drop-tables" e)))) ;; error -- return false



;; ***************** config above ***************************************

; **************** UPDATE BELOW *********************************

(defn update-applicant
	[attribute-map]
  	(let [statement (str "UPDATE applicants "
                              "SET name='" (attribute-map :name) 
                              	"', stage="(attribute-map :stage)
                              	", goalie=" (attribute-map :goalie) 
                              	", phone='" (attribute-map :phone)
                              	"', email='" (attribute-map :email)
                              	"', position='" (attribute-map :position)
                              	"', notes='" (attribute-map :notes)
                              	"', referral='" (attribute-map :referral)
                              	"', resume_url='" (attribute-map :resume_url)
                              	"', completed=" (attribute-map :completed)
                              	", pass=" (attribute-map :pass) " "
                              "WHERE id=" (attribute-map :id))]
    	(try (database/execute-sql statement)
			(catch Exception e (util/handle-exception "update-applicant" e))))) ;; error -- return false

(defn update-homework
	[attribute-map]
	(let [statement (str "UPDATE homeworks "
		"SET applicant=" (attribute-map :applicant)
		", prompt='" (attribute-map :prompt)
		"', text_answer='" (attribute-map :text_answer)
		"', attachment_url='" (attribute-map :attachment_url)
		"', feedback='" (attribute-map :feedback)
		"', reviewer=" (attribute-map :reviewer))]
		(try (database/execute-sql statement)
			(catch Exception e (util/handle-exception "update-homework" e)))))

(defn update-interviewer
	[attribute-map]
	(let [statement (str "UPDATE interviewers "
							"SET name='" (attribute-map :name)
							"', phone='" (attribute-map :phone)
							"', email='" (attribute-map :email)
							"', pic_url='" (attribute-map :pic_url)
						"' WHERE id=" (attribute-map :id))]
		(try (database/execute-sql statement)
			(catch Exception e (util/handle-exception "update-interviewer")))))

(defn update-task
	[attribute-map]

  	(let [statement (str "UPDATE tasks "
                              "SET applicant=" (attribute-map :applicant) 
                              	", interviewer=" (attribute-map :interviewer) 
                              	", title='" (attribute-map :title)
                              	"', description='" (attribute-map :description)
                              	"', feedback='" (attribute-map :feedback)
                              	"', date='" (attribute-map :date)
                              	"', feedback_due='" (attribute-map :feedback_due)
                              	"', completed=" (attribute-map :completed)
                              	", pass=" (attribute-map :pass) " "
                              "WHERE id=" (attribute-map :id))]
    	(try (database/execute-sql statement)
			(catch Exception e (util/handle-exception "update-task" e))))) ;; error -- return false

; **************** DELETE BELOW *********************************

(defn delete-homework
	"Deletes homework with given id"
	[homework-id]
	(try (jdbc/with-connection database/db
		(jdbc/delete-rows :homeworks ["id=?" homework-id])
		true) ; success
	(catch Exception e (util/handle-exception "delete-homework" e))))

(defn delete-task
	"Deletes and task given id"
	[task-id]
  	(try (jdbc/with-connection database/db
    	(jdbc/delete-rows :tasks ["id=?" task-id])
    		true) ; success
		(catch Exception e (util/handle-exception "delete-task" e))))

(defn delete-interviewer
	"Deletes interviewer with given id
	Must first delete all their tasks"
	[interviewer-id]
  	(try (jdbc/with-connection database/db
    	(jdbc/delete-rows :tasks ["interviewer=?" interviewer-id])
    	(jdbc/delete-rows :interviewers ["id=?" interviewer-id])
    		true) ; success
		(catch Exception e (util/handle-exception "delete-interviewer" e)))) ;; error -- return false

(defn delete-applicant
	"Deletes and applicant given id
	Must first delete all their tasks since tasks reference applicant"
	[applicant-id]
  	(try (jdbc/with-connection database/db
    	(jdbc/delete-rows :tasks ["applicant=?" applicant-id])
    	(jdbc/delete-rows :applicants ["id=?" applicant-id])
    		true) ; success
		(catch Exception e (util/handle-exception "delete-applicant" e)))) ;; error -- return false

(defn delete-listing
	"Deletes listing given id
	Must first delete all related responsibilities and requirements
	Returns true on success, false on error"
	[listing-id]
	(try (jdbc/with-connection database/db
		(jdbc/delete-rows :responsibilities ["listing=?" listing-id])
		(jdbc/delete-rows :requirements ["listing=?" listing-id])
		(jdbc/delete-rows :listings ["id=?" listing-id])
		true)
	(catch Exception e (util/handle-exception "delete-listing" e))))
(defn delete-responsibility
	"Deletes responsibility given id.  Returns true on success, false on error"
	[responsibility-id]
  	(try (jdbc/with-connection database/db
    	(jdbc/delete-rows :responsibilities ["id=?" responsibility-id])
    		true) ; success
		(catch Exception e (util/handle-exception "delete-responsibility" e))))
(defn delete-requirement
	"Deletes requirement given id.  Returns true on success, false on error"
	[requirement-id]
  	(try (jdbc/with-connection database/db
    	(jdbc/delete-rows :requirements ["id=?" requirement-id])
    		true) ; success
		(catch Exception e (util/handle-exception "delete-requirement" e))))

; **************** INSERT BELOW *********************************

(defn insert-listing
	"Returns false on error"
	[attribute-map]
	(try (jdbc/with-connection database/db
		(jdbc/insert-record :listings
			{:hiring_manager (attribute-map :hiring_manager)
				:title (attribute-map :title)
				:description (attribute-map :description)
				:homework_required (attribute-map :homework_required)
				:homework_question (attribute-map :homework_question)}))
	(catch Exception e (util/handle-exception "isnert-listing" e))))
(defn insert-requirement
	"Returns false on error"
	[attribute-map]
	(try (jdbc/with-connection database/db
		(jdbc/insert-record :requirements
			{:listing (attribute-map :listing)
				:title (attribute-map :title)}))
	(catch Exception e (util/handle-exception "insert-requirement" e))))
(defn insert-responsibility
	"Returns false on error"
	[attribute-map]
	(try (jdbc/with-connection database/db
		(jdbc/insert-record :responsibilities
			{:listing (attribute-map :listing)
				:title (attribute-map :title)}))
	(catch Exception e (util/handle-exception "insert-responsibility" e))))

(defn insert-task
	"Returns false on error"
	[attribute-map]
	(try (jdbc/with-connection database/db
		(jdbc/insert-record :tasks
			{:applicant (attribute-map :applicant)
				:interviewer (attribute-map :interviewer)
				:title (attribute-map :title)
				:description (attribute-map :description)
				:date (attribute-map :date)
				:feedback (attribute-map :feedback)
				:feedback_due (attribute-map :feedback_due)
				:completed 0
				:pass 1}))
	(catch Exception e (util/handle-exception "insert-task" e))))

(defn insert-homework
	"Returns false on error"
	[attribute-map]
	(try (jdbc/with-connection database/db
		(jdbc/insert-record :homeworks
			{:applicant (attribute-map :applicant)
				:prompt (attribute-map :prompt)
				:text_answer (attribute-map :text_answer)
				:attachment_url (attribute-map :attachment_url)
				:reviewer (attribute-map :reviewer)
				:feedback (attribute-map :feedback)}))
	(catch Exception e (util/handle-exception "insert-homework" e))))

(defn insert-interviewer
	"Returns false on error"
	[attribute-map]
	(try (jdbc/with-connection database/db
		(jdbc/insert-record :interviewers
			{:name (attribute-map :name)
				:phone (attribute-map :phone)
				:email (attribute-map :email)
				:pic_url (attribute-map :pic_url)})) ; success
		(catch Exception e (util/handle-exception "insert-interviewer" e)))) ;; error -- return false


(defn insert-applicant
	"Returns false on error"
	[attribute-map]
	(try (jdbc/with-connection database/db
		(jdbc/insert-record :applicants
			{:name (attribute-map :name)
				:stage (attribute-map :stage)
				:goalie (attribute-map :goalie)
				:phone (attribute-map :phone)
				:email (attribute-map :email)
				:position (attribute-map :position)
				:notes (attribute-map :notes)
				:resume_url (attribute-map :resume_url)
				:pass 1 ; 0/1 boolean
				:completed 0})) ; success
		(catch Exception e (util/handle-exception "insert-applicant" e)))) ;; error -- return false
