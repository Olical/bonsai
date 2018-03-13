(ns bonsai.diff-test
  (:require [bonsai.diff :as sut]
            [expound.alpha :as expound]
            [#?(:clj orchestra.spec.test, :cljs orchestra-cljs.spec.test) :as st]
            [#?(:clj clojure.spec.alpha, :cljs cljs.spec.alpha) :as s]
            [#?(:clj clojure.test, :cljs cljs.test) :as t]))

(st/instrument)
(set! s/*explain-out* expound/printer)

(t/deftest diff
  (t/testing "empty trees yield no changes"
    (t/is (= (sut/diff nil nil) [])))
  (t/testing "identical trees yield no changes"
    (t/is (= (sut/diff [:p] [:p]) [])))
  (t/testing "nodes can be removed"
    (t/is (= (sut/diff [:p] nil) [{::sut/op :remove
                                   ::sut/path [0]}])))
  (t/testing "nodes can be added"
    (t/is (= (sut/diff nil [:p]) [{::sut/op :add
                                   ::sut/path [0]
                                   ::sut/node [:p]}]))))
