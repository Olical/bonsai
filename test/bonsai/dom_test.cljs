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
      (t/is (= "<p>Oh, Hi!</p>" (.-innerHTML mount)))))

  (t/testing "more complex nesting with tag type changes"
    (let [mount (build-mount)
          prev (sut/render! [:ul [:li "Hello, " [:span "World!"]] [:li "Complex " "enough?"]] mount)]
      (t/is (= "<ul><li>Hello, <span>World!</span></li><li>Complex enough?</li></ul>" (.-innerHTML mount)))
      (let [prev (sut/render! prev [:ul [:li "Hello, " [:span "Bonsai!"]] [:li "Hard " "enough?"]] mount)]
        (t/is (= "<ul><li>Hello, <span>Bonsai!</span></li><li>Hard enough?</li></ul>" (.-innerHTML mount)))
        (sut/render! prev [:ol [:li "Hello, " [:span "Bonsai!"]] [:li "Hard " "enough?"]] mount)
        (t/is (= "<ol><li>Hello, <span>Bonsai!</span></li><li>Hard enough?</li></ol>" (.-innerHTML mount))))))

  (t/testing "lengthening"
    (let [mount (build-mount)
          prev (sut/render! [:ul [:li "A"]] mount)]
      (t/is (= "<ul><li>A</li></ul>" (.-innerHTML mount)))
      (sut/render! prev [:ul [:li "A"] [:li "B"]] mount)
      (t/is (= "<ul><li>A</li><li>B</li></ul>" (.-innerHTML mount))))
    (let [mount (build-mount)
          prev (sut/render! [:ul [:li "A"] nil] mount)]
      (t/is (= "<ul><li>A</li></ul>" (.-innerHTML mount)))
      (sut/render! prev [:ul [:li "A"] nil nil [:li "B"]] mount)
      (t/is (= "<ul><li>A</li><li>B</li></ul>" (.-innerHTML mount)))))

  (t/testing "shortening"
    (let [mount (build-mount)
          prev (sut/render! [:ul [:li "A"] [:li "B"]] mount)]
      (t/is (= "<ul><li>A</li><li>B</li></ul>" (.-innerHTML mount)))
      (sut/render! prev [:ul [:li "A"]] mount)
      (t/is (= "<ul><li>A</li></ul>" (.-innerHTML mount))))
    (let [mount (build-mount)
          prev (sut/render! [:ul [:li "A"] nil [:li "B"]] mount)]
      (t/is (= "<ul><li>A</li><li>B</li></ul>" (.-innerHTML mount)))
      (sut/render! prev [:ul nil [:li "A"] nil] mount)
      (t/is (= "<ul><li>A</li></ul>" (.-innerHTML mount)))))

  (t/testing "node->text"
    (let [mount (build-mount)
          prev (sut/render! [:p "boop"] mount)]
      (t/is (= "<p>boop</p>" (.-innerHTML mount)))
      (sut/render! prev "boop" mount)
      (t/is (= "boop" (.-innerHTML mount)))))

  (t/testing "text->node"
    (let [mount (build-mount)
          prev (sut/render! "boop" mount)]
      (t/is (= "boop" (.-innerHTML mount)))
      (sut/render! prev [:p "boop"] mount)
      (t/is (= "<p>boop</p>" (.-innerHTML mount)))))

  (t/testing "completely different trees"
    (let [mount (build-mount)
          prev (sut/render! [:div "A" [:div "B" [:p "C" "D"]] [:input] [:div "Hello, " [:header [:footer "World!"]]]] mount)]
      (t/is (= "<div>A<div>B<p>CD</p></div><input><div>Hello, <header><footer>World!</footer></header></div></div>" (.-innerHTML mount)))
      (sut/render! prev [:p [:span "This is completely"] " " [:span "different."]] mount)
      (t/is (= "<p><span>This is completely</span> <span>different.</span></p>" (.-innerHTML mount)))))

  (t/testing "reordering"
    (let [mount (build-mount)
          prev (sut/render! [:ul [:li "A"] [:li "B"] [:li "C"]] mount)]
      (t/is (= "<ul><li>A</li><li>B</li><li>C</li></ul>" (.-innerHTML mount)))
      (sut/render! prev [:ul [:li "A"] [:li "C"] [:li "B"]] mount)
      (t/is (= "<ul><li>A</li><li>C</li><li>B</li></ul>" (.-innerHTML mount)))))

  (t/testing "gaps"
    (let [mount (build-mount)
          prev (sut/render! [:ul [:li "A"] [:li "B"] [:li "C"]] mount)]
      (t/is (= "<ul><li>A</li><li>B</li><li>C</li></ul>" (.-innerHTML mount)))
      (sut/render! prev [:ul [:li "A"] nil [:li "C"]] mount)
      (t/is (= "<ul><li>A</li><li>C</li></ul>" (.-innerHTML mount))))
    (let [mount (build-mount)
          prev (sut/render! [:ul [:li "A"] nil [:li "C"]] mount)]
      (t/is (= "<ul><li>A</li><li>C</li></ul>" (.-innerHTML mount)))
      (sut/render! prev [:ul [:li "A"] [:li "B"] [:li "C"]] mount)
      (t/is (= "<ul><li>A</li><li>B</li><li>C</li></ul>" (.-innerHTML mount))))
    (let [mount (build-mount)
          prev (sut/render! [:ul [:li "A"] nil nil nil [:li "C"]] mount)]
      (t/is (= "<ul><li>A</li><li>C</li></ul>" (.-innerHTML mount)))
      (sut/render! prev [:ul [:li "A"] nil [:li "B"] nil [:li "C"]] mount)
      (t/is (= "<ul><li>A</li><li>B</li><li>C</li></ul>" (.-innerHTML mount))))))

(t/run-tests 'bonsai.dom-test)
