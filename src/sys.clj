(ns sys
  (:refer-clojure :exclude [test])
  (:require
    [com.stuartsierra.component :as component :refer [system-map using]]
    (system.components
      [jetty :refer [new-web-server]]
      [datomic :refer [new-datomic-db]])
    [app.schema :refer [new-schema]]
    [app.core :refer [make-ring-handler]]
    [app.demo]
    [datomic.api :as d]
    [environ.core :refer [env]]
    [clojure.string :as str]))

(defrecord RingHandler [handler]
  component/Lifecycle
  (start [component]
    (assoc component :handler (make-ring-handler)))
  (stop [_]))

(defn new-ring-handler []
  (map->RingHandler {}))

(defn in-memory-db? [datomic]
  (-> datomic :uri (str/starts-with? "datomic:mem:")))

(defrecord DemoData [datomic schema]
  component/Lifecycle
  (start [this]
    (when (in-memory-db? datomic) (app.demo/setup this))
    :ok)
  (stop [_]))

(defn new-demo-data []
  (map->DemoData {}))

(defn dev []
  (let [db-uri (or (env :datomic-uri)
                   "datomic:mem://dev")]
    (system-map
      :datomic (new-datomic-db db-uri)
      :schema (using (new-schema) [:datomic])
      :demo-data (using (new-demo-data) [:schema :datomic])
      :handler (new-ring-handler)
      :web (using (new-web-server 8888) [:handler]))))

(defn test []
  (let [db-uri (str (gensym "datomic:mem://test-"))]
    (system-map
      :datomic (new-datomic-db db-uri)
      :schema (using (new-schema) [:datomic]))))
