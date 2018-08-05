(ns bonsai.tree)

(defn removed? [a b]
  (and (seq a) (empty? b)))

(defn added? [a b]
  (and (empty? a) (seq b)))

(defn diff [a b]
  (loop [acc []
         path []
         index 0
         [a & ar] a
         [b & br] b]
    (if (= a b nil)
      acc
      (recur (cond
               (removed? a b) (conj acc [:remove (conj path index)])
               (added? a b) (conj acc [:insert (conj path index) b])
               (not= a b) (conj acc [:replace (conj path index) b])
               :else acc)
             path
             (if (seq b)
               (inc index)
               index)
             ar
             br))))
