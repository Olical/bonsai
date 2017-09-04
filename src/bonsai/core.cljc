(ns bonsai.core
  "Minimalistic state management designed for Reagent and state that changes
  over time."
  (:refer-clojure :exclude [stepper]))

;; Forward declaration to resolve a circular dependency between step! and
;; consume-effects!.
(declare step!)

(defn with-effect
  "Takes a state object, effect fn and extra arguments to pass to the effect fn.
  The effect is registered in the meta of the state. Returns the new state with
  the effect attached for later execution within step!.

  Ex: (-> state
          (with-effect post-comment comment))"
  [state effect & args]
  (vary-meta state update ::effects conj [effect args]))

(defn without-effects
  "Takes a state object and removes the effects from it. Returns the state
  without the effects meta data.

  Ex: (-> state
          (bonsai/with-effect post-comment)
          (without-effects))"
  [state]
  (vary-meta state dissoc ::effects))

(defn consume-effects!
  "Takes a state atom containing your state. Pulls any effects out of the state
  and removes them when done. The effects are sequentially applied to the
  state.

  The effect fn is applied with a partially applied step! fn it should call to
  pass results on to further actions.

  Ex: (bonsai/consume-effects! state!)"
  [state!]
  (let [effects (-> state! deref meta ::effects)]
    (swap! state! without-effects)
    (doseq [[effect args] effects]
      (apply effect (partial step! state!) args))))

(defn step!
  "Takes a state atom containing your state, an action fn and any arguments you
  want to pass to the action. The action can attach effects with with-effect,
  the effects can use their first argument to dispatch further actions carrying
  results.

  Your action fn will be given the current state and any arguments provided, it
  should return the new state, using the -> threading macro is a good idea
  here.

  Ex: (bonsai/step! state! add 10 20)"
  [state! action & args]
  (apply swap! state! action args)
  (consume-effects! state!))

(def stepper
  "Partially applies step! with your arguments, if you pass the same arguments
  twice, you will get the same function reference back. Useful for attaching to
  Reagent event handlers.

  Ex: [:button {:on-click (bonsai/stepper state! add 10 20)}]"
  (memoize (fn [& args] #(apply step! args))))
