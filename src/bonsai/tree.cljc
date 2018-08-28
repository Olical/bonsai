(ns bonsai.tree
  (:require [clojure.string :as str]
            [clojure.set :as set]))

;; TODO Refactor out mundane recursion.
;; TODO Handle infinitely nested seqs of nodes inside trees.
;; TODO Self documenting error messages.
;; TODO Event and state system.

(defn void? [[kind]]
  (or (nil? kind) (= kind ::void)))

(defn added? [a-node b-node]
  (and (void? a-node) (not (void? b-node))))

(defn removed? [a-node b-node]
  (and (not (void? a-node)) (void? b-node)))

(def reserved-character-entities
  {\< "&lt;"
   \> "&gt;"
   \& "&amp;"
   \" "&quot;"})

(defn escape-html-entities [s]
  (str/escape s reserved-character-entities))

(defn normalise-node [node]
  (cond
    (nil? node) [::void nil nil]
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

(defn render [tree]
  (loop [acc {:html []
              :diff []}
         tree tree]
    (if (empty? tree)
      (update acc :html str/join)
      (let [[node & tree] tree
            [kind attrs & children] (normalise-node node)
            attr-str (when attrs
                       (str/join " " (map (fn [[k v]] (str (name k) "=\"" (escape-html-entities v) "\"")) attrs)))]
        (recur (cond
                 (= kind ::void) acc
                 (= kind ::text) (update acc :html conj (escape-html-entities (first children)))
                 :else (let [node-name (name kind)
                             open (str "<" node-name (when attr-str " ") attr-str ">")
                             close (str "</" node-name ">")
                             {:keys [html diff]} (render children)]
                        (update acc :html conj open html close)))
               tree)))))

(defn diff-attrs [path a b]
  (if (= a b)
    []
    (let [a-keys (set (keys a))
          b-keys (set (keys b))]
      (loop [acc []
             [k & remainder] (set/union a-keys b-keys)]
        (if k
          (recur
            (let [a? (a-keys k)
                  b? (b-keys k)
                  av (k a)
                  bv (k b)]
              (cond
                (and a? (not b?)) (conj acc [:dissoc path k])
                (or (and (not a?) b?)
                    (and a? b? (not= av bv))) (conj acc [:assoc path k bv])
                :else acc))
            remainder)
          acc)))))

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
             [a-kind a-attrs & a-children] (normalise-node a-node)
             [b-kind b-attrs & b-children] (normalise-node b-node)
             path (conj parent-path index)]
         (recur (cond
                  (added? a-node b-node) (conj acc [:insert path [b-node]])
                  (removed? a-node b-node) (conj acc [:remove path])
                  (or (not= a-kind b-kind)
                      (and (= a-kind b-kind ::text)
                           (not= a-children b-children))) (conj acc [:remove path] [:insert path [b-node]])
                  (= a-kind b-kind) (into (if (not= a-children b-children)
                                            (diff a-children b-children acc path)
                                            acc)
                                          (diff-attrs path a-attrs b-attrs))
                  :else acc)
                parent-path
                (if (not (void? b-node))
                  (inc index)
                  index)
                a-tree
                b-tree))))))
