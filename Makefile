IMG = unified-provisioning-wdf.common.repositories.cloud.sap/com.sap.lm.sl.unifiedprovisioning.wdf/spfiv2:v1alpha1

mvn-package:
	mvn clean package
docker-build: ## Build docker image with the manager.
	docker build -t ${IMG}  --platform linux/amd64 .

docker-push: ## Push docker image with the manager.
	docker push ${IMG}

docker-all: docker-build docker-push
