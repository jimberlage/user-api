(defproject user-api "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[clj-time "0.14.2"]  
                 [compojure "1.5.1"]
                 [me.raynes/fs "1.4.6"]
                 [org.clojure/clojure "1.9.0"]
                 [org.clojure/data.csv "0.1.4"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/tools.cli "0.3.5"]
                 [prismatic/schema "1.1.7"]
                 [ring/ring-defaults "0.2.1"]]
  :main user-api.core
  :aot [user-api.core]
  :plugins [[lein-bin/lein-bin "0.3.5"]
            [lein-ring "0.9.7"]]
  :bin {:name "user-api"
        :bin-path "bin"}
  :ring {:handler user-api.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
