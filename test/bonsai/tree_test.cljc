(ns bonsai.tree-test
  (:require [bonsai.tree :as sut]
            [clojure.spec.test.alpha :as st]
            #?(:clj [clojure.test :as t]
               :cljs [cljs.test :as t :include-macros true])))

(st/instrument)

(t/deftest parse
  (t/testing "trees are parsed as expected"
    (t/is (= [:empty nil] (sut/parse nil)))
    (t/is (= [:text "foo"] (sut/parse "foo")))
    (t/is (= [:tag {:name :p}] (sut/parse [:p])))
    (t/is (= [:tag {:name :p :children [[:text "bar"]]}] (sut/parse [:p "bar"])))
    (t/is (= [:seq [[:text "0"] [:text "1"] [:text "2"]]]
             (sut/parse (for [x (range 3)] (str x)))))
    (t/is (= [:tag {:name :p :children [[:seq [[:text "a"] [:text "b"]]]
                                        [:tag {:name :span :children [[:text "c"]]}]]}]
             (sut/parse [:p ["a" "b"] [:span "c"]])))
    (t/is (= [:tag {:name :p :attrs {:foo "bar"}}]
             (sut/parse [:p {:foo "bar"}])))
    (t/is (= [:tag {:name :p
                    :attrs {:foo "bar"}
                    :children [[:text "baz"]]}]
             (sut/parse [:p {:foo "bar"} "baz"])))))
