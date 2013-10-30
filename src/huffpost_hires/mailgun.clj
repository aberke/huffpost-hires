;; File handles sending emails through mailgun

(ns huffpost-hires.mailgun
  (refer-clojure :exclude [send])
  (import [java.net URLEncoder])
  (:require [clj-http.client :as client]
            [cheshire.core :as json]))

(def base-url "https://api.mailgun.net/v2")
(def domain "huffpostlabs.mailgun.org")
(def base-with-domain (str base-url domain))
(def api-key (System/getenv "MAILGUN_API_KEY"))
(def from-address "Huffpost Hires <huffpost-hires@huffpostlabs.mailgun.org>")

(defn make-request-url []
	(format "%s/%s/messages"
		base-url
		domain))


(defn encode-url [url] (URLEncoder/encode url))

(defn post-request
  "Make a HTTP POST request to mailgun with mailgun credentials in basic-auth
  Returns true if successful, otherwise returns false"
  [data]
  (try
    (let [url (make-request-url) result (client/post url {:accept :json
			:form-params data
			:basic-auth ["api" api-key]})]
			(println "****** result of post request to mailgun*******")
			(println result)
				(if (= (result :status 200)) 
					true 
					false))
  (catch Exception e
  	(println "****************Exception " e)
     (let [exception-info (.getData e)]
     (select-keys
       (into {} (map (fn [[k v]] [(keyword k) v])
         (json/parse-string
             (get-in exception-info [:object :body]))))
             (vector :status :message :code))))))

(defn send-email-notification
  "Send an email message Notification"
  [to content]
  (let [msg {:from from-address 
			:to to 
			:subject "[Huffpost Hires] Notification"
			:text content}]
    (post-request msg)))


(defn test-mail 
	[]
	(send-email-notification "alexandra.berke@huffingtonpost.com" "test email"))







