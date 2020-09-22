(ns graphique.core
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.set :as cset]
            [clojure.spec.alpha :as s]
            [cljfx.api :as fx])
  (:import (javafx.scene.web WebView)))


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

(def base-url (-> (io/file "resources/public/index.html")
                  (.toURI)
                  (.toURL)
                  (.toString)))

(defn web-pane [{:keys [state]}]
  {:fx/type    fx/ext-on-instance-lifecycle
   :on-created (fn [cmp]
                 (let [engine (.getEngine ^WebView cmp)
                       window (.executeScript engine "window")]
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

(defn normalize-data [data {:keys [id name dependents]
                            :or   {id :id name :name dependents :dependents}
                            :as   keymap}]
  {:nodes (map #(cset/rename-keys % keymap) data)
   :links (for [n data
                d (get n dependents)]
            {:source (n id)
             :target d})})

(defn view
  ([data] (view data {}))
  ([data opts]
   (fx/on-fx-thread
     (fx/create-component
       (root (normalize-data data opts))))))
