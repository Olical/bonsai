(ns bonsai.dom
  (:require [clojure.zip :as zip]))

;; Learned a lot about zipping up hiccup from this file.
;; https://github.com/davidsantiago/tinsel/blob/master/src/tinsel/zip.clj

(defn tree-children
  "Get the children of a Bonsai tree node."
  [node]
  (if (map? (second node))
    (drop 2 node)
    (rest node)))

(defn make-tree
  "Build a Bonsai tree node from a node and some children."
  [node children]
  (if (map? (second node))
    (into (subvec node 0 2) children)
    (apply vector (first node) children)))

(defn tree-zipper
  "Constructs a zipper for a Bonsai tree, which is basically Hiccup."
  [tree]
  (zip/zipper vector? tree-children make-tree tree))

(defn dom-children
  "Get the children of a DOM node."
  [node]
  (array-seq (.-childNodes node)))

(defn dom-node?
  "Is it a DOM node? Just checks for a .-nodeName."
  [node]
  (string? (.-nodeName node)))

(defn dom-zipper
  "Constructs a zipper from a DOM tree."
  [root]
  (zip/zipper dom-node? dom-children nil root))

(defn render!
  "Walks the 'to' tree and compares it to the old 'from' tree as we traverse the
  DOM from the 'root' node. It assumes the 'root' is in sync with 'from'.

  If we encounter something that's in 'to' but isn't in 'from', it's added. If
  something is missing from 'to' but is in 'from', we remove it."
  [root from to]
  (loop [dz (dom-zipper root)
         fz (tree-zipper from)
         tz (tree-zipper to)]
    (if (zip/end? tz)
      (zip/root tz)
      (let [dn (zip/node dz)
            fn (zip/node fz)
            tn (zip/node tz)]
        (prn (.-outerHTML dn) fn tn)
        (recur (zip/next dz)
               (zip/next fz)
               (zip/next tz))))))
