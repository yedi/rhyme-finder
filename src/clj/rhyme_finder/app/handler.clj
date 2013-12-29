(ns rhyme-finder.app.handler
  (:use compojure.core)
  (:use ring.middleware.edn)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]
            [ring.util.response :as resp]
            [ring.adapter.jetty :refer [run-jetty]]
            [rhyme-finder.core :as rhyme]
            [rhyme-finder.app.db :as db]
            [selmer.parser :as selmer]))

(defn generate-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})

(defn analyze!
  ([filename]
     (analyze! filename (slurp filename)))
  ([title txt]
     (let [poem (rhyme/format-as-poem txt)
           rs (rhyme/rhyme-streams poem 2 6 36 2)]
       (db/add-poem-analysis! title txt (pr-str rs))
       rs)))

(defn new-analysis [req]
  (let [title (-> req :params :title)
        txt (-> req :params :text)
        rs (analyze! title txt)]
    (generate-response (rhyme/rhyme-combos rs))))

(selmer/cache-off!)

(defroutes app-routes
  (GET "/" [] (selmer/render-file "rhyme_finder/app/client.html"
                                  {:titles (db/get-all-titles)}))
  (POST "/analyze" req (new-analysis req))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (-> app-routes
      wrap-edn-params
      handler/site))

(defn start-server [port]
  (run-jetty app {:port port :join? false}))

(defn -main [& args]
  (let [port (Integer. (or (first args) "3000"))]
    (start-server port)))

