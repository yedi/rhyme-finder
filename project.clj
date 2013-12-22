(defproject rhyme-finder "0.1.0-SNAPSHOT"
  :description "Rhyme Analysis Library"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2127"]]
  :plugins [[lein-cljsbuild "1.0.1"]]
  :source-paths ["src/clj"]
  :cljsbuild {:builds [{
                        :id "rhyme-finder"
                        :source-paths ["src/cljs"]
                        :compiler {
                                   :output-to "resources/public/rhymer.js"
                                   :optimizations :none
                                   :output-dir "resources/public/out"
                                   ;:pretty-print true
                                   :source-map true}}]})
