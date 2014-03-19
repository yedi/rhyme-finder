(ns rhyme-finder.rhymer
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]]
                   [dommy.macros :refer [node sel sel1]])
  (:require [goog.events :as events]
            [cljs.core.async :refer [<! >! put! take! chan]]
            [cljs.reader :refer [read-string]]
            [dommy.core :as dom]
            [ajax.core :refer [GET POST]]
            [clojure.string :as str]
            [om.core :as om]
            [om.dom :as d]))

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


(defn gen-app-state [init-data comms]
  (assoc init-data :comms comms :viewing nil))

(def controls-ch
  (chan))

(def api-ch
  (chan))

(def app-state
  (let [init-data (cljs.reader/read-string
                   (str/replace js/initial_app_state #"&quot;" "\""))]
    (atom (gen-app-state init-data {:controls control-ch :api api-ch}))))

(defn header [data owner]
  (reify
    om/IRender
    (render [this]
      (d/div #js {:className "page-header"}
        (d/ul #js {:className "nav nav-pills pull-right"}
          (d/li #js {:id "view-analyses-btn"
                     :className (when (= (:viewing data) :get-analysis) "active")}
            (d/a #js {:href "#"
                      :onClick #(om/update! data :viewing :get-analysis)}
               "View Analyses"))
          (d/li #js {:id "analyze-poem-btn"
                     :className (when (= (:viewing data) :post-analysis) "active")}
            (d/a #js {:href "#"
                      :onClick #(om/update! data :viewing :post-analysis)}
               "Analyze Poem"))
          (d/li nil
            (d/a #js {:href "http://github.com/yedi/rhyme-finder"}
              "View on Github")))
        (d/h3 #js {:className "text-muted"} "Reasoned Rhymer")))))

(defn post-view [data owner]
  (reify
    om/IRender
    (render [this]
       (d/div #js {:id "add-poem" :className "nav-section"}
         (d/form #js {:id "add-poem-form" :className "form-info"}
           (d/button #js {:type "submit" :className "btn btn-info"}
              "Analyze poem or song")
           (d/input #js {:id "poem-title" :type "text" :className "form-control"
                         :placeholder "Name of poem or song"})
           (d/textarea #js {:id "poem-text" :rows 16 :className "form-control"
                         :placeholder "Lyrics go here"}))))))

(defn handle-change [e owner]
  (print (.. e -target -value))
  (om/set-state! owner :selected (.. e -target -value)))

(defn get-view [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:get (chan) :resp (chan) :selected ""})
    om/IWillMount
    (will-mount [_]
      (let [get-ch (om/get-state owner :get)
            resp-ch (om/get-state owner :resp)]
        (go-loop []
          (let [getting (<! get-ch)]
            (GET "/analysis"
                  {:params {:title (om/get-state owner :selected)}
                   :handler (fn [resp] (print resp) (om/update! data :analysis resp))})
            (print getting))
          (recur))))
    om/IRender
    (render [this]
      (let [selected (om/get-state owner :selected)
            get-ch (om/get-state owner :get)]
        (d/div #js {:id "view-analyses" :className "nav-section"}
          (d/form #js {:id "get-analysis-form" :className "form-inline"}
            (apply d/select #js {:id "select-title"
                                 :onChange #(handle-change % owner)}
              (map (fn [title]
                     (let [selected (= (om/get-state owner :selected) title)]
                       (d/option #js {:selected (when selected "true")} title)))
                   (:titles data)))
            (d/button #js {:type "button" :className "btn btn-info"
                           :onClick (fn [e] (put! get-ch selected) false)}
              "See Analysis"))
          (d/div nil (get-in data [:analysis :text])))))))
 
(defn app [data owner opts]
  (reify
    om/IRender
    (render [this]
      (d/div nil
        (om/build header data)
        (cond
           (= (get-in data [:viewing]) :get-analysis) (om/build get-view data)
           (= (get-in data [:viewing]) :post-analysis) (om/build post-view data)
           :else (d/h2 nil "No View Hooked Up"))))))

(defn start [target state]
  (let [comms (:comms state)]
    (om/root
     app
     state
     {:target target
      :opts {:comms comms}})))

@app-state

(start (sel1 :#app) app-state)

