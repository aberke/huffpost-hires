;; Thanks to https://github.com/owainlewis/twilio

;; This file handles sending text notifications through twilio

(ns huffpost-hires.twilio
  (refer-clojure :exclude [send])
  (:require [clj-http.client :as client]
            [cheshire.core :as json]))

(def base-url "https://api.twilio.com/2010-04-01")
(def sid (System/getenv "TWILIO_ACCOUNT_SID"))
(def auth-token (System/getenv "TWILIO_AUTH_TOKEN"))
(def from-number (System/getenv "TWILIO_NUMBER"))

(defn make-request-url []
  (format
    "%s/Accounts/%s/SMS/Messages.json"
    base-url
    sid))

(defn request
  "Make a generic HTTP request with twilio credentials in basic-auth"
  [method url & params]
  (try
    (let [f (condp = method
              :post client/post
              :else client/get)]
    (f url
      {:accept :json
       :form-params (first params)
       :basic-auth [sid auth-token]}))
  (catch Exception e
     (let [exception-info (.getData e)]
     (select-keys
       (into {} (map (fn [[k v]] [(keyword k) v])
         (json/parse-string
             (get-in exception-info [:object :body]))))
             (vector :status :message :code))))))

(defn send-text
  "Send an SMS message"
  [to-number body]
  (let [url (make-request-url) msg {:From from-number :To to-number :Body body}]
    (request :post url msg)))





