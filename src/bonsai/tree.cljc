(ns bonsai.tree
  (:require [clojure.string :as str]))

;; TODO Refactor out mundane recursion.
;; TODO Handle infinitely nested seqs of nodes inside trees.
;; TODO Self documenting error messages.
;; TODO A node parsing / normalising function that finds the kind, attributes and children.

(defn added? [a-node b-node]
  (and (empty? a-node) (seq b-node)))

(defn removed? [a-node b-node]
  (and (seq a-node) (empty? b-node)))

(def reserved-character-entities
  {\< "&lt;"
   \> "&gt;"
   \& "&amp;"
   \" "&quot;"})

(defn escape-html-entities [s]
  (str/escape s reserved-character-entities))

(defn parse-node [node]
  (cond
    (nil? node) [::void]
    (string? node) [::text nil node]
    :else (let [[kind & others] node
                maybe-attrs (first others)
                attrs? (map? maybe-attrs)]
            (into [kind
                   (when attrs?
                     maybe-attrs)]
                  (if attrs?
                    (rest others)
                    others)))))

(defn ->html [tree]
  (loop [acc []
         tree tree]
    (if (empty? tree)
      (str/join acc)
      (let [[node & tree] tree
            [kind attrs & children] (parse-node node)
            attr-str (when attrs
                       (str/join " " (map (fn [[k v]] (str (name k) "=\"" (escape-html-entities v) "\"")) attrs)))]
        (recur (cond
                 (= kind ::text) (conj acc (escape-html-entities (first children)))
                 (= kind ::void) acc
                 :else (let [node-name (name kind)
                             open (str "<" node-name (when attr-str " ") attr-str ">")
                             close (str "</" node-name ">")]
                        (conj acc open (->html children) close)))
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
