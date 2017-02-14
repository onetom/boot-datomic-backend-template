(ns app.utils
  (:require
    [debux.core :as dx]                                     ; Just to establish ns alias for Cursive
    [clojure.spec :as s]
    [clojure.spec.gen :as gen]
    [datomic.api :as d]))

(defn gen [spec & assocs]
  (merge (-> spec s/gen gen/generate)
         (apply hash-map assocs)))

(defn conn [sys] (-> sys :datomic :conn))

(defn latest-db [sys] (-> sys :datomic :conn d/db))

(defn txv
  "Transaction results as a vector"
  [{:keys [tempids db-after db-before tx-data]}]
  [tempids db-after db-before tx-data])

(defn tx! [sys tx]
  (-> sys conn (d/transact tx) deref txv))
