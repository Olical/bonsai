(ns bonsai.dom-test
  (:require [cljs.test :as t :include-macros true]
            [bonsai.dom :as sut]
            [jsdom]))

(defn build-mount []
  (-> (new jsdom/JSDOM)
      (-> .-window .-document .-body)))

(t/deftest canary
  (t/testing "verifies that the tools I'm using to test are working"
    (let [mount (build-mount)
          child (-> mount .-ownerDocument (.createElement "p"))
          text (-> mount .-ownerDocument (.createTextNode "Hello, World!"))]
      (.appendChild mount child)
      (.appendChild child text)
      (t/is (= "<p>Hello, World!</p>" (.-innerHTML mount))))))

(t/deftest render!
  (t/testing "nothing to nothing is nothing"
    (let [mount (build-mount)]
      (sut/render! nil mount)
      (t/is (= "" (.-innerHTML mount)))))

  (t/testing "adding and removing a tag"
    (let [mount (build-mount)
          prev (sut/render! [:p "Hi, Bonsai!"] mount)]
      (t/is (= "<p>Hi, Bonsai!</p>" (.-innerHTML mount)))
      (sut/render! prev nil mount)
      (t/is (= "" (.-innerHTML mount)))))

  (t/testing "changing a nested node"
    (let [mount (build-mount)
          prev (sut/render! [:p "Hi, Bonsai!"] mount)]
      (t/is (= "<p>Hi, Bonsai!</p>" (.-innerHTML mount)))
      (sut/render! prev [:p "Oh, Hi!"] mount)
      (t/is (= "<p>Oh, Hi!</p>" (.-innerHTML mount))))))

(t/run-tests 'bonsai.dom-test)
