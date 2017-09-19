(ns bonsai.tree
  (:require [clojure.spec.alpha :as s]
            [clojure.data :as data]))

(s/def ::tag (s/cat :name keyword?
                    :attrs (s/? map?)
                    :children (s/* ::tree)))

(s/def ::tree (s/or :empty nil?
                    :text string?
                    :tag ::tag
                    :seq (s/coll-of ::tree :kind sequential?)))

(s/def ::parsed-tree (s/keys ::opt-un [::empty ::text ::tag ::children]))

(s/def ::tree-diff (s/cat :a vector?
                          :b vector?
                          :both vector?))

(s/fdef parse
        :args (s/cat :data ::tree)
        :ret ::parsed-tree)
(defn parse [data]
  (s/conform ::tree data))

(s/fdef changes
        :args (s/cat :old ::parsed-tree
                     :new ::parsed-tree)
        :ret ::tree-diff)
(defn changes [old new]
  (data/diff old new))
