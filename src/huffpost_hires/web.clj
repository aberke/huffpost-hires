(ns huffpost-hires.web
  (:use ring.middleware.resource)
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            
            [ring.middleware.stacktrace :as trace]
            [ring.middleware.session :as session]
            [ring.middleware.session.cookie :as cookie]
            [ring.middleware.params :as params]
            [ring.middleware.multipart-params :as mp]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.basic-authentication :as basic]
            [cemerick.drawbridge :as drawbridge]
            [environ.core :refer [env]]
            [clojure.java.io :as io]

            [huffpost-hires.api :as api]
            [huffpost-hires.apply :as apply])
  )

(defn- authenticated? [user pass]
  ;; TODO: heroku config:add REPL_USER=[...] REPL_PASSWORD=[...]
  (= [user pass] [(env :repl-user false) (env :repl-password false)]))

(def ^:private drawbridge
  (-> (drawbridge/ring-handler)
      (session/wrap-session)
      (basic/wrap-basic-authentication authenticated?)))

(defn serve-partial
  "Handles serving the partial html files"
  [request]
  (println (request :uri))
      {:status 200 :headers {} :body (io/file (io/resource (str "public" (request :uri))))})

(defn serve-hires
  "Serves the hires.html template"
  [request]
  {:status 200 :headers {} :body (io/file (io/resource "html/hires.html"))})

(defn test-route
  [request]
  {:status 200 :headers {} :body (io/file (io/resource "html/test.html"))})

(defroutes app
  (ANY "/repl" {:as req}
       (drawbridge req))

  (ANY "/test" [] test-route)

  (ANY "/apply/*" [] apply/request-handler)

  (GET "/api/*/*" [] api/handle-get-request)
  (POST "/api/*" [] api/handle-post-request)
  (PUT "/api/*" [] api/handle-put-request)
  (DELETE "/api/*" [] api/handle-delete-request)
  (GET "/api" []
       {:status 200
        :headers {"Content-Type" "text/plain"}
        :body (pr-str ["Hello" :from 'api])})

  (GET "/partials/*" [] serve-partial)
  (GET "/applicants" [] serve-hires)
  (GET "/interviewers" [] serve-hires)
  (GET "/applicant" [] serve-hires)
  (GET "/interviewer" [] serve-hires)
  (route/resources "/")
  (GET "/" [] serve-hires)
  (route/not-found (slurp (io/resource "html/404.html"))))

(defn wrap-error-page [handler]
  (fn [req]
    (try (handler req)
         (catch Exception e
           {:status 500
            :headers {"Content-Type" "text/html"}
            :body (slurp (io/resource "html/500.html"))}))))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))
        ;; TODO: heroku config:add SESSION_SECRET=$RANDOM_16_CHARS
        store (cookie/cookie-store {:key (env :session-secret)})]
    (jetty/run-jetty (-> #'app
                         ((if (env :production)
                            wrap-error-page
                            trace/wrap-stacktrace))
                         params/wrap-params
                         mp/wrap-multipart-params
                         (site {:session {:store store}}))
                     {:port port :join? false})))


;; For interactive development: -- can't push to heroku with this uncommented!
(defonce server (-main))

(defn stop [] 
  (.stop server))

