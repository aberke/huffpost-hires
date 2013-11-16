(ns huffpost-hires.apply
  (:require [ring.middleware.basic-authentication :as basic]
            [cemerick.drawbridge :as drawbridge]
            [environ.core :refer [env]]
            [clojure.java.io :as io]

            [huffpost-hires.api :as api]
            [huffpost-hires.util :as util]
            [huffpost-hires.models :as models]
            [huffpost-hires.mailgun :as mailgun]))



(defn huffpost-live-application-submission
    "Handles incoming application submissions for the huffpost-live application page"
    [params]
    (println "**********huffpost-live-application-submission with params: ")
    (println params)

    (let [applicant-attribute-map {
                    :name (util/string->sql-safe (get params "name" ""))
                    :stage 2 ;; homework received
                    :goalie 1 ;; john siragussa
                    :position "Developer"
                    :phone (get params "phone" "")
                    :email (get params "email" "")
                    :notes "Applied via huffpost-hires.com/apply/huffpost-live page"
                    :resume_url ((api/upload-resume params) "resume_url")
                    :completed 0
                    :pass 1}
            new-applicant (models/insert-applicant applicant-attribute-map)]
        (if-not new-applicant "ERROR"
        
        (let [homework-attribute-map {
                    :applicant (new-applicant :id) ;; id from the applicant just created
                    :prompt (get params "prompt" "Write a script adhering to the prompt and test your script with the sample input.  Submit the output from feeding the sample input to your script in order to upload your script and resume for consideration.")
                    :text_answer (get params "text_answer" "")
                    :attachment_url ((api/upload-homework-attachment params) "attachment_url")
                    :reviewer 1} ;; john siragussa
                new-homework (models/insert-homework homework-attribute-map)]
            (if-not new-homework "ERROR"

            (do 
                ;; send confirmation email to applicant
                (mailgun/send-notification 
                    (applicant-attribute-map :email) 
                    "Huffpost Live Application Recieved"
                    "Thank you for applying to HUFFPOST LIVE....")

                ;; send email to interviewer -- TODO: FIX THIS
                (mailgun/send-notification
                    "alexandra.berke@huffingtonpost.com"
                    "[Huffpost Hires] New applicant submission"
                    (str "Hello John,\n\n" 
                        "A new applicant named " (new-applicant :name) " has applied via the HUFFPOST LIVE apply page.\n"
                        "To view the applicant's information, resume and homework submission, visit: " 
                        "http://0.0.0.0:5000/applicant?id=" (new-applicant :id) ".\n\n"
                        "- Huffpost Hires"
                        ))

                "OK"))))))






(defn POST-handler
    "Handles all POST requests to /apply/*"
    [route params]
    (case route
        "huffpost-live" (huffpost-live-application-submission params)
        (str "INVALID POST ROUTE TO " route)))

(defn GET-handler
    "Handles all GET requests to /apply/*"
    [route params]
    (case route
        "huffpost-live" {:status 200 :headers {} :body (io/file (io/resource "html/apply-siragussa.html"))}
        (str "Invalid route: " route)))


(defn request-handler
    "Handles ANY request to /apply/*"
    [request]
    (println "/apply/* ************************ request:")
    (println request)
    (let [route ((request :route-params) :*) 
            params (request :params) 
            multipart-params (request :multipart-params) 
            method (request :request-method)]
        (case method
            :get (GET-handler route params)
            :post (POST-handler route multipart-params)
            "Invalid request type to /apply/*")))






