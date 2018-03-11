.PHONY: yarn-deps test test-clj test-cljs

yarn-deps:
	yarn

test: test-clj test-cljs

test-clj:
	clojure -Adev:test-clj

test-cljs: yarn-deps
	clojure -Adev:test-cljs
