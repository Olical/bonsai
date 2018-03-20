(ns bonsai.tree
  (:require [orchestra.core #?(:clj :refer, :cljs :refer-macros) [defn-spec]]
            [#?(:clj clojure.spec.alpha, :cljs cljs.spec.alpha) :as s]))

(s/def ::kind keyword?)
(s/def ::tree (s/or
               :nothing nil?
               :text string?
               :node (s/cat :kind ::kind
                            :children (s/* ::tree))))
(s/def ::trees (s/* ::tree))

(defmulti change ::op)
(defmethod change ::insert-node [_]
  (s/keys :req [::op ::path ::kind]))
(defmethod change ::remove-node [_]
  (s/keys :req [::op ::path]))
(defmethod change ::insert-text-node [_]
  (s/keys :req [::op ::path ::text]))

(s/def ::path (s/coll-of integer? :kind vector?))
(s/def ::change (s/multi-spec change ::op))
(s/def ::diff (s/coll-of ::change :kind vector?))

(defn-spec kind (s/nilable ::kind)
  "Get the kind of a node."
  [node ::tree]
  (cond
    (string? node) ::text
    :else (first node)))

(defn-spec children ::trees
  "Get the children of a node, if any."
  [node ::tree]
  (cond
    (= (kind node) ::text) nil
    :else (rest node)))

(defn- insert-op [node path]
  (merge {::path path}
         (cond
           (= (kind node) ::text) {::op ::insert-text-node
                                   ::text node}
           :else {::op ::insert-node
                  ::kind (kind node)})))

(defn- remove-op [node path]
  {::op ::remove-node
   ::path path})

(defn- diff* [{:keys [xs ys path]} groups acc]
  (loop [xs xs
         ys ys
         index 0
         groups groups
         acc acc]
    (if (or (seq xs) (seq ys))
      (let [[x & xs] xs
            [y & ys] ys
            xcs (children x)
            ycs (children y)
            path (conj path index)
            action (cond
                     (and (= (kind x) (kind y))
                          (not= (kind x) ::text)) :skip
                     (nil? x) :insert
                     (nil? y) :remove
                     :else :replace)]
        (recur xs
               ys
               (cond-> index
                 y inc)
               (cond
                 (= action :replace) (conj groups {:xs nil
                                                  :ys ycs
                                                  :path path})
                 (= xcs ycs) groups
                 :else (conj groups {:xs xcs
                                     :ys ycs
                                     :path path}))
               (case action
                 :skip acc
                 :insert (conj acc (insert-op y path))
                 :remove (conj acc (remove-op x path))
                 :replace (conj acc (remove-op x path) (insert-op y path)))))
      [groups acc])))

(defn-spec diff ::diff
  "Find the diff between two trees."
  [x ::tree, y ::tree]
  (loop [[{:keys [xs ys path] :as group} & groups] [{:xs [x], :ys [y], :path []}]
         acc []]
    (if group
      (let [[groups acc] (diff* group groups acc)]
        (recur groups acc))
      acc)))
