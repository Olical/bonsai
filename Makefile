.PHONY: test yarn test-clj test-cljs test-cljs-watch

test: test-clj test-cljs

test-clj:
	clojure -A:scotch:test-clj

yarn:
	yarn

test-cljs: yarn
	clojure -A:scotch:test-cljs

test-cljs-watch: yarn
	clojure -A:scotch:test-cljs --watch src
