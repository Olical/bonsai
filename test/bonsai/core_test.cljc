(ns bonsai.core-test
  (:require [clojure.test :as t]
            [bonsai.core :as bonsai]))

(t/deftest html
  (t/testing "empty tree yields no html"
    (t/is (= (bonsai/html nil) ""))
    (t/is (= (bonsai/html []) "")))

  (t/testing "just a text node"
    (t/is (= (bonsai/html ["Hi!"]) "Hi!")))

  (t/testing "simple trees"
    (t/is (= (bonsai/html [[:p "Hello" ", " "World!"] [:p [:span "Hi!"]]])
             "<p>Hello, World!</p><p><span>Hi!</span></p>"))
    (t/is (= (bonsai/html [[:ul [:li "x"] [:li "y"] [:li [:div "z"]]]])
             "<ul><li>x</li><li>y</li><li><div>z</div></li></ul>"))))

(t/deftest diff
  (t/testing "empty trees yield no diff"
    (t/is (= (bonsai/diff nil nil) []))
    (t/is (= (bonsai/diff [] []) [])))

  (t/testing "just a text node"
    (t/is (= (bonsai/diff [nil] ["Hi!"]) [[:insert [0] "Hi!"]])))

  (t/testing "simple diffs of flat trees"
    (t/is (= (bonsai/diff [[:x] [:y]] [nil [:y]]) [[:remove [0]]]))
    (t/is (= (bonsai/diff [nil [:y]] [[:x] [:y]]) [[:insert [0] [:x]]]))
    (t/is (= (bonsai/diff [nil [:y]] [[:x] [:z]]) [[:insert [0] [:x]] [:replace [1] [:z]]])))

  (t/testing "simple recursive tree diffs"
    (t/is (= (bonsai/diff [[:x [:y "henlo" [:z "world"]]] [:foo nil nil]]
                        [[:x [:y "Hello" [:z [:strong "World!"]]]] [:foo nil "This is new!"]])
             [[:replace [0 0 0] "Hello"]
              [:replace [0 0 1 0] [:strong "World!"]]
              [:insert [1 1] "This is new!"]])))

  (t/testing "very different trees"
    (t/is (= (bonsai/diff [[:ul [:li "x"] [:li "y"] [:li "z"]]
                         [:h1 "Hello"]
                         [:p "from"]
                         [:h2 "World!"]]
                        [[:h1 "World!"]
                         [:ol [:li "z"] [:li "y"] [:li "x"]]
                         [:p "to"]
                         [:p "Hello"]])
             [[:replace [0] [:h1 "World!"]]
              [:replace [1] [:ol [:li "z"] [:li "y"] [:li "x"]]]
              [:replace [2 0] "to"]
              [:replace [3] [:p "Hello"]]]))))
