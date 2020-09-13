(ns graphique.core
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.set :as cset]
            [cljfx.api :as fx])
  (:import (javafx.scene.web WebView)
           (javafx.event EventHandler)))


(def test-data [{:id 0
                 :name "Italian"
                 :dependents [1 2 3]}
                {:id 1
                 :name "Pizza"}
                {:id 2
                 :name "Pasta"}
                {:id 3
                 :name "Calzone"}
                {:id 4
                 :name "Mexican"
                 :dependents [5 6 7]}
                {:id 5
                 :name "Taco"}
                {:id 6
                 :name "Fajita"}
                {:id 7
                 :name "Burrito"}])

(def base-url (.toString (.toURL (.toURI (io/file "resources/public/index.html")))))
(def root-comp (atom nil))

(defn web-pane [{:keys [state]}]
  {:fx/type    fx/ext-on-instance-lifecycle
   :on-created (fn [cmp]
                 (let [engine (.getEngine ^WebView cmp)
                       window (.executeScript engine "window")]
                   (.setOnError engine
                                (reify EventHandler
                                  (handle [_ e]
                                    (println e))))
                   (.setOnAlert engine
                                (reify EventHandler
                                  (handle [_ e]
                                    (println e))))
                   (.setMember window "data" (json/write-str state))))
   :desc       {:fx/type     :web-view
                :pref-height 1000
                :pref-width  1500
                :url         base-url}})

(defn body-pane [{:keys [state]}]
  {:fx/type :v-box
   :padding 10
   :spacing 10
   :children [{:fx/type web-pane
               :state state}]})

(defn root [state]
  {:fx/type :stage
   :showing true
   :title "Graph Viewer"
   :scene {:fx/type :scene
           :root {:fx/type body-pane :state state}}})

(defn normalize-data [data {:keys [kid kname kdependents]
                            :or {kid :id kname :name kdependents :dependents}}]
  (let [nnames (cond-> {}
                 kid (assoc kid :id)
                 kname (assoc kname :name)
                 kdependents (assoc kdependents :dependents))]
    {:nodes (map #(cset/rename-keys % nnames) data)
     :links (for [n data
                  d (get n kdependents)]
              {:source (n kid)
               :target d})}))

;; Ok so this is pretty neat but I need a reference to the webengine
;; object that is encapsulated by the web-view in order to communicate
;; with the CLJS UI...
(defn view
  ([data] (view data {}))
  ([data opts]
   (reset! root-comp
           (fx/on-fx-thread
             (fx/create-component
               (root (normalize-data data opts)))))))
