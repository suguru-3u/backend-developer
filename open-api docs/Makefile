# Swagger UI spec切り替え用Makefile

SWAGGER_UI_IMAGE=swaggerapi/swagger-ui
OPENAPI_DIR=./openapi

swagger-ui-default:
	docker-compose run -e SWAGGER_JSON=$(OPENAPI_DIR)/your-spec.yaml swagger-ui

swagger-ui-projects:
	docker-compose run -e SWAGGER_JSON=$(OPENAPI_DIR)/projects.yaml swagger-ui

swagger-ui-users:
	docker-compose run -e SWAGGER_JSON=$(OPENAPI_DIR)/users.yaml swagger-ui

swagger-ui-custom:
	@if [ "$(SPEC)" = "" ]; then \
	  echo "SPEC変数にspecファイル名を指定してください (例: make swagger-ui-custom SPEC=another.yaml)"; \
	  exit 1; \
	fi; \
	docker-compose run -e SWAGGER_JSON=$(OPENAPI_DIR)/$(SPEC) swagger-ui

.PHONY: swagger-ui-default swagger-ui-projects swagger-ui-users swagger-ui-custom

