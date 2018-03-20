(ns bonsai.tree
  (:require [orchestra.core #?(:clj :refer, :cljs :refer-macros) [defn-spec]]
            [#?(:clj clojure.spec.alpha, :cljs cljs.spec.alpha) :as s]))

(s/def ::tree (s/or
               :void nil?
               :text ::text
               :node (s/cat :kind ::kind
                            :children ::trees)))
(s/def ::trees (s/* ::tree))

(defmulti change ::op)
(defmethod change ::insert-node [_]
  (s/keys :req [::op ::path ::kind]))
(defmethod change ::insert-text [_]
  (s/keys :req [::op ::path ::text]))
(defmethod change ::remove-node [_]
  (s/keys :req [::op ::path ::dry?]))

(s/def ::dry? boolean?)
(s/def ::kind keyword?)
(s/def ::text string?)
(s/def ::path (s/coll-of integer? :kind vector?))
(s/def ::change (s/multi-spec change ::op))
(s/def ::changes (s/* ::change))
(s/def ::xs ::trees)
(s/def ::ys ::trees)
(s/def ::group (s/keys :req [::xs ::ys ::path]))
(s/def ::groups (s/* ::group))

(defn-spec kind (s/nilable ::kind)
  [node ::tree]
  (first node))

(defn-spec children ::trees
  [node ::tree]
  (next node))

(defn-spec insert-node-op ::change
  [path ::path, kind ::kind]
  {::op ::insert-node
   ::path path
   ::kind kind})

(defn-spec insert-text-op ::change
  [path ::path, text ::text]
  {::op ::insert-text
   ::path path
   ::text text})

(defn-spec remove-node-op ::change
  ([path ::path]
   (remove-node-op path false))
  ([path ::path, dry? ::dry?]
   {::op ::remove-node
    ::path path
    ::dry? (boolean dry?)}))

(defn-spec diff-group (s/tuple ::groups ::changes)
  [{:keys [::xs ::ys ::path]} ::group
   groups (s/spec ::groups)
   changes (s/spec ::changes)]
  (let [dry? (nil? ys)]
    (loop [xs xs
           ys ys
           index 0
           groups groups
           changes changes]
      (if (or (seq xs) (seq ys))
        (let [[x & xs] xs
              [y & ys] ys
              xk (kind x)
              yk (kind y)
              xcs (children x)
              ycs (children y)
              path (conj path index)
              action (cond
                       (= xk yk) :skip
                       (nil? x) :insert
                       (nil? y) :remove
                       :else :replace)]
          (recur xs
                 ys
                 (cond-> index
                   y inc)
                 (cond
                   (= action :replace) (conj groups {::xs nil
                                                     ::ys ycs
                                                     ::path path})
                   (= xcs ycs) groups
                   :else (conj groups {::xs xcs
                                       ::ys ycs
                                       ::path path}))
                 (case action
                   :skip changes
                   :insert (conj changes (insert-node-op path yk))
                   :remove (conj changes (remove-node-op path dry?))
                   :replace (conj changes
                                  (remove-node-op path dry?)
                                  (insert-node-op path yk)))))
        [groups changes]))))

(defn-spec diff ::changes
  [x ::tree, y ::tree]
  (loop [[group & groups] [{::xs [x], ::ys [y], ::path []}]
         changes []]
    (if group
      (let [[groups changes] (diff-group group groups changes)]
        (recur groups changes))
      changes)))
