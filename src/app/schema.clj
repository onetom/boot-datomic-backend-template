(ns app.schema
  (:require
    [datomic-schema.schema :refer :all]
    [datomic.api :as d]
    [com.stuartsierra.component :as component]
    [schema.core :as s]
    [clojure.string :as str]))

(defdbfn dbinc [db e a qty] :db.part/user
  [[:db/add e a (+ qty (or (get (d/entity db e) a) 0))]])

(defdbfn edit-tx [db user-id org-id profile] :db.part/user
  (if (-> '[:find (count ?user) .
            :in $ ?user ?org
            :where [?user :user/orgs ?org]]
          (d/q db user-id org-id))
    [(merge {:db/id org-id} profile)]
    (throw (RuntimeException.
             (str "User (" user-id ")"
                  " is not a member of the organization"
                  " (" org-id ").")))))

(defn dbparts []
  [(part "app")])

(defn dbschema []
  [(schema user
     (fields
       [email :string :unique-value]
       [hash :string "Hashed password string"]
       [full-name :string]
       [preferred-name :string]
       [phone :string]
       [confirmations :enum [:email
                             :terms-of-use
                             :privacy] :many]
       [title :string]
       [orgs :ref :many]))
   (schema org
     (fields
       [name :string]
       [country :keyword "2-letter ISO country code"]
       [address :string]))
   (schema permission
     (fields
       [of :ref]
       [to :ref :many]
       [level :enum [:read :write] :many]))])

(defrecord Schema [datomic]
  component/Lifecycle

  (start [_]
    (let [{:keys [uri conn]} datomic
          mem? (-> uri (str/starts-with? "datomic:mem:"))]
      @(d/transact conn
                   (concat
                     (generate-parts (dbparts))
                     (generate-schema (dbschema) {:index-all? true})
                     (dbfns->datomic dbinc edit-tx)))
      :ok))

  (stop [_]))

(defn new-schema []
  (map->Schema {}))
