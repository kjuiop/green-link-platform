IMAGE_NAME=green-link-admin-api
CONTAINER_NAME=green-link-admin-api
PORT=8080

.PHONY: build run stop restart logs clean

build:
	cd .. && ./gradlew :green-link-admin-api:bootJar --no-daemon
	docker build -t $(IMAGE_NAME) .

run:
	docker run -d \
		--name $(CONTAINER_NAME) \
		--env-file .env.local \
		-e SPRING_PROFILES_ACTIVE=local \
		-p $(PORT):8080 \
		--restart always \
		$(IMAGE_NAME)

stop:
	docker stop $(CONTAINER_NAME) || true
	docker rm $(CONTAINER_NAME) || true

restart: stop run

logs:
	docker logs -f $(CONTAINER_NAME)

clean: stop
	docker rmi $(IMAGE_NAME) || true
