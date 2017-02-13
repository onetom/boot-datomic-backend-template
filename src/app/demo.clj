(ns app.demo
  (:use app.utils)
  (:require
    [clojure.repl :refer [pst]]

    [app.users]))

(defn setup! [sys]
  (doto sys
    (app.users/reg! (gen :a.user/rnd
                         :user/full-name "Buyer"
                         :user/email "buyer@example.com"
                         :org/name "Bill Investment Holdings"))

    (app.users/reg! (gen :a.user/rnd
                         :user/full-name "Seller"
                         :user/email "seller@example.com"
                         :org/name "Sam Capital")))

  sys)

(defn setup [sys]
  (try
    (setup! sys)
    (catch Exception e
      (println "Demo data error:")
      (pst e)
      e)))
