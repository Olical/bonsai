(ns bonsai.tree
  "Tools to parse and diff valid Bonsai trees."
  (:require [clojure.spec.alpha :as s]
            [clojure.data :as data]))

(s/def ::tag (s/cat :name keyword?
                    :attrs (s/? map?)
                    :children (s/* ::tree)))

(s/def ::tree (s/or :empty nil?
                    :text string?
                    :tag ::tag
                    :seq (s/coll-of ::tree :kind sequential?)))

(s/def ::parsed-tree vector?)

(s/def ::tree-diff (s/cat :a vector?
                          :b vector?
                          :both vector?))

(s/fdef parse
        :args (s/cat :data ::tree)
        :ret ::parsed-tree)
(defn parse
  "Parse the given tree into a richer data structure using spec.

  [:p {:id \"foo\"} \"hi\"] -> [:tag {:name :p
                                      :attrs {:id \"foo\"}
                                      :children [[:text \"hi\"]]}]"
  [data]
  (s/conform ::tree data))

(s/fdef changes
        :args (s/cat :old ::parsed-tree
                     :new ::parsed-tree)
        :ret ::tree-diff)
(defn changes
  "Find the changes between two parsed trees using clojure.data/diff. Returns a
  vector of three items. Things only in the old tree, things only in the new
  tree and things in both."
  [old new]
  (data/diff old new))
