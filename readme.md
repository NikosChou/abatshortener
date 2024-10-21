# AbatShortener

**AbatShortener** is a URL shortening service that transforms long URLs into shorter, more manageable links. It helps simplify link sharing by generating concise versions of lengthy URLs, making them easier to use and remember.

## Prerequisites
- **Java 21**: Make sure you have JDK 21 installed on your system.
- **Maven**: Required to build and manage the project dependencies.
- **PostgreSQL**: Used as the main database for storing URL mappings.
- **Redis**: Used as a unique code pool to store and manage pre-generated codes, ensuring the uniqueness of shortened URLs. (Can be disabled if desired.)

## Installation Instructions
1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd abatshortener
   ```

2. **Start PostgreSQL and Redis using Docker Compose**
   Make sure you have Docker and Docker Compose installed. Then, run:
   ```bash
   docker-compose up -d
   ```

3. **Run the application using Maven**
   Use the Maven wrapper to start the Spring Boot application:
   ```bash
   mvn spring-boot:run
   ```

## Configuration
If you prefer not to use Redis as the unique code pool, you can disable it using the following configuration options:

1. **application.properties file**
   ```properties
   de.abat.url.shortener.redis.enabled=false
   ```

2. **Environment Variable**
   ```bash
   DE_ABAT_URL_SHORTENER_REDIS_ENABLED=false
   ```

## API Endpoints

### 1. Shorten a URL
- **Endpoint**: `/api/v1/short`
- **Method**: `POST`
- **Description**: Accepts a long URL and returns a shortened version.
- **Request Body**:
  ```json
  {
    "url": "string",
    "code": "string",
    "ttl": "string"
  }
  ```
- **Responses**:
    - **201 Created**: Successfully created a shortened URL.
    - **400 Bad Request**: The request was malformed or invalid.
    - **500 Internal Server Error**: An unexpected error occurred.

### Example Requests
Using HTTPie:
- **With TTL**:
  ```bash
  http POST :8080/api/v1/short url=http://google.com ttl=PT10m
  ```
  Equivalent cURL command:
  ```bash
  curl -X POST http://localhost:8080/api/v1/short -d '{"url":"http://google.com", "ttl":"PT10m"}' -H 'Content-Type: application/json'
  ```

- **Without TTL**:
  ```bash
  http POST :8080/api/v1/short url=http://google.comasd
  ```
  Equivalent cURL command:
  ```bash
    curl -X POST http://localhost:8080/api/v1/short -d '{"url":"http://google.com"}' -H 'Content-Type: application/json'
  ```

### 2. Retrieve a Shortened URL
- **Endpoint**: `/api/v1/short/{shortCode}`
- **Method**: `GET`
- **Description**: Retrieves the original URL associated with a given shortened code.
- **Path Parameters**:
    - `shortCode`: The shortened code for the URL (required).
- **Responses**:
    - **200 OK**: Returns the original URL.
    - **400 Bad Request**: The request was malformed or invalid.
    - **500 Internal Server Error**: An unexpected error occurred.

### Response Schemas

#### ExceptionMessage
```json
{
  "message": "string"
}
```

#### ShortRequest
```json
{
  "url": "string",
  "code": "string",
  "ttl": "string"
}
```

#### ShortenedUrlDto
```json
{
  "url": "string",
  "code": "string",
  "validUntil": "string",
  "createdAt": "string"
}
```

## Swagger UI
You can access the Swagger UI for AbatShortener at:
[http://localhost:8080/swagger-ui/index.html#/short-controller/shortUrl](http://localhost:8080/swagger-ui/index.html#/short-controller/shortUrl)

The OpenAPI definition for AbatShortener is as follows:

```yaml
openapi: 3.0.1
info:
  title: OpenAPI definition
  version: v0
servers:
- url: http://localhost:8080
  description: Generated server url
paths:
  /api/v1/short:
    post:
      tags:
      - short-controller
      operationId: shortUrl
      requestBody:
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/ShortRequest"
        required: true
      responses:
        "500":
          description: Internal Server Error
          content:
            '*/*':
              schema:
                $ref: "#/components/schemas/ExceptionMessage"
        "400":
          description: Bad Request
          content:
            '*/*':
              schema:
                $ref: "#/components/schemas/ExceptionMessage"
        "201":
          description: Created
          content:
            '*/*':
              schema:
                $ref: "#/components/schemas/ShortenedUrlDto"
  /api/v1/short/{shortCode}:
    get:
      tags:
      - short-controller
      operationId: get
      parameters:
      - name: shortCode
        in: path
        required: true
        schema:
          type: string
      responses:
        "500":
          description: Internal Server Error
          content:
            '*/*':
              schema:
                $ref: "#/components/schemas/ExceptionMessage"
        "400":
          description: Bad Request
          content:
            '*/*':
              schema:
                $ref: "#/components/schemas/ExceptionMessage"
        "200":
          description: OK
          content:
            '*/*':
              schema:
                type: object
components:
  schemas:
    ExceptionMessage:
      type: object
      properties:
        message:
          type: string
    ShortRequest:
      type: object
      properties:
        url:
          type: string
          format: url
          example: https://google.com
        code:
          type: string
          format: string
          nullable: true
          example: abcde
        ttl:
          type: string
          format: duration
          nullable: true
          example: PT20S
    ShortenedUrlDto:
      type: object
      properties:
        url:
          type: string
        code:
          type: string
        validUntil:
          type: string
          format: date-time
        createdAt:
          type: string
          format: date-time
```

This specification outlines the available endpoints and their expected request and response formats, making it easier to understand how to interact with the AbatShortener service.
