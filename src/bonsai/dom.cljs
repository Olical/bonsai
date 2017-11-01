(ns bonsai.dom
  (:require [cljs.spec.alpha :as s]
            [expound.alpha :as expound]))

(s/def ::tree (s/nilable
               (s/or :text string?
                     :node (s/cat :name keyword?
                                  :children (s/* ::tree)))))

(defn append-child! [host-node node]
  (.appendChild host-node node))

(defn remove-child! [host-node node]
  (.removeChild host-node node))

(defn first-child [node]
  (.-firstChild node))

(defn document [node]
  (.-ownerDocument node))

(defn build-node [document [type value]]
  (cond
    (= type :text) (.createTextNode document value)
    (= type :node) (let [node (.createElement document (name (:name value)))]
                     (doseq [child (:children value)]
                       (append-child! node (build-node document child)))
                     node)))

(defn build-tree [src]
  (let [tree (s/conform ::tree src)]
    (if (s/invalid? tree)
      (throw (js/Error. (expound/expound-str ::tree src)))
      tree)))

(defn tree-type [[type value]]
  (cond
    (= type :text) [:text :text]
    (= type :node) [:node (:name value)]))

(defn tree-children [[_ {:keys [children]}]]
  children)

(defn render! [prev-tree next-tree host-node]
  (let [host-document (document host-node)]
    (cond
      (= prev-tree next-tree) :noop
      (and (nil? prev-tree) next-tree) (append-child! host-node (build-node host-document next-tree))
      (and prev-tree (nil? next-tree)) (remove-child! host-node (first-child host-node))
      (not= (tree-type prev-tree) (tree-type next-tree)) (do (remove-child! host-node (first-child host-node))
                                                             (append-child! host-node (build-node host-document next-tree))))
    (let [prev-children (tree-children prev-tree)
          next-children (tree-children next-tree)]
      (doall
       (map
        (fn [next-child idx]
          (prn next-child)
          (render! (get prev-children idx) next-child (first-child host-node)))
        next-children (range))))
    next-tree))

(tree-children [:text "hi"])
