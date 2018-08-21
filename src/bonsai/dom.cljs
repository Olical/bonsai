(ns bonsai.dom
  (:require [bonsai.tree :as tree]))

(defn node->child [node n]
  (aget (.-childNodes node) n))

(defn path->node [node [n & path]]
  (if n
    (recur (node->child node n) path)
    node))

(defn patch! [root diff]
  (let [staging (-> root (.-ownerDocument) (.createElement "div"))]
    (doseq [[action path tree] diff]
      (case action
        :insert (let [parent (path->node root (butlast path))
                      target (node->child parent (last path))
                      node (do
                             (aset staging "innerHTML" (tree/->html tree))
                             (.-firstChild staging))]
                  (.insertBefore parent node target))
        :remove (let [target (path->node root path)
                      parent (.-parentNode target)]
                  (.removeChild parent target))))))
