(ns bonsai.diff-test
  (:require [bonsai.diff :as sut]
            [expound.alpha :as expound]
            [#?(:clj orchestra.spec.test
                :cljs orchestra-cljs.spec.test) :as st]
            [#?(:clj clojure.spec.alpha
                :cljs cljs.spec.alpha) :as s]
            [#?(:clj clojure.test
                :cljs cljs.test) :as t]))

(st/instrument)
(set! s/*explain-out* expound/printer)

(t/deftest adding
  (t/testing "just some basic stuff"
    (t/is (= (sut/add 5 10) (+ 5 10)))))
