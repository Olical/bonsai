(ns bonsai.dom
  (:require [clojure.string :as str]
            [bonsai.tree :as tree]))

(defn node->child [node n]
  (aget (.-childNodes node) n))

(defn path->node [node [n & path]]
  (if n
    (recur (node->child node n) path)
    node))

(defn patch! [root diff]
  (doseq [[action path tree] diff]
    (case action
      :insert (let [parent (path->node root (butlast path))
                    target (node->child parent (inc (last path)))
                    content (tree/->html tree)]
                (if target
                  (.insertAdjacentHTML target "beforebegin" content)
                  (.insertAdjacentHTML parent "beforeend" content))))))
