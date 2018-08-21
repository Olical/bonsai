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

(defn batch-patch! [node diffs]
  (loop [a-tree []
         [b-tree & diffs] diffs]
    (when b-tree
      (dom/patch! node (tree/diff a-tree b-tree))
      (recur b-tree diffs))))

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

  (t/testing "multiple patches to insert at the end twice"
    (let [node (body)]
      (batch-patch! node [[[:p [:span "Hello"]]]
                          [[:p [:span "Hello"] ", "]]
                          [[:p [:span "Hello"] ", " [:span "World!"]]]])
      (t/is (= (->html node) "<p><span>Hello</span>, <span>World!</span></p>"))))

  #_(t/testing "multiple patches to insert in reverse with placeholder nils (without these it would be replace operations)"
    (let [node (body)]
      (batch-patch! node [[[:p nil nil [:span "World!"]]]
                          [[:p nil ", " [:span "World!"]]]
                          [[:p [:span "Hello"] ", " [:span "World!"]]]])
      (t/is (= (->html node) "<p><span>Hello</span>, <span>World!</span></p>")))))
