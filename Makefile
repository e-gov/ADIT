ifeq ($(user),)
# USER retrieved from env, UID from shell.
HOST_USER ?= $(strip $(if $(USER),$(USER),nodummy))
HOST_UID ?= $(strip $(if $(shell id -u),$(shell id -u),1000))
else
# allow override by adding user= and/ or uid=  (lowercase!).
# uid= defaults to 0 if user= set (i.e. root).
HOST_USER = $(user)
HOST_UID = $(strip $(if $(uid),$(uid),0))
endif


#Cache dependencies
ifdef MAVEN_LOCAL_REPOSITORY
MAVEN_ARGUMENTS="-s /usr/src/project/settings.xml"
else ifneq ($(wildcard ~/.m2/.),)
MAVEN_LOCAL_REPOSITORY=$(HOME)/.m2
else
CREATE_M2_DIRECTORY_CMD=mkdir -p .m2
MAVEN_LOCAL_REPOSITORY=$(CURDIR)/.m2
endif

#Pass settings.xml to docker command if file is in workspace
ifneq ($(wildcard ./settings.xml),)
MAVEN_ARGUMENTS=-s /usr/src/project/settings.xml #with trailing space
endif
CMD_ARGUMENTS ?= $(cmd)
DOCKERHUB_MIRROR ?=
CREATE_SONAR_DIRECTORY_CMD=mkdir -p .sonar
.PHONY: help build
help:
	@echo ''
	@echo 'Usage: make [TARGET] [EXTRA_ARGUMENTS]'
	@echo 'Targets:'
	@echo '  build    	build docker --image-- for current user: $(HOST_USER)(uid=$(HOST_UID))'
	@echo ''

build:
	$(CREATE_M2_DIRECTORY_CMD)
	docker run -v "$(MAVEN_LOCAL_REPOSITORY)"\:/var/maven/.m2 -v "$(CURDIR)"\:/usr/src/project --rm -u $(HOST_UID) -w /usr/src/project -e MAVEN_CONFIG=/var/maven/.m2 $(DOCKERHUB_MIRROR)maven\:3.6.3-jdk-8 mvn $(MAVEN_ARGUMENTS)-Duser.home=/var/maven -Dconf=adit-ci-tomcat clean package
