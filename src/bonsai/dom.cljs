(ns bonsai.dom
  (:require [cljs.spec.alpha :as s]
            [expound.alpha :as expound]))

(s/def ::tree (s/nilable
               (s/or :text string?
                     :node (s/cat :name keyword?
                                  :children (s/* ::tree)))))

(defn build-tree [src]
  (let [tree (s/conform ::tree src)]
    (if (s/invalid? tree)
      (throw (js/Error. (expound/expound-str ::tree src)))
      tree)))

(defn append-child! [host-node node]
  (.appendChild host-node node))

(defn remove-child! [host-node node]
  (.removeChild host-node node))

(defn build-node [document [type value]]
  (cond
    (= type :text) (.createTextNode document value)
    (= type :node) (let [node (.createElement document (name (:name value)))]
                     (doseq [child (:children value)]
                       (append-child! node (build-node document child)))
                     node)))

(defn first-child [node]
  (.-firstChild node))

(defn document [node]
  (.-ownerDocument node))

(defn render! [prev-tree next-src host-node]
  (let [next-tree (build-tree next-src)
        host-document (document host-node)]
    (cond
      (and (nil? prev-tree) next-tree) (append-child! host-node (build-node host-document next-tree))
      (and prev-tree (nil? next-tree)) (remove-child! host-node (first-child host-node))
      (not= prev-tree next-tree) (prn "~" prev-tree "->" next-tree))
    next-tree))
