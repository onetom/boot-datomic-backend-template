(ns app.users-test
  (:use app.utils [clojure.test :exclude [is]] test-helpers juxt.iota)
  (:require
    [sys]
    [app.users]
    [debux.core :as dx]
    [datomic.api :as d]))

(def org-names '[:user/full-name
                 {:user/orgs [:db/id :org/name]}])

(deftest reg-test
  (with-components [sys (sys/test)]
    (testing "When a user registers"
      (testing "they get an organization by default named after the user"
        (let [[{:strs [user-id]}
               db] (->> (dissoc (gen :a.user/rnd
                                     :db/id "user-id"
                                     :user/full-name "User Full Name")
                                :org/name)
                        (app.users/reg! sys))]
          (given (d/pull db org-names user-id)
            :user/full-name := "User Full Name"
            :user/orgs :> [{:db/id    user-id
                            :org/name "User Full Name"}])))

      (testing "they can customize their name as an organization"
        (let [[{:strs [user-id]}
               db] (->> (gen :a.user/rnd
                             :db/id "user-id"
                             :user/full-name "User Full Name"
                             :org/name "Custom Org Name")
                        (app.users/reg! sys))]
          (given (d/pull db org-names user-id)
            :user/full-name := "User Full Name"
            :user/orgs :> [{:db/id    user-id
                            :org/name "Custom Org Name"}])))

      #_(testing "with an already registered email address, registration fails"
          (let [user (gen-user :user/email "joe@x.y")]
            (register! sys user)
            (is (thrown-with-msg? Exception #"Duplicate email"
                                  (register! sys user))))))))

(comment
  (->> (gen :a.user/rnd
            ;:db/id "user-id"
            :user/full-name "Full Name")
       (app.users/reg)))
