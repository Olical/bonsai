(ns user
  (:require [cljs.repl :as repl]
            [cljs.repl.node :as node]
            [cemerick.piggieback :as piggieback]))

(defn cljs-repl []
  (piggieback/cljs-repl (node/repl-env)))
