(ns huffpost-hires.jobs
	"Handles requests to & from the Jobs page on code.huffingtonpost.com (CORS)"
  (:require [ring.middleware.basic-authentication :as basic]
            [cemerick.drawbridge :as drawbridge]
            [environ.core :refer [env]]
            [clojure.java.io :as io]

            [huffpost-hires.api :as api]
            [huffpost-hires.util :as util]
            [huffpost-hires.models :as models]
            [huffpost-hires.mailgun :as mailgun]))

(defn handler
	[request]
	(let [route-params (request :route-params)
			route-prefix (first route-params)
			route-suffix (second route-params)
			method ((request :headers) "access-control-request-method")]
		(println "route-prefix: " route-prefix)
		(println "route-suffix: " route-suffix)
		(println "method: " method)))