(ns bonsai.tree-test
  (:require [bonsai.tree :as sut]
            #?(:clj [clojure.test :as t]
               :cljs [cljs.test :as t :include-macros true])))

(t/deftest conform
  (t/testing "nil is void"
    (t/is (= [:void nil] (sut/conform nil)))))
