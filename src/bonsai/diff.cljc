(ns bonsai.diff
  (:require [orchestra.core #?(:clj :refer, :cljs :refer-macros) [defn-spec]]
            [#?(:clj clojure.spec.alpha, :cljs cljs.spec.alpha) :as s]))

(s/def ::node (s/nilable
               (s/and vector?
                      (s/cat :name keyword?
                             :children (s/* ::node)))))

(s/def ::change vector?)

(defn-spec diff ::change
  "Find the changes between two trees."
  [from ::node, to ::node]
  (loop [acc []]
    acc))
