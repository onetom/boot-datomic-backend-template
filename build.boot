; To inform IntelliJ explicitely about deftask, set-env!, task-options!
(require '[boot.core :refer :all]
         '[boot.task.built-in :refer :all])

(set-env!
  :source-paths #{"src" "test"}
  :dependencies
  '[[org.clojure/clojure "1.9.0-alpha14" :scope "provided"]
    [org.clojure/test.check "0.9.0" :scope "test"]
    [onetom/boot-lein-generate "0.1.3" :scope "test"]
    [tailrecursion/boot-jetty "0.1.3"]
    [org.danielsz/system "0.4.0"]
    [ring "1.6.0-beta7"]
    [buddy "1.3.0"]
    [philoskim/debux "0.2.1"]
    [hoplon/castra "3.0.0-alpha7"]
    [jumblerg/ring.middleware.cors "1.0.1"]
    [com.datomic/datomic-pro "0.9.5554"]
    [boot/core "2.7.1"]
    [adzerk/bootlaces "0.1.13" :scope "test"]
    [datomic-schema "1.3.0"]
    [juxt/iota "0.2.3" :scope "test"]
    ;[amazonica "0.3.86" :exclusions [com.amazonaws/amazon-kinesis-client
    ;                                 com.amazonaws/aws-java-sdk]]
    ;[com.amazonaws/aws-java-sdk-core "1.11.89"]
    ;[com.amazonaws/aws-java-sdk-s3 "1.11.89"]
    ;[com.amazonaws/aws-java-sdk-dynamodb "1.11.89"]
    [environ "1.1.0"]
    [boot-environ "1.1.0"]
    [joda-time "2.9.7"]
    [org.clojure/data.json "0.2.6"]
    [org.clojure/data.fressian "0.2.1"]])

(def +version+ "1.1")
(task-options!
  pom {:project 'onetom/boot-datomic-backend-template
       :version +version+})

(require 'boot.lein)
(boot.lein/generate)

(require
  '[clojure.test]
  '[system.boot]
  '[sys]
  '[adzerk.bootlaces :refer :all]
  '[environ.boot :refer [environ]])

(bootlaces! +version+)

(task-options!
  system.boot/system {:auto true})

(defn test-all []
  (clojure.test/run-all-tests #"app\..*-test"))

(let [hostname (System/getenv "HOSTNAME")]
  (assert hostname "HOSTNAME can not be empty")
  (def dev-frontend-url (str "http://" hostname ":8100")))

(defn convenience-refers []
  (use 'app.utils)
  (require '[system.repl :refer [system]]
           '[datomic.api :as d]))

(deftask dev
  "Backend in development mode"
  []
  (convenience-refers)
  (comp
    (environ :env {:frontend-url dev-frontend-url})
    (repl :server true
          ; FIXME It should avoid printing namespaced maps
          ; but it doesn't seem to work in IntelliJ
          :eval '(set! *print-namespace-maps* false))
    (watch)
    (speak :theme "ordinance")
    (system.boot/system
      :sys #'sys/dev
      :files ["sys.clj" "core.clj" "demo.clj"])))

(deftask prod
  "Backend in production mode"
  []
  (convenience-refers)
  (comp
    (environ)
    (repl :server true)
    (system.boot/system :sys #'sys/dev)
    (wait)))
