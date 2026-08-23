# PicPay Simples
[![English](https://img.shields.io/badge/lang-English-blue)](README.md)
[![Português](https://img.shields.io/badge/lang-Portugu%C3%AAs-green)](README.pt-BR.md)

API REST em Java + Spring Boot que simula uma plataforma de pagamentos: é possível cadastrar usuários (comum e lojista) e transferir dinheiro entre eles.
Este projeto foi feito como resolução de um desafio técnico backend, buscando manter as camadas bem separadas (controller, service, repository, domain, dtos) e seguir boas práticas em geral.
## Sobre o projeto
Existem dois tipos de usuário:
- Usuário comum: pode enviar e receber dinheiro.
- Lojista: só recebe, não pode fazer transferências.
  Antes de fechar uma transferência, o sistema chama um serviço autorizador externo (mock). Após a transação ser concluída, uma notificação é disparada para os usuários envolvidos, também via mock. Essa notificação foi desacoplada de propósito, pois se ela falhar isso não pode derrubar a transação.
## Stack técnica
- Java
- Spring Boot
- Spring Data JPA
- H2 Database (em memória)
- Maven
- Docker
- GitHub Actions
## Regras de negócio
- Nome completo, CPF, email e senha são obrigatórios para os dois tipos de usuário. CPF/CNPJ e email precisam ser únicos no sistema. Não pode haver dois cadastros com o mesmo CPF ou email.
- Usuários podem transferir dinheiro para lojistas e para outros usuários.
- Lojistas apenas recebem, nunca enviam dinheiro.
- Antes de transferir, o sistema precisa validar se o usuário tem saldo suficiente.
- Antes de finalizar a transferência, um serviço autorizador externo precisa ser consultado (mock `GET https://util.devi.tools/api/v2/authorize`). **Nota:** essa integração ainda não está totalmente funcional, veja [Limitações conhecidas](#limitações-conhecidas).
- A transferência é uma transação: se algo der errado no caminho, tudo é revertido e o dinheiro volta para a carteira do remetente.
- Quando alguém recebe um pagamento, precisa ser notificado (email/sms) por um serviço de terceiros (mock `POST https://util.devi.tools/api/v1/notify`). Esse serviço pode estar fora do ar, então isso não pode bloquear a transação.
- A API precisa ser RESTful.
## Endpoints
Cadastrar usuário:
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
Criar transação:
```
POST /transactions
Content-Type: application/json
{
    "senderId": 1,
    "receiverId": 2,
    "value": 100
}
```
## Como rodar o projeto
### Pré-requisitos
- Java 17+ (verifique a versão no pom.xml)
- Não é necessário instalar o Maven globalmente, o projeto já vem com o wrapper (mvnw / mvnw.cmd)
### Passos
```bash
git clone https://github.com/GustavoKS412/picpay-simples.git
cd picpay-simples
./mvnw spring-boot:run
```
A aplicação roda em `http://localhost:8080`.
## Rodando os testes
```bash
./mvnw test
```
Os testes rodam automaticamente a cada push e pull request via GitHub Actions.
## Rodando com Docker
Construir a imagem:
```bash
docker build -t picpay-simples .
```
Rodar o container:
```bash
docker run -p 8080:8080 picpay-simples
```
Uma imagem pré-construída também é publicada no GitHub Container Registry a cada push na branch `main`:
```bash
docker pull ghcr.io/gustavoks412/picpay-simples:main
docker run -p 8080:8080 ghcr.io/gustavoks412/picpay-simples:main
```
## Limitações conhecidas
- A integração com o autorizador (`app.authorizationApi`) ainda não está configurada em `application.properties`. Está deixada em branco por enquanto. A autorização de transações é exercitada nos testes usando uma URL mock em `application-test.properties`, mas rodar a aplicação localmente com `./mvnw spring-boot:run` não terá um autorizador funcional até que isso seja configurado.
## CI/CD
GitHub Actions roda a suíte de testes a cada push e pull request. Em pushes para `main`, depois que os testes passam, uma imagem Docker é construída e publicada no GitHub Container Registry.
