;; Handles routing api requests

(ns huffpost-hires.api
	(require [huffpost-hires.models :as models]
		[huffpost-hires.util :as util]
		[huffpost-hires.database :as database]

        [aws.sdk.s3 :as s3]
        [clojure.java.io :as io]
        [clojure.string :as string]
		[cheshire.core :as json])

	(:import [java.io File]))

;; ********************* HELPERS **********************


(def s3-credentials {:access-key (System/getenv "AWS_ACCESS_KEY_ID"), :secret-key (System/getenv "AWS_SECRET_ACCESS_KEY")})


(defn upload-to-s3
	"Helper to upload-homework, upload-resume and upload-pic.  Uploads files to amazon s3 bucket
	Takes file object to upload, taken out of multipart-params of http request, and prefix for object name
	Returns url to uploaded file"
	[file object-key]
	;; parse out file data
	(let [file-name (file :filename)
		file-size (file :size)
		content-type (file :content-type)
		actual-file (file :tempfile)
		object-url (str "https://s3.amazonaws.com/" (System/getenv "S3_BUCKET_NAME") "/" object-key)]
		;; upload to our s3 bucket
        (s3/put-object s3-credentials 
    					(System/getenv "S3_BUCKET_NAME") 
    					object-key 
    					actual-file
    					{:content-type content-type :content-length file-size})
		object-url))

;; helper to put/post-homework to upload attachment and return map {'attachment_url' attachment_url} **IF** the attachment is in params
(defn upload-homework-attachment
	"helper to put/post-homework to upload attachment
	Returns map {'attachment_url' attachment_url} **IF** the attachment is in params
	Otherwise returns empty map"
	[params]
	(if (get params "attachment")
		{"attachment_url" (upload-to-s3 (params "attachment") (str "applicant/homework/" (params "applicant")))}
		{}))

;; helper to put/post-appliant to upload resume and then return map {'resume_url' resume_url}  **IF** the resume is in params 
(defn upload-resume
	"If resume was included in params, uploads resume to s3 and returns map {'resume_url' resume_url}
	Otherwise returns empty map"
	[params]
	(if (get params "resume")
		{"resume_url" (upload-to-s3 (params "resume") (str "applicant/resume/" (params "id")))}
		{})) 
;; helper to put/post-interviewer to upload pic and then return map {'pic_url' pic_url} **IF** the pic is in the params
(defn upload-pic
	"If pic was included in params, uploads pic to s3 and returns map {'pic_url' pic_url}
	Otherwise returns empty map"
	[params]
	(if (get params "pic")
		{"pic_url" (upload-to-s3 (params "pic") (str "interviewer/pic" (params "id")))}
		{}))

(defn params->listings-attributeMap
	[params]
	(println "params->listings-attributeMap -- params: " params)
	{:id (util/string->number (get params "id"))
		:hiring_manager (util/string->number-or-0 (get params "hiring_manager" 0))
		:title (util/string->sql-safe (get params "title"))
		:description (util/string->sql-safe (get params "description" ""))
		:homework_required (util/string->number-or-0 (get params "homework_required" 0))
		:homework_question (util/string->sql-safe (get params "homework_question" ""))})
(defn params->requirements-attributeMap
	[params]
	(println "params->requirements-attributeMap -- params: " params)
	{:id (util/string->number (get params "id"))
		:listing (util/string->number (get params "listing"))
		:title (util/string->sql-safe (get params "title"))})
(defn params->responsibilities-attributeMap
	[params]
	(params->requirements-attributeMap params))

(defn params->homeworks-attributeMap
	[params]
	(println "params->homeworks-attributeMap -- params: " params)
	{:id (util/string->number (get params "id"))
		:applicant (util/string->number (get params "applicant"))
		:prompt (util/string->sql-safe (get params "prompt" ""))
		:text_answer (util/string->sql-safe (get params "text_answer" ""))
		:attachment_url (get params "attachment_url" "")
		:reviewer (util/string->number (get params "reviewer"))
		:feedback (util/string->sql-safe (get params "feedback" ""))})

(defn params->applicants-attributeMap
	[params]
	(println "params->applicants-attributeMap -- params: " params)
	{:id (util/string->number (get params "id"))
		:name (util/string->sql-safe (get params "name" ""))
		:stage (util/string->number-or-0 (get params "stage" 0))
		:goalie (util/string->number-or-0 (get params "goalie" 0))
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
	{:id (util/string->number (get params "id"))
		:applicant (util/string->number-or-0 (get params "applicant"))
		:interviewer (util/string->number-or-0 (get params "interviewer"))
		:title (util/string->sql-safe (get params "title" ""))
		:description (util/string->sql-safe (get params "description" ""))
		:feedback (util/string->sql-safe (get params "feedback" ""))
		:date (get params "date" "")
		:pic_url (get params "pic_url" "/img/default.jpg")
		:feedback_due (get params "feedback_due" "")
		:completed (util/string->number-or-0 (get params "completed"))
		:pass (util/string->number (get params "pass" 1))})

(defn params->interviewers-attributeMap
	[params]
	(println "params->interviewers-attributeMap -- params: " params)
	{:id (util/string->number (get params "id"))
		:name (util/string->sql-safe (get params "name" ""))
		:phone (get params "phone" "")
		:email (get params "email" "")
		:pic_url (get params "pic_url" "")})


;; ******************************* GET requests below ******************************


;; GET /api/listing/?id='listingID'
(defn get-listing
	"Returns specified listing"
	[request]
	(println (str "api/listing with request:" request))
	(database/query-json [(str "SELECT * FROM listings WHERE id=" (util/string->number (request :id)))]))

;; GET /api/listing/all
(defn get-listings-all
	"Returns all listings"
	[params]
	(database/query-json ["SELECT * FROM listings"]))
;; GET /api/listing/requirements?id=listingID
(defn get-listing-requirements
	"Returns all requirements for given listing"
	[params]
	(database/query-json [(str "SELECT * FROM requirements WHERE listing=" (util/string->number (get params :id)))]))
;; GET /api/listing/responsibilities?id=listingID
(defn get-listing-responsibilities
	"Returns all responsibilities for given listing"
	[params]
	(database/query-json [(str "SELECT * FROM responsibilities WHERE listing=" (get params :id))]))

;; GET /api/stage/all
(defn get-stages-all
	"Returns all stages"
	[params]
	(database/query-json ["SELECT * FROM stages ORDER BY number"]))

;; GET /api/stage/applicants?stage=number&pass=0/1&[goalie=inteviewerID]
(defn get-applicants-by-stage
	"Returns all applicants for given stage and passing status
	Defaults to stage=0 (pending) and pass=1 (passing true)"
	[params]
	(if (params :goalie) 
		(database/query-json [(str "SELECT * FROM applicants 
							WHERE stage=" (get params :stage 0)
							" AND pass=" (get params :pass 1)
							" AND goalie=" (params :goalie))])
		(database/query-json [(str "SELECT * FROM applicants 
							WHERE stage=" (get params :stage 0)
							" AND pass=" (get params :pass 1))])))

;; GET /api/interviewer/all?extra-data=true/false
(defn get-interviewers-all
	"Returns All Interviewers in Database in json 
	joined with the number of their incomplete tasks and number of current applicants for whom they're the goalie for"
	[params]
	(println (str "GET api/interviewers-all with params:" params))
	(if (= (params :extra-data) "true")
		(database/query-json ["SELECT i.*,
							(SELECT SUM(CASE WHEN t.completed=0 THEN 1 ELSE 0 END) FROM tasks t WHERE t.interviewer=i.id) AS incomplete_tasks,
							(SELECT SUM(CASE WHEN a.completed=0 THEN 1 ELSE 0 END) FROM applicants a WHERE a.goalie=i.id) AS current_applicants
							FROM interviewers i
							INNER JOIN applicants a ON i.id=a.goalie
							INNER JOIN tasks t ON i.id=t.interviewer
							GROUP BY i.id;"])
		(database/query-json ["select * from interviewers order by name"])))


;; GET /api/applicant/rejected
(defn get-applicants-rejected
	"Returns all rejected applicants, ordered by asof date"
	[params]
	(database/query-json ["SELECT * FROM applicants WHERE pass=0 ORDER BY asof"]))

;; GET /api/applicant/all?task-count=true/false
(defn get-applicants-all
	"Returns All Interviewers in Database in json 
	optionally adds count of complete_tasks and incomplete_tasks.
	Dope ass SQL queries courtesy of Mike Adler"
	[params]

	(if (= (params :task-count) "true")
		(database/query-json ["SELECT a.*, 
								SUM(CASE WHEN t.completed=1 THEN 1 ELSE 0 END) AS complete_tasks, 
								SUM(CASE WHEN t.completed=0 THEN 1 ELSE 0 END) AS incomplete_tasks,
								COUNT(t.completed) AS total_tasks 
								FROM applicants a 
								LEFT JOIN tasks t ON a.id=t.applicant 
								GROUP BY a.id;"])
		(database/query-json ["select * from applicants order by asof"])))

;; GET /api/applicant/homework?id='applicantID'
(defn get-applicant-homework
	"Returns homework where homework.applicant = 'applicantID'"
	[params]
	(println (str "api/applicant/homework where applicantID=" (params :id)))
	(database/query-json [(str "SELECT * FROM homeworks WHERE applicant=" (params :id))]))


;; GET /api/applicant/?id='applicantID'
(defn get-applicant
	"Returns specified applicant"
	[request]
	(println (str "api/appicant with request:" request))
	(database/query-json [(str "SELECT * FROM applicants WHERE id=" (request :id))]))

;; GET /api/interviewer/?id='interviewerID'
(defn get-interviewer
	"Returns specified interviewer"
	[params]
	(println (str "api/interviewer with params:" params))
	(database/query-json [(str "SELECT * FROM interviewers WHERE id=" (params :id))]))

;; /api/applicant/tasks?id='applicantID'
(defn tasks-by-applicant
	"Returns all tasks for applicant"
	[params]
	(database/query-json [(str "select * from tasks WHERE applicant=" (params :id))]))

;; /api/applicant/complete-tasks?id='applicantID'
(defn complete-tasks-by-applicant
	"Returns all completed tasks for applicant"
	[params]
	(database/query-json [(str "select * from tasks WHERE applicant=" (params :id) " AND completed=1")]))

;; /api/interviewer/incomplete-tasks?id='applicantID'
(defn incomplete-tasks-by-applicant
	[params]
	(database/query-json [(str "select * from tasks WHERE applicant=" (params :id) " AND completed=0")]))

;; /api/interviewer/tasks?id='applicantID'
(defn tasks-by-interviewer
	[params]
	(database/query-json [(str "select * from tasks WHERE interviewer=" (params :id))]))

;; /api/interviewer/complete-tasks?id='interviewID'
(defn complete-tasks-by-interviewer
	[params]
	(database/query-json [(str "select * from tasks where interviewer=" (params :id) " AND completed=1")]))

;; /api/interviewer/incomplete-tasks?id='interviewID'
(defn incomplete-tasks-by-interviewer
	"Returns all completed tasks for interviewer"
	[params]
	(database/query-json [(str "select * from tasks where interviewer=" (params :id) " AND completed=0")]))

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
		"homework" (get-applicant-homework params)
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

(defn handle-get-request-listing
	"routing helper for handle-get-request:
	Called upon GET request to url /api/listing/*"
	[route params] ; route == * in the GET request

	(case route
		"" (get-listing params)
		"all" (get-listings-all params)
		"responsibilities" (get-listing-responsibilities params)
		"requirements" (get-listing-requirements params)
		(str "Invalid api request to /api/listing/" route)))

(defn handle-get-request
	"Called by web upon GET request to url /api/*/*"
	[request]
	(let [params (request :params) route (params :*) route-prefix (first route) route-suffix (second route)]
		(case route-prefix
			"stage" (handle-get-request-stage route-suffix params)
			"listing" (handle-get-request-listing route-suffix params)
			"applicant" (handle-get-request-applicant route-suffix params)
			"interviewer" (handle-get-request-interviewer route-suffix params)
			"Invalid api request")))

;; ******************************* GET requests above ******************************

;; ******************************* POST requests below ******************************

;; POST /api/interviewer
(defn post-interviewer-new
	[params]
	(let [extra-params (upload-pic params) full-params (merge params extra-params)]
		(if (models/insert-interviewer (params->interviewers-attributeMap full-params))
			"OK"
			"ERROR")))

;; POST /api/homework
(defn post-homework-new
	[params]
	(let [extra-params (upload-homework-attachment params) full-params (merge params extra-params)]
		(if (models/insert-homework (params->homeworks-attributeMap full-params))
			"OK"
			"ERROR")))
;; POST /api/applicant
(defn post-applicant-new
	[params]
	(let [extra-params (upload-resume params) full-params (merge params extra-params)]
		(if (models/insert-applicant (params->applicants-attributeMap full-params))
			"OK"
			"ERROR")))
;; POST /api/task
(defn post-task-new
	[params]
	(if (models/insert-task (params->tasks-attributeMap params))
		"OK"
		"ERROR"))


;; POST /api/listing
(defn post-listing-new
	"Returns JSON string of new listing"
	[params]
	(if-let [result (models/insert-listing (params->listings-attributeMap params))]
		(json/generate-string result)
		"ERROR"))
;; POST /api/requirement
(defn post-listing-requirement
	[params]
	(if (models/insert-requirement (params->requirements-attributeMap params))
		"OK"
		"ERROR"))
;; POST /api/responsibility
(defn post-listing-responsibility
	[params]
	(if (models/insert-responsibility (params->responsibilities-attributeMap params))
		"OK"
		"ERROR"))

(defn handle-post-request
	[request]
	(println "**************** API POST *******************")
	(let [params (request :multipart-params) route ((request :route-params) :*)]
		(println (str "params: " params))
		(case route
			"listing" (post-listing-new params)
			"requirement" (post-listing-requirement params)
			"responsibility" (post-listing-responsibility params)

			"applicant" (post-applicant-new params)
			"interviewer" (post-interviewer-new params)
			"task" (post-task-new params)
			"homework" (post-homework-new params)
			"Invalid POST request")))

;; ******************************* PUT requests below ******************************

;; PUT /api/homework
(defn put-homework
	"If an attachment was included in the put request, uploads the attachment to s3 and 
	merges the url for the attachment in with the rest of the parameters to be included in the attribute map
	before updating the applicant in the table"
	[params]
	(let [extra-params (upload-homework-attachment params) full-params (merge params extra-params)]
		(if models/update-homework (params->homeworks-attributeMap full-params))
		"OK"
		"ERROR"))

;; PUT /api/applicant
(defn put-applicant
	"If a resume was included in the put request, uploads the resume to s3 and 
	merges the url for the resume in with the rest of the parameters to be included in the attribute map
	before updating the applicant in the table"
	[params]
	(let [extra-params (upload-resume params) full-params (merge params extra-params)]
		(if (models/update-applicant (params->applicants-attributeMap full-params)) 
			"OK" 
			"ERROR")))

;; PUT /api/interviewer
(defn put-interviewer
	"If a pic was included in the PUT request, uploads the pi to s3 and
	merges the url for the pic in with the rest of the parameters to be included in the attribute map
	before updating the interviewer in the table"
	[params]
	(let [extra-params (upload-pic params) full-params (merge params extra-params)]
		(if (models/update-interviewer (params->interviewers-attributeMap full-params))
			"OK"
			"ERROR")))

;; PUT /api/listing
(defn put-listing
	[params]
	(if (models/update-listing (params->listings-attributeMap params))
		"OK"
		"ERROR"))

(defn handle-put-request
	[request]
	(let [params (request :multipart-params) route ((request :route-params) :*)]
		(case route
			"listing" (put-listing params)
			"applicant" (put-applicant params)
			"homework" (put-homework params)
			"interviewer" (put-interviewer params)
			"task" (if (models/update-task (params->tasks-attributeMap params)) "OK" "ERROR")
			"Invalid PUT request")))

;; ******************************* DELETE requests below ******************************
(defn handle-delete-request
	[request]
	(let [params (request :params) route (params :*) id (util/string->number (params :id))]
		(case route
			"listing" (if (models/delete-listing id) "OK" "ERROR")
			"responsibility" (if (models/delete-responsibility id) "OK" "ERROR")
			"requirement" (if (models/delete-requirement id) "OK" "ERROR")

			"applicant" (if (models/delete-applicant id) "OK" "ERROR")
			"interviewer" (if (models/delete-interviewer id) "OK" "ERROR")
			"task" (if (models/delete-task id) "OK" "ERROR")
			"homework" (if (models/delete-homework id) "OK" "ERROR")
			"Invalid DELETE request")))











