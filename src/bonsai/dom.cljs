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
    (fn [tree path]
      (let [{:keys [html diff]} (tree/render tree path)]
        (aset staging "innerHTML" html)
        {:node (.-firstChild staging)
         :diff diff}))))

(defn attr-kw->listener-name [kw]
  (second (re-matches tree/listener-re (name kw))))

(defn patch! [root diff]
  (let [tree->node (tree->node-fn (.-ownerDocument root))]
    (doseq [[action path item attr-value] diff]
      (case action
        :insert (let [parent-path (butlast path)
                      parent (path->node root parent-path)
                      target (node->child parent (last path))
                      {node :node, render-diff :diff} (tree->node item parent-path)]
                  (.insertBefore parent node target)
                  (when (seq render-diff)
                    (patch! root render-diff)))
        :remove (let [target (path->node root path)
                      parent (.-parentNode target)]
                  (.removeChild parent target))
        :assoc (.setAttribute (path->node root path) (name item) attr-value)
        :dissoc (.removeAttribute (path->node root path) (name item))
        :add-listener (.addEventListener (path->node root path) (attr-kw->listener-name item) attr-value)
        :remove-listener (.removeEventListener (path->node root path) (attr-kw->listener-name item) attr-value)))))
