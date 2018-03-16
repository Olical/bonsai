(ns bonsai.diff-test
  (:require [bonsai.diff :as sut]
            [expound.alpha :as expound]
            [#?(:clj orchestra.spec.test, :cljs orchestra-cljs.spec.test) :as st]
            [#?(:clj clojure.spec.alpha, :cljs cljs.spec.alpha) :as s]
            [#?(:clj clojure.test, :cljs cljs.test) :as t]))

(st/instrument)
(set! s/*explain-out* expound/printer)

(t/deftest changes
  (t/testing "empty trees yield no changes"
    (t/is (= (sut/changes nil nil) [])))
  (t/testing "identical trees yield no changes"
    (t/is (= (sut/changes [:p] [:p]) [])))
  (t/testing "nodes can be removed"
    (t/is (= (sut/changes [:p] nil) [{::sut/op ::sut/remove-node
                                      ::sut/path []}])))
  (t/testing "nodes can be added"
    (t/is (= (sut/changes nil [:p]) [{::sut/op ::sut/insert-node
                                      ::sut/path []
                                      ::sut/kind :p}])))
  (t/testing "nodes can be replaced"
    (t/is (= (sut/changes [:div [:ul [:li] [:li]]]
                          [:div [:ol [:li] [:li]]])
             [{::sut/op ::sut/replace-node
               ::sut/path []
               ::sut/kind :ol}]))))
