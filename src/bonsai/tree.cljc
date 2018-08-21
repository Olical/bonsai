(ns bonsai.tree
  (:require [clojure.string :as str]))

(defn added? [a-node b-node]
  (and (empty? a-node) (seq b-node)))

(defn removed? [a-node b-node]
  (and (seq a-node) (empty? b-node)))

;; TODO Refactor out mundane recursion.
;; TODO Handle infinitely nested seqs of nodes inside trees.
;; TODO Self documenting error messages.
;; TODO A node parsing / normalising function that finds the kind, attributes and children.
;; TODO Handle HTML entities in text nodes.

(defn ->html [tree]
  (loop [acc []
         tree tree]
    (if (empty? tree)
      (str/join acc)
      (let [[node & tree] tree]
        (recur (cond
                 (string? node) (conj acc node)
                 (vector? node) (let [node-name (-> node first name)
                                      open (str "<" node-name ">")
                                      close (str "</" node-name ">")]
                                  (conj acc open (->html (rest node)) close))
                 :else acc)
               tree)))))

(defn diff
  ([a-tree b-tree]
   (diff a-tree b-tree [] []))
  ([a-tree b-tree acc parent-path]
   (loop [acc acc
          parent-path parent-path
          index 0
          a-tree a-tree
          b-tree b-tree]
     (if (and (empty? a-tree) (empty? b-tree))
       acc
       (let [[a-node & a-tree] a-tree
             [b-node & b-tree] b-tree
             [a-kind & a-children] a-node
             [b-kind & b-children] b-node
             path (conj parent-path index)]
         (recur (cond
                  (added? a-node b-node) (conj acc [:insert path [b-node]])
                  (removed? a-node b-node) (conj acc [:remove path])
                  (not= a-kind b-kind) (conj acc [:remove path] [:insert path [b-node]])
                  (and (= a-kind b-kind) (not= a-children b-children)) (diff a-children b-children acc path)
                  :else acc)
                parent-path
                (if (seq b-node)
                  (inc index)
                  index)
                a-tree
                b-tree))))))
