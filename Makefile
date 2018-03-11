.PHONY: test test-clj test-cljs yarn-deps

test: test-clj test-cljs

test-clj:
	clojure -Atest-clj

yarn-deps:
	yarn

test-cljs: yarn-deps
	clojure -Atest-cljs
