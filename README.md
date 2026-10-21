# GitHub Repository Searcher

A Spring Boot backend application that integrates with GitHub's REST API, persists repository data into a PostgreSQL database, and provides endpoints to query, filter, and sort stored repositories.

---

## Tech Stack & Architecture

- **Language:** Java 21
- **Framework:** Spring Boot 3.3.4
- **Persistence:** Spring Data JPA, Hibernate, PostgreSQL
- **HTTP Client:** Spring `RestClient`
- **Documentation:** SpringDoc OpenAPI 3 (Swagger UI)
- **Testing:** JUnit 5, Mockito

### Layered Architecture

- **`controller`**: Exposes REST API endpoints (`/api/github/*`).
- **`service`**: Handles GitHub API integration and upsert business logic.
- **`repository`**: Spring Data JPA interfaces and dynamic query specifications.
- **`entity`**: PostgreSQL table mappings with indexes and unique constraints.
- **`dto`**: Request, response, and external GitHub payload models.
- **`exception`**: Centralized error handling using `@RestControllerAdvice`.

---

## Features

1. **GitHub API Search & Persistence:** Fetches public repositories from GitHub based on query terms, programming language, and sort order.
2. **Idempotent Upsert:** Uses GitHub's unique repository ID (`github_id`) to update existing records instead of creating duplicate database entries.
3. **Flexible Retrieval:** Queries stored repositories with dynamic filtering (`language`, `minStars`) and multi-field sorting (`stars`, `forks`, `lastUpdated`).
4. **Resilience & Validation:** Handles GitHub API errors, rate limits, request body validation, and returns clear error messages.

---

## Getting Started

### Prerequisites

- JDK 21+
- Apache Maven 3.9+
- PostgreSQL 15+

### Database Setup

1. Create a PostgreSQL database:

   ```sql
   CREATE DATABASE freightfox_github;
   ```

2. Update your database credentials in `src/main/resources/application.properties`:

   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/freightfox_github
   spring.datasource.username=postgres
   spring.datasource.password=YOUR_POSTGRES_PASSWORD
   ```

3. (Optional) Add a personal GitHub token to avoid or increase the limits of unauthenticated API requests:

   ```properties
   github.api.token=YOUR_GITHUB_TOKEN
   ```

**Note:** Keep your database password and GitHub token private. Do not commit actual credentials to your repository.

### Running the Application Locally

Run the following command from the project root:

```powershell
mvn spring-boot:run
```

The application will start on port `8080`.

### Running Automated Tests

Execute the following command to run the automated tests:

```powershell
mvn test
```

---

## API Documentation

### Interactive Swagger UI

Once the application is running, open the following URL in your browser:

[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## API Endpoints

### 1. Search & Save Repositories

Search GitHub for repositories and persist the results in the PostgreSQL database.

- **Method:** `POST`
- **URL:** `/api/github/search`
- **Content-Type:** `application/json`

**Request Body:**

```json
{
  "query": "spring boot",
  "language": "Java",
  "sort": "stars"
}
```

**Example Response (`200 OK`):**

```json
{
  "message": "Repositories fetched and saved successfully",
  "repositories": [
    {
      "id": 123456,
      "name": "spring-boot-example",
      "description": "An example repository for Spring Boot",
      "owner": "user123",
      "language": "Java",
      "stars": 450,
      "forks": 120,
      "lastUpdated": "2024-01-01T12:00:00Z"
    }
  ]
}
```

### 2. Retrieve Stored Repositories

Retrieve repositories already stored in the database, with optional filtering and sorting.

- **Method:** `GET`
- **URL:** `/api/github/repositories`

**Query Parameters:**

| Parameter | Required | Description |
|---|---|---|
| `language` | No | Filter by programming language, e.g., `Java`. |
| `minStars` | No | Filter repositories by minimum star count, e.g., `100`. |
| `sort` | No | Sort field: `stars` (default), `forks`, or `updated`. |

**Example Request:**

```http
GET /api/github/repositories?language=Java&minStars=100&sort=stars
```

**Example Response (`200 OK`):**

```json
{
  "repositories": [
    {
      "id": 123456,
      "name": "spring-boot-example",
      "description": "An example repository for Spring Boot",
      "owner": "user123",
      "language": "Java",
      "stars": 450,
      "forks": 120,
      "lastUpdated": "2024-01-01T12:00:00Z"
    }
  ]
}
```

---

## Error Handling

The application provides centralized exception handling to return meaningful error responses for issues such as:

- Invalid request data.
- GitHub API failures.
- GitHub API rate-limit restrictions.
- Database or persistence errors.

Refer to the API responses and Swagger UI documentation for details about the errors handled by the application.

---

## Testing

The project uses **JUnit 5** and **Mockito** for automated testing.

Run the test suite with:

```bash
mvn test
```

---

## Future Enhancements

- Add pagination for repository retrieval.
- Introduce scheduled synchronization with GitHub.
- Add Docker and Docker Compose support.
- Implement additional filters and sorting options.
- Add integration tests using Testcontainers.

---