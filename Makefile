.PHONY: yarn-deps dev test test-clj test-cljs

yarn-deps:
	yarn

dev:
	clojure -Adev

test: test-clj test-cljs

test-clj:
	clojure -Adev:test-clj

test-cljs: yarn-deps
	clojure -Adev:test-cljs
