(ns bonsai.tree)

(defn diff [a b]
  (loop [acc []
         path []
         index 0
         [a & ar] a
         [b & br] b]
    (if (= a b nil)
      acc
      (recur (cond
               (and (seq a) (empty? b)) (conj acc [:remove (conj path index)])
               (and (empty? a) (seq b)) (conj acc [:insert (conj path index) b])
               (not= a b) (conj acc [:replace (conj path index) b])
               :else acc)
             path
             (inc index)
             ar
             br))))
