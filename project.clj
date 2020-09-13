(defproject dev.gpeltier/graphique "0.1.0"
  :description "Utility to visualize and interact with graph data from the
                REPL"
  :url "https://github.com/gfpeltier/graphique"
  :license {:name "GNU LGPL-3.0"
            :url "https://www.gnu.org/licenses/lgpl-3.0.en.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [cljfx "1.7.9"]
                 [org.clojure/clojurescript "1.10.764"]
                 [com.cognitect/transit-cljs "0.8.264"]
                 [reagent "1.0.0-alpha2"]
                 [cljsjs/d3 "5.12.0-0"]]
  :plugins [[lein-cljsbuild "1.1.8"]]
  :repl-options {:init-ns graphique.core}
  :source-paths ["src/clj"]
  :cljsbuild {:builds [{:source-paths ["src/cljs"]
                        :compiler {:output-to "resources/public/js/core.js"
                                   :optimizations :whitespace
                                   :pretty-print true}}]})
