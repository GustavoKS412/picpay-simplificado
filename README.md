# PicPay Simples
[![English](https://img.shields.io/badge/lang-English-blue)](README.md)
[![Português](https://img.shields.io/badge/lang-Portugu%C3%AAs-green)](README.pt-BR.md)

REST API in Java + Spring Boot that simulates a payment platform: it's possible to register users (common and merchant) and transfer money between them.
This project was made as the resolution of a backend technical challenge, trying to keep the layers well separated (controller, service, repository, domain, dtos) and follow good practices in general.
## About the project
There are two types of user:
- Common user: can send and receive money.
- Merchant: only receives, can't make transfers.
  Before closing a transfer, the system calls an external authorizer service (mock). After the transaction is completed, a notification is fired to the users involved, also via mock. This notification was decoupled on purpose, because if it fails that can't bring down the transaction.
## Tech stack
- Java
- Spring Boot
- Spring Data JPA
- H2 Database (in memory)
- Maven
- Docker
- GitHub Actions
## Business rules
- Full name, CPF, email and password are required for both user types. CPF/CNPJ and email need to be unique in the system. There can't be two registrations with the same CPF or email.
- Users can transfer money to merchants and to other users.
- Merchants only receive, never send money.
- Before transferring, the system needs to validate whether the user has sufficient balance.
- Before finalizing the transfer, an external authorizer service needs to be consulted (mock `GET https://util.devi.tools/api/v2/authorize`). **Note:** this integration isn't fully working yet, see [Known limitations](#known-limitations).
- The transfer is a transaction: if something goes wrong along the way, everything is reverted and the money returns to the sender's wallet.
- When someone receives a payment, they need to be notified (email/sms) by a third-party service (mock `POST https://util.devi.tools/api/v1/notify`). This service might be down, so that can't block the transaction.
- The API needs to be RESTful.
## Endpoints
Register user:
```
POST /users
Content-Type: application/json
{
    "firstName": "exemplo",
    "lastName": "exemplo",
    "document": "123456789",
    "password": "exemplo",
    "email": "exemplo@exemplo.com",
    "userType": "COMMON",
    "balance": 2200
}
```
Create transaction:
```
POST /transactions
Content-Type: application/json
{
    "senderId": 1,
    "receiverId": 2,
    "value": 100
}
```
## How to run the project
### Prerequisites
- Java 17+ (check the version in pom.xml)
- Maven doesn't need to be installed globally, the project already comes with the wrapper (mvnw / mvnw.cmd)
### Steps
```bash
git clone https://github.com/GustavoKS412/picpay-simples.git
cd picpay-simples
./mvnw spring-boot:run
```
The application runs on `http://localhost:8080`.
## Running tests
```bash
./mvnw test
```
Tests run automatically on every push and pull request via GitHub Actions.
## Running with Docker
Build the image:
```bash
docker build -t picpay-simples .
```
Run the container:
```bash
docker run -p 8080:8080 picpay-simples
```
A pre-built image is also published to GitHub Container Registry on every push to `main`:
```bash
docker pull ghcr.io/gustavoks412/picpay-simples:main
docker run -p 8080:8080 ghcr.io/gustavoks412/picpay-simples:main
```
## Known limitations
- The authorizer integration (`app.authorizationApi`) isn't configured yet in `application.properties`. It's currently left blank. Transaction authorization is exercised in tests using a mock URL in `application-test.properties`, but running the app locally with `./mvnw spring-boot:run` won't have a working authorizer until this is set.
## CI/CD
GitHub Actions runs the test suite on every push and pull request. On pushes to `main`, once tests pass, a Docker image is built and pushed to GitHub Container Registry.
