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
      (list [result]))))

(defn parent [nodes]
  (-> nodes meta :parent))

(defn sync-node! [parent old new idx]
  (cond
    (and (not old) new) (prn "+" new)
    (and old (not new)) (prn "-" old)
    (and old new) (prn "~" old "->" new)))

(defn render! [old-stack new-source mount]
  (loop [old-stack old-stack
         new-stack (start-stack new-source)
         acc []]
    (if (and (nil? old-stack) (nil? new-stack))
      acc
      (let [[old-nodes & old-rest] old-stack
            [new-nodes & new-rest] new-stack
            parent-dom (or (parent old-nodes) mount)]
        (doall (map (partial sync-node! parent-dom) old-nodes new-nodes (range)))
        (recur old-rest
               new-rest
               (conj acc new-nodes))))))

#_(defn children [nodes]
    (into []
          (comp (filter (fn [[type value]] (= type :node)))
                (map (fn [[type value]] (:children value))))
          nodes))
