(ns huffpost-hires.web
  (:use ring.middleware.resource)
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [ring.middleware.stacktrace :as trace]
            [ring.middleware.session :as session]
            [ring.middleware.session.cookie :as cookie]
            [ring.middleware.params :as params]
            [ring.middleware.multipart-params :as mp]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.basic-authentication :as basic]
            [cemerick.drawbridge :as drawbridge]
            [environ.core :refer [env]]
            [aws.sdk.s3 :as s3]

            [huffpost-hires.api :as api])

  (:import [java.io File]))

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
      {:status 200
      :headers {}
      :body (io/file (io/resource (str "public" (request :uri))))})

(defn serve-hires
  "Serves the hires.html template"
  [request]
  (println "serve-hires")
  (println ((request :params) :id))
      {:status 200
      :headers {}
      :body (io/file (io/resource "html/hires.html"))})

;; ************* GET RID OF BELOW

(def s3-credentials {:access-key (System/getenv "AWS_ACCESS_KEY_ID"), :secret-key (System/getenv "AWS_SECRET_ACCESS_KEY")})

(println "access-key: " (s3-credentials :access-key) ", secret-key: " (s3-credentials :secret-key))

(defn test-put-route
  [request]
  (println "test-route")
  (let [answer (s3/put-object s3-credentials (System/getenv "S3_BUCKET_NAME") "test-key" "test-value")]
     (println (str "answer: " answer))
     {:status 200
         :headers {"Content-Type" "text/html"}
         :body (str "answer: " answer)}))


(defn upload-file
  [request]
  (println "FILE request ********************")
  (println request)
  (println "FILE params ********************")


  (let [params (request :params) 
        multipart-params (request :multipart-params) 
        file (get multipart-params "file")
        file-name (file :filename)
        file-size (file :size)
        actual-file (file :tempfile)]
    (println params)
    (println "FILE multipart-params ********************")
    (println multipart-params)
    (println "FILE file ********************")
    (println file)
    (if file
      (do
        (println "actual-file: " actual-file)
        (io/copy actual-file (File. (format "./resources/uploads/%s" file-name))) ;(format "/Users/aberke13/huffpost-hires-tempfiles/%s" file-name)))
        (s3/put-object s3-credentials (System/getenv "S3_BUCKET_NAME") file-name (slurp actual-file))
        {:status 200
         :headers {"Content-Type" "text/html"}
         :body (str "filename: " file-name ", size: " file-size)})
      "NO FILE FOUND")))

;; ************* GET RID OF above

(defn test-route
  [request]
  (println "serve-test")
  {:status 200
  :headers {}
  :body (io/file (io/resource "html/test.html"))})

(defroutes app
  (ANY "/repl" {:as req}
       (drawbridge req))

  (GET "/test-put" [] test-put-route)
  (GET "/test" [] test-route)

  (POST "/file" [] upload-file) ;{params :params} (upload-file (get params "file"))))

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
; (defonce server (-main))

; (defn stop [] 
;   (.stop server))

