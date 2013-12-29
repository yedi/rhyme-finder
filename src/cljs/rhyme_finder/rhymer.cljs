(ns rhyme-finder.rhymer
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require [goog.dom :as dom]
            [goog.events :as events]
            [cljs.core.async :refer [<! >! put! chan]]
            [cljs.reader :refer [read-string]])
  (:import [goog.net Jsonp]
           [goog Uri]))

(enable-console-print!)

(defn listen
  ([el type] (listen el type false))
  ([el type prevent?]
     (let [out (chan)]
       (events/listen el type
         (fn [e] (put! out e)
           (cond prevent? (.preventDefault e))))
       out)))

(defn init []
  (let [new-analyses (listen (dom/getElement "add-poem-form") "submit" true)]
    (go (while true
          (<! new-analyses)
          (print "got new analysis")))))

(init)
