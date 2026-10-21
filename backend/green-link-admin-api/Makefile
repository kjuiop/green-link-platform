IMAGE_NAME=green-link-admin-api
CONTAINER_NAME=green-link-admin-api
PORT=8080
SERVER_HOST=3.35.44.85
SERVER_USER=ubuntu
SERVER_KEY=~/.ssh/green-link-key
SERVER_ENV_PATH=/home/ubuntu/green-link/.env.server

.PHONY: build run stop restart logs clean sync

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

sync:
	ssh -i $(SERVER_KEY) $(SERVER_USER)@$(SERVER_HOST) "mkdir -p /home/ubuntu/green-link"
	scp -i $(SERVER_KEY) .env.server $(SERVER_USER)@$(SERVER_HOST):$(SERVER_ENV_PATH)
