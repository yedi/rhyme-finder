(defproject rhyme-finder "0.1.0-SNAPSHOT"
  :description "Rhyme Analysis Library"
  :url "https://github.com/yedi/rhyme-finder"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :source-paths ["src/cljx"]

  :plugins [[com.keminglabs/cljx "0.3.2"]]
  :cljx {:builds [{:source-paths ["src/cljx"]
                   :output-path "target/classes"
                   :rules :clj}

                  {:source-paths ["src/cljx"]
                   :output-path "target/classes"
                   :rules :cljs}]})
