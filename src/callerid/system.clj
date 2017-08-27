(ns callerid.system
  (require [com.stuartsierra.component :as component]
           [callerid.data :refer [map->DataSet]]
           [callerid.server :as server])
  (:gen-class))

(defn add-shutdown-hook
  [f]
  (.addShutdownHook (java.lang.Runtime/getRuntime)
                    (Thread. ^Runnable f)))

(defmacro on-shutdown
  [& body]
  `(add-shutdown-hook (fn [] ~@body)))

(def default-port
  (Integer/parseInt
   (or (System/getenv "PORT") "8080")))

(defrecord Callerid []
  component/Lifecycle
  (start [this]
    (println "Starting Callerid API Service")
    (component/start-system this))
  (stop [this]
    (println "Stopping Callerid API Service")
    (component/stop-system this)))

(defn create-system [port]
  (component/system-map
   :data-set  (map->DataSet {})
   :server    (component/using (server/new-server port)
                               [:data-set])))

(defn -main [& args]
  (let [port default-port
        system (component/start (create-system port))]
    (on-shutdown
     (println "interrupted! shutting down")
     (component/stop system))))
