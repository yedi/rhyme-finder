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

(defn ajax-get [uri data]
  (let [out (chan)]
    (GET uri {:params data
              :handler (fn [resp] (put! out resp))})
    out))

(defn ajax-post! [uri data]
  (let [out (chan)]
    (POST uri {:params data
               :handler (fn [resp] (put! out resp))})
    out))

(defn handle-new-analysis []
  (print "handling new analysis")
  (let [out (chan)
        poem-title (.-value (sel1 :#poem-title))
        poem-txt (.-value (sel1 :#poem-text))
        req (ajax-post! "/analyze" {:title poem-title :text poem-txt})]
    (take! req (fn [resp] (put! out resp)))
    out))

(defn handle-get-analysis []
  (let [out (chan)
        title (.-value (sel1 :#select-title))
        req (ajax-get "/analysis" {:title title})]
    (print (str "retrieving analysis: " title))
    (take! req (fn [resp] (put! out resp)))
    out))

(defn handle-nav [btn div]
  (print "handling nav")
  (mapv #(dom/add-class! % :hide) (sel :.nav-section))
  (dom/remove-class! div :hide)
  (mapv #(dom/remove-class! % :active) (sel [:.nav :li]))
  (dom/add-class! btn :active))

(defn init []
  ;; for handling new poem additions
  (let [new-analyses (listen (sel1 :#add-poem-form) "submit" true)]
    (go (while true
          (<! new-analyses)
          (print (<! (handle-new-analysis)))
          (print "analysis added to the database"))))

  ;; for handling get analysis requests
  (let [get-analysis (listen (sel1 :#get-analysis-form) "submit" true)]
    (go (while true
          (<! get-analysis)
          (print (<! (handle-get-analysis))))))

  ;; for handling navigation
  (let [view-btn (sel1 :#view-analyses-btn)
        analyze-btn (sel1 :#analyze-poem-btn)
        viewing (listen view-btn "click")
        analyzing (listen analyze-btn "click")]
    (go (while true
          (alt!
           viewing (handle-nav view-btn (sel1 :#view-analyses))
           analyzing (handle-nav analyze-btn (sel1 :#add-poem)))))))

(init)
