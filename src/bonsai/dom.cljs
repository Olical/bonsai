(ns bonsai.dom
  (:require [cljs.spec.alpha :as s]
            [expound.alpha :as expound]))

(s/def ::tree (s/nilable
               (s/or :text string?
                     :node (s/cat :name keyword?
                                  :children (s/* ::tree)))))

(defn conform-tree [src]
  (let [tree (s/conform ::tree src)]
    (if (s/invalid? tree)
      (throw (js/Error. (expound/expound-str ::tree src)))
      tree)))

(defn tree->el [document [type value :as tree]]
  (case type
    :text (.createTextNode document value)
    :node (.createElement document (name (:name value)))
    (throw (js/Error. (str "Could not build an element from a node of type '" (pr-str type) "'.")))))

(defn document [el]
  (.-ownerDocument el))

(defn remove! [host el]
  (.removeChild host el))

(defn insert! [host ref-el tree]
  (let [el (tree->el (document host) tree)
        target (when ref-el (.-nextSibling ref-el))]
    (if target
      (.insertBefore host el target)
      (.appendChild host el))))

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
    :node [:node (:name value)]
    nil nil
    (throw (js/Error. (str "Could not fingerprint type '" (pr-str type) "'.")))))

(defn node-children [[type value :as tree]]
  (case type
    :text nil
    :node (:children value)
    nil nil
    (throw (js/Error. (str "Could not get children for '" (pr-str type) "'.")))))

(defn render-recur! [pvs nxs host]
  (loop [pvs pvs
         nxs nxs
         n 0]
    (let [pv (first pvs)
          nx (first nxs)
          el (nth-child host n)]
      (when (or pv nx)
        (cond
          (= pv nx) nil
          (and pv (nil? nx)) (remove! host el)
          (and (nil? pv) nx) (insert! host el nx)
          (not= (fingerprint pv) (fingerprint nx)) (migrate! host el pv nx))
        (let [pc (node-children pv)
              nc (node-children nx)]
          (when (and nc (not= pc nc))
            (render-recur! pc nc (nth-child host n))))
        (recur (rest pvs) (rest nxs) (inc n))))))

(defn render!
  ([nx-src host]
   (render! nil nx-src host))
  ([pv nx-src host]
   (let [nx (conform-tree nx-src)]
     (render-recur! [pv] [nx] host)
     nx)))
