(ns bonsai.tree
  (:require #?(:clj [clojure.spec.alpha :as s]
               :cljs [cljs.spec.alpha :as s])
            [expound.alpha :as expound]))

(s/def ::tree (s/or :void nil?
                    :text string?
                    :node-seq (s/coll-of ::tree :kind seq?)
                    :node (s/cat :name keyword?
                                 :attrs (s/? (s/map-of keyword?
                                                       (s/or :void nil?
                                                             :text string?)))
                                 :children (s/* ::tree))))

(defn conform [src]
  (let [tree (s/conform ::tree src)]
    (if (s/invalid? tree)
      (throw (#?(:clj Exception. :cljs js/Error.) (expound/expound-str ::tree src)))
      tree)))

(defn fingerprint [[type value]]
  (case type
    :text [:text value]
    :node [:node (:name value)]))

(defn children [[type value :as tree]]
  (case type
    :text nil
    :node (:children value)
    nil))

(defn attrs [[type value :as tree]]
  (case type
    :text nil
    :node (:attrs value)
    nil))

(defn void? [[type _ :as node]]
  (or (nil? node) (= type :void)))

(def real? (complement void?))

(defn flatten [nodes]
  (loop [nodes nodes
         acc []]
    (if-let [[type value :as node] (first nodes)]
      (if (= type :node-seq)
        (recur (concat value (rest nodes)) acc)
        (recur (rest nodes) (conj acc node)))
      acc)))
