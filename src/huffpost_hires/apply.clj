(ns huffpost-hires.apply
  (:require [ring.middleware.basic-authentication :as basic]
            [cemerick.drawbridge :as drawbridge]
            [environ.core :refer [env]]
            [clojure.java.io :as io]

            [huffpost-hires.models :as models]
            [huffpost-hires.mailgun :as mailgun]))








(defn POST-handler
    "Handles all POST requests to /apply/*"
    [route params]
    ("hi"))

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
    (let [route ((request :route-params) :*) params (request :params) multipart-params (request :multipart-params) method (request :request-method)]
        (case method
            :get (GET-handler route params)
            :post (POST-handler route multipart-params)
            "Invalid request type to /apply/*")))