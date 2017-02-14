(ns app.users-test
  (:use app.utils [clojure.test :exclude [is]] test-helpers juxt.iota)
  (:require
    [sys]
    [app.users]
    [datomic.api :as d]))

(def org-names '[:user/full-name
                 {:user/orgs [:db/id :org/name]}])

(deftest reg-test
  (with-components [sys (sys/test)]
    (testing "When a user registers"
      (testing "they get an organization by default named after the user"
        (let [[_ db] (->> (gen :a.user/rnd
                               {:db/ident       :a-user
                                :user/full-name "User Full Name"}
                               [:org/name])
                          (app.users/reg! sys))]
          (given (d/pull db org-names :a-user)
            :user/full-name := "User Full Name"
            :user/orgs :> [{:db/id    (d/entid db :a-user)
                            :org/name "User Full Name"}])))

      (testing "they can customize their name as an organization"
        (let [[{:strs [user-id]}
               db] (->> (gen :a.user/rnd
                             {:db/id          "user-id"
                              :user/full-name "User Full Name"
                              :org/name       "Custom Org Name"})
                        (app.users/reg! sys))]
          (given (d/pull db org-names user-id)
            :user/full-name := "User Full Name"
            :user/orgs :> [{:db/id    user-id
                            :org/name "Custom Org Name"}])))

      (testing "with an already registered email address, registration fails"
        (let [{:keys [user/email] :as user} (gen :a.user/rnd)
              _first-reg (app.users/reg! sys user)]
          (is (thrown-with-msg? Exception #"Duplicate email"
                                (app.users/reg! sys user))))))))

(comment
  (with-components [sys (sys/test)]
    (->> (gen :a.user/rnd
              {;:db/id "user-id"
               :user/full-name "Full Name"})
         (app.users/reg-tx (latest-db sys)))))
