(ns bonsai.tree-test
  (:require [bonsai.tree :as sut]
            #?(:clj [clojure.test :as t]
               :cljs [cljs.test :as t :include-macros true])))

(t/deftest basic?
  (t/testing "does it work?"
    (t/is (= true sut/good?))))
