# Lab Notification Service

A Spring Boot application for managing laboratory notifications at Muhimbili.

## Prerequisites

- JDK 17+
- Maven 3.8+
- Docker 20+
- Access to the remote deployment server (192.168.30.246)
- Optional: `sshpass` for automated deployments via the helper script

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

## Remote Deployment (Option 2)

The repository includes `scripts/deploy_remote.sh` which automates:
1. Building the application and Docker image
2. Saving the image to a tarball
3. Copying it to the target server
4. Loading and running the container remotely

### Usage

```bash
./scripts/deploy_remote.sh
```

The script prompts for the server password unless `SERVER_PASSWORD` is set:

```bash
export SERVER_PASSWORD='your_password'
./scripts/deploy_remote.sh
```

Optional arguments:

```bash
./scripts/deploy_remote.sh <image_tag> <container_name>
```

Example:

```bash
./scripts/deploy_remote.sh myorg/lab-notification:1.0 lab-notification-prod
```

### Requirements on the Server

- Docker installed and running
- SSH access for the `babou_mamc` user
- Port `8080` open for HTTP traffic

## Environment Variables

Use an `.env` file (ignored by Git) or system environment variables for sensitive configuration (database credentials, SMS gateway, etc.).

## License

Proprietary software. All rights reserved.
