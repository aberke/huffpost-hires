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

(defn post-handler
	[request]
	"TODO: POST HANDLER")





