(ns bonsai.diff-test
  (:require [bonsai.diff :as sut]
            [clojure.test :as t]))

(t/deftest adding
  (t/testing "just some basic stuff"
    (t/is (= (sut/add 5 10) (+ 5 10)))))
