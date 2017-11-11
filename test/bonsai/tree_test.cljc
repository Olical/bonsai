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
  (t/testing "bad conforms throw errors"
    (t/is (thrown-with-msg? #?(:clj Exception
                               :cljs js/Error)
                            #"should satisfy"
                            (sut/conform :bad)))))

(t/deftest fingerprint
  (t/testing "text gives itself back"
    (t/is (= (sut/fingerprint (sut/conform "hi")) [:text "hi"])))
  (t/testing "node gives the type and name"
    (t/is (= (sut/fingerprint (sut/conform [:div])) [:node :div]))))

(t/deftest children
  (t/testing "children of things without children is nil"
    (t/is (= (sut/children nil) nil))
    (t/is (= (sut/children (sut/conform "hi")) nil)))
  (t/testing "children of things with children is their children"
    (t/is (= (sut/children (sut/conform [:p "hi"])) [[:text "hi"]]))))

(t/deftest attrs
  (t/testing "attrs of things without attrs is nil"
    (t/is (= (sut/attrs (sut/conform nil)) nil))
    (t/is (= (sut/attrs (sut/conform "hi")) nil))
    (t/is (= (sut/attrs (sut/conform [:div])) nil)))
  (t/testing "children of things with children is their children"
    (t/is (= (sut/attrs (sut/conform [:div {:id "foo"}])) {:id [:text "foo"]}))))

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

(t/deftest with-children
  (t/testing "assocs children if the node supports them"
    (t/is (= (sut/conform "hi") (sut/with-children (sut/conform "hi") [(sut/conform "no")])))
    (t/is (= (sut/conform [:p "yes"]) (sut/with-children (sut/conform [:p]) [(sut/conform "yes")])))))
