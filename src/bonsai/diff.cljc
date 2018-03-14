(ns bonsai.diff
  (:require [orchestra.core #?(:clj :refer, :cljs :refer-macros) [defn-spec]]
            [#?(:clj clojure.spec.alpha, :cljs cljs.spec.alpha) :as s]))

(s/def ::node (s/nilable
               (s/and vector?
                      (s/cat :name keyword?
                             :children (s/* ::node)))))
(s/def ::nodes (s/nilable (s/coll-of ::node)))
(s/def ::path (s/coll-of integer? :kind vector?))
(s/def ::changes (s/coll-of (s/or :add (s/cat :op #{::add}, :params (s/keys :req [::path ::node]))
                                  :remove (s/cat :op #{::remove}, :params (s/keys :req [::path])))
                            :kind vector?))

(defn-spec changes ::changes
  "Find the changes between two trees."
  [from ::nodes, to ::nodes]
  (loop [[from & next-from] from
         [to & next-to] to
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
                 (= to nil) (conj acc [::remove {::path path}])
                 (= from nil) (conj acc [::add {::path path, ::node to}])))))))
