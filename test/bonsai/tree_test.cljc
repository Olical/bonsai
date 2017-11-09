(ns bonsai.tree-test
  (:require [bonsai.tree :as sut]
            #?(:clj [clojure.test :as t]
               :cljs [cljs.test :as t :include-macros true])))

(t/deftest conform
  (t/testing "nil is void"
    (t/is (= [:void nil] (sut/conform nil))))
  (t/testing "strings are text"
    (t/is (= [:text "hi"] (sut/conform "hi"))))
  (t/testing "nodes are nodes (containing children and attrs)"
    (t/is (= [:node {:name :p}] (sut/conform [:p])))
    (t/is (= [:node {:name :p
                     :children [[:text "hi"]]}]
             (sut/conform [:p "hi"])))
    (t/is (= [:node {:name :p
                     :attrs {:id [:text "foo"]
                             :class [:void nil]}
                     :children [[:text "hi"]]}]
             (sut/conform [:p {:id "foo" :class nil} "hi"]))))
  (t/testing "seqs of nodes are, well, seqs of nodes"
    (t/is (= [:node-seq '([:text "a"] [:text "b"] [:text "c"])]
             (sut/conform (map identity ["a" "b" "c"])))))
  (t/testing "bad conforms throw errors"
    (t/is (thrown-with-msg? #?(:clj Exception
                               :cljs js/Error)
                            #"should satisfy"
                            (sut/conform :bad)))))
