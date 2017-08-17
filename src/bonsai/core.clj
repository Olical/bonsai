(ns bonsai.core)

(def next!)

(defn with-effect [state effect & args]
  (vary-meta state update ::effects conj [effect args]))

(defn without-effects [state]
  (vary-meta state dissoc ::effects))

(defn consume-effects! [state!]
  (let [effects (-> state! deref meta ::effects)]
    (swap! state! without-effects)
    (doseq [[effect args] effects]
      (apply effect (partial next! state!) args))))

(defn next! [state! action & args]
  (apply swap! state! action args)
  (consume-effects! state!))

;; Testing...

(def app (atom {:val 0}))

(defn after-ms [next! ms message & args]
  (future
    (println "start")
    (Thread/sleep ms)
    (apply next! message args)
    (println "end")))

(defn add [state n]
  (-> state
      (update :val + n)))

(defn slow-add [state a b]
  (-> state
      (add a)
      (with-effect after-ms 1000 add b)))

(next! app slow-add 10 5)

@app
