(ns bonsai.tree
  (:require [orchestra.core #?(:clj :refer, :cljs :refer-macros) [defn-spec]]
            [#?(:clj clojure.spec.alpha, :cljs cljs.spec.alpha) :as s]))

(s/def ::tree (s/or
               :nothing nil?
               :text string?
               :node (s/cat :kind ::kind
                            :children (s/* ::tree))))

(defmulti change ::op)
(defmethod change ::insert-node [_]
  (s/keys :req [::op ::path ::kind]))
(defmethod change ::remove-node [_]
  (s/keys :req [::op ::path] :opt [::dry?]))
(defmethod change ::insert-text-node [_]
  (s/keys :req [::op ::path ::text]))

(s/def ::dry? boolean?)
(s/def ::kind keyword?)
(s/def ::path (s/coll-of integer? :kind vector?))
(s/def ::change (s/multi-spec change ::op))

(defn-spec kind (s/nilable ::kind)
  "Get the kind of a node."
  [node ::tree]
  (first node))

(defn-spec children (s/* ::tree)
  "Get the children of a node, if any."
  [node ::tree]
  (next node))

(defn- insert-op [path kind]
  {::op ::insert-node
   ::path path
   ::kind kind})

(defn- remove-op [path dry?]
  (merge {::op ::remove-node
          ::path path}
         (when dry?
           {::dry? true})))

(defn- diff-group [{:keys [xs ys path]} groups acc]
  (let [dry? (nil? ys)]
    (loop [xs xs
           ys ys
           index 0
           groups groups
           acc acc]
      (if (or (seq xs) (seq ys))
        (let [[x & xs] xs
              [y & ys] ys
              xk (kind x)
              yk (kind y)
              xcs (children x)
              ycs (children y)
              path (conj path index)
              action (cond
                       (= xk yk) :skip
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
                   :insert (conj acc (insert-op path yk))
                   :remove (conj acc (remove-op path dry?))
                   :replace (conj acc (remove-op path dry?) (insert-op path yk)))))
        [groups acc]))))

(defn-spec diff (s/* ::change)
  "Find the diff between two trees."
  [x ::tree, y ::tree]
  (loop [[{:keys [xs ys path] :as group} & groups] [{:xs [x], :ys [y], :path []}]
         acc []]
    (if group
      (let [[groups acc] (diff-group group groups acc)]
        (recur groups acc))
      acc)))
