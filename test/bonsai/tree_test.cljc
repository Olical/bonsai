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

(t/deftest attr-kind
  (t/testing "we can tell if something is a normal string attribute or an event listener"
    (t/is (= (tree/attr-kind :foo) :normal))
    (t/is (= (tree/attr-kind :on-foo) :listener))))

(t/deftest render
  (t/testing "empty tree yields no html"
    (t/is (= (:html (tree/render nil)) ""))
    (t/is (= (:html (tree/render [])) "")))

  (t/testing "just a text node"
    (t/is (= (:html (tree/render ["Hi!"])) "Hi!")))

  (t/testing "simple trees"
    (t/is (= (:html (tree/render [[:p "Hello" ", " "World!"] [:p [:span "Hi!"]]]))
             "<p>Hello, World!</p><p><span>Hi!</span></p>"))
    (t/is (= (:html (tree/render [[:ul [:li "x"] [:li "y"] [:li [:div "z"]]]]))
             "<ul><li>x</li><li>y</li><li><div>z</div></li></ul>")))

  (t/testing "escaping reserved HTML characters"
    (t/is (= (:html (tree/render [[:p "This is text & <strong style=\"\">escaped!</strong>"]]))
             "<p>This is text &amp; &lt;strong style=&quot;&quot;&gt;escaped!&lt;/strong&gt;</p>")))

  (t/testing "simple text attributes"
    (t/is (= (:html (tree/render [[:p {:title "Hello"} "World!"]]))
             "<p title=\"Hello\">World!</p>"))
    (t/is (= (:html (tree/render [[:p {:title "Hello & \"you\"", :name "&"} "World!"]]))
             "<p title=\"Hello &amp; &quot;you&quot;\" name=\"&amp;\">World!</p>")))

  (t/testing "event attrs generate diffs"
    (t/is (= (tree/render [[:div {:on-click identity}]])
             {:html "<div></div>"
              :diff [[:add-listener [0] :on-click identity]]}))
    (t/is (= (tree/render [[:ul [:li] [:li [:div] [:div {:on-click identity}]]]])
             {:html "<ul><li></li><li><div></div><div></div></li></ul>"
              :diff [[:add-listener [0 1 1] :on-click identity]]}))))

(t/deftest diff-attrs
  (t/testing "identical attrs yield no diff"
    (t/is (= (tree/diff-attrs nil nil [0]) []))
    (t/is (= (tree/diff-attrs {} {} [0]) []))
    (t/is (= (tree/diff-attrs {:foo "hi"} {:foo "hi"} [0]) [])))

  (t/testing "simple diffs"
    (t/is (= (tree/diff-attrs {:bar "bye" :x :y} {:foo "hi" :x :y} [0])
             [[:dissoc [0] :bar]
              [:assoc [0] :foo "hi"]])))

  (t/testing "events"
    (t/is (= (tree/diff-attrs {} {:on-click identity} [0])
             [[:add-listener [0] :on-click identity]]))
    (t/is (= (tree/diff-attrs {:on-click identity} {} [0])
             [[:remove-listener [0] :on-click identity]]))
    (t/is (= (tree/diff-attrs {:on-click +} {:on-click -} [0])
             [[:remove-listener [0] :on-click +]
              [:add-listener [0] :on-click -]]))))

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

  (t/testing "with attributes"
    (t/is (= (sort (tree/diff [[:div [:p {:title "hi", :data-vanish "???"} "you"]]]
                              [[:div [:p {:title "bye", :on-click +} "you"]]]))
             [[:dissoc [0 0] :data-vanish]
              [:add-listener [0 0] :on-click +]
              [:assoc [0 0] :title "bye"]]))))
