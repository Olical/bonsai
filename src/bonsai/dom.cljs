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
    :node (.createElement document (name (:name value)))))

(defn document [el]
  (.-ownerDocument el))

(defn remove! [el]
  (.removeChild (.-parentNode el) el))

(defn append! [host tree]
  (let [el (tree->el (document host) tree)]
    (.appendChild host el)))

(defn migrate! [old [type value :as tree]]
  (if (= type :text)
    (aset old "nodeValue" value)
    (let [el (tree->el (document old) tree)
          host (.-parentNode old)]
      (.replaceChild host el old)
      (doseq [child (array-seq (.-children old))]
        (.appendChild el child)))))

(defn first-child [el]
  (.-firstChild el))

(defn next-child [el]
  (when el
    (.-nextChild el)))

(defn last-child [el]
  (.-lastChild el))

(defn kind [[type value]]
  (case type
    :text [:text value]
    :node [:node (:children value)]
    nil nil))

(defn node-children [[type value :as tree]]
  (case type
    :text nil
    :node (:children value)
    nil nil))

(defn ensure-vec [tree]
  (if (keyword? (first tree))
    [tree]
    tree))

(defn render-recur! [pvs nxs host]
  (loop [pvs pvs
         nxs nxs
         el (first-child host)]
    (let [pv (first pvs)
          nx (first nxs)]
      (when (or pv nx)
        (cond
          (= pv nx) nil
          (and pv (nil? nx)) (remove! el)
          (and (nil? pv) nx) (append! host nx)
          (not= (kind pv) (kind nx)) (migrate! el nx))
        (let [pc (node-children pv)
              nc (node-children nx)]
          (when (not= pc nc)
            (render-recur! pc nc (or el (last-child host)))))
        (recur (rest pvs) (rest nxs) (or (next-child el) (last-child host)))))))

(defn render! [pv nx-src host]
  (let [nx (conform-tree nx-src)]
    (render-recur! (ensure-vec pv) (ensure-vec nx) host)
    nx))
