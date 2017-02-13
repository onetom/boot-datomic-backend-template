(ns app.utils
  (:require
    [clojure.spec :as s]
    [clojure.spec.gen :as gen]
    [datomic.api :as d]))

(defn gen [spec & assocs]
  (apply assoc (-> spec s/gen gen/generate) assocs))

(defn txv
  "Transaction results as a vector"
  [{:keys [tempids db-after db-before tx-data]}]
  [tempids db-after db-before tx-data])

(defn tx! [sys tx]
  (-> sys :datomic :conn (d/transact tx) deref txv))
