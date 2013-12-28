(ns rhyme-finder.app.db
  (:require [datomic.api :as d]))

(def uri "datomic:mem://rhymer")
(def schema (read-string (slurp "src/clj/rhyme_finder/app/schema.edn")))

(defn create-db! []
  (d/create-database uri))

(defn connect-db! []
  (d/connect uri))

(defn init-db! []
  (create-db!)
  (let [conn (connect-db!)]
    @(d/transact conn schema)
    conn))