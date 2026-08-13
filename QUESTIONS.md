# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
Yes. The code uses two styles: Product and Store are active-record, while Warehouse uses a repository behind a port with a separate domain model.

I'd standardize on the Warehouse style wherever there is real business logic, because:
- The logic sits in use cases and can be unit-tested without a database.
- It doesn't expose the JPA entity directly on the API, so the DB and the API stay decoupled.
- One consistent pattern is easier to maintain.

For very simple CRUD with no rules, active-record is fine, so I'd keep it there and not
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
Contract-first (Warehouse, from the yaml):
+ The spec is the single source of truth, so code can't drift from the contract.
+ Easy to share docs and generate clients for other teams.
- Needs extra tooling and a code-generation step.

Code-first (Product, Store):
+ Fast and simple, good for small internal endpoints.
- The API doc can drift, and it's easy to leak DB fields.

My choice: contract-first for shared or public APIs, code-first only for small internal ones.
Most importantly, be consistent - since the project already uses contract-first for Warehouse,
I'd make that the default.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
I'd follow the test pyramid and test the risky parts first.

1. Unit tests for the business rules (most important, fastest): the warehouse
   create/replace/archive checks and the fulfilment limits (2 per product/store, 3 per store,
   5 products per warehouse). These use in-memory fakes, so no database is needed.

2. A few integration tests (@QuarkusTest) for the main flows to check wiring, persistence,
   transactions and HTTP status codes end to end.

3. A small set of error-case checks (400/404) for validation.

To keep it effective: run tests in CI on every PR, add a regression test for every bug, and
keep tests fast and deterministic so people actually run them.
```