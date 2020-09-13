(ns graphique.graph
  "Reagent graph component. Handles drawing/redrawing of D3 force graph.
  Unfortunately D3 is rather side-effecty so some aspects of this
  component are less than ideal. Look to improve in the future."
  (:require [reagent.core :as r]
            [cljsjs.d3]))


(defn build-svg! [gid width height]
  (let [root (.getElementById js/document gid)]
    (-> js/d3
        (.select root)
        (.selectAll "svg")
        (.remove))
    (-> js/d3
        (.select root)
        (.append "svg")
        (.attr "width" width)
        (.attr "height" height))))

(defn build-sim! [nodes links width height]
  (-> js/d3
      (.forceSimulation)
      (.nodes nodes)
      (.force "charge" (-> js/d3 (.forceManyBody) (.strength -200)))
      (.force "x" (-> js/d3 (.forceX (/ width 2)) (.strength 0.05)))
      (.force "y" (-> js/d3 (.forceY (/ height 2)) (.strength 0.05)))
      (.force "link" (-> js/d3
                         (.forceLink links)
                         (.id (fn [d] (.-id d)))
                         (.strength 0.07)))))

(def ^:const link-color "#c4c4c4")

(defn add-marker-defs [svg]
  (-> svg
      (.append "defs")
      (.append "marker")
      (.attr "id" "arrowhead")
      (.attr "viewBox" "0 -5 10 10")
      (.attr "refX" 15)
      (.attr "refY" -1.5)
      (.attr "markerWidth" 6)
      (.attr "markerHeight" 6)
      (.attr "orient" "auto")
      (.attr "fill" link-color)
      (.append "path")
      (.attr "d" "M0,-5L10,0L0,5")))

(def ^:const node-radius 5)

;; TODO: Allow users to set node colors
(defn node-color [n]
  "#ff867d")

(defn build-node-elems [svg nodes width height]
  (-> svg
      (.append "g")
      (.selectAll "circle")
      (.data nodes)
      (.enter)
      (.append "circle")
      (.attr "id" (fn [d] (str "node" (.-id d))))
      (.attr "cx" (/ width 2))
      (.attr "cy" (/ height 2))
      (.attr "r" node-radius)
      (.attr "fill" node-color)
      ;; TODO: Optional mouseover info?
      ))

;; TODO: Allow users to modify link style?
(defn build-link-elems [svg links]
  (-> svg
      (.append "g")
      (.selectAll "line")
      (.data links)
      (.enter)
      (.append "line")
      (.attr "class" "link")
      (.attr "stroke-width" 1)
      (.attr "stroke" link-color)
      (.attr "marker-end" "url(#arrowhead)")))

(defn build-label-elems [svg nodes]
  (-> svg
      (.append "g")
      (.selectAll "text")
      (.data nodes)
      (.enter)
      (.append "text")
      (.text (fn [n] (.-name n)))
      (.attr "fill" "black")
      (.attr "font-size" 15)
      (.attr "dx" 15)
      (.attr "dy" 4)))

(defn run-sim! [sim node-els link-els label-els width height]
  (-> sim
      (.on "tick"
           (fn []
             (-> node-els
                 (.attr "cx" (fn [n]
                               (set! (.-x n) (-> (.-x n)
                                                 (min (- width node-radius))
                                                 (max node-radius)))))
                 (.attr "cy" (fn [n]
                               (set! (.-y n) (-> (.-y n)
                                                 (min (- height node-radius))
                                                 (max node-radius))))))
             (-> link-els
                 (.attr "x1" (fn [l] (-> l .-source .-x)))
                 (.attr "x2" (fn [l] (-> l .-target .-x)))
                 (.attr "y1" (fn [l] (-> l .-source .-y)))
                 (.attr "y2" (fn [l] (-> l .-target .-y))))
             (-> label-els
                 (.attr "x" (fn [n] (.-x n)))
                 (.attr "y" (fn [n] (.-y n))))))))

(defn drag-drop-behavior
  "Make node drag/drop 'sticky'"
  [sim]
  (-> js/d3
      (.drag)
      (.on "start" (fn [n]
                     (set! (.-fx n) (.-x n))
                     (set! (.-fy n) (.-y n))
                     (set! (.-sx n) (.-x n))
                     (set! (.-sy n) (.-y n))))
      (.on "drag" (fn [n]
                    (-> sim (.alphaTarget 0.7) (.restart))
                    (set! (.-fx n) (-> js/d3 .-event .-x))
                    (set! (.-fy n) (-> js/d3 .-event .-y))))
      (.on "end" (fn [n]
                   (when-not (-> js/d3 .-event .-active)
                     (.alphaTarget sim 0))
                   (set! (.-fixed n) true)))))

(defn draw-graph! [gid gdata]
  (let [width (.-innerWidth js/window)
        height (.-innerHeight js/window)
        svg (build-svg! gid width height)
        ;; TODO... Should graph data be parsed here?... I guess but it should
        ;; be checked a level above...
        js-nodes (.-nodes gdata)
        js-links (.-links gdata)
        sim (build-sim! js-nodes js-links width height)
        links (build-link-elems svg js-links)
        nodes (build-node-elems svg js-nodes width height)
        labels (build-label-elems svg js-nodes)]
    (add-marker-defs svg)
    (run-sim! sim nodes links labels width height)
    (.call nodes (drag-drop-behavior sim))))

(defn graph [gdata]
  (let [gid (str (gensym "gmount"))]
    (r/create-class
      {:component-did-mount
       (fn [_]
         (draw-graph! gid gdata)
         (.addEventListener js/window "resize" #(draw-graph! gid gdata)))

       :reagent-render
       (fn [gdata]
         [:div {:id gid}])})))