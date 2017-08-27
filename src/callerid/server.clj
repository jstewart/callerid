(ns callerid.server
  (:require [clojure.string :as s]
            [com.stuartsierra.component :as component]
            [compojure.core :refer [routes defroutes GET POST]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.util.response :refer [response]]
            [callerid.data :refer [find-number insert-number is-e164?]]))

(defn bad-request
  "Constructs a 400 bad request response"
  [body]
  {:status 400
   :headers {}
   :body body})

(defn conflict
  "Constructs a 409 conflict response"
  [body]
  {:status 409
   :headers {}
   :body body})

(defn make-app-routes
  "Constructs API routes, injecting the data store dependency"
  [data]
  (defroutes app-routes
    (GET "/query" [number]
         (if-let [results (find-number data number)]
           (response results)
           (route/not-found "Not Found")))

    (POST "/number" [name number context]
          ;; all params must be present and number must be E.164
          (if (or (not (is-e164? number))
                  (->> [name number context]
                       (filter s/blank?)
                       seq))
            (bad-request "Bad Request")
            (if (insert-number data name number context)
              (response "Success")
              (conflict "This number and context were already entered"))))
    (route/not-found "Not Found")))

(defn make-handler
  "Constructs the compojure API handler"
  [data-set]
  (-> (make-app-routes (:data-set data-set))
      wrap-json-response
      handler/api))

(defn- start-server [handler port]
  (let [server (run-jetty handler {:port port})]
    (println (str "Started server on localhost:" port))
    server))

(defn- stop-server [server]
  (when server
    (server)))

(defrecord Server [port data-set]
  component/Lifecycle
  (start [this]
    (let [handler (make-handler data-set)]
      (assoc this :jetty (start-server handler port))))

  (stop [this]
    (println  "stopping server")
    (.stop (:jetty this))
    (dissoc this :jetty)))

(defn new-server [port]
  (map->Server {:port port}))
