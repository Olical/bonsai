(ns bonsai.diff
  (:require [orchestra.core #?(:clj :refer, :cljs :refer-macros) [defn-spec]]
            [#?(:clj clojure.spec.alpha, :cljs cljs.spec.alpha) :as s]))

(s/def ::kind keyword?)
(s/def ::tree (s/nilable
               (s/cat :kind ::kind
                      :children (s/* ::tree))))
(s/def ::children (s/* ::tree))

(defmulti change ::op)
(defmethod change ::insert-node [_]
  (s/keys :req [::op ::path ::kind]))
(defmethod change ::remove-node [_]
  (s/keys :req [::op ::path]))
(defmethod change ::replace-node [_]
  (s/keys :req [::op ::path ::kind]))

(s/def ::path (s/coll-of integer? :kind vector?))
(s/def ::change (s/multi-spec change ::op))
(s/def ::changes (s/coll-of ::change :kind vector?))

(defn-spec kind (s/nilable ::kind)
  "Get the kind of a node."
  [node ::tree]
  (first node))

(defn-spec children ::children
  "Get the children of a node, if any."
  [node ::tree]
  (rest node))

(defn- changes* [{:keys [xs ys path]} frames acc]
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
            path (conj path index)]
        (recur xs
               ys
               (inc index)
               (if (= xcs ycs)
                 frames
                 (conj frames {:xs xcs
                               :ys ycs
                               :path path}))
               (cond
                 (= (kind x) (kind y))
                 acc

                 (nil? x)
                 (conj acc {::op ::insert-node
                            ::path path
                            ::kind (kind y)})

                 (nil? y)
                 (conj acc {::op ::remove-node
                            ::path path})

                 :else
                 (conj acc {::op ::replace-node
                            ::path path
                            ::kind (kind y)}))))
      {:frames frames
       :acc acc})))

(defn-spec changes ::changes
  "Find the changes between two trees."
  [x ::tree, y ::tree]
  (loop [[{:keys [xs ys path] :as frame} & frames] [{:xs [x], :ys [y], :path []}]
         acc []]
    (if frame
      (let [{:keys [frames acc]} (changes* frame frames acc)]
        (recur frames acc))
      acc)))
