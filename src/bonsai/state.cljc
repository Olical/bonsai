(ns bonsai.state)

(declare next!)

(defn with-effect [state effect & args]
  (vary-meta state update ::effects conj [effect args]))

(defn without-effects [state]
  (vary-meta state dissoc ::effects))

(defn consume-effects! [state!]
  (let [effects (-> @state! meta ::effects)]
    (swap! state! without-effects)
    (doseq [[effect args] effects]
      (apply effect (partial next! state!) args))))

(defn next! [state! action & args]
  (apply swap! state! action args)
  (consume-effects! state!))

