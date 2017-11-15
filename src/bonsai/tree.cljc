(ns bonsai.tree
  (:require #?(:clj [clojure.spec.alpha :as s]
               :cljs [cljs.spec.alpha :as s])
            [expound.alpha :as expound]
            [clojure.string :as str]))

(def lifecycle-events #{:on-insert :on-remove})

(s/def ::fn-vec (s/and vector?
                       (s/cat :fn fn?
                              :args (s/* any?))))

(s/def ::tree (s/or :void nil?
                    :text string?
                    :node-fn ::fn-vec
                    :node-seq (s/coll-of ::tree :kind seq?)
                    :node (s/and vector?
                                 (s/cat :name keyword?
                                        :attrs (s/? (s/map-of keyword?
                                                              (s/or :void nil?
                                                                    :text string?
                                                                    :handler ::fn-vec)))
                                        :children (s/* ::tree)))))

(defn conform [src]
  (let [tree (s/conform ::tree src)]
    (if (s/invalid? tree)
      (throw (#?(:clj Exception. :cljs js/Error.) (expound/expound-str ::tree src)))
      (into [] tree))))

(defn fingerprint [[type value :as node]]
  (case type
    :text node
    :node-fn node
    :node [type (:name value)]))

(defn children [[type value]]
  (case type
    :text nil
    :node (:children value)
    nil))

(defn with-children [[type _ :as node] children]
  (if (= type :node)
    (assoc-in node [1 :children] children)
    node))

(defn attr-type [kw]
  (cond
    (contains? lifecycle-events kw) :lifecycle
    (str/starts-with? (name kw) "on-") :event
    :else :attr))

(defn attrs [[type value]]
  (let [groups (group-by
                (comp attr-type first)
                (case type
                  :text nil
                  :node (:attrs value)
                  nil))]
    {:event (into {} (:event groups))
     :attr (into {} (:attr groups))
     :lifecycle (into {} (:lifecycle groups))}))

(defn void? [[type _ :as node]]
  (or (nil? node) (= type :void)))

(def real? (complement void?))

(defn flatten-seqs [nodes]
  (loop [nodes nodes
         acc []]
    (if-let [[type value :as node] (first nodes)]
      (if (= type :node-seq)
        (recur (concat value (rest nodes)) acc)
        (recur (rest nodes) (conj acc node)))
      acc)))

(defn expand
  ([node]
   (expand nil node))
  ([pv [type {:keys [fn args] :as value} :as node]]
   (if (= type :node-fn)
     (if (= (::node-fn (meta pv)) value)
       pv
       (with-meta (conform (apply fn args)) {::node-fn value}))
     node)))
