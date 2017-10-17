(ns bonsai.dom-test
  (:require [cljs.test :as t :include-macros true]
            [bonsai.dom :as sut]
            [jsdom]))

(defn build-mount
  "Builds a fresh jsdom and pulls out the body for you to mount into."
  []
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

(t/deftest simple-dom
  (t/testing "nothing to nothing is nothing"
    (let [mount (build-mount)]
      (sut/render! mount nil nil)
      (t/is (= "" (.-innerHTML mount)))))
  (t/testing "adding and removing a tag"
    (let [mount (build-mount)]
      (sut/render! mount nil [:p "Hi, Bonsai!"])
      (t/is (= "<p>Hi, Bonsai!</p>" (.-innerHTML mount)))
      (sut/render! mount [:p "Hi, Bonsai!"] nil)
      (t/is (= "<p>Hi, Bonsai!</p>" (.-innerHTML mount)))))

  (t/testing "changing a nested node"
    (let [mount (build-mount)]
      (sut/render! mount nil [:p "Hi, Bonsai!"])
      (t/is (= "<p>Hi, Bonsai!</p>" (.-innerHTML mount)))
      (sut/render! mount [:p "Hi, Bonsai!"] [:p "Oh, Hi!"])
      (t/is (= "<p>Oh, Hi!</p>" (.-innerHTML mount))))))

(t/run-tests 'bonsai.dom-test)
