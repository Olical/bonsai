(ns bonsai.dom-test
  (:require [clojure.test :as t]
            [goog.object :as object]
            [bonsai.dom :as dom]
            [bonsai.tree :as tree]
            [jsdom :as jsdom]))

(defn body
  ([]
   (body ""))
  ([content]
   (object/getValueByKeys (jsdom/JSDOM. (str "<body>" content "</body>")) "window" "document" "body")))

(defn ->html [node]
  (.-innerHTML node))

(defn patch-all! [node diffs]
  (loop [a-tree []
         [b-tree & diffs] diffs]
    (when b-tree
      (dom/patch! node (tree/diff a-tree b-tree))
      (recur b-tree diffs))))

(t/deftest node->child
  (t/testing "an element that doesn't exist gives you nil"
    (t/is (nil? (dom/node->child (body) 0))))

  (t/testing "an element that does exist"
    (t/is (= (.-outerHTML (dom/node->child (body "<p>Hi!</p>") 0))
             "<p>Hi!</p>"))))

(t/deftest tree->node-fn
  (t/testing "allows us to convert trees to nodes"
    (let [tree->node (dom/tree->node-fn (.-ownerDocument (body)))]
      (t/is (= (.-outerHTML (tree->node [[:p "Hi!"]]))
               "<p>Hi!</p>")))))

(t/deftest path->node
  (t/testing "nil paths"
    (let [node (body "<p><span>Hello</span>, <span>World!</span></p>")]
      (t/is (= (dom/path->node node [0 1 3]) nil))))

  (t/testing "simple drill down"
    (let [node (body "<p><span>Hello</span>, <span>World!</span></p>")]
      (t/is (= (.-innerHTML (dom/path->node node [0 2])) "World!")))))

(t/deftest patch!
  (t/testing "an empty patch does nothing"
    (let [node (body)]
      (dom/patch! node nil)
      (t/is (= (->html node) "")))
    (let [node (body)]
      (dom/patch! node (tree/diff nil nil))
      (t/is (= (->html node) ""))))

  (t/testing "a simple patch can insert DOM nodes"
    (let [node (body)]
      (dom/patch! node (tree/diff [] [[:p "Hello, World!"]]))
      (t/is (= (->html node) "<p>Hello, World!</p>"))))

  (t/testing "multiple patches to insert from the end"
    (let [node (body)]
      (patch-all! node [[[:p [:span "Hello"]]]
                          [[:p [:span "Hello"] ", "]]
                          [[:p [:span "Hello"] ", " [:span "World!"]]]])
      (t/is (= (->html node) "<p><span>Hello</span>, <span>World!</span></p>"))))

  (t/testing "multiple patches to insert from the front"
    (let [node (body)]
      (patch-all! node [[[:p nil nil [:span "World!"]]]
                          [[:p nil ", " [:span "World!"]]]
                          [[:p [:span "Hello"] ", " [:span "World!"]]]])
      (t/is (= (->html node) "<p><span>Hello</span>, <span>World!</span></p>"))))

  (t/testing "growing and shrinking"
    (let [node (body)]
      (patch-all! node [[[:ul [:li "a"] nil [:li "c"]]]
                          [[:ul [:li "a"] nil [:li "c"] [:li "d"]]]
                          [[:ul [:li "a"] [:li "b"] [:li "c"]]]])
      (t/is (= (->html node) "<ul><li>a</li><li>b</li><li>c</li></ul>"))))

  (t/testing "simple replacing"
    (let [node (body)]
      (patch-all! node [["=> " [:p "Hello"] " <="]
                          ["=> " [:p "Goodbye"] " <="]])
      (t/is (= (->html node) "=&gt; <p>Goodbye</p> &lt;=")))))
