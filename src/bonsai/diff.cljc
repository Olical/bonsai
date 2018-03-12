(ns bonsai.diff
  (:require [orchestra.core #?(:clj :refer, :cljs :refer-macros) [defn-spec]]
            [#?(:clj clojure.spec.alpha, :cljs cljs.spec.alpha) :as s]))

(s/def ::tree (s/nilable
               (s/or :node (s/and vector?
                                  (s/cat :name keyword?
                                         :children (s/* ::tree)))
                     :nodes (s/coll-of ::tree))))

(s/def ::op #{:add :remove})
(s/def ::path (s/coll-of integer? :kind vector?))
(s/def ::changes (s/coll-of (s/keys :req [::op ::path]
                                    :opt [::tree])
                            :kind vector?))

(defn-spec ->nodes ::tree
  [tree ::tree]
  (if (keyword? (first tree))
    [tree]
    tree))

(defn-spec diff ::changes
  "Find the changes between two trees."
  [from ::tree, to ::tree]
  (loop [[from & next-from] (->nodes from)
         [to & next-to] (->nodes to)
         path []
         index 0
         acc []]
    (if (= from to nil)
      acc
      (recur next-from
             next-to
             path
             (inc index)
             (let [path (conj path index)]
               (cond
                 (= to from) acc
                 (= to nil) (conj acc {::op :remove
                                       ::path path})
                 (= from nil) (conj acc {::op :add
                                         ::path path
                                         ::tree to})))))))
