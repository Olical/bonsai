(ns bonsai.dom
  (:require [cljs.spec.alpha :as s]
            [expound.alpha :as expound]))

(s/def ::tree (s/or :nothing nil?
                    :text string?
                    :node (s/cat :name keyword?
                                 :children (s/* ::tree))))

(defn start-stack [source]
  (let [result (s/conform ::tree source)]
    (if (s/invalid? result)
      (throw (js/Error. (expound/expound-str ::tree source)))
      (list (list result)))))

(defn parent [nodes]
  (-> nodes meta :parent))

(defn sync-node [parent old new idx]
  (prn "SYNC" old new idx))

(defn children [nodes]
  (into []
        (comp (filter (fn [[type value]] (= type :node)))
              (map (fn [[type value]] (:children value))))
        nodes))

(defn render! [old-stack new-source mount]
  (loop [old-stack old-stack
         new-stack (start-stack new-source)
         acc []]
    (if (= old-stack new-stack nil)
      acc
      (let [[old-nodes & old-rest] old-stack
            [new-nodes & new-rest] new-stack
            parent-dom (or (parent old-nodes) mount)]
        (doall (map (partial sync-node parent-dom) old-nodes new-nodes (range)))
        (prn "oh" (concat (children new-nodes) new-rest))
        (recur old-rest
               (concat (children new-nodes parent-dom) new-rest)
               (conj acc (with-meta new-nodes {:parent parent-dom})))))))

(comment
  (-> (render! nil [:p "hi"] :dom)
      (render! [:p "bye"] :dom)))
