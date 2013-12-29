(ns rhyme-finder.app.handler
  (:use compojure.core)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]
            [ring.util.response :as resp]
            [ring.adapter.jetty :refer [run-jetty]]
            [rhyme-finder.core :as rhyme]
            [rhyme-finder.app.db :as db]))

(defroutes app-routes
  (GET "/" [] (resp/file-response "src/clj/rhyme_finder/app/client.html"))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (-> app-routes
      handler/site))

(defn start-server [port]
  (run-jetty app {:port port :join? false}))

(defn -main [& args]
  (let [port (Integer. (or (first args) "3000"))]
    (start-server port)))

(defn analyze
  ([filename]
     (analyze filename (slurp filename)))
  ([title txt]
     (let [poem (rhyme/format-as-poem txt)
           rs (rhyme/rhyme-streams poem 2 6 36 2)]
       (db/add-poem-analysis! title txt (pr-str rs))
       rs)))
