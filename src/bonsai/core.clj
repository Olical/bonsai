(ns bonsai.core)

(defn create [!state]
  {::!state !state
   ::!event-handlers (atom {})
   ::!effect-handlers (atom {})})

(defn define-event! [app key f]
  (swap! (::!event-handlers app) #(assoc % key f)))

(defn define-effect! [app key f]
  (swap! (::!effect-handlers app) #(assoc % key f)))

(defmacro defevent [app key args body]
  `(define-event! ~app ~key (fn ~args ~body)))

(defmacro defeffect [app key args body]
  `(define-effect! ~app ~key (fn ~args ~body)))

(defn apply-effects! [app effects handle!]
  (doseq [[effect args] effects]
    (let [f (-> app ::!effect-handlers deref effect)]
      (apply f handle! args))))

(defn with-effect [state effect & args]
  (with-meta state (update (meta state) ::effects conj [effect args])))

(defn without-effects [state]
  (with-meta state (dissoc (meta state) ::effects)))

(defn handle! [app key & args]
  (let [f (-> app ::!event-handlers deref key)
        state (swap! (::!state app) #(apply f % args))]
    (apply-effects! app (-> state meta ::effects) (partial handle! app))
    (swap! (::!state app) without-effects)
    app))

;; Tests...

(def state (atom {:val 0}))
(def app (create state))

(defevent app :up [state]
  (update state :val inc))

(defevent app :down [state]
  (update state :val dec))

(defevent app :add [state & args]
  (apply update state :val + args))

(defevent app :slow-add-pi [state]
  (-> state
      (with-effect :compute-pi :add)))

(defeffect app :compute-pi [handle! response-event]
  (future (do (Thread/sleep 3000)
              (handle! response-event 3.14)
              (println "fired!"))))

(handle! app :up)
(handle! app :add 5)
(handle! app :slow-add-pi)

@state
