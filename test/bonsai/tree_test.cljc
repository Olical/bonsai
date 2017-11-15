(ns bonsai.tree-test
  (:require [bonsai.tree :as sut]
            #?(:clj [clojure.test :as t]
               :cljs [cljs.test :as t :include-macros true])))

(t/deftest conform
  (t/testing "nil is void"
    (t/is (= [:void nil] (sut/conform nil))))
  (t/testing "strings are text"
    (t/is (= [:text "hi"] (sut/conform "hi"))))
  (t/testing "nodes are nodes (containing children and attrs)"
    (t/is (= [:node {:name :p}] (sut/conform [:p])))
    (t/is (= [:node {:name :p
                     :children [[:text "hi"]]}]
             (sut/conform [:p "hi"])))
    (t/is (= [:node {:name :p
                     :attrs {:id [:text "foo"]
                             :class [:void nil]}
                     :children [[:text "hi"]]}]
             (sut/conform [:p {:id "foo" :class nil} "hi"]))))
  (t/testing "seqs of nodes are, well, seqs of nodes"
    (t/is (= [:node-seq '([:text "a"] [:text "b"] [:text "c"])]
             (sut/conform (map identity ["a" "b" "c"])))))
  (t/testing "functions are conformed into node-fns"
    (t/is (= [:node-fn {:fn +}] (sut/conform [+])))
    (t/is (= [:node-fn {:fn + :args [1 2 3]}] (sut/conform [+ 1 2 3])))
    (t/is (= [:node {:name :p
                     :children [[:text "hi"]
                                [:node-fn {:fn + :args [1 2 3]}]]}]
             (sut/conform [:p "hi" [+ 1 2 3]]))))
  (t/testing "bad conforms throw errors"
    (t/is (thrown-with-msg? #?(:clj Exception
                               :cljs js/Error)
                            #"should satisfy"
                            (sut/conform :bad)))))

(t/deftest fingerprint
  (t/testing "text and node-fn gives itself back"
    (t/is (= (sut/fingerprint (sut/conform "hi")) (sut/conform "hi")))
    (t/is (= (sut/fingerprint (sut/conform [+ "hi"])) (sut/conform [+ "hi"]))))
  (t/testing "node gives the type and name"
    (t/is (= (sut/fingerprint (sut/conform [:div])) [:node :div]))))

(t/deftest children
  (t/testing "children of things without children is nil"
    (t/is (= (sut/children nil) nil))
    (t/is (= (sut/children (sut/conform "hi")) nil))
    (t/is (= (sut/children (sut/conform [+ "hi"])) nil)))
  (t/testing "children of things with children is their children"
    (t/is (= (sut/children (sut/conform [:p "hi"])) [[:text "hi"]]))))

(t/deftest with-children
  (t/testing "assocs children if the node supports them"
    (t/is (= (sut/conform "hi") (sut/with-children (sut/conform "hi") [(sut/conform "no")])))
    (t/is (= (sut/conform [:p "yes"]) (sut/with-children (sut/conform [:p]) [(sut/conform "yes")])))))

(t/deftest attrs
  (t/testing "attrs of things without attrs is nil"
    (t/is (= (sut/attrs (sut/conform nil)) {:attr {} :event {} :lifecycle {}}))
    (t/is (= (sut/attrs (sut/conform "hi")) {:attr {} :event {} :lifecycle {}}))
    (t/is (= (sut/attrs (sut/conform [:div])) {:attr {} :event {} :lifecycle {}}))
    (t/is (= (sut/attrs (sut/conform [+ "hi"])) {:attr {} :event {} :lifecycle {}})))
  (t/testing "children of things with children is their children"
    (t/is (= (sut/attrs (sut/conform [:div {:id "foo"}]))
             {:attr {:id [:text "foo"]}
              :event {}
              :lifecycle {}})))
  (t/testing "lifecycle hooks and event names are grouped separately"
    (t/is (= (sut/attrs (sut/conform [:div {:id "a" :on-click [+] :on-insert [+]}]))
             {:attr {:id [:text "a"]}
              :event {:on-click [:handler {:fn +}]}
              :lifecycle {:on-insert [:handler {:fn +}]}}))))

(t/deftest attr-type
  (t/testing "an attr kw can be converted into it's type"
    (t/is (= :event (sut/attr-type :on-click)))
    (t/is (= :attr (sut/attr-type :id)))))

(t/deftest voidness
  (t/testing "void? and real? detect void and not-void respectively"
    (t/is (= (sut/void? (sut/conform nil)) true))
    (t/is (= (sut/void? nil) true))
    (t/is (= (sut/void? (sut/conform "hi")) false))
    (t/is (= (sut/real? (sut/conform "hi")) true))
    (t/is (= (sut/real? (sut/conform nil)) false))))

(t/deftest flatten-seqs
  (t/testing "does nothing to a normal tree"
    (t/is (= (sut/flatten-seqs [(sut/conform "hi")]) [(sut/conform "hi")]))
    (t/is (= (sut/flatten-seqs [(sut/conform [:p "hi"])]) [(sut/conform [:p "hi"])])))
  (t/testing "single levels of seqs are flattened"
    (t/is (= (sut/flatten-seqs [(sut/conform (list "hi"))]) [(sut/conform "hi")])))
  (t/testing "multiple levels of multiple seqs are flattened"
    (t/is (= (sut/flatten-seqs [(sut/conform (list "hi" (list "world" (list "!"))))
                                (sut/conform (list (list "another")))])
             [(sut/conform "hi")
              (sut/conform "world")
              (sut/conform "!")
              (sut/conform "another")]))))

(t/deftest expand
  (t/testing "on things like :text, nothing happens"
    (t/is (= (sut/expand (sut/conform "hi")) (sut/conform "hi"))))
  (t/testing "on node-fns, they are applied and the result is returned"
    (let [f (fn [a] [:p "hi " a])]
      (t/is (= (sut/expand (sut/conform [f "foo"])) (sut/conform [:p "hi " "foo"])))))
  (t/testing "the args are encoded in the meta"
    (let [f (fn [a] [:p "hi " a])]
      (t/is (= {:bonsai.tree/node-fn {:fn f :args ["foo"]}}
               (meta (sut/expand (sut/conform [f "foo"])))))))
  (t/testing "if prev is provided, it will use that if the meta is the same"
    (let [calls (atom 0)
          f (fn [a] (swap! calls inc) [:p "hi " a])
          prev (sut/expand (sut/conform [f "foo"]))]
      (t/is (= prev (sut/conform [:p "hi " "foo"])))
      (t/is (= @calls 1))
      (t/is (= (sut/expand prev (sut/conform [f "foo"])) (sut/conform [:p "hi " "foo"])))
      (t/is (= @calls 1))
      (t/is (= (sut/expand prev (sut/conform [f "bar"])) (sut/conform [:p "hi " "bar"])))
      (t/is (= @calls 2)))))
