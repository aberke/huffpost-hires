(ns huffpost-hires.api
	(require [huffpost-hires.models :as models]))

;; Handles routing api requests


;; ******************************* GET requests below ******************************

;; GET /api/interviewer/all?task-count=true/false
(defn get-interviewers-all
	"Returns All Interviewers in Database in json 
	and optionally adds count of complete_tasks and incomplete_tasks.
	Dope ass SQL queries courtesy of Mike Adler"
	[params]
	(println (str "api/interviewers-all with params:" params))
	(if (= (params :task-count) "true")
		(models/query-json ["SELECT i.*, 
								SUM(CASE WHEN t.completed=1 THEN 1 ELSE 0 END) AS complete_tasks, 
								SUM(CASE WHEN t.completed=0 THEN 1 ELSE 0 END) AS incomplete_tasks,
								COUNT(t.completed) AS total_tasks 
								FROM interviewers i 
								LEFT JOIN tasks t ON i.id=t.interviewer 
								GROUP BY i.id;"])
		(models/query-json ["select * from interviewers order by name"])))

;; GET /api/applicant/all?task-count=true/false
(defn get-applicants-all
	"Returns All Interviewers in Database in json 
	optionally adds count of complete_tasks and incomplete_tasks.
	Dope ass SQL queries courtesy of Mike Adler"
	[params]
	(println (str "api/applicants-all with params:" params))
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

(defn handle-get-request-applicant
	"routing helper for handle-get-request:
	Called upon GET request to url /api/applicant/*"
	[route params] ; route == * in the GET request
	(println (str "api/handle-get-request-applicant with route: " route "; params: " params))
	(case route
		"" (get-applicant params)
		"all" (get-applicants-all params)
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

	(let [params (request :params) route (params :*) route-prefix (first route) route-suffix (second route)]
		(println (str "params: " params))
		(println (str "route:" route))
		(case route-prefix
			"applicant" (handle-get-request-applicant route-suffix params)
			"interviewer" (handle-get-request-interviewer route-suffix params)
			"Invalid api request")))

;; ******************************* GET requests above ******************************

;; POST /api/applicant
(defn post-applicant-new
	[params]
	(println (str "post-applicant-new with params: " params)))

(defn handle-post-request
	[request]
	(println "TODO: handle-post-request")
	(println request)
	(println "(:data request)")
	(println (:data request))
	(println "(:params request)")
	(println (:params request))
	"TODO")

(defn handle-put-request
	[request]
	(println "TODO: handle-put-request"))

(defn handle-delete-request
	[request]
	(println "TODO: handle-delete-request"))











