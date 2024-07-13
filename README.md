# APIGateway
![Java](https://img.shields.io/badge/Java-17-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.1-brightgreen.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)


APIGateway is a Spring Cloud Gateway application built with Spring Boot version 3.3.1 and Java version 17. This project serves as an API gateway, routing and managing requests to various backend services. The routes are dynamically configured using a JSON file, and database configurations are managed through environment variables.

## Table of Contents

- [Features](#features)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Configuration](#configuration)
    - [Route Configuration](#route-configuration)
    - [Installation](#installation)
- [Usage](#usage)



## Features

- Dynamic route configuration from a JSON file.
- Integration with SQLite for data persistence.
- Environment variable support for flexible configuration.
- Restriction of routes (feature under development).

## Getting Started

### Prerequisites

- Java 17
- Maven 
- SQLite

### Configuration

The application can be configured using the `application.yml` file or environment variables.

#### application.yml

```yaml
spring:
  application:
    name: liberty-api-gateway
    api-route-json-path: ${API_ROUTE_JSON_PATH:json/apiRoute.json}
  jpa:
    database-platform: org.hibernate.community.dialect.SQLiteDialect
    hibernate:
      ddl-auto: create-drop
  datasource:
    url: ${DATASOURCE_URL:jdbc:sqlite:gateway.db}
    driver-class-name: ${DATASOURCE_DRIVER_CLASS_NAME:org.sqlite.JDBC}
    username: ${DATASOURCE_USERNAME:admin}
    password: ${DATASOURCE_PASSWORD:admin}
```
#### Route Configuration
Routes are configured using a JSON file specified by the api-route-json-path property. The default path is `resources/json/apiRoute.json`.

Example JSON Schema
```json
[
  {
    "path": "/eInvoice-preprod-service/**",
    "authKey": "123",
    "uri": "https://preprod-api.myinvois.hasil.gov.my"
  }
]
```

### Installation

1. Clone the repository:
    ```bash
    git clone https://github.com/MdShohanurRahman/api-gateway.git
    cd apigateway
    ```

2. Build the project:
    ```bash
    mvn clean install
    ```

3. Run the application:
    ```bash
    mvn spring-boot:run
    ```
   or
   ```bash
    API_ROUTE_JSON_PATH=/path/to/your/apiRoute.json DATASOURCE_URL=jdbc:sqlite:/path/to/your/gateway.db mvn spring-boot:run
    ```

## Usage
Once the application is running, it will route incoming requests based on the configured paths and URIs. For example, a request to `/eInvoice-preprod-service/any-path` will be routed to https://preprod-api.myinvois.hasil.gov.my/any-path.