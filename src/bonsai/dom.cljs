(ns bonsai.dom
  (:require [bonsai.tree :as tree]
            [cljs.spec.alpha :as s]
            [clojure.string :as str]))

(defonce listeners-key (gensym "bonsai-listeners"))

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

(defn replace-attr! [el attr-key attr-value]
  (.setAttribute el (name attr-key) attr-value))

(defn event-key->str [event-key]
  (str/replace (name event-key) "on-" ""))

(defn remove-listener! [el event-key]
  (let [listeners (aget el listeners-key)
        f (event-key listeners)
        event-name (event-key->str event-key)]
    (.removeEventListener el event-name f)
    (aset el listeners-key (dissoc listeners event-key))))

(defn replace-listener! [el event-key event]
  (let [listeners (aget el listeners-key)
        old-f (event-key listeners)
        new-f (fn [& args] (apply (:fn event) (concat args (:args event))))
        event-name (event-key->str event-key)]
    (when old-f
      (.removeEventListener el event-name old-f))
    (.addEventListener el event-name new-f)
    (aset el listeners-key (assoc listeners event-key new-f))))

(defn children [el]
  (when el
    (into [] (array-seq (aget el "childNodes")))))

(defn nth-child [el n]
  (nth (children el) n nil))

(defn render-attrs!
  ([nas el]
   (render-attrs! nil nas el))
  ([ps ns el]
   (let [pas (:attr ps)
         nas (:attr ns)]
     (doseq [attr-key (set (concat (keys pas) (keys nas)))]
       (let [pa (get pas attr-key)
             na (get nas attr-key)]
         (cond
           (= pa na) nil
           (and (tree/real? pa) (tree/void? na)) (remove-attr! el attr-key)
           :else (replace-attr! el attr-key (second na))))))
   (let [pes (:event ps)
         nes (:event ns)]
     (doseq [event-key (set (concat (keys pes) (keys nes)))]
       (let [pe (get pes event-key)
             ne (get nes event-key)]
         (cond
           (= pe ne) nil
           (and (tree/real? pe) (tree/void? ne)) (remove-listener! el event-key)
           :else (replace-listener! el event-key (second ne))))))))

(defn migrate! [host old [prev-type _ :as prev-tree] [type value :as tree]]
  (if (= prev-type type :text)
    (aset old "nodeValue" value)
    (let [el (tree->el (document old) tree)]
      (render-attrs! (tree/attrs prev-tree) el)
      (.replaceChild host el old)
      (doseq [child (children old)]
        (.appendChild el child)))))

(defn render-recur! [pvs nxs host]
  (loop [pvs pvs
         nxs (tree/flatten-seqs nxs)
         acc []
         n 0]
    (let [pv (first pvs)
          nx (tree/expand pv (first nxs))
          el (nth-child host n)]
      (if (or pv nx)
        (do
          (cond
            (= pv nx) nil
            (and (tree/real? pv) (tree/void? nx)) (remove! host el)
            (and (tree/void? pv) (tree/real? nx)) (insert! host el nx)
            (and (tree/real? pv) (tree/real? nx) (not= (tree/fingerprint pv) (tree/fingerprint nx))) (migrate! host el pv nx))
          (let [n (+ n (when (tree/void? nx) -1))
                child-el (nth-child host n)
                result (when (tree/real? nx)
                         (render-attrs! (tree/attrs pv) (tree/attrs nx) child-el)
                         (render-recur! (tree/children pv) (tree/children nx) child-el))]
            (recur (rest pvs)
                   (rest nxs)
                   (conj acc (tree/with-children nx result))
                   (inc n))))
        acc))))

(defn render!
  ([nx-src host]
   (render! nil nx-src host))
  ([pv nx-src host]
   (let [nx (tree/conform nx-src)]
     (render-recur! pv
                    (if (seq? nx)
                      nx
                      (list nx))
                    host))))
