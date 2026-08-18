# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
Yes, I would refactor the codebase to use the Repository Pattern consistently everywhere. Mixing the Active Record pattern (Store) with the Repository pattern (Warehouse) hurts maintainability and breaks team standards. Standardizing on repositories keeps domain models clean and simplifies core business logic testing.

2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
I choose Contract-First (OpenAPI). For a distributed warehouse application with multiple modules, ensuring contract stability and preventing breaking API changes is a critical operational priority.

3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
I would focus first on Unit Tests for the use cases because they are fast and make sure our core business rules work properly. Next, I would add Integration Tests using RestAssured to verify that endpoints return the correct HTTP status codes and that database transactions save safely. To keep coverage effective over time, I would set up the automated build pipeline to block any new code that lacks proper tests.