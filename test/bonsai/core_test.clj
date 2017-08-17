(ns bonsai.core-test
  (:require [bonsai.core :as b]
            [clojure.test :as t]))

(defn get-foo
  "Effect: Passes :foo to the given action fn."
  [next! f]
  (next! f :foo))

(defn val-to
  "Action: Replaces val with the given value in the state."
  [state v]
  (assoc state :val v))

(defn val-to-foo-eff
  "Action: Sets the value to foo through an effect."
  [state]
  (-> state
      (b/with-effect get-foo val-to)))

(defn effects
  "Extracts the effects out of the meta of an object."
  [o]
  (-> o meta :bonsai.core/effects))

(t/deftest next!-test)

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
  (t/testing "executes the effects with next! and removes them"
    (let [state! (atom (b/with-effect {} get-foo val-to))]
      (t/is (= [[get-foo [val-to]]] (-> state! deref effects)))
      (b/consume-effects! state!)
      (t/is (= nil (-> state! deref effects)))
      (t/is (= {:val :foo} (-> state! deref))))))

(t/deftest next!-test
  (t/testing "applies the action to the state"
    (let [state! (atom {})]
      (b/next! state! val-to :foo)
      (t/is (= {:val :foo} (-> state! deref)))))
  (t/testing "effects can be applied from actions"
    (let [state! (atom {})]
      (b/next! state! val-to-foo-eff)
      (t/is (= {:val :foo} (-> state! deref))))))
