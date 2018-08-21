(ns bonsai.dom
  (:require [clojure.string :as str]
            [bonsai.tree :as tree]))

(defn path->node [node [idx & path]]
  (if idx
    (recur (aget (.-children node) idx) path)
    node))

(defn patch! [host diff]
  (doseq [[action path tree] diff]
    (case action
      :insert (aset host "innerHTML" (tree/->html tree)))))
