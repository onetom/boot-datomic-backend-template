(ns app.users
  (:use app.utils)
  (:require
    [clojure.spec :as s]
    [clojure.spec.gen :as gen]
    [datomic.api :as d]
    [datomic-schema.schema :refer :all]

    [castra.core :refer [defrpc ex *session*]]
    [system.repl :refer [system]]
    [environ.core :refer [env]]
    [buddy.sign.jwt :as jwt]))

(def admin-domain "company.com")

(def domain-generator
  (gen/elements [admin-domain "gmail.com" "hotmail.com" "computer.org" "example.com"]))

(def email-generator
  (gen/fmap (fn [[name domain-name]] (str name "@" domain-name))
            (gen/tuple (gen/not-empty (gen/string-alphanumeric))
                       domain-generator)))

(s/def :user/email (-> (s/spec string?)
                       (s/with-gen (fn [] email-generator))))

(s/def :user/full-name (-> (s/spec string?)
                           (s/with-gen #(gen/string-alphanumeric))))

(s/def :org/name (-> (s/spec string?)
                     (s/with-gen #(gen/string-alphanumeric))))

(s/def :a.user/rnd (s/keys :req [:user/email
                                 :user/full-name]
                           :opt [:org/name]))

(defdbfn reg-tx [db user] :db.part/user
  (let [{:keys [db/id user/email user/full-name org/name]} user]
    (when (d/entity db [:user/email email])
      (-> (str "Duplicate email: " email) RuntimeException. throw))
    (let [user-id (or id "user-id")]
      [(assoc user :db/id user-id
                   :org/name (or name full-name)
                   :user/orgs [user-id])])))

(defn reg! [sys user]
  (->> [[:reg-tx user]]
       (tx! sys)))
