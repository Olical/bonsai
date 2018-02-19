.PHONY: test

test:
	clj -C:test -m bonsai.test-runner
