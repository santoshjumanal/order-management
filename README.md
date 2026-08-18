# Java Code Assignment

This is a short code assignment that explores various aspects of software development, including API implementation, documentation, persistence layer handling, and testing.

## About the assignment

You will find the tasks of this assignment on [CODE_ASSIGNMENT](CODE_ASSIGNMENT.md) file

## About the code base

This is based on https://github.com/quarkusio/quarkus-quickstarts

### Requirements

To compile and run this demo you will need:

- JDK 17+

In addition, you will need either a PostgreSQL database, or Docker to run one.

### Configuring JDK 17+

Make sure that `JAVA_HOME` environment variables has been set, and that a JDK 17+ `java` command is on the path.

## Building the demo

Execute the Maven build on the root of the project:

```sh
./mvnw package
```

## Running the demo

### Live coding with Quarkus

The Maven Quarkus plugin provides a development mode that supports
live coding. To try this out:

```sh
./mvnw quarkus:dev
```

In this mode you can make changes to the code and have the changes immediately applied, by just refreshing your browser.

    Hot reload works even when modifying your JPA entities.
    Try it! Even the database schema will be updated on the fly.

## (Optional) Run Quarkus in JVM mode

When you're done iterating in developer mode, you can run the application as a conventional jar file.

First compile it:

```sh
./mvnw package
```

Next we need to make sure you have a PostgreSQL instance running (Quarkus automatically starts one for dev and test mode). To set up a PostgreSQL database with Docker:

```sh
docker run -it --rm=true --name quarkus_test -e POSTGRES_USER=quarkus_test -e POSTGRES_PASSWORD=quarkus_test -e POSTGRES_DB=quarkus_test -p 15432:5432 postgres:13.3
```

Connection properties for the Agroal datasource are defined in the standard Quarkus configuration file,
`src/main/resources/application.properties`.

Then run it:

```sh
java -jar ./target/quarkus-app/quarkus-run.jar
```
    Have a look at how fast it boots.
    Or measure total native memory consumption...


## See the demo in your browser

Navigate to:

<http://localhost:8080/index.html>

Have fun, and join the team of contributors!

## Implementation Summary

This assignment implements the following features:

### 1. Location Management
- `LocationGateway.resolveByIdentifier()` - Resolves locations by their identifier

### 2. Store Management  
- Store creation, update (PUT), and partial update (PATCH) operations
- **Key Design Pattern**: Transaction separation - database operations complete and commit before calling the legacy system gateway, ensuring data consistency across systems

### 3. Warehouse Management
- Full CRUD operations for warehouse units with business rule validation:
  - **Business Unit Code Verification**: Ensures unique warehouse codes
  - **Location Validation**: Confirms location exists and is valid
  - **Warehouse Creation Feasibility**: Checks if maximum warehouses limit reached
  - **Capacity and Stock Validation**: Ensures capacity doesn't exceed location limits
  - **Replacement Constraints**: Validates new warehouse can accommodate previous stock

### 4. Fulfillment Units (BONUS)
- Associates products with warehouses in stores with the following constraints:
  - Each product can be fulfilled by maximum 2 different warehouses per store
  - Each store can be fulfilled by maximum 3 different warehouses  
  - Each warehouse can store maximum 5 different product types

## Architecture Notes

- **Database Access**: Uses both active-record pattern (Store, Product) and repository pattern (Warehouse) for different levels of complexity
- **Transaction Management**: Critical operations are separated into transactional helper methods to ensure database commits complete before external system calls
- **API Contracts**: Warehouse API uses OpenAPI specification for contract-first development

## Troubleshooting

Using **IntelliJ**, in case the generated code is not recognized and you have compilation failures, you may need to add `target/.../jaxrs` folder as "generated sources".

### Build Issues
If you encounter "@Transactional annotation on private method" errors, ensure that Quarkus configurations are properly set:
```properties
quarkus.arc.fail-on-intercepted-private-method=false
```

This is already configured in the project. Transactional methods must be package-private or public for Quarkus to properly apply transaction proxying.