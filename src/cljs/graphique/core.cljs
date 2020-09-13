(ns graphique.core
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [cognitect.transit :as t]
            [graphique.graph :as graph]))

(def counter (r/atom 0))

(defn root []
  (let [data (t/read (t/reader :json) (.-data js/window))]
    [:div
     [graph/graph (clj->js data)]]))

(defn test []
  (swap! counter inc))

(rd/render [root]
           (.-body js/document))