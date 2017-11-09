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
      (t/is (= "<ul><li>A</li><li>B</li><li>C</li></ul>" (.-innerHTML mount)))))

  (t/testing "nils everywhere!"
    (let [mount (build-mount)
          prev (sut/render! [:ul nil nil [:li nil "A" nil nil] nil nil [:li nil "B"] nil nil nil [:li "C" nil] nil nil] mount)]
      (t/is (= "<ul><li>A</li><li>B</li><li>C</li></ul>" (.-innerHTML mount)))
      (sut/render! prev [:ul nil nil [:li nil "A" nil nil] nil nil [:li nil "C"] nil nil [:li "B" nil]] mount)
      (t/is (= "<ul><li>A</li><li>C</li><li>B</li></ul>" (.-innerHTML mount)))))

  (t/testing "seqs are flattened"
    (let [mount (build-mount)]
      (sut/render! [:p (map identity ["a" "b" [:span "c"]])] mount)
      (t/is (= "<p>ab<span>c</span></p>" (.-innerHTML mount))))
    (let [mount (build-mount)]
      (sut/render! [:p (map identity ["a" '("1" "2" "3" ("4" [:em "5"] "6")) "b" [:span "c"]])] mount)
      (t/is (= "<p>a1234<em>5</em>6b<span>c</span></p>" (.-innerHTML mount)))))

  (t/testing "attrs are added"
    (let [mount (build-mount)
          prev (sut/render! [:p "hi"] mount)]
      (t/is (= "<p>hi</p>" (.-innerHTML mount)))
      (sut/render! prev [:p {:id "foo"} "hi"] mount)
      (t/is (= "<p id=\"foo\">hi</p>" (.-innerHTML mount)))))

  (t/testing "attrs are updated"
    (let [mount (build-mount)
          prev (sut/render! [:p {:id "foo"} "hi"] mount)]
      (t/is (= "<p id=\"foo\">hi</p>" (.-innerHTML mount)))
      (sut/render! prev [:p {:id "bar"} "hi"] mount)
      (t/is (= "<p id=\"bar\">hi</p>" (.-innerHTML mount)))))

  (t/testing "attrs are removed"
    (let [mount (build-mount)
          prev (sut/render! [:p {:id "foo"} "hi"] mount)]
      (t/is (= "<p id=\"foo\">hi</p>" (.-innerHTML mount)))
      (sut/render! prev [:p "hi"] mount)
      (t/is (= "<p>hi</p>" (.-innerHTML mount))))
    (let [mount (build-mount)
          prev (sut/render! [:p {:id "foo"} "hi"] mount)]
      (t/is (= "<p id=\"foo\">hi</p>" (.-innerHTML mount)))
      (sut/render! prev [:p {:id nil} "hi"] mount)
      (t/is (= "<p>hi</p>" (.-innerHTML mount)))))

  (t/testing "class is an attr"
    (let [mount (build-mount)]
      (sut/render! [:p {:class "yay"} "hi"] mount)
      (t/is (= "<p class=\"yay\">hi</p>" (.-innerHTML mount)))))

  (t/testing "migrated nodes carry attrs over"
    (let [mount (build-mount)
          prev (sut/render! [:p {:id "foo"} "hi"] mount)]
      (t/is (= "<p id=\"foo\">hi</p>" (.-innerHTML mount)))
      (sut/render! prev [:div {:id "foo"} "hi"] mount)
      (t/is (= "<div id=\"foo\">hi</div>" (.-innerHTML mount))))))
