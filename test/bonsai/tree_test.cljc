(ns bonsai.tree-test
  (:require [clojure.test :as t]
            [bonsai.tree :as tree]))

(t/deftest diff
  (t/testing "empty trees yield no diff"
    (t/is (= (tree/diff nil nil) []))
    (t/is (= (tree/diff [] []) [])))

  (t/testing "simple diffs of flat trees"
    (t/is (= (tree/diff [[:x] [:y]] [nil [:y]]) [[:remove [0]]]))
    (t/is (= (tree/diff [nil [:y]] [[:x] [:y]]) [[:insert [0] [:x]]]))
    (t/is (= (tree/diff [nil [:y]] [[:x] [:z]]) [[:insert [0] [:x]] [:replace [1] [:z]]])))

  (t/testing "simple recursive tree diffs"
    (t/is (= (tree/diff [[:x [:y "henlo" [:z "world"]]] [:foo nil nil]]
                        [[:x [:y "Hello" [:z [:strong "World!"]]]] [:foo nil "This is new!"]])
             [[:replace [0 0 0] "Hello"]
              [:replace [0 0 1 0] [:strong "World!"]]
              [:insert [1 1] "This is new!"]]))))
