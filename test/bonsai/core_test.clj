(ns bonsai.core-test
  (:require [bonsai.core :as b]
            [clojure.test :as t]))

(defn atom? [x]
  (instance? clojure.lang.Atom x))

(defn init []
  (b/create (atom {})))

(defn state [app]
  (-> app :bonsai.core/!state deref))

(defn events [app]
  (-> app :bonsai.core/!event-handlers deref))

(defn effects [app]
  (-> app :bonsai.core/!effect-handlers deref))

(t/deftest bonsai-test
  (t/testing "create"
    (t/testing "builds a map of keys to atoms"
      (let [app (init)]
        (t/is (= (count (keys app)) 3))
        (t/is (every? atom? (vals app)))
        (t/is (= (map deref (vals app)) [{} {} {}])))))

  (t/testing "define-event!"
    (t/testing "associates an event key with a fn"
      (let [app (init)]
        (b/define-event! app :up inc)
        (t/is (= (events app) {:up inc}))))
    (t/testing "last association wins"
      (let [app (init)]
        (b/define-event! app :up inc)
        (b/define-event! app :up dec)
        (t/is (= (events app) {:up dec})))))

  (t/testing "define-effect!"
    (t/testing "associates an effect key with a fn"
      (let [app (init)]
        (b/define-effect! app :get identity)
        (t/is (= (effects app) {:get identity}))))
    (t/testing "last association wins"
      (let [app (init)]
        (b/define-effect! app :get identity)
        (b/define-effect! app :get not)
        (t/is (= (effects app) {:get not})))))

  (t/testing "defevent"
    (t/testing "performs a define-event! wrapped in an fn"
      (let [app (init)]
        (b/defevent app :foo [] :bar)
        (t/is (= ((:foo (events app))) :bar)))))

  (t/testing "defeffect"
    (t/testing "performs a define-effect! wrapped in an fn"
      (let [app (init)]
        (b/defeffect app :foo [] :bar)
        (t/is (= ((:foo (events app))) :bar)))))

  (t/testing "with-effect"
    (t/testing "adds an effect to the effects seq")
    (t/testing "effects can have arguments")
    (t/testing "there can be multiple effects"))

  (t/testing "apply-effects!"
    (t/testing "with no effects, does nothing")
    (t/testing "with-effect it performs the effect")
    (t/testing "the effects are consumed and removed after application"))

  (t/testing "without-effects"
    (t/testing "applied to nothing does nothing")
    (t/testing "applied to effects removes them cleanly"))

  (t/testing "handle!"
    (t/testing "applies event handlers to the state")
    (t/testing "applies effects to the state")
    (t/testing "removes the effects when done")
    (t/testing "returns the original application map")))
