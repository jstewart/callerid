(defproject callerid "0.1.0-SNAPSHOT"
  :description "Caller ID Information API"
  :url "https://github.com/jstewart/callerid"
  :main callerid.system
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.stuartsierra/component "0.3.2"]
                 [compojure "1.5.1"]
                 [ring/ring-core "1.6.2"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-jetty-adapter "1.6.2"]
                 [ring/ring-json "0.4.0"]]

  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]
         :resource-paths ["resources"
                          "test/resources"]}})
