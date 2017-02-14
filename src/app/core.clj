(ns app.core
  (:require
    [castra.core :refer [defrpc *session*]]
    [castra.middleware :refer [wrap-castra wrap-castra-session]]
    [ring.middleware.cors :refer [wrap-cors]]
    [app.users]))

(defn make-ring-handler []
  (-> (constantly {:body "Not a castra request"})
      (wrap-castra 'app.users)
      (wrap-castra-session (str "a 16-byte secret"))
      (wrap-cors
        #"http://localhost:8100"
        #"http://.*\.local:8100")))
