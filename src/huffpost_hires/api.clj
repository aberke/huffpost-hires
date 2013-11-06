;; Handles routing api requests

(ns huffpost-hires.api
	(require [huffpost-hires.models :as models]
		[huffpost-hires.util :as util]

        [aws.sdk.s3 :as s3]
        [clojure.java.io :as io]
        [clojure.string :as string]
		[cheshire.core :as json])

	(:import [java.io File]))

;; ********************* HELPERS **********************


(def s3-credentials {:access-key (System/getenv "AWS_ACCESS_KEY_ID"), :secret-key (System/getenv "AWS_SECRET_ACCESS_KEY")})


(defn params->applicants-attributeMap
	[params]
	(println "params->applicants-attributeMap -- params: " params)
	{:id (util/string->number (get params "id"))
		:name (util/string->sql-safe (get params "name" ""))
		:stage (util/string->number-or-0 (get params "stage" 0))
		:goalie (util/string->number-or-0 (get params "goalie"))
		:phone (get params "phone" "")
		:email (get params "email" "")
		:position (util/string->sql-safe (get params "position" ""))
		:notes (util/string->sql-safe (get params "notes" ""))
		:referral (util/string->sql-safe (get params "referral" ""))
		:resume_url (get params "resume_url" "")
		:completed (util/string->number (get params "completed" 0))
		:pass (util/string->number (get params "pass" 1))})

(defn params->tasks-attributeMap
	[params]
	(println "params->tasks-attributeMap -- params: " params)
	{:id (util/string->number (get params :id))
		:applicant (util/string->number-or-0 (get params :applicant))
		:interviewer (util/string->number-or-0 (get params :interviewer))
		:title (util/string->sql-safe (get params :title ""))
		:description (util/string->sql-safe (get params :description ""))
		:feedback (util/string->sql-safe (get params :feedback ""))
		:date (get params :date "")
		:pic_url (get params :pic_url "/img/default.jpg")
		:feedback_due (get params :feedback_due "")
		:completed (util/string->number-or-0 (get params :completed))
		:pass (util/string->number (get params :pass 1))})

(defn params->interviewers-attributeMap
	[params]
	(println "params->interviewers-attributeMap -- params: " params)
	{:id (util/string->number (get params :id))
		:name (get params :name "")
		:phone (get params :phone "")
		:email (get params :email "")})


;; ******************************* GET requests below ******************************

;; GET /api/stage/all
(defn get-stages-all
	"Returns all stages"
	[params]
	(models/query-json ["SELECT * FROM stages ORDER BY number"]))

;; GET /api/stage/applicants?stage=number&pass=0/1&[goalie=inteviewerID]
(defn get-applicants-by-stage
	"Returns all applicants for given stage and passing status
	Defaults to stage=0 (pending) and pass=1 (passing true)"
	[params]
	(if (params :goalie) 
		(models/query-json [(str "SELECT * FROM applicants 
							WHERE stage=" (get params :stage 0)
							" AND pass=" (get params :pass 1)
							" AND goalie=" (params :goalie))])
		(models/query-json [(str "SELECT * FROM applicants 
							WHERE stage=" (get params :stage 0)
							" AND pass=" (get params :pass 1))])))

;; GET /api/interviewer/all?extra-data=true/false
(defn get-interviewers-all
	"Returns All Interviewers in Database in json 
	joined with the number of their incomplete tasks and number of current applicants for whom they're the goalie for"
	[params]
	(println (str "GET api/interviewers-all with params:" params))
	(if (= (params :extra-data) "true")
		(models/query-json ["SELECT i.*, 
								SUM(CASE WHEN t.completed=0 THEN 1 ELSE 0 END) AS incomplete_tasks,
								SUM(CASE WHEN a.completed=0 THEN 1 ELSE 0 END) AS current_applicants
								FROM interviewers i 
								LEFT JOIN applicants a on i.id=a.goalie
								LEFT JOIN tasks t ON i.id=t.interviewer 
								GROUP BY i.id;"])
		(models/query-json ["select * from interviewers order by name"])))


;; GET /api/applicant/rejected
(defn get-applicants-rejected
	"Returns all rejected applicants, ordered by asof date"
	[params]
	(models/query-json ["SELECT * FROM applicants WHERE pass=0 ORDER BY asof"]))

;; GET /api/applicant/all?task-count=true/false
(defn get-applicants-all
	"Returns All Interviewers in Database in json 
	optionally adds count of complete_tasks and incomplete_tasks.
	Dope ass SQL queries courtesy of Mike Adler"
	[params]

	(if (= (params :task-count) "true")
		(models/query-json ["SELECT a.*, 
								SUM(CASE WHEN t.completed=1 THEN 1 ELSE 0 END) AS complete_tasks, 
								SUM(CASE WHEN t.completed=0 THEN 1 ELSE 0 END) AS incomplete_tasks,
								COUNT(t.completed) AS total_tasks 
								FROM applicants a 
								LEFT JOIN tasks t ON a.id=t.applicant 
								GROUP BY a.id;"])
		(models/query-json ["select * from applicants order by asof"])))


;; GET /api/applicant/?id='applicantID'
(defn get-applicant
	"Returns specified applicant"
	[request]
	(println (str "api/appicant with request:" request))
	(models/query-json [(str "SELECT * FROM applicants WHERE id=" (request :id))]))

;; GET /api/interviewer/?id='interviewerID'
(defn get-interviewer
	"Returns specified interviewer"
	[params]
	(println (str "api/interviewer with params:" params))
	(models/query-json [(str "SELECT * FROM interviewers WHERE id=" (params :id))]))

;; /api/applicant/tasks?id='applicantID'
(defn tasks-by-applicant
	"Returns all tasks for applicant"
	[params]
	(models/query-json [(str "select * from tasks WHERE applicant=" (params :id))]))

;; /api/applicant/complete-tasks?id='applicantID'
(defn complete-tasks-by-applicant
	"Returns all completed tasks for applicant"
	[params]
	(models/query-json [(str "select * from tasks WHERE applicant=" (params :id) " AND completed=1")]))

;; /api/interviewer/incomplete-tasks?id='applicantID'
(defn incomplete-tasks-by-applicant
	[params]
	(models/query-json [(str "select * from tasks WHERE applicant=" (params :id) " AND completed=0")]))

;; /api/interviewer/tasks?id='applicantID'
(defn tasks-by-interviewer
	[params]
	(models/query-json [(str "select * from tasks WHERE interviewer=" (params :id))]))

;; /api/interviewer/complete-tasks?id='interviewID'
(defn complete-tasks-by-interviewer
	[params]
	(models/query-json [(str "select * from tasks where interviewer=" (params :id) " AND completed=1")]))

;; /api/interviewer/incomplete-tasks?id='interviewID'
(defn incomplete-tasks-by-interviewer
	"Returns all completed tasks for interviewer"
	[params]
	(models/query-json [(str "select * from tasks where interviewer=" (params :id) " AND completed=0")]))

(defn handle-get-request-stage
	"Routing helper for handle-get-request:
	Called upon GET request to url /api/stage/*"
	[route params]
	(println (str "api/handle-get-request-stage with route: " route "; params: " params))
	(case route
		"all" (get-stages-all params)
		"applicants" (get-applicants-by-stage params)
		(str "Invalid api request to /api/stage/" route)))

(defn handle-get-request-applicant
	"routing helper for handle-get-request:
	Called upon GET request to url /api/applicant/*"
	[route params] ; route == * in the GET request
	(println (str "api/handle-get-request-applicant with route: " route "; params: " params))
	(case route
		"" (get-applicant params)
		"all" (get-applicants-all params)
		"rejected" (get-applicants-rejected params)
		"tasks" (tasks-by-applicant params)
		"complete-tasks" (complete-tasks-by-applicant params)
		"incomplete-tasks" (incomplete-tasks-by-applicant params)
		(str "Invalid api request to /api/applicant/" route)))


(defn handle-get-request-interviewer
	"routing helper for handle-get-request:
	Called upon GET request to url /api/interviewer/*"
	[route params] ; route == * in the GET request

	(case route
		"" (get-interviewer params)
		"all" (get-interviewers-all params)
		"tasks" (tasks-by-interviewer params)
		"complete-tasks" (complete-tasks-by-interviewer params)
		"incomplete-tasks" (incomplete-tasks-by-interviewer params)
		(str "Invalid api request to /api/interviewer/" route)))


(defn handle-get-request
	"Called by web upon GET request to url /api/*/*"
	[request]
	(println "***************** GET *****")
	(println request)

	(let [params (request :params) route (params :*) route-prefix (first route) route-suffix (second route)]
		(println (str "params: " params))
		(println (str "route:" route))
		(case route-prefix
			"stage" (handle-get-request-stage route-suffix params)
			"applicant" (handle-get-request-applicant route-suffix params)
			"interviewer" (handle-get-request-interviewer route-suffix params)
			"Invalid api request")))

;; ******************************* GET requests above ******************************

;; ******************************* POST requests below ******************************

;; POST /api/interviewer
(defn post-interviewer-new
	[params]
	(if (models/insert-interviewer (params->interviewers-attributeMap params))
		"OK"
		"ERROR"))

;; POST /api/applicant
(defn post-applicant-new
	[params]
	(if (models/insert-applicant (params->applicants-attributeMap params))
		"OK"
		"ERROR"))

;; POST /api/task
(defn post-task-new
	[params]
	(if (models/insert-task (params->tasks-attributeMap params))
		"OK"
		"ERROR"))

(defn handle-post-request
	[request]
	(println "**************** API POST *******************")
	(let [params (request :params) route (params :*)]
		(println (str "params: " params))
		(println (str "route: " route))
		(case route
			"applicant" (post-applicant-new params)
			"interviewer" (post-interviewer-new params)
			"task" (post-task-new params)
			"Invalid POST request")))

;; ******************************* PUT requests below ******************************

;; helper to put-appliant to upload resume and then return map {'resume_url' resume_url}  **IF** the resume is in params 
(defn upload-resume
	"If resume was included in params, uploads resume to s3 and returns map {'resume_url' resume_url}
	Otherwise returns empty map"
	[params]
	(if (get params "resume")
		;; then: parse out file data and upload to s3
		(let [file (params "resume")
	        file-name (file :filename)
	        file-size (file :size)
	        content-type (file :content-type)
	        actual-file (file :tempfile)
	        object-key (string/replace (str "interviewer/pic/" file-name) " " "-")
	        resume-url (str "https://s3.amazonaws.com/" (System/getenv "S3_BUCKET_NAME") "/" object-key)]
	        ;; upload to our s3 bucket
	        (s3/put-object s3-credentials 
        					(System/getenv "S3_BUCKET_NAME") 
        					object-key 
        					actual-file
        					{:content-type content-type :content-length file-size})
			{"resume_url" resume-url})
		;; else: return empty map
		{})) 


;; PUT /api/applicant
(defn put-applicant
	"If a resume was included in the put request, uploads the resume to s3 and 
	merges the url for the resume in with the rest of the parameters to be included in the attribute map
	before updating the applicant in the table"
	[params]
	(println "*********** put-applicant")
	(let [extra-params (upload-resume params) full-params (merge params extra-params)]
		(if (models/update-applicant (params->applicants-attributeMap (merge params extra-params))) 
			"OK" 
			"ERROR")))



(defn handle-put-request
	[request]
	(println "**************** API PUT *******************")
	(println request)
	(let [params (request :multipart-params)
		route ((request :route-params) :*)]
		(println (str "params: " params))
		(println (str "route: " route))
		(case route
			"applicant" (put-applicant params)
			"interviewer" (if (models/update-interviewer (params->interviewers-attributeMap params)) "OK" "ERROR")
			"task" (if (models/update-task (params->tasks-attributeMap params)) "OK" "ERROR")
			"Invalid DELETE request")))

(defn handle-delete-request
	[request]
	(println "**************** API DELETE *******************")
	(let [params (request :params) route (params :*) id (util/string->number (params :id))]
		(println (str "params: " params))
		(println (str "route: " route))
		(case route
			"applicant" (if (models/delete-applicant id) "OK" "ERROR")
			"interviewer" (if (models/delete-interviewer id) "OK" "ERROR")
			"task" (if (models/delete-task id) "OK" "ERROR")
			"Invalid DELETE request")))











