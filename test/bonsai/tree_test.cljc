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
    (t/is (= (tree/diff [nil [:y]] [[:x] [:z]]) [[:insert [0] [:x]] [:replace [1] [:z]]]))))
