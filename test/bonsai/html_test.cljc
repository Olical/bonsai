(ns bonsai.html-test
  (:require [bonsai.html :as sut]
            #?(:clj [clojure.test :as t]
               :cljs [cljs.test :as t :include-macros true])))

#_(t/deftest render
  (t/testing "nil gives you nothing"
    (t/is (= "" (sut/render))))
  (t/testing "basic trees are rendered"
    (t/is (= "<p><span>Hello</span>, <span>World!<span></p>"
             (sut/render [:p [:span "Hello"] ", " [:span "World!"]]))))
  (t/testing "we still get nice spec errors"))
