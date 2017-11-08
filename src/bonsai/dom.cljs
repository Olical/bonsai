(ns bonsai.dom
  (:require [cljs.spec.alpha :as s]
            [expound.alpha :as expound]))

(s/def ::tree (s/or :void nil?
                    :text string?
                    :node-seq (s/coll-of ::tree :kind seq?)
                    :node (s/cat :name keyword?
                                 :attrs (s/? (s/map-of keyword?
                                                       (s/or :void nil?
                                                             :text string?)))
                                 :children (s/* ::tree))))

(expound/expound-str (s/coll-of number? :kind seq?) (map inc [1 2 3]))

(defn conform-tree [src]
  (let [tree (s/conform ::tree src)]
    (if (s/invalid? tree)
      (throw (js/Error. (expound/expound-str ::tree src)))
      tree)))

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

(defn migrate! [host old [prev-type _] [type value :as tree]]
  (if (= prev-type type :text)
    (aset old "nodeValue" value)
    (let [el (tree->el (document old) tree)]
      (.replaceChild host el old)
      (doseq [child (children old)]
        (.appendChild el child)))))

(defn nth-child [el n]
  (nth (children el) n nil))

(defn fingerprint [[type value]]
  (case type
    :text [:text value]
    :node [:node (:name value)]))

(defn node-children [[type value :as tree]]
  (case type
    :text nil
    :node (:children value)
    nil))

(defn node-attrs [[type value :as tree]]
  (case type
    :text nil
    :node (:attrs value)
    nil))

(defn void? [[type _ :as node]]
  (or (nil? node) (= type :void)))

(def real? (complement void?))

(defn flatten-node-seqs [nodes]
  (loop [nodes nodes
         acc []]
    (if-let [[type value :as node] (first nodes)]
      (if (= type :node-seq)
        (recur (concat value (rest nodes)) acc)
        (recur (rest nodes) (conj acc node)))
      acc)))

(defn render-attrs! [pas nas el]
  (doseq [attr-key (set (concat (keys pas) (keys nas)))]
    (let [pa (get pas attr-key)
          na (get nas attr-key)]
      (cond
        (= pa na) nil
        (void? na) (remove-attr! el attr-key)
        (real? na) (set-attr! el attr-key (second na))))))

(defn render-recur! [pvs nxs host]
  (loop [pvs (flatten-node-seqs pvs)
         nxs (flatten-node-seqs nxs)
         n 0]
    (let [pv (first pvs)
          nx (first nxs)
          el (nth-child host n)]
      (when (or pv nx)
        (cond
          (= pv nx) nil
          (and (real? pv) (void? nx)) (remove! host el)
          (and (void? pv) (real? nx)) (insert! host el nx)
          (and (real? pv) (real? nx) (not= (fingerprint pv) (fingerprint nx))) (migrate! host el pv nx))
        (let [n (+ n (when (void? nx) -1))
              child-el (nth-child host n)]
          (when (real? nx)
            (render-attrs! (node-attrs pv) (node-attrs nx) child-el)
            (render-recur! (node-children pv) (node-children nx) child-el))
          (recur (rest pvs) (rest nxs) (inc n)))))))

(defn render!
  ([nx-src host]
   (render! nil nx-src host))
  ([pv nx-src host]
   (let [nx (conform-tree nx-src)]
     (render-recur! [pv] [nx] host)
     nx)))
