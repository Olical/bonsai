(ns bonsai.tree
  (:require [orchestra.core #?(:clj :refer, :cljs :refer-macros) [defn-spec]]
            [#?(:clj clojure.spec.alpha, :cljs cljs.spec.alpha) :as s]))

(s/def ::tree (s/or
               :void nil?
               :text ::text
               :node (s/cat :kind ::kind
                            :children (s/* ::tree))))

(defmulti change ::op)
(defmethod change ::insert-node [_]
  (s/keys :req [::op ::path ::kind]))
(defmethod change ::insert-text [_]
  (s/keys :req [::op ::path ::text]))
(defmethod change ::remove-node [_]
  (s/keys :req [::op ::path ::dry?]))

(s/def ::dry? boolean?)
(s/def ::kind keyword?)
(s/def ::text string?)
(s/def ::path (s/coll-of integer? :kind vector?))
(s/def ::change (s/multi-spec change ::op))

(defn-spec kind (s/nilable ::kind)
  [node ::tree]
  (first node))

(defn-spec children (s/* ::tree)
  [node ::tree]
  (next node))

(defn-spec insert-node-op ::change
  [path ::path, kind ::kind]
  {::op ::insert-node
   ::path path
   ::kind kind})

(defn-spec insert-text-op ::change
  [path ::path, text ::text]
  {::op ::insert-text
   ::path path
   ::text text})

(defn-spec remove-node-op ::change
  ([path ::path]
   (remove-node-op path false))
  ([path ::path, dry? ::dry?]
   {::op ::remove-node
    ::path path
    ::dry? (boolean dry?)}))

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
                   :insert (conj acc (insert-node-op path yk))
                   :remove (conj acc (remove-node-op path dry?))
                   :replace (conj acc
                                  (remove-node-op path dry?)
                                  (insert-node-op path yk)))))
        [groups acc]))))

(defn-spec diff (s/* ::change)
  [x ::tree, y ::tree]
  (loop [[{:keys [xs ys path] :as group} & groups] [{:xs [x], :ys [y], :path []}]
         acc []]
    (if group
      (let [[groups acc] (diff-group group groups acc)]
        (recur groups acc))
      acc)))
