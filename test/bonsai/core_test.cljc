(ns bonsai.core-test
  (:require [bonsai.core :as b]
            [#?(:clj clojure.test
                :cljs cljs.test) :as t]))

(defn get-foo
  "Effect: Passes :foo to the given action fn."
  [step! f]
  (step! f :foo))

(defn val-to
  "Action: Replaces val with the given value in the state."
  [state v]
  (assoc state :val v))

(defn val-to-foo-eff
  "Action: Sets the value to foo through an effect."
  [state]
  (b/with-effect state get-foo val-to))

(defn effects
  "Extracts the effects out of the meta of an object."
  [o]
  (-> o meta :bonsai.core/effects))

(t/deftest with-effect-test
  (t/testing "adds an effect to the meta data"
    (t/is (= [[+ [5 10]]] (-> {} (b/with-effect + 5 10) effects))))
  (t/testing "can add multiple effects"
    (let [state (-> {}
                    (b/with-effect + 5)
                    (b/with-effect - 10))]
      (t/is (= [[- [10]] [+ [5]]] (effects state))))))

(t/deftest without-effects-test
  (t/testing "does nothing to nil effects"
    (t/is (= nil (-> {} b/without-effects effects))))
  (t/testing "removes any effects"
    (t/is (= nil (-> {} (b/with-effect + 5 10) b/without-effects effects)))))

(t/deftest consume-effects!-test
  (t/testing "executes the effects with step! and removes them"
    (let [state! (atom (b/with-effect {} get-foo val-to))]
      (t/is (= [[get-foo [val-to]]] (-> @state! effects)))
      (b/consume-effects! state!)
      (t/is (= nil (-> @state! effects)))
      (t/is (= {:val :foo} @state!)))))

(t/deftest step!-test
  (t/testing "applies the action to the state"
    (let [state! (atom {})]
      (b/step! state! val-to :foo)
      (t/is (= {:val :foo} @state!))))
  (t/testing "effects can be applied from actions"
    (let [state! (atom {})]
      (b/step! state! val-to-foo-eff)
      (t/is (= {:val :foo} @state!)))))

(t/deftest stepper-test
  (t/testing "creates a function that will apply the action to the state"
    (let [state! (atom {})]
      ((b/stepper state! val-to :foo))
      (t/is (= {:val :foo} @state!))
      (t/is (= (b/stepper state! val-to :foo)
               (b/stepper state! val-to :foo)))
      (t/is (not= (b/stepper state! val-to :foo)
                  (b/stepper state! val-to :bar))))))

(t/deftest consumer
  (t/testing "a consumer can define actions and effects that can be used in conjunction with each other"
    (let [state! (atom {:val 0})
          calc-pi (fn [step! a] (step! a 3.14))
          add (fn [state n] (-> state (update :val (partial + n))))
          add-pi (fn [state] (-> state (b/with-effect calc-pi add)))]
      (t/is (= {:val 0} @state!))
      (b/step! state! add 5)
      (t/is (= {:val 5} @state!))
      (b/step! state! add-pi)
      (t/is (= {:val 8.14} @state!)))))
