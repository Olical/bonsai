(ns bonsai.dom
  (:require [cljs.spec.alpha :as s]
            [expound.alpha :as expound]))

(s/def ::tree (s/or :nothing nil?
                    :text string?
                    :nodes (s/coll-of ::tree)
                    :node (s/cat :name keyword?
                                 :children (s/* ::tree))))

(defn parse [source]
  (let [result (s/conform ::tree source)]
    (if (s/invalid? result)
      (throw (js/Error. (expound/expound-str ::tree source)))
      (if (= (first result) :nodes)
        (second result)
        [result]))))

(defn render [mount {:keys [old new]}]
  (loop [old-nodes (parse old)
         new-nodes (parse new)]
    (when (not= old-nodes new-nodes)
      (doseq [[idx [new-type new-value]] (map-indexed vector new-nodes)]
        (let [[old-type old-value] (nth old-nodes idx)]
          (case new-type
            :nothing (prn "DOING NOTHING" new-value)
            :text (prn "HANDLING TEXT" new-value)
            :nodes (prn "HIT A SEQ OF NODES" new-value)
            :node (prn "HIT A NODE" new-value)))))))

;; So I should be able to handle the top level with this.
;; To handle children and seqs of nodes, I'll need to concat and recur + attach some DOM node.
