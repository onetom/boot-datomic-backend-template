(ns app.users
  (:use app.utils)
  (:require
    [clojure.spec :as s]
    [clojure.spec.gen :as gen]
    [datomic.api :as d]
    [castra.core :refer [defrpc ex *session*]]
    [system.repl :refer [system]]
    [debux.core :as dx :refer :all]
    [environ.core :refer [env]]
    [buddy.sign.jwt :as jwt]))

(def admin-domain "company.com")
(def domain-gen (gen/elements [admin-domain "gmail.com" "hotmail.com" "computer.org" "example.com"]))
(def email-gen
  (gen/fmap (fn [[name domain-name]] (str name "@" domain-name))
            (gen/tuple (gen/not-empty (gen/string-alphanumeric)) domain-gen)))

(s/def :user/email (-> (s/spec string?)
                       (s/with-gen (fn [] email-gen))))

(s/def :user/full-name (-> (s/spec string?)
                           (s/with-gen #(gen/string-alphanumeric))))

(s/def :org/name (-> (s/spec string?)
                     (s/with-gen #(gen/string-alphanumeric))))

(s/def :a.user/rnd (s/keys :req [:user/email
                                 :user/full-name]
                           :opt [:org/name]))

(defn reg [{:keys [db/id user/full-name org/name] :as user}]
  (let [user-id (or id "user-id")
        user+org (assoc user :db/id user-id
                             :org/name (or name full-name)
                             :user/orgs [user-id])]
    [user+org]))

(defn reg! [sys user]
  (->> (reg user)
       (tx! sys)))
