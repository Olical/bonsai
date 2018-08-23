(ns bonsai.tree-test
  (:require [clojure.test :as t]
            [bonsai.tree :as tree]))

(t/deftest normalise-node
  (t/testing "nil is ::void"
    (t/is (= (tree/normalise-node nil) [::tree/void nil nil])))

  (t/testing "strings are ::text"
    (t/is (= (tree/normalise-node "Hello!") [::tree/text nil "Hello!"])))

  (t/testing "nodes without attrs"
    (t/is (= (tree/normalise-node [:p "Hello!"]) [:p nil "Hello!"])))

  (t/testing "nodes with attrs"
    (t/is (= (tree/normalise-node [:p {:title "xyz"} "Hello!"]) [:p {:title "xyz"} "Hello!"]))))

(t/deftest ->html
  (t/testing "empty tree yields no html"
    (t/is (= (tree/->html nil) ""))
    (t/is (= (tree/->html []) "")))

  (t/testing "just a text node"
    (t/is (= (tree/->html ["Hi!"]) "Hi!")))

  (t/testing "simple trees"
    (t/is (= (tree/->html [[:p "Hello" ", " "World!"] [:p [:span "Hi!"]]])
             "<p>Hello, World!</p><p><span>Hi!</span></p>"))
    (t/is (= (tree/->html [[:ul [:li "x"] [:li "y"] [:li [:div "z"]]]])
             "<ul><li>x</li><li>y</li><li><div>z</div></li></ul>")))

  (t/testing "escaping reserved HTML characters"
    (t/is (= (tree/->html [[:p "This is text & <strong style=\"\">escaped!</strong>"]])
             "<p>This is text &amp; &lt;strong style=&quot;&quot;&gt;escaped!&lt;/strong&gt;</p>")))

  (t/testing "simple text attributes"
    (t/is (= (tree/->html [[:p {:title "Hello"} "World!"]])
             "<p title=\"Hello\">World!</p>"))
    (t/is (= (tree/->html [[:p {:title "Hello & \"you\""
                                :name "&"} "World!"]])
             "<p title=\"Hello &amp; &quot;you&quot;\" name=\"&amp;\">World!</p>"))))

(t/deftest diff
  (t/testing "empty trees yield no diff"
    (t/is (= (tree/diff nil nil) []))
    (t/is (= (tree/diff [] []) [])))

  (t/testing "just a text node"
    (t/is (= (tree/diff [nil] ["Hi!"]) [[:insert [0] ["Hi!"]]])))

  (t/testing "simple diffs of flat trees"
    (t/is (= (tree/diff [[:x] [:y]] [nil [:y]]) [[:remove [0]]]))
    (t/is (= (tree/diff [nil [:y]] [[:x] [:y]]) [[:insert [0] [[:x]]]]))
    (t/is (= (tree/diff [nil [:y]] [[:x] [:z]]) [[:insert [0] [[:x]]] [:remove [1]] [:insert [1] [[:z]]]])))

  (t/testing "simple recursive tree diffs"
    (t/is (= (tree/diff [[:x [:y "henlo" [:z "world"]]] [:foo nil nil]]
                        [[:x [:y "Hello" [:z [:strong "World!"]]]] [:foo nil "This is new!"]])
             [[:remove [0 0 0]] [:insert [0 0 0] ["Hello"]]
              [:remove [0 0 1 0]] [:insert [0 0 1 0] [[:strong "World!"]]]
              [:insert [1 0] ["This is new!"]]])))

  (t/testing "very different trees"
    (t/is (= (tree/diff [[:ul [:li "x"] [:li "y"] [:li "z"]]
                         [:h1 "Hello"]
                         [:p "from"]
                         [:h2 "World!"]]
                        [[:h1 "World!"]
                         [:ol [:li "z"] [:li "y"] [:li "x"]]
                         [:p "to"]
                         [:p "Hello"]])
             [[:remove [0]] [:insert [0] [[:h1 "World!"]]]
              [:remove [1]] [:insert [1] [[:ol [:li "z"] [:li "y"] [:li "x"]]]]
              [:remove [2 0]] [:insert [2 0] ["to"]]
              [:remove [3]] [:insert [3] [[:p "Hello"]]]])))

  #_(t/testing "with simple attributes"
    (t/is (= (tree/diff [[:div [:p {:title "hi", :data-vanish "???"} "you"]]]
                        [[:div [:p {:title "bye"} "you"]]])
             [[:assoc [0 0] :title "bye"]
              [:dissoc [0 0] :data-vanish]]))))
