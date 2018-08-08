.PHONY: test test-clj test-cljs

test: test-clj test-cljs

test-clj:
	clojure -A:scotch:test-clj

test-cljs:
	yarn
	clojure -A:scotch:test-cljs
