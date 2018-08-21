(ns bonsai.dom
  (:require [bonsai.tree :as tree]))

(defn node->child [node n]
  (aget (.-childNodes node) n))

(defn path->node [node [n & path]]
  (if n
    (recur (node->child node n) path)
    node))

(defn tree->node [tree staging]
  (aset staging "innerHTML" (tree/->html tree))
  (.-firstChild staging))

(defn patch! [root diff]
  (let [staging (-> root (.-ownerDocument) (.createElement "div"))]
    (doseq [[action path tree] diff]
      (case action
        :insert (let [parent (path->node root (butlast path))
                      target (node->child parent (last path))
                      node (tree->node tree staging)]
                  (.insertBefore parent node target))
        :remove (let [target (path->node root path)
                      parent (.-parentNode target)]
                  (.removeChild parent target))
        :replace (let [target (path->node root path)
                       parent (.-parentNode target)
                       node (tree->node tree staging)]
                   (.replaceChild parent node target))))))
