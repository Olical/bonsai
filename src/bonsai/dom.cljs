(ns bonsai.dom
  (:require [bonsai.tree :as tree]))

(defn node->child [node n]
  (aget (.-childNodes node) n))

(defn path->node [node [n & path]]
  (if n
    (recur (node->child node n) path)
    node))

(defn tree->node-fn [document]
  (let [staging (.createElement document "div")]
    (fn [tree]
      (aset staging "innerHTML" (:html (tree/render tree)))
      (.-firstChild staging))))

(defn patch! [root diff]
  (let [tree->node (tree->node-fn (.-ownerDocument root))]
    (doseq [[action path item attr-value] diff]
      (case action
        :insert (let [parent (path->node root (butlast path))
                      target (node->child parent (last path))
                      node (tree->node item)]
                  (.insertBefore parent node target))
        :remove (let [target (path->node root path)
                      parent (.-parentNode target)]
                  (.removeChild parent target))
        :assoc (.setAttribute (path->node root path) (name item) attr-value)
        :dissoc (.removeAttribute (path->node root path) (name item))))))
