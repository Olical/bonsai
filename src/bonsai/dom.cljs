(ns bonsai.dom
  (:require [bonsai.tree :as tree]))

(defn tree->el [document [type value :as tree]]
  (case type
    :text (.createTextNode document value)
    :node (.createElement document (name (:name value)))))

(defn document [el]
  (.-ownerDocument el))

(defn remove! [host el]
  (.removeChild host el))

(defn insert! [host ref-el tree]
  (let [el (tree->el (document host) tree)]
    (if ref-el
      (.insertBefore host el ref-el)
      (.appendChild host el))))

(defn remove-attr! [el attr-key]
  (.removeAttribute el (name attr-key)))

(defn set-attr! [el attr-key attr-value]
  (.setAttribute el (name attr-key) attr-value))

(defn children [el]
  (when el
    (into [] (array-seq (aget el "childNodes")))))

(defn nth-child [el n]
  (nth (children el) n nil))

(defn render-attrs!
  ([nas el]
   (render-attrs! nil nas el))
  ([pas nas el]
   (doseq [attr-key (set (concat (keys pas) (keys nas)))]
     (let [pa (get pas attr-key)
           na (get nas attr-key)]
       (cond
         (= pa na) nil
         (tree/void? na) (remove-attr! el attr-key)
         (tree/real? na) (set-attr! el attr-key (second na)))))))

(defn migrate! [host old [prev-type _ :as prev-tree] [type value :as tree]]
  (if (= prev-type type :text)
    (aset old "nodeValue" value)
    (let [el (tree->el (document old) tree)]
      (render-attrs! (tree/attrs prev-tree) el)
      (.replaceChild host el old)
      (doseq [child (children old)]
        (.appendChild el child)))))

(defn render-recur! [pvs nxs host]
  (loop [pvs (tree/flatten-seqs pvs)
         nxs (tree/flatten-seqs nxs)
         n 0]
    (let [pv (first pvs)
          nx (first nxs)
          el (nth-child host n)]
      (when (or pv nx)
        (cond
          (= pv nx) nil
          (and (tree/real? pv) (tree/void? nx)) (remove! host el)
          (and (tree/void? pv) (tree/real? nx)) (insert! host el nx)
          (and (tree/real? pv) (tree/real? nx) (not= (tree/fingerprint pv) (tree/fingerprint nx))) (migrate! host el pv nx))
        (let [n (+ n (when (tree/void? nx) -1))
              child-el (nth-child host n)]
          (when (tree/real? nx)
            (render-attrs! (tree/attrs pv) (tree/attrs nx) child-el)
            (render-recur! (tree/children pv) (tree/children nx) child-el))
          (recur (rest pvs) (rest nxs) (inc n)))))))

(defn render!
  ([nx-src host]
   (render! nil nx-src host))
  ([pv nx-src host]
   (let [nx (tree/conform nx-src)]
     (render-recur! [pv] [nx] host)
     nx)))
