(ns bonsai.tree
  (:require [clojure.walk :as walk]))

(defn enrich-node [n]
  (cond
    (string? n) {:type :text :value n}
    (vector? n) (let [[name & [?attrs & ?children :as children]] n
                      attrs (when (map? ?attrs) ?attrs)
                      children (if attrs
                                 ?children
                                 children)]
                  {:type :tag
                   :name name
                   :attrs attrs
                   :children children})))

(defn enrich-tree [t]
  (walk/prewalk enrich-node t))

(enrich-tree [:p [:span {:class "left"} "Hello"] ", " [:span {:class "right"} "World!"]])
