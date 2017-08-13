(ns bonsai.core-test
  (:require [bonsai.core :as b]
            [clojure.test :as t]))

(t/deftest bonsai-test
  (t/testing "create"
    (t/testing "builds a map of keys to atoms"))

  (t/testing "define-event!"
    (t/testing "associates an event key with a fn")
    (t/testing "last association wins"))

  (t/testing "define-effect!"
    (t/testing "associates an effect key with a fn")
    (t/testing "last association wins"))

  (t/testing "defevent"
    (t/testing "performs a define-event! wrapped in an fn"))

  (t/testing "defeffect"
    (t/testing "performs a define-effect! wrapped in an fn"))

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
