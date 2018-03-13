(ns bonsai.diff
  (:require [orchestra.core #?(:clj :refer, :cljs :refer-macros) [defn-spec]]
            [#?(:clj clojure.spec.alpha, :cljs cljs.spec.alpha) :as s]))

(s/def ::node (s/nilable
               (s/and vector?
                      (s/cat :name keyword?
                             :children (s/* ::node)))))

(s/def ::op #{:add :remove})
(s/def ::path (s/coll-of integer? :kind vector?))
(s/def ::changes (s/coll-of (s/keys :req [::op ::path]
                                    :opt [::node])
                            :kind vector?))

(defn-spec diff ::changes
  "Find the changes between two trees."
  [from ::node, to ::node]
  (loop [[from & next-from] [from]
         [to & next-to] [to]
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
                                         ::node to})))))))
