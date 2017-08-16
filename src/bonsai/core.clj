(ns bonsai.core)

(defmulti event identity)
(defmulti effect identity)

(defmethod event :default [ev-name]
  (throw (str "No event called " ev-name)))

(defmethod effect :default [ef-name]
  (throw (str "No effect called " ef-name)))

(def apply-effects!)

(defmethod event :foo []
  (println "hi"))

(event :foo)

(defn handle! [state! ev-name & args]
  (swap! state! #(apply event ev-name % args))
  (apply-effects! state!))

;; (defn apply-effects! [state!]
;;   (doseq [[ef-name args] ()]
;;     (apply effect ef-name (partial handle! state!) args))
;;   (with-meta state (dissoc (meta state) ::effects)))

;; (defn with-effect [state effect & args]
;;   (with-meta state (update (meta state) ::effects conj [effect args])))

;; Testing...

(def app (atom {:val 0}))

(defmethod event :add [state n]
  (-> state
      (update :val #(+ n %))))

(handle! app :add 10)

@app
