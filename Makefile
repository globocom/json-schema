.PHONY: unit-test build publish_release publish-snapshot snapshot upload-release upload-snapshot

unit-test:
	./gradlew test

build:
	./gradlew clean build

upload-release:
	./gradlew uploadArchives -Partifactory_user="${JFROG_USER}" -Partifactory_password="${JFROG_PASSWORD}" -Partifactory_contextUrl=https://globocom.jfrog.io/artifactory/horizon-release-local/

publish-release: build unit-test upload-release

upload-snapshot:
	./gradlew uploadArchives -Partifactory_user="${JFROG_USER}" -Partifactory_password="${JFROG_PASSWORD}" -Partifactory_contextUrl=https://globocom.jfrog.io/artifactory/horizon-snapshot-local/

publish-snapshot: unit-test upload-snapshot

