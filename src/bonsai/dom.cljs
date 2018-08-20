(ns bonsai.dom
  (:require [clojure.string :as str]
            [bonsai.tree :as tree]))

;; TODO Nodes need some sort of ID target on them for path->id. Although [0] should just be the host. Prolly for tree/->html.

(defn patch! [host diff]
  (doseq [[action path tree] diff]
    (case action
      :insert (aset host "innerHTML" (tree/->html tree)))))
