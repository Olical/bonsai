(ns bonsai.tree
  (:require [clojure.spec.alpha :as s]
            [clojure.data :as data]))

(s/def ::tree (s/or :tag (s/cat :name keyword?
                                :attrs (s/? map?)
                                :children (s/* ::tree))
                    :seq (s/* ::tree)
                    :any any?))

(defn changes [old new]
  (data/diff old new))

(defn parse [data]
  (let [tree (s/conform ::tree data)]
    (when (s/invalid? tree)
      (throw (ex-info "Failed to validate tree against spec."
                      (s/explain-data ::tree data))))
    tree))

(changes nil (parse [:p [:span {:class "left"} "Hello"] ", " [:span {:class "right"} "World!"]]))

(changes (parse [:p [:span {:class "left"} "Hello"] ", " [:span {:class "right"} "World!"]]) (parse [:p [:span {:class "left"} "Oh Hai"] ", " [:span {:class "right"} "World!"]]))

(changes (parse [:p [1 2 3 5]])
         (parse [:p [1 2 3 4 5]]))
