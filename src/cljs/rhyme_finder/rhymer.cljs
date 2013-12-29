(ns rhyme-finder.rhymer
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]]
                   [dommy.macros :refer [node sel sel1]])
  (:require [goog.events :as events]
            [cljs.core.async :refer [<! >! put! take! chan]]
            [cljs.reader :refer [read-string]]
            [dommy.core :as dom]
            [ajax.core :refer [GET POST]]))

(enable-console-print!)

(defn listen
  ([el type] (listen el type false))
  ([el type prevent?]
     (let [out (chan)]
       (events/listen el type
         (fn [e] (put! out e)
           (cond prevent? (.preventDefault e))))
       out)))

(defn post! [uri data]
  (let [out (chan)]
    (POST uri {:params data
               :handler (fn [resp] (put! out resp))})
    out))

(defn handle-new-analysis []
  (print "handling new analysis")
  (let [out (chan)
        poem-title (.-value (sel1 :#poem-title))
        poem-txt (.-value (sel1 :#poem-text))
        req (post! "/analyze" {:title poem-title :text poem-txt})]
    (take! req (fn [resp] (put! out resp)))
    out))

(defn init []
  (let [new-analyses (listen (sel1 :#add-poem-form) "submit" true)]
    (go (while true
          (<! new-analyses)
          (print (<! (handle-new-analysis)))
          (print "analysis added to the database")))))

(init)
