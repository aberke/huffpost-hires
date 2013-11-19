(ns huffpost-hires.jobs
	"Handles requests to & from the Jobs page on code.huffingtonpost.com (CORS)"
  (:require [ring.middleware.basic-authentication :as basic]
            [cemerick.drawbridge :as drawbridge]
            [environ.core :refer [env]]
            [clojure.java.io :as io]
            [clojure.pprint :as pprint]

            [huffpost-hires.api :as api]
            [huffpost-hires.util :as util]
            [huffpost-hires.models :as models]
            [huffpost-hires.mailgun :as mailgun]))


;; ******************** GET HANDLERS **************

(defn get-handler
	[request]
	(let [params (request :params)
			route-params (params :*)
			route-prefix (first route-params)
			route-suffix (second route-params)]

		(case route-prefix
			"listing" (api/handle-get-request-listing route-suffix params)
			(str "unknown request endpoint: /jobs/" route-prefix "/" route-suffix))))

;; ******************** POST HANDLERS **************

(defn apply-handler
	[params]

    (let [applicant-attribute-map {
                    :name (util/string->sql-safe (get params "name" ""))
                    :stage 0 ;; pending
                    :goalie (util/string->number-or-0 (get params "goalie"))
                    :position (get params "position")
                    :phone (get params "phone" "")
                    :email (get params "email" "")
                    :notes "Applied via code.huffingtonpost.com/jobs"
                    :referal "code.huffingtonpost.com/jobs"
                    :resume_url ((api/upload-resume params) "resume_url")
                    :completed 0
                    :pass 1}
            new-applicant (models/insert-applicant applicant-attribute-map)]
        (if-not new-applicant "ERROR"
            (do 
                ;; send confirmation email to applicant
                (mailgun/send-notification 
                    (applicant-attribute-map :email) 
                    "Huffington Post Application Recieved"
                    (str (applicant-attribute-map :name) 
                    	",\n\nThank you for applying to The Huffington Post for the " 
                    	(get params "position") " position.\nWe will review your resume and reach out to you."
                    	"\n\nBest,\nThe Huffington Post Tech Team"))

                ;; send email to interviewer/interested parties -- TODO: FIX THIS
                (mailgun/send-notification
                    (list (System/getenv "RECRUITER_EMAIL") "alexandra.berke@huffingtonpost.com")
                    "[Huffpost Hires] New applicant submission"
                    (str "Hello,\n\n" 
                        "A new applicant has applied via the code.huffingtonpost.com/jobs page.\n\n"
                        
                        "Applicant Infomation:\n"
                        "\t\tName: " (new-applicant :name) "\n"
                        "\t\tEmail: " (new-applicant :email) "\n"
                        "\t\tResume: " (new-applicant :resume_url) "\n\n"
                        
                        "To view the applicant's information in the huffpost-hires portal, visit: " 
                        "http://0.0.0.0:5000/applicant?id=" (new-applicant :id) ".\n\n"
                        "- Huffpost Hires"
                        ))

                "OK"))))

(defn post-handler
	[request]
    (let [route ((request :route-params) :*) 
            params (request :params) 
            multipart-params (request :multipart-params)]
            (case route
            	"apply" (apply-handler multipart-params)
            	(str "Invalid request to /jobs" route))))




