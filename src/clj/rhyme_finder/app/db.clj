(ns rhyme-finder.app.db
  (:require [datomic.api :as d]))

(def uri "datomic:free://localhost:4334/rhymer")
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

(defn add-poem-analysis!
  "Takes a poem's title, the poem's text, and the poem's analysis and
   stores it in the db"
  [title text analysis]
  (let [conn (connect-db!)]
    @(d/transact conn [{:db/id #db/id[:db.part/user]
                        :poem/title title
                        :poem/text text
                        :poem/analysis analysis}])))

(defn get-analysis [title]
  (let [conn (connect-db!)
        query '[:find ?analysis :in $ ?title
                :where
                [?poem :poem/title ?title]
                [?poem :poem/analysis ?analysis]]]
    (when-let [results (first (d/q query (d/db conn) title))]
      (-> results first read-string))))
