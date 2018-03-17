(ns bonsai.tree
  (:require [orchestra.core #?(:clj :refer, :cljs :refer-macros) [defn-spec]]
            [#?(:clj clojure.spec.alpha, :cljs cljs.spec.alpha) :as s]))

(s/def ::kind keyword?)
(s/def ::tree (s/nilable
               (s/cat :kind ::kind
                      :children (s/* ::tree))))
(s/def ::trees (s/* ::tree))

(defmulti change ::op)
(defmethod change ::insert-node [_]
  (s/keys :req [::op ::path ::kind]))
(defmethod change ::remove-node [_]
  (s/keys :req [::op ::path]))

(s/def ::path (s/coll-of integer? :kind vector?))
(s/def ::change (s/multi-spec change ::op))
(s/def ::diff (s/coll-of ::change :kind vector?))

(defn-spec kind (s/nilable ::kind)
  "Get the kind of a node."
  [node ::tree]
  (first node))

(defn-spec children ::trees
  "Get the children of a node, if any."
  [node ::tree]
  (rest node))

(defn- diff* [{:keys [xs ys path]} frames acc]
  (loop [xs xs
         ys ys
         index 0
         frames frames
         acc acc]
    (if (or (seq xs) (seq ys))
      (let [[x & xs] xs
            [y & ys] ys
            xcs (children x)
            ycs (children y)
            path (conj path index)
            action (cond
                     (= (kind x) (kind y)) :skip
                     (nil? x) :insert
                     (nil? y) :remove
                     :else :replace)]
        (recur xs
               ys
               (cond-> index
                 y inc)
               (cond
                 (= action :replace) (conj frames {:xs nil
                                                  :ys ycs
                                                  :path path})
                 (= xcs ycs) frames
                 :else (conj frames {:xs xcs
                                     :ys ycs
                                     :path path}))
               (case action
                 :skip acc
                 :insert (conj acc {::op ::insert-node
                                    ::path path
                                    ::kind (kind y)})
                 :remove (conj acc {::op ::remove-node
                                    ::path path})
                 :replace (conj acc
                                {::op ::remove-node
                                 ::path path}
                                {::op ::insert-node
                                 ::path path
                                 ::kind (kind y)}))))
      {:frames frames
       :acc acc})))

(defn-spec diff ::diff
  "Find the diff between two trees."
  [x ::tree, y ::tree]
  (loop [[{:keys [xs ys path] :as frame} & frames] [{:xs [x], :ys [y], :path []}]
         acc []]
    (if frame
      (let [{:keys [frames acc]} (diff* frame frames acc)]
        (recur frames acc))
      acc)))
