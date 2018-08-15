(ns bonsai.dom-test
  (:require [clojure.test :as t]
            [bonsai.dom :as dom]
            [jsdom :as jsdom]))

(t/deftest patch
  (t/testing "patching nothing should yield no change"
    (t/is (= (dom/patch) nil))))
