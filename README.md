# Assignment 08 — Exercise 05: GraphQL

This is an independent project for exercise 05. It retains the existing Thymeleaf-based UI, layout, assets, entities, and database flow from the prior assignment, then adds the requested GraphQL API and AJAX rendering on the home page.

## What was added

- GraphQL schema at `src/main/resources/graphql/catalog.graphqls`.
- Public catalog queries: price ordering, category filtering, lookup, search, and pagination.
- Admin-only category/product CRUD mutations.
- Flat GraphQL DTOs, server-side validation, bounded pagination, and parameterized database queries.
- AJAX GraphQL catalog rendering while keeping the existing purple music UI.

## Database setup

By default, the project creates and uses a local H2 file database at `data/assignment08-exercise05` on its first run. Hibernate creates the required schema automatically.

To use MySQL instead, set `APP_DB_URL`, `APP_DB_USERNAME`, `APP_DB_PASSWORD`, `APP_DB_DRIVER=com.mysql.cj.jdbc.Driver`, and `APP_JPA_DIALECT=org.hibernate.dialect.MySQLDialect` before startup. Do not put a password in source control.

The project identity and upload directory are already separated from the previous exercise:

```text
Assignment08-Exercise05-Graphql
app.upload.dir=/home/thien/uploads/assignment08-exercise05-graphql
```

Use `mvn test` to verify the project, or `mvn spring-boot:run` to start it locally.
