;; File handles sending emails through mailgun

(ns huffpost-hires.mailgun
  (refer-clojure :exclude [send])
  (import [java.net URLEncoder])
  (:require [clj-http.client :as client]
            [cheshire.core :as json]

            [clojure.java.io :as io]))

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
    (let [url (make-request-url) 
        result (client/post url {:accept :json
			     :form-params data
			     :basic-auth ["api" api-key]})]
			(println "****** result of post request to mailgun*******")
			(println result)
				(if (= (result :status 200)) 
					true 
					false))
  (catch Exception e (println "****************Exception " e))))

(defn post-multipart-request
  "Make a HTTP POST request to mailgun with mailgun credentials in basic-auth
  Returns true if successful, otherwise returns false"
  [multipart-params]
  (try
    (println "********** post-multipart-request with multipart-params: *********")
    (println multipart-params)
    (let [url (make-request-url) 
        result (client/post url {:accept :json
           :multipart multipart-params
           :basic-auth ["api" api-key]})]
      (println "****** result of post request to mailgun*******")
      (println result)
        (if (= (result :status 200)) 
          true 
          false))
  (catch Exception e (println "****************Exception " e))))

(defn send-notification
  "Send an email message Notification"
  [to subject content]
  (let [msg {
      :from from-address 
			:to to 
			:subject subject
			:text content}]
    (post-request msg)))

(defn send-with-attachment
  "Send an email Notification with attachment"
  [to subject content file]
  (client/post (make-request-url) {:accept :json 
                      :multipart {:from from-address 
                                                :to "alexandra.berke@huffingtonpost.com" 
                                                :subject "test with attachment" 
                                                :text "sent with attachment?" 
                                                :attachment file} 
                      :basic-auth ["api" api-key]}))


(defn test-mail 
	[]
	(send-notification (list "alexandra.berke@huffingtonpost.com" "berke.alexandra@gmail.com") "test subject" "test email"))







