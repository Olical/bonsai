(ns bonsai.html-test
  (:require [bonsai.html :as sut]
            [bonsai.tree :as tree]
            [clojure.spec.test.alpha :as st]
            #?(:clj [clojure.test :as t
                     :cljs [cljs.test :as t :include-macros true]])))

(st/instrument)

(t/deftest render
  (t/testing "nothing is nothing"
    (t/is (= "" (sut/render (tree/parse nil)))))
  (t/testing "some basic tags with attrs emit HTML"
    (t/is (= "<p><span class=\"left\">Hello</span>, <span class=\"right\">World!</span>"
             (sut/render (tree/parse [:p
                                      [:span {:class "left"} "Hello"]
                                      ", "
                                      nil
                                      [:span {:class "right"} "World!"]]))))
    (t/is (= "<div>Some numbers: 0 1 2</div>"
             (sut/render (tree/parse [:div "Some numbers:"
                                      (for [n (range 3)] (str " " n))]))))))
