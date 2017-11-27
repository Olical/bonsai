(ns bonsai.dom
  (:require [bonsai.tree :as tree]
            [cljs.spec.alpha :as s]
            [clojure.string :as str]))

(defonce listeners-key (gensym "bonsai-listeners"))
(defonce opts-key (gensym "bonsai-opts"))

(defn tree->el [document [type value :as tree]]
  (case type
    :text (.createTextNode document value)
    :node (.createElement document (name (:name value)))))

(defn document [el]
  (.-ownerDocument el))

(defn apply-listener [tree key {:keys [transient-state!]}]
  (when-let [listener (key (tree/attrs tree))]
    (swap! transient-state! #(apply (first listener) % (rest listener)))))

(defn remove! [host el tree opts]
  (apply-listener tree :on-remove opts)
  (.removeChild host el))

(defn insert! [host ref-el tree opts]
  (apply-listener tree :on-insert opts)
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

(defn replace-listener! [el event-key event {:keys [host]}]
  (let [listeners (aget el listeners-key)
        old-f (event-key listeners)
        new-f (fn [ev]
                (let [{:keys [transient-state! on-change]} (aget host opts-key)
                      state @transient-state!
                      next-state (apply (first event) state ev (rest event))]
                  (when (and on-change (not= state next-state))
                    (on-change next-state))))
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

(defn render-attrs! [pas nas el opts]
  (doseq [attr-key (set (concat (keys pas) (keys nas)))]
    (let [pa (get pas attr-key)
          na (get nas attr-key)]
      (cond
        (= pa na) nil
        (tree/lifecycle-event? attr-key) nil
        (tree/on-keyword? attr-key) (cond
                                      (and (tree/real? pa) (tree/void? na)) (remove-listener! el attr-key)
                                      :else (replace-listener! el attr-key na opts))
        :else (cond
                (and (tree/real? pa) (tree/void? na)) (remove-attr! el attr-key)
                :else (replace-attr! el attr-key na))))))

(defn migrate! [host old [prev-type _ :as prev-tree] [type value :as tree] opts]
  (if (= prev-type type :text)
    (aset old "nodeValue" value)
    (let [el (tree->el (document old) tree)]
      (render-attrs! nil (tree/attrs tree) el opts)
      (apply-listener prev-tree :on-remove opts)
      (.replaceChild host el old)
      (doseq [child (children old)]
        (.appendChild el child))
      (apply-listener tree :on-insert opts))))

(defn render-recur! [pvs nxs host opts]
  (loop [pvs pvs
         nxs (tree/flatten-seqs nxs)
         acc []
         n 0]
    (let [pv (first pvs)
          nx (tree/expand pv (first nxs) (assoc opts :state (deref (:transient-state! opts))))
          el (nth-child host n)]
      (if (or pv nx)
        (do
          (cond
            (= pv nx) nil
            (and (tree/real? pv) (tree/void? nx)) (remove! host el pv opts)
            (and (tree/void? pv) (tree/real? nx)) (insert! host el nx opts)
            (and (tree/real? pv) (tree/real? nx) (not= (tree/fingerprint pv) (tree/fingerprint nx))) (migrate! host el pv nx opts))
          (let [n (+ n (when (tree/void? nx) -1))
                child-el (nth-child host n)
                result (when (tree/real? nx)
                         (render-attrs! (tree/attrs pv) (tree/attrs nx) child-el opts)
                         (render-recur! (tree/children pv) (tree/children nx) child-el opts))]
            (recur (rest pvs)
                   (rest nxs)
                   (conj acc (tree/with-children nx result))
                   (inc n))))
        acc))))

(defn render! [pv nx-src host {:keys [state on-change] :as opts}]
  (let [nx (tree/conform nx-src)
        transient-state! (atom state)
        opts (merge opts {:host host
                          :transient-state! transient-state!})
        result (do
                 (aset host opts-key opts)
                 (render-recur! pv
                                (if (seq? nx)
                                  nx
                                  (list nx))
                                host
                                opts))
        next-state @transient-state!]
    (when (and on-change (not= state next-state))
      (on-change next-state))
    result))

(def request-animation-frame
  (if (exists? js/window)
    #(.requestAnimationFrame js/window %)
    #(js/setTimeout % 0)))

(defn mount-recur! [prev-tree tree host state on-render]
  (let [render-result (atom nil)]
    (reset! render-result
            (render! prev-tree
                     tree
                     host
                     {:state state
                      :on-change (fn [next-state]
                                   (request-animation-frame
                                    #(mount-recur! @render-result tree host next-state on-render)))}))
    (when on-render
      (on-render))))

(defn mount! [{:keys [tree host state on-render]}]
  (mount-recur! nil tree host state on-render))
