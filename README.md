# Lab Notification Service

A Spring Boot application for managing laboratory notifications at Muhimbili.

## Prerequisites

- JDK 17+
- Maven 3.8+
- Docker 20+
- Access to the remote deployment server or Docker registry credentials

## Local Development

```bash
./mvnw spring-boot:run
```

Application runs on [http://localhost:8080](http://localhost:8080).

### Running Tests

```bash
./mvnw test
```

## Building the Docker Image

```bash
./mvnw clean package -DskipTests
docker build -t lab-notification:latest .
```

## Manual Remote Deployment Script

`scripts/deploy_remote.sh` automates a local build followed by SCP/SSH to the server.

```bash
./scripts/deploy_remote.sh
```

Set `SERVER_PASSWORD` (or use SSH keys) before running.

## GitHub Actions Pipeline

`.github/workflows/deploy.yml` builds the app, creates a Docker image, and pushes it to a registry. Configure these secrets:

- `DOCKER_REGISTRY_URL`
- `DOCKER_REGISTRY_USERNAME`
- `DOCKER_REGISTRY_PASSWORD`
- `DOCKER_REGISTRY_REPOSITORY`

## Pulling the Image from Docker Hub (example)

When the pipeline pushes to Docker Hub at `https://hub.docker.com/repository/docker/developerml/lab-notification`, deploy on the server with:

```bash
docker login -u developerml
docker pull developerml/lab-notification:latest

docker stop lab-notification || true
docker rm lab-notification || true

docker run -d --restart unless-stopped \
  --name lab-notification \
  -p 9091:9091 \
  developerml/lab-notification:latest
```

Replace the repository URL/credentials if you are using a different registry.

## Environment Variables

Use an `.env` file or host-level variables for sensitive configuration (database credentials, SMS gateway, etc.).

## License

Proprietary software. All rights reserved.
