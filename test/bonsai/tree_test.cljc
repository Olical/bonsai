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
    (t/is (= [(sut/remove-node-op [0])]
             (sut/diff [:p] nil))))
  (t/testing "nodes can be added"
    (t/is (= [{::sut/op ::sut/insert-node
               ::sut/path [0]
               ::sut/kind :p}]
             (sut/diff nil [:p]))))
  (t/testing "nodes can be replaced (remove then insert)"
    (t/is (= [(sut/remove-node-op [0 0])
              (sut/insert-node-op [0 0] :ol)
              (sut/insert-node-op [0 0 0] :li)
              (sut/insert-node-op [0 0 1] :li)]
             (sut/diff [:div [:ul [:li] [:li]]]
                       [:div [:ol [:li] [:li]]]))))
  (t/testing "growing"
    (t/is (= [(sut/insert-node-op [0 1] :li)
              (sut/insert-node-op [0 2] :li)]
             (sut/diff [:ul [:li]]
                       [:ul [:li] [:li] [:li]]))))
  (t/testing "shrinking"
    (t/is (= [(sut/remove-node-op [0 1])
              (sut/remove-node-op [0 1])]
             (sut/diff [:ul [:li] [:li] [:li]]
                       [:ul [:li]]))))
  (t/testing "inserting by replacing a nil is calm"
    (t/is (= [(sut/insert-node-op [0 1] :b)]
             (sut/diff [:ul [:a] nil [:c] [:d]]
                       [:ul [:a] [:b] [:c] [:d]]))))
  (t/testing "inserting without replacing a nil is violent"
    (t/is (= [(sut/remove-node-op [0 1])
              (sut/insert-node-op [0 1] :b)
              (sut/remove-node-op [0 2])
              (sut/insert-node-op [0 2] :c)
              (sut/insert-node-op [0 3] :d)]
             (sut/diff [:ul [:a] [:c] [:d]]
                       [:ul [:a] [:b] [:c] [:d]]))))
  (t/testing "replacing with a nil removes and keeps indexes correct"
    (t/is (= [(sut/remove-node-op [0 1])
              (sut/insert-node-op [0 2] :d)]
             (sut/diff [:ul [:a] [:b] [:c]]
                       [:ul [:a] nil [:c] [:d]])))
    (t/is (= [(sut/remove-node-op [0 0])
              (sut/remove-node-op [0 0 1])
              (sut/insert-node-op [0 0 2] :d)
              (sut/remove-node-op [0 0 0] true)]
             (sut/diff [:div [:div [:p]] [:ul [:a] [:b] [:c]]]
                       [:div nil [:ul [:a] nil [:c] [:d]]]))))
  (t/testing "flipping multiple nils is detected"
    (t/is (= [(sut/insert-node-op [0 1] :b)
              (sut/remove-node-op [0 2])
              (sut/insert-node-op [0 2] :e)]
             (sut/diff [:ul [:a] nil [:c] nil nil [:f]]
                       [:ul [:a] [:b] nil nil [:e] [:f]]))))
  (comment
    (t/testing "nodes can be strings"
      (t/is (= [(sut/insert-text-op [0] "Hello, World!")]
               (sut/diff nil "Hello, World!")))
      (t/is (= [(sut/remove-node-op [0 0])
                (sut/insert-text-op [0 0] "world")]
               (sut/diff [:p "hello"]
                         [:p "world"]))))
    (t/testing "toggling with text nodes"
      (t/is (= [(sut/insert-node-op [0 1] :li)
                (sut/insert-text-op [0 2 0] "c")
                (sut/insert-text-op [0 1 0] "b")
                (sut/remove-node-op [0 0 0])]
               (sut/diff [:ul [:li "a"] nil [:li nil]]
                         [:ul [:li nil] [:li "b"] [:li "c"]]))))))
