(ns bonsai.diff
  (:require [orchestra.core #?(:clj :refer, :cljs :refer-macros) [defn-spec]]
            [#?(:clj clojure.spec.alpha, :cljs cljs.spec.alpha) :as s]))

(s/def ::tree (s/nilable
               (s/or :node (s/and vector?
                                  (s/cat :name keyword?
                                         :children (s/* ::tree)))
                     :nodes (s/coll-of ::tree))))

(s/conform ::tree (list [:p]))

(s/def ::change vector?)

(defn-spec diff ::change
  "Find the changes between two trees."
  [from ::tree, to ::tree]
  (loop [acc []]
    acc))
