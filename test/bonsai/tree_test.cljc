(ns bonsai.tree-test
  (:require [bonsai.tree :as sut]
            [expound.alpha :as expound]
            [#?(:clj orchestra.spec.test, :cljs orchestra-cljs.spec.test) :as st]
            [#?(:clj clojure.spec.alpha, :cljs cljs.spec.alpha) :as s]
            [#?(:clj clojure.test, :cljs cljs.test) :as t]))

(st/instrument)
(set! s/*explain-out* expound/printer)

(t/deftest diff
  (t/testing "empty trees yield no diff"
    (t/is (= []
             (sut/diff nil nil))))
  (t/testing "identical trees yield no diff"
    (t/is (= []
             (sut/diff [:p] [:p]))))
  (t/testing "nodes can be removed"
    (t/is (= [{::sut/op ::sut/remove-node
               ::sut/path [0]}]
             (sut/diff [:p] nil))))
  (t/testing "nodes can be added"
    (t/is (= [{::sut/op ::sut/insert-node
               ::sut/path [0]
               ::sut/kind :p}]
             (sut/diff nil [:p]))))
  (t/testing "nodes can be replaced (remove then insert)"
    (t/is (= [{::sut/op ::sut/remove-node
               ::sut/path [0 0]}
              {::sut/op ::sut/insert-node
               ::sut/path [0 0]
               ::sut/kind :ol}
              {::sut/op ::sut/insert-node
               ::sut/path [0 0 0]
               ::sut/kind :li}
              {::sut/op ::sut/insert-node
               ::sut/path [0 0 1]
               ::sut/kind :li}]
             (sut/diff [:div [:ul [:li] [:li]]]
                       [:div [:ol [:li] [:li]]]))))
  (t/testing "growing"
    (t/is (= [{::sut/op ::sut/insert-node
               ::sut/path [0 1]
               ::sut/kind :li}
              {::sut/op ::sut/insert-node
               ::sut/path [0 2]
               ::sut/kind :li}]
             (sut/diff [:ul [:li]]
                       [:ul [:li] [:li] [:li]]))))
  (t/testing "shrinking"
    (t/is (= [{::sut/op ::sut/remove-node
               ::sut/path [0 1]}
              {::sut/op ::sut/remove-node
               ::sut/path [0 2]}]
             (sut/diff [:ul [:li] [:li] [:li]]
                       [:ul [:li]]))))

  ;; Flipping something to and from nil is fast.
  (t/testing "inserting by replacing a nil is calm"
    (t/is (= [{::sut/op ::sut/insert-node
               ::sut/path [0 1]
               ::sut/kind :b}]
             (sut/diff [:ul [:a] nil [:c] [:d]]
                       [:ul [:a] [:b] [:c] [:d]]))))

  ;; Inserting new things or removing things completely is slow.
  (t/testing "inserting without replacing a nil is violent"
    (t/is (= [{::sut/op ::sut/remove-node
               ::sut/path [0 1]}
              {::sut/op ::sut/insert-node
               ::sut/path [0 1]
               ::sut/kind :b}
              {::sut/op ::sut/remove-node
               ::sut/path [0 2]}
              {::sut/op ::sut/insert-node
               ::sut/path [0 2]
               ::sut/kind :c}
              {::sut/op ::sut/insert-node
               ::sut/path [0 3]
               ::sut/kind :d}]
             (sut/diff [:ul [:a] [:c] [:d]]
                       [:ul [:a] [:b] [:c] [:d]])))))
