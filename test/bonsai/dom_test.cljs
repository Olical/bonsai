(ns bonsai.dom-test
  (:require [cljs.test :as t :include-macros true]
            [bonsai.dom :as sut]
            [jsdom]))

(defn body []
  (-> (new jsdom/JSDOM) .-window .-document .-body))

(t/deftest "essential rendering"
  (t/testing "single p tag"
    (let [host (body)
          render (sut/mount {:host host
                             :render (fn []
                                       [:p "Hello, World!"])})]
      (t/is (= (.-innerHTML host) "<p>Hello, World!</p>")))))
