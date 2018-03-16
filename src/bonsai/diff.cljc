(ns bonsai.diff
  (:require [orchestra.core #?(:clj :refer, :cljs :refer-macros) [defn-spec]]
            [#?(:clj clojure.spec.alpha, :cljs cljs.spec.alpha) :as s]))

(s/def ::kind keyword?)
(s/def ::tree (s/or :nothing nil?
                    :node (s/cat :kind ::kind
                                 :children (s/* ::tree))
                    :nodes (s/coll-of ::tree)))

(defmulti change ::op)
(defmethod change ::insert-node [_]
  (s/keys :req [::op ::path ::kind]))
(defmethod change ::remove-node [_]
  (s/keys :req [::op ::path]))

(s/def ::path (s/coll-of integer? :kind vector?))
(s/def ::change (s/multi-spec change ::op))
(s/def ::changes (s/coll-of ::change :kind vector?))

(defn-spec changes ::changes
  "Find the changes between two trees."
  [xs ::tree, ys ::tree]
  [])
