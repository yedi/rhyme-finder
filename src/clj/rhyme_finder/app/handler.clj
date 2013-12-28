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
  (GET "/" [] (resp/redirect "/client.html"))
  (route/resources "/")
  (route/not-found "Page not found"))

(defn wrap-dir-index [handler]
  (fn [req]
    (handler
     (update-in req [:uri]
                #(if (= "/" %) "/client.html" %)))))

(def app
  (-> app-routes
      handler/site
      (wrap-dir-index)))

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
