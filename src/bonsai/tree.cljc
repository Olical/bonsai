(ns bonsai.tree)

(defn added? [a-node b-node]
  (and (empty? a-node) (seq b-node)))

(defn removed? [a-node b-node]
  (and (seq a-node) (empty? b-node)))

;; TODO Refactor out mundane recursion.
;; TODO Handle infinitely nested seqs of nodes inside trees.

(defn diff
  ([a-tree b-tree]
   (diff a-tree b-tree [] []))
  ([a-tree b-tree acc parent-path]
   (loop [acc acc
          parent-path parent-path
          index 0
          [a-node & a-tree] a-tree
          [b-node & b-tree] b-tree]
     (if (= a-node b-node a-tree b-tree nil)
       acc
       (let [[a-kind & a-children] a-node
             [b-kind & b-children] b-node
             path (conj parent-path index)]
         (recur (cond
                  (added? a-node b-node) (conj acc [:insert path b-node])
                  (removed? a-node b-node) (conj acc [:remove path])
                  (not= a-kind b-kind) (conj acc [:replace path b-node])
                  (and (= a-kind b-kind) (not= a-children b-children)) (diff a-children b-children acc path)
                  :else acc)
                parent-path
                (inc index)
                a-tree
                b-tree))))))
