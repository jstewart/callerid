(ns callerid.data
  (require [clojure.java.io :as io]
           [clojure.string :as s]
           [com.stuartsierra.component :as component]))

(defn insert-number
  "Inserts a E.164 format number into the application state atom.
  returns true upon successful insertion, false upon collision with
  previously existing number and context pair"
  [state name number context]
  (let [context-key (s/lower-case context)]
    (when-not (get-in @state [number context-key])
      (swap! state update-in [number] assoc context-key [context name])
      true)))

(defn find-number
  "Finds number n in application state atom. returns map of number and
  known contexts, nil when not found"
  [state number]
  (when-let [n (get @state number)]
    (let [contexts (vals n)
          results (map (fn [context] {:name (last context)
                             :number number
                             :context (first context)})
               (vals n))]
          {:results results})))

(defn is-e164?
  "Tests number to determine if it's in E.164 format
  For the purposes of this API exercise, we're assuming that it's
  +1xxxxxxxxxx"
  [s]
  (re-matches #"^\+1\d{10}$" s))

(defn to-e164
  "converts phone number s to E.164 format.
  for the purposes of this exercise, we simply check
  if it's already E.164 and if not, make it that way."
  [s]
  (if (= (count s) 12)
    s
    (str "+1" (s/replace s #"[^\d]" ""))))

(defn load-seed-data
  "Loads a clojure.java.io/reader r, normalizes, then hashes each line"
  [r]
  (letfn [(normalize [m s]
                    (let [[number context name] (s/split s #",")
                          number (to-e164 number)
                          context-key (s/lower-case context)]
                      (update-in m [number] assoc context-key [context name])))]
    (reduce normalize {} (line-seq r))))

(defrecord DataSet []
  component/Lifecycle

  (start [this]
    (let [r (-> "interview-callerid-data.csv"
                io/resource
                io/reader)]
    (assoc this :data-set (atom (load-seed-data r)))))

  (stop [this]
    (dissoc this :data-set)))
