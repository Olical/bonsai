(ns bonsai.state-test
  (:require [bonsai.state :as sut]
            #?(:clj [clojure.test :as t]
               :cljs [cljs.test :as t :include-macros true])))

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
  (sut/with-effect state get-foo val-to))

(defn effects
  "Extracts the effects out of the meta of an object."
  [o]
  (-> o meta :bonsai.state/effects))

(t/deftest with-effect-test
  (t/testing "adds an effect to the meta data"
    (t/is (= [[+ [5 10]]] (-> {} (sut/with-effect + 5 10) effects))))
  (t/testing "can add multiple effects"
    (let [state (-> {}
                    (sut/with-effect + 5)
                    (sut/with-effect - 10))]
      (t/is (= [[- [10]] [+ [5]]] (effects state))))))

(t/deftest without-effects-test
  (t/testing "does nothing to nil effects"
    (t/is (= nil (-> {} sut/without-effects effects))))
  (t/testing "removes any effects"
    (t/is (= nil (-> {} (sut/with-effect + 5 10) sut/without-effects effects)))))

(t/deftest consume-effects!-test
  (t/testing "executes the effects with next! and removes them"
    (let [state! (atom (sut/with-effect {} get-foo val-to))]
      (t/is (= [[get-foo [val-to]]] (-> @state! effects)))
      (sut/consume-effects! state!)
      (t/is (= nil (-> @state! effects)))
      (t/is (= {:val :foo} @state!)))))

(t/deftest next!-test
  (t/testing "applies the action to the state"
    (let [state! (atom {})]
      (sut/next! state! val-to :foo)
      (t/is (= {:val :foo} @state!))))
  (t/testing "effects can be applied from actions"
    (let [state! (atom {})]
      (sut/next! state! val-to-foo-eff)
      (t/is (= {:val :foo} @state!)))))

(t/deftest consumer
  (t/testing "a consumer can define actions and effects that can be used in conjunction with each other"
    (let [state! (atom {:val 0})
          calc-pi (fn [next! a] (next! a 3.14))
          add (fn [state n] (-> state (update :val (partial + n))))
          add-pi (fn [state] (-> state (sut/with-effect calc-pi add)))]
      (t/is (= {:val 0} @state!))
      (sut/next! state! add 5)
      (t/is (= {:val 5} @state!))
      (sut/next! state! add-pi)
      (t/is (= {:val 8.14} @state!)))))
