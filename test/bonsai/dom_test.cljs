(ns bonsai.dom-test
  (:require [clojure.test :as t]
            [goog.object :as object]
            [bonsai.dom :as dom]
            [bonsai.tree :as tree]
            [jsdom :as jsdom]))

(defn create-node []
  (object/getValueByKeys (jsdom/JSDOM. "<body></body>") "window" "document" "body"))

(defn ->html [node]
  (.-innerHTML node))

(t/deftest patch
  (t/testing "an empty patch does nothing"
    (let [node (create-node)]
      (dom/patch! node nil)
      (t/is (= (->html node) "")))
    (let [node (create-node)]
      (dom/patch! node (tree/diff nil nil))
      (t/is (= (->html node) ""))))

  (t/testing "a simple patch can insert DOM nodes"
    (let [node (create-node)]
      (dom/patch! node (tree/diff [] [[:p "Hello, World!"]]))
      (t/is (= (->html node) "<p>Hello, World!</p>")))))
