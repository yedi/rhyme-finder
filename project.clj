(defproject rhyme-finder "0.1.0-SNAPSHOT"
  :description "Rhyme Analysis Library"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2127"]
                 [ring "1.1.6"]
                 [compojure "1.1.6"]
                 [com.datomic/datomic-free "0.9.4324"]]
  :plugins [[lein-cljsbuild "1.0.1"]
            [lein-ring "0.8.8"]]
  :source-paths ["src/clj"]
  :cljsbuild {:builds [{
                        :id "rhyme-finder"
                        :source-paths ["src/cljs"]
                        :compiler {
                                   :output-to "resources/public/rhymer.js"
                                   :optimizations :none
                                   :output-dir "resources/public/out"
                                   :source-map true}}]}
  :main rhyme-finder.app.handler
  :ring {:handler rhyme-finder.app.handler/app
         :auto-reload? true
         :auto-refresh true})
