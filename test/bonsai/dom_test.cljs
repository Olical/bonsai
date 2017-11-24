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
      (sut/render! nil nil mount {})
      (t/is (= "" (.-innerHTML mount)))))

  (t/testing "adding and removing a tag"
    (let [mount (build-mount)
          prev (sut/render! nil [:p "Hi, Bonsai!"] mount {})]
      (t/is (= "<p>Hi, Bonsai!</p>" (.-innerHTML mount)))
      (sut/render! prev nil mount {})
      (t/is (= "" (.-innerHTML mount)))))

  (t/testing "changing a nested node"
    (let [mount (build-mount)
          prev (sut/render! nil [:p "Hi, Bonsai!"] mount {})]
      (t/is (= "<p>Hi, Bonsai!</p>" (.-innerHTML mount)))
      (sut/render! prev [:p "Oh, Hi!"] mount {})
      (t/is (= "<p>Oh, Hi!</p>" (.-innerHTML mount)))))

  (t/testing "more complex nesting with tag type changes"
    (let [mount (build-mount)
          prev (sut/render! nil [:ul [:li "Hello, " [:span "World!"]] [:li "Complex " "enough?"]] mount {})]
      (t/is (= "<ul><li>Hello, <span>World!</span></li><li>Complex enough?</li></ul>" (.-innerHTML mount)))
      (let [prev (sut/render! prev [:ul [:li "Hello, " [:span "Bonsai!"]] [:li "Hard " "enough?"]] mount {})]
        (t/is (= "<ul><li>Hello, <span>Bonsai!</span></li><li>Hard enough?</li></ul>" (.-innerHTML mount)))
        (sut/render! prev [:ol [:li "Hello, " [:span "Bonsai!"]] [:li "Hard " "enough?"]] mount {})
        (t/is (= "<ol><li>Hello, <span>Bonsai!</span></li><li>Hard enough?</li></ol>" (.-innerHTML mount))))))

  (t/testing "lengthening"
    (let [mount (build-mount)
          prev (sut/render! nil [:ul [:li "A"]] mount {})]
      (t/is (= "<ul><li>A</li></ul>" (.-innerHTML mount)))
      (sut/render! prev [:ul [:li "A"] [:li "B"]] mount {})
      (t/is (= "<ul><li>A</li><li>B</li></ul>" (.-innerHTML mount))))
    (let [mount (build-mount)
          prev (sut/render! nil [:ul [:li "B"]] mount {})]
      (t/is (= "<ul><li>B</li></ul>" (.-innerHTML mount)))
      (sut/render! prev [:ul [:li "A"] [:li "B"]] mount {})
      (t/is (= "<ul><li>A</li><li>B</li></ul>" (.-innerHTML mount))))
    (let [mount (build-mount)
          prev (sut/render! nil [:ul [:li "A"] nil] mount {})]
      (t/is (= "<ul><li>A</li></ul>" (.-innerHTML mount)))
      (sut/render! prev [:ul [:li "A"] nil nil [:li "B"]] mount {})
      (t/is (= "<ul><li>A</li><li>B</li></ul>" (.-innerHTML mount)))))

  (t/testing "shortening"
    (let [mount (build-mount)
          prev (sut/render! nil [:ul [:li "A"] [:li "B"]] mount {})]
      (t/is (= "<ul><li>A</li><li>B</li></ul>" (.-innerHTML mount)))
      (sut/render! prev [:ul [:li "A"]] mount {})
      (t/is (= "<ul><li>A</li></ul>" (.-innerHTML mount))))
    (let [mount (build-mount)
          prev (sut/render! nil [:ul [:li "A"] [:li "B"]] mount {})]
      (t/is (= "<ul><li>A</li><li>B</li></ul>" (.-innerHTML mount)))
      (sut/render! prev [:ul [:li "B"]] mount {})
      (t/is (= "<ul><li>B</li></ul>" (.-innerHTML mount))))
    (let [mount (build-mount)
          prev (sut/render! nil [:ul [:li "A"] nil [:li "B"]] mount {})]
      (t/is (= "<ul><li>A</li><li>B</li></ul>" (.-innerHTML mount)))
      (sut/render! prev [:ul nil [:li "A"] nil] mount {})
      (t/is (= "<ul><li>A</li></ul>" (.-innerHTML mount)))))

  (t/testing "node->text"
    (let [mount (build-mount)
          prev (sut/render! nil [:p "boop"] mount {})]
      (t/is (= "<p>boop</p>" (.-innerHTML mount)))
      (sut/render! prev "boop" mount {})
      (t/is (= "boop" (.-innerHTML mount)))))

  (t/testing "text->node"
    (let [mount (build-mount)
          prev (sut/render! nil "boop" mount {})]
      (t/is (= "boop" (.-innerHTML mount)))
      (sut/render! prev [:p "boop"] mount {})
      (t/is (= "<p>boop</p>" (.-innerHTML mount)))))

  (t/testing "completely different trees"
    (let [mount (build-mount)
          prev (sut/render! nil [:div "A" [:div "B" [:p "C" "D"]] [:input] [:div "Hello, " [:header [:footer "World!"]]]] mount {})]
      (t/is (= "<div>A<div>B<p>CD</p></div><input><div>Hello, <header><footer>World!</footer></header></div></div>" (.-innerHTML mount)))
      (sut/render! prev [:p [:span "This is completely"] " " [:span "different."]] mount {})
      (t/is (= "<p><span>This is completely</span> <span>different.</span></p>" (.-innerHTML mount)))))

  (t/testing "reordering"
    (let [mount (build-mount)
          prev (sut/render! nil [:ul [:li "A"] [:li "B"] [:li "C"]] mount {})]
      (t/is (= "<ul><li>A</li><li>B</li><li>C</li></ul>" (.-innerHTML mount)))
      (sut/render! prev [:ul [:li "A"] [:li "C"] [:li "B"]] mount {})
      (t/is (= "<ul><li>A</li><li>C</li><li>B</li></ul>" (.-innerHTML mount)))))

  (t/testing "gaps"
    (let [mount (build-mount)
          prev (sut/render! nil [:ul [:li "A"] [:li "B"] [:li "C"]] mount {})]
      (t/is (= "<ul><li>A</li><li>B</li><li>C</li></ul>" (.-innerHTML mount)))
      (sut/render! prev [:ul [:li "A"] nil [:li "C"]] mount {})
      (t/is (= "<ul><li>A</li><li>C</li></ul>" (.-innerHTML mount))))
    (let [mount (build-mount)
          prev (sut/render! nil [:ul [:li "A"] nil [:li "C"]] mount {})]
      (t/is (= "<ul><li>A</li><li>C</li></ul>" (.-innerHTML mount)))
      (sut/render! prev [:ul [:li "A"] [:li "B"] [:li "C"]] mount {})
      (t/is (= "<ul><li>A</li><li>B</li><li>C</li></ul>" (.-innerHTML mount))))
    (let [mount (build-mount)
          prev (sut/render! nil [:ul [:li "A"] nil nil nil [:li "C"]] mount {})]
      (t/is (= "<ul><li>A</li><li>C</li></ul>" (.-innerHTML mount)))
      (sut/render! prev [:ul [:li "A"] nil [:li "B"] nil [:li "C"]] mount {})
      (t/is (= "<ul><li>A</li><li>B</li><li>C</li></ul>" (.-innerHTML mount)))))

  (t/testing "nils everywhere!"
    (let [mount (build-mount)
          prev (sut/render! nil [:ul nil nil [:li nil "A" nil nil] nil nil [:li nil "B"] nil nil nil [:li "C" nil] nil nil] mount {})]
      (t/is (= "<ul><li>A</li><li>B</li><li>C</li></ul>" (.-innerHTML mount)))
      (sut/render! prev [:ul nil nil [:li nil "A" nil nil] nil nil [:li nil "C"] nil nil [:li "B" nil]] mount {})
      (t/is (= "<ul><li>A</li><li>C</li><li>B</li></ul>" (.-innerHTML mount)))))

  (t/testing "seqs are flattened"
    (let [mount (build-mount)]
      (sut/render! nil [:p (map identity ["a" "b" [:span "c"]])] mount {})
      (t/is (= "<p>ab<span>c</span></p>" (.-innerHTML mount))))
    (let [mount (build-mount)]
      (sut/render! nil [:p (map identity ["a" '("1" "2" "3" ("4" [:em "5"] "6")) "b" [:span "c"]])] mount {})
      (t/is (= "<p>a1234<em>5</em>6b<span>c</span></p>" (.-innerHTML mount)))))

  (t/testing "seqs can be used at the top level"
    (let [mount (build-mount)
          prev (sut/render! nil (map identity ["a" "b" [:span "c"]]) mount {})]
      (t/is (= "ab<span>c</span>" (.-innerHTML mount)))
      (sut/render! prev (map identity ["a" "b" [:span "c"] [:strong "nice"]]) mount {})
      (t/is (= "ab<span>c</span><strong>nice</strong>" (.-innerHTML mount))))
    (let [mount (build-mount)]
      (sut/render! nil (map identity ["a" '("1" "2" "3" ("4" [:em "5"] "6")) "b" [:span "c"]]) mount {})
      (t/is (= "a1234<em>5</em>6b<span>c</span>" (.-innerHTML mount)))))

  (t/testing "attrs are added"
    (let [mount (build-mount)
          prev (sut/render! nil [:p "hi"] mount {})]
      (t/is (= "<p>hi</p>" (.-innerHTML mount)))
      (sut/render! prev [:p {:id "foo"} "hi"] mount {})
      (t/is (= "<p id=\"foo\">hi</p>" (.-innerHTML mount)))))

  (t/testing "attrs are updated"
    (let [mount (build-mount)
          prev (sut/render! nil [:p {:id "foo"} "hi"] mount {})]
      (t/is (= "<p id=\"foo\">hi</p>" (.-innerHTML mount)))
      (sut/render! prev [:p {:id "bar"} "hi"] mount {})
      (t/is (= "<p id=\"bar\">hi</p>" (.-innerHTML mount)))))

  (t/testing "attrs are removed"
    (let [mount (build-mount)
          prev (sut/render! nil [:p {:id "foo"} "hi"] mount {})]
      (t/is (= "<p id=\"foo\">hi</p>" (.-innerHTML mount)))
      (sut/render! prev [:p "hi"] mount {})
      (t/is (= "<p>hi</p>" (.-innerHTML mount))))
    (let [mount (build-mount)
          prev (sut/render! nil [:p {:id "foo"} "hi"] mount {})]
      (t/is (= "<p id=\"foo\">hi</p>" (.-innerHTML mount)))
      (sut/render! prev [:p {:id nil} "hi"] mount {})
      (t/is (= "<p>hi</p>" (.-innerHTML mount)))))

  (t/testing "class is an attr"
    (let [mount (build-mount)]
      (sut/render! nil [:p {:class "yay"} "hi"] mount {})
      (t/is (= "<p class=\"yay\">hi</p>" (.-innerHTML mount)))))

  (t/testing "listeners can be added and triggered"
    (let [mount (build-mount)
          calls (atom 0)
          f (fn [_ _ x] (swap! calls x))]
      (sut/render! nil [:div {:on-click [f inc]} "click me"] mount {})
      (t/is (= "<div>click me</div>" (.-innerHTML mount)))
      (.click (.-firstChild mount))
      (t/is (= @calls 1))
      (.click (.-firstChild mount))
      (t/is (= @calls 2)))
    (let [mount (build-mount)
          calls (atom [])
          f (fn [_ e & args] (swap! calls conj [(-> e .-target .-nodeName) args]))]
      (sut/render! nil [:div {:on-click [f 1 2 3]} "click me"] mount {})
      (.click (.-firstChild mount))
      (t/is (= @calls [["DIV" [1 2 3]]]))))

  (t/testing "listeners can be removed"
    (let [mount (build-mount)
          calls (atom 0)
          f (fn [_ x] (swap! calls x))
          prev (sut/render! nil [:div {:on-click [f inc]} "click me"] mount {})]
      (sut/render! prev [:div "click me"] mount {})
      (t/is (= "<div>click me</div>" (.-innerHTML mount)))
      (.click (.-firstChild mount))
      (t/is (= @calls 0))))

  (t/testing "you can listen for insert / remove of the node (called just before it happens)"
    (let [mount (build-mount)
          calls (atom 0)
          f (fn [_ x] (swap! calls x))
          prev (sut/render! nil [:div "bloop"] mount {})]
      (t/is (= "<div>bloop</div>" (.-innerHTML mount)))
      (t/is (= @calls 0))
      (let [prev (sut/render! prev [:div "bloop" [:span {:on-insert [f inc]} "floop"]] mount {})]
        (t/is (= "<div>bloop<span>floop</span></div>" (.-innerHTML mount)))
        (t/is (= @calls 1))
        (sut/render! prev [:div "bloop" [:span {:on-insert [f inc]} "floop"] "gloop"] mount {})
        (t/is (= "<div>bloop<span>floop</span>gloop</div>" (.-innerHTML mount)))
        (t/is (= @calls 1))))
    (let [mount (build-mount)
          calls (atom 0)
          f (fn [_ x] (swap! calls x))
          prev (sut/render! nil [:div "bloop" [:span {:on-remove [f inc]} "floop"] "gloop"] mount {})]
      (t/is (= "<div>bloop<span>floop</span>gloop</div>" (.-innerHTML mount)))
      (t/is (= @calls 0))
      (let [prev (sut/render! prev [:div "bloop" [:span {:on-remove [f inc]} "floop"]] mount {})]
        (t/is (= "<div>bloop<span>floop</span></div>" (.-innerHTML mount)))
        (t/is (= @calls 0))
        (sut/render! prev [:div "bloop"] mount {})
        (t/is (= "<div>bloop</div>" (.-innerHTML mount)))
        (t/is (= @calls 1)))))

  (t/testing "migrating is technically a remove and then an insert"
    (let [mount (build-mount)
          calls (atom [])
          f (fn [_ x] (swap! calls conj x))
          attrs {:on-insert [f :i] :on-remove [f :r]}
          prev (sut/render! nil [:div attrs "hi"] mount {})]
      (t/is (= "<div>hi</div>" (.-innerHTML mount)))
      (t/is (= @calls [:i]))
      (sut/render! prev [:p attrs "hi"] mount {})
      (t/is (= "<p>hi</p>" (.-innerHTML mount)))
      (t/is (= @calls [:i :r :i]))))

  (t/testing "migrated nodes carry attrs over"
    (let [mount (build-mount)
          prev (sut/render! nil [:p {:id "foo"} "hi"] mount {})]
      (t/is (= "<p id=\"foo\">hi</p>" (.-innerHTML mount)))
      (sut/render! prev [:div {:id "foo"} "hi"] mount {})
      (t/is (= "<div id=\"foo\">hi</div>" (.-innerHTML mount))))
    (let [mount (build-mount)
          calls (atom 0)
          f (fn [_ _ x] (swap! calls x))
          prev (sut/render! nil [:p {:on-click [f inc]} "hi"] mount {})]
      (.click (.-firstChild mount))
      (t/is (= 1 @calls))
      (sut/render! prev [:div {:on-click [f inc]} "hi"] mount {})
      (.click (.-firstChild mount))
      (t/is (= 2 @calls)))
    (let [mount (build-mount)
          calls (atom 0)
          f (fn [_ _ x] (swap! calls x))
          prev (sut/render! nil [:p {:on-click [f inc]} "hi"] mount {})]
      (.click (.-firstChild mount))
      (t/is (= 1 @calls))
      (sut/render! prev [:div "hi"] mount {})
      (.click (.-firstChild mount))
      (t/is (= 1 @calls))))

  (t/testing "functions are called in the tree"
    (let [mount (build-mount)
          no-args (fn [] "no args")]
      (sut/render! nil [no-args] mount {})
      (t/is (= "no args" (.-innerHTML mount))))
    (let [mount (build-mount)
          no-args (fn [] "no args")]
      (sut/render! nil [:p [no-args]] mount {})
      (t/is (= "<p>no args</p>" (.-innerHTML mount))))
    (let [mount (build-mount)
          with-args (fn [a] [:p "with arg " a])]
      (sut/render! nil [:div [with-args "hi"] "!"] mount {})
      (t/is (= "<div><p>with arg hi</p>!</div>" (.-innerHTML mount)))))

  (t/testing "functions are called when their inputs change (and only then)"
    (let [mount (build-mount)
          calls (atom 0)
          with-args (fn [x a] (swap! calls x) [:p "with arg " a])
          prev (sut/render! nil [:div [with-args inc "hi"] "!"] mount {})]
      (t/is (= "<div><p>with arg hi</p>!</div>" (.-innerHTML mount)))
      (t/is (= @calls 1))
      (let [prev (sut/render! prev [:div [with-args inc "hi"] "!"] mount {})]
        (t/is (= "<div><p>with arg hi</p>!</div>" (.-innerHTML mount)))
        (t/is (= @calls 1))
        (let [prev (sut/render! prev [:div [with-args inc "bye"] "!"] mount {})]
          (t/is (= "<div><p>with arg bye</p>!</div>" (.-innerHTML mount)))
          (t/is (= @calls 2))
          (sut/render! prev [:div [with-args inc "bye"] "!"] mount {})
          (t/is (= "<div><p>with arg bye</p>!</div>" (.-innerHTML mount)))
          (t/is (= @calls 2))))))

  (t/testing "listeners are provided with the state"
    (let [mount (build-mount)
          states (atom [])
          f (fn [_ n hopefully-nil] (swap! states conj [n hopefully-nil]))]
      (sut/render! nil [:div {:on-insert [f 0]}] mount {})
      (t/is (= @states [[0 nil]])))
    (let [mount (build-mount)
          states (atom [])
          f (fn [state n] (swap! states conj [n state]))]
      (-> (sut/render! nil [:div {:on-insert [f 0]
                                  :on-remove [f 1]}] mount {:state :foo})
          (sut/render! [:p] mount {:state :bar}))
      (t/is (= @states [[0 :foo] [1 :bar]])))
    (let [mount (build-mount)
          states (atom [])
          f (fn [state e n] (swap! states conj [n (-> e .-target .-nodeName) state]))
          prev (sut/render! nil [:div {:on-click [f 0]}] mount {:state :foo})]
      (.click (.-firstChild mount))
      (sut/render! prev [:div {:on-click [f 1]}] mount {:state :bar})
      (.click (.-firstChild mount))
      (t/is (= @states [[0 "DIV" :foo] [1 "DIV" :bar]])))
    (let [mount (build-mount)
          calls (atom [])
          maybe-node-name (fn [e] (if (and e (.-target e)) (-> e .-target .-nodeName) e))
          f (fn [& args] (swap! calls conj (map maybe-node-name args)))
          prev (sut/render! nil [:div {:on-click [f 0]}] mount {})]
      (.click (.-firstChild mount))
      (let [prev (sut/render! prev [:div {:on-click [f 0]}] mount {:state :foo})]
        (.click (.-firstChild mount))
        (sut/render! prev [:div {:on-click [f 0]}] mount {})
        (.click (.-firstChild mount))
        (t/is (= @calls [[nil "DIV" 0] [:foo "DIV" 0] [nil "DIV" 0]])))))

  (t/testing "the change handler is called with the updated state if it changed"
    (let [mount (build-mount)
          calls (atom [])
          f (fn [state _ x] (x state))
          change (fn [state] (swap! calls conj state))
          prev (sut/render! nil [:div {:on-click [f inc]}] mount {:state 0, :on-change change})]
      (t/is (= @calls []))
      (.click (.-firstChild mount))
      (t/is (= @calls [1]))
      (let [prev (sut/render! prev [:div {:on-click [f inc]}] mount {:state 1, :on-change change})]
        (.click (.-firstChild mount))
        (t/is (= @calls [1 2]))))
    (let [mount (build-mount)
          calls (atom [])
          f (fn [state x] (x state))
          change (fn [state] (swap! calls conj state))]
      (sut/render! nil [:div {:on-insert [f inc]}] mount {:state 0, :on-change change})
      (t/is (= @calls [1]))))

  (t/testing "nodes that request state are provided it as their first argument"
    (let [mount (build-mount)
          node-fn (with-meta (fn [state post] [:p (str state post)]) {:state :pre})]
      (sut/render! nil [node-fn "World!"] mount {:state {:pre "Hello, "}})
      (t/is (= "<p>Hello, World!</p>" (.-innerHTML mount)))))

  (t/testing "nodes with state are only called when that required sub-state changes"
    (let [mount (build-mount)
          calls (atom [])
          node-fn (with-meta
                    (fn [state post]
                      (swap! calls conj [state post])
                      [:p (str state post)])
                    {:state :pre})
          prev (sut/render! nil [node-fn "World!"] mount {:state {:pre "Hello, ", :other :foo}})]
      (t/is (= "<p>Hello, World!</p>" (.-innerHTML mount)))
      (t/is (= @calls [["Hello, " "World!"]]))
      (let [prev (sut/render! prev [node-fn "World!"] mount {:state {:pre "Hey, ", :other :foo}})]
        (t/is (= "<p>Hey, World!</p>" (.-innerHTML mount)))
        (t/is (= @calls [["Hello, " "World!"] ["Hey, " "World!"]]))
        (sut/render! prev [node-fn "World!"] mount {:state {:pre "Hey, ", :other :bar}})
        (t/is (= "<p>Hey, World!</p>" (.-innerHTML mount)))
        (t/is (= @calls [["Hello, " "World!"] ["Hey, " "World!"]])))))

   (t/testing "deep node-fns are updated, even when it's just the state that changed"
    (let [mount (build-mount)
          node-fn (with-meta (fn [state] [:p state]) {:state :myval})
          prev (sut/render! nil [:div [node-fn]] mount {:state {:myval "FOO"}})]
      (t/is (= "<div><p>FOO</p></div>" (.-innerHTML mount)))
      (sut/render! prev [:div [node-fn]] mount {:state {:myval "BAR"}})
      (t/is (= "<div><p>BAR</p></div>" (.-innerHTML mount))))
    (let [mount (build-mount)
          node-fn (with-meta (fn [state] [:p state]) {:state :myval})
          prev (sut/render! nil [:ul [:li "boop"] [:li [:div [node-fn]]]] mount {:state {:myval "FOO"}})]
      (t/is (= "<ul><li>boop</li><li><div><p>FOO</p></div></li></ul>" (.-innerHTML mount)))
      (sut/render! prev [:ul [:li "boop"] [:li [:div [node-fn]]]] mount {:state {:myval "BAR"}})
      (t/is (= "<ul><li>boop</li><li><div><p>BAR</p></div></li></ul>" (.-innerHTML mount))))))
