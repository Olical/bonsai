(ns bonsai.core)

(defn create [!state]
  {::!state !state
   ::!event-handlers (atom {})
   ::!effect-handlers (atom {})})

;; Maybe make this register-event! and wrap it in a nice macro.
(defn event! [app key f]
  (swap! (::!event-handlers app) #(assoc % key f)))

;; Same goes for this.
;; defeffect and defevent should be good.
(defn effect! [app key f]
  (swap! (::!effect-handlers app) #(assoc % key f)))

;; Break this down into other functions.
(defn handle! [app key & args]
  (let [f (-> app ::!event-handlers deref key)
        state (swap! (::!state app) #(apply f % args))
        effects (-> state meta ::effects)]
    (doseq [[effect args] effects]
      (let [f (-> app ::!effect-handlers deref effect)]
        (apply f (partial handle! app) args)))
    (swap! (::!state app) #(with-meta % (dissoc (meta %) ::effects)))
    app))

(defn with-effect [state effect & args]
  (with-meta state (update (meta state) ::effects conj [effect args])))

;; Tests...

(def state (atom {:val 0}))
(def app (create state))

(event! app :up #(update % :val inc))
(event! app :down #(update % :val dec))
(event! app :add (fn [state & args] (apply update state :val + args)))
(event! app :slow-add-pi
        (fn [state]
          (-> state
              (with-effect :compute-pi :add))))

(effect! app :compute-pi
         (fn [handle! response-event]
           (future (do (Thread/sleep 3000)
                       (handle! response-event 3.14)
                       (println "fired!")))))

(handle! app :up)
(handle! app :add 5)
(handle! app :slow-add-pi)

@state
