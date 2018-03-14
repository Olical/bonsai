(ns bonsai.diff
  (:require [orchestra.core #?(:clj :refer, :cljs :refer-macros) [defn-spec]]
            [#?(:clj clojure.spec.alpha, :cljs cljs.spec.alpha) :as s]))

(s/def ::node-kind keyword?)

(s/def ::tree (s/or :nothing nil?
                    :node (s/cat :name ::node-kind
                                 :children (s/* ::tree))
                    :nodes (s/coll-of ::tree)))

(s/def ::path (s/coll-of integer? :kind vector?))

(s/def ::changes (s/coll-of
                  (s/or :insert-node (s/cat :op #{::insert-node}
                                            :params (s/keys :req [::path ::node-kind]))
                        :remove-node (s/cat :op #{::remove-node}
                                            :params (s/keys :req [::path])))
                  :kind vector?))

(defn-spec changes ::changes
  "Find the changes between two trees."
  [from ::tree, to ::tree]
  [])
