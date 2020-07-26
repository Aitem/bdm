.EXPORT_ALL_VARIABLES:
.PHONY: test


repl:
	clj -A:repl:test

test:
	clj -A:repl:test:kaocha
ci-test:
	clj -A:repl:test:kaocha:ci
