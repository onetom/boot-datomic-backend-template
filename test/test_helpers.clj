(ns test-helpers
  (:use app.utils)
  (:require
    [com.stuartsierra.component :refer [stop start]]
    [datomic.api :as d]
    [clojure.spec :as s]
    [clojure.spec.gen :as gen]))

; Source: https://github.com/stuartsierra/component/issues/6
(defmacro with-components
  "Evaluates body in a try expression with names bound to the started
  versions of the components passed in and a finally clause that stops
  the components in reverse order."
  [bindings & body]
  {:pre [(vector? bindings)
         (even? (count bindings))
         (every? symbol? (take-nth 2 bindings))]}
  (if (= (count bindings) 0)
    `(do ~@body)
    `(let [~(bindings 0) (start ~(bindings 1))]
       (try
         (with-components ~(subvec bindings 2) ~@body)
         (finally (stop ~(bindings 0)))))))

(defn speculate [{:keys [datomic]} & txs]
  (reduce
    (fn [db tx] (:db-after (d/with db tx)))
    (d/db (:conn datomic))
    txs))

(defn speculate* [{:keys [datomic]} tx]
  (d/with (d/db (:conn datomic)) tx))
