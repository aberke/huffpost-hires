(defproject huffpost-hires "1.0.0-SNAPSHOT"
  :description "Created by Huffpost Labs"
  :url "http://huffpost-hires.herokuapp.com"
  :license {:name "FIXME: choose"
            :url "http://example.com/FIXME"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                  [org.clojure/clojure-contrib "1.2.0"]
                  [compojure "1.1.1"]
                  [ring/ring-jetty-adapter "1.1.0"]
                  [ring/ring-devel "1.1.0"]
                  [ring-basic-authentication "1.0.1"]
                  [ring.middleware.jsonp "0.1.3"]

                  [environ "0.2.1"]
                  [com.cemerick/drawbridge "0.0.6"]
                  [org.clojure/java.jdbc "0.3.0-alpha5"]
                  [postgresql "9.1-901.jdbc4"]
                  [clj-aws-s3 "0.3.7"]]
  :min-lein-version "2.0.0"
  :plugins [[environ/environ.lein "0.2.1"]]
  :hooks [environ.leiningen.hooks]
  :profiles {:production {:env {:production true}}})
