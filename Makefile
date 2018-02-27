.PHONY: run test pack deploy

test:
	clj -Atest

pack:
	clj -Apack

deploy: test pack
	clj -Spom
	mvn deploy:deploy-file \
		-DpomFile=pom.xml \
		-Dfile=dist/bonsai.jar \
		-DrepositoryId=clojars \
		-Durl=https://clojars.org/repo
