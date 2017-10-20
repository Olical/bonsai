(ns bonsai.core
  (:require [cljs.spec.alpha :as s]))

(defn comparable [vnode]
  (cond
    (string? vnode) vnode
    (nil? vnode) vnode
    (vector? vnode) (first vnode)
    :else (js/console.error "Could not get comparable from" vnode)))

(defn doc [node]
  (.-ownerDocument node))

(defn build-node [doc vnode]
  (cond
    (string? vnode) (.createTextNode doc vnode)
    (vector? vnode) (.createElement doc (name (first vnode)))
    :else (js/console.error "Could not build node from" vnode)))

(defn remove-node [node]
  (.removeChild (.-parentNode node) node))

(defn add-node [node vnode]
  (.appendChild node (build-node (doc node) vnode)))

(defn replace-node [node vnode]
  (.replaceChild (.-parentNode node) (build-node (doc node) vnode) node))

(defn children [vnode]
  (cond
    (vector? vnode) (rest vnode)
    :else nil))

(defn render [mount old new]
  (prn "trace" mount old new)
  (let [co (comparable old)
        cn (comparable new)]
    (when (and co (not cn))
      (remove-node mount))
    (when (and (not co) cn)
      (add-node mount new))
    (when (and co cn (not= co cn))
      (replace-node mount cn)))
  (let [child-nodes (array-seq (.-childNodes mount))]
    (doall (map render child-nodes (children old) (children new)))))
