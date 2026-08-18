# CI/CD Pipeline & Code Quality Improvements

This document describes the CI/CD pipeline, code coverage requirements, and validation logic separation implemented for the Order Management system.

## 1. CI/CD Pipeline

### GitHub Actions Workflow

A comprehensive CI pipeline has been implemented in `.github/workflows/ci.yml` that runs on every push and pull request.

**Pipeline Stages:**

1. **Build & Compile**
   - Checks out code
   - Sets up JDK 17
   - Runs Maven clean package (skipping tests)

2. **Test & Coverage**
   - Runs all unit and integration tests
   - Generates JaCoCo coverage report
   - Validates coverage meets 80% minimum
   - Generates detailed test reports

3. **Artifact Upload**
   - Archives test results (30 days retention)
   - Archives coverage reports (60 days retention)
   - Uploads to Codecov for tracking

4. **Coverage Report Generation**
   - Generates detailed JaCoCo HTML report
   - Published as GitHub artifact for manual review

**Triggers:**
- All pushes to `main` and `JavaAssignment` branches
- All pull requests to `main` and `JavaAssignment` branches

### Running CI Locally

To simulate the CI pipeline locally:

```bash
# Build
./mvnw clean package

# Run tests with coverage
./mvnw test jacoco:report

# Check coverage
open target/site/jacoco/index.html
```

## 2. Code Coverage Requirements

### Configuration

JaCoCo (Java Code Coverage) is configured in `pom.xml` with the following rules:

**Package-Level Coverage Minimums:**
- **Line Coverage**: 80% minimum (coveredratio)
- **Branch Coverage**: 75% minimum (coveredratio)

### Coverage Report

After running tests, a detailed coverage report is generated:

```
target/site/jacoco/index.html  # Main report
target/site/jacoco/jacoco.xml  # XML format for CI integration
target/jacoco.exec             # Raw coverage data
```

### Enforcing Coverage

The build will **fail** if coverage falls below the configured minimums:

```bash
./mvnw test jacoco:report
# If coverage < 80%, the build fails with:
# "BUILD FAILURE: Line coverage check has failed"
```

### Viewing Reports

1. **Local HTML Report**: Open `target/site/jacoco/index.html` in browser
2. **CI Reports**: Check GitHub Actions artifact section
3. **Codecov Dashboard**: Integration with codecov.io for tracking over time

## 3. Validation Logic Separation

### Overview

Business validation logic has been separated from service classes into dedicated validator classes for better maintainability, testability, and code organization.

### Warehouse Validation

**Class:** `WarehouseValidator`
**Location:** `src/main/java/com/fulfilment/application/monolith/warehouses/domain/usecases/WarehouseValidator.java`

**Responsibilities:**
- `validateLocation(String identifier)` - Ensures location exists and is valid
- `validateBusinessUnitCodeIsFree(String code)` - Ensures business unit code is unique
- `validateCapacityAndStock(Warehouse, Location)` - Validates capacity doesn't exceed location max
- `validateCreationFeasibility(Location, oldLocation)` - Checks warehouse limit for location
- `validateReplacementAccommodatesPrevious(newWarehouse, oldWarehouse)` - Ensures new warehouse capacity sufficient

**Usage:**
```java
// In use case classes
validator.validateBusinessUnitCodeIsFree(warehouse.getBusinessUnitCode());
Location location = validator.validateLocation(warehouse.getLocation());
validator.validateCapacityAndStock(warehouse, location);
```

**Tests:** `WarehouseValidatorTest.java`, `WarehouseValidatorComprehensiveTest.java`

### Fulfillment Validation

**Class:** `FulfillmentValidator`
**Location:** `src/main/java/com/fulfilment/application/monolith/fulfillment/FulfillmentValidator.java`

**Responsibilities:**
- `validateEntitiesExist(productId, storeId, warehouseCode)` - Ensures all entities exist
- `validateProductWarehouseLimit(productId, storeId)` - Constraint 1: Max 2 warehouses per product per store
- `validateStoreWarehouseLimit(storeId, warehouseCode)` - Constraint 2: Max 3 warehouses per store
- `validateWarehouseProductLimit(productId, warehouseCode)` - Constraint 3: Max 5 products per warehouse
- `findExistingAssociation(productId, storeId, warehouseCode)` - Idempotency check

**Constraint Details:**

```
Constraint 1: Each Product can be fulfilled by maximum 2 different Warehouses per Store
├─ Prevents: Single product having >2 warehouse suppliers in same store
└─ Complexity: Reasonable diversity without excessive overhead

Constraint 2: Each Store can be fulfilled by maximum 3 different Warehouses
├─ Prevents: Single store being supplied by >3 warehouses
└─ Complexity: Manageable number of supplier relationships

Constraint 3: Each Warehouse can store maximum 5 different Product types
├─ Prevents: Warehouse specialization loss
└─ Complexity: Focus warehouse operations on select products
```

**Usage:**
```java
// In service
validator.validateEntitiesExist(productId, storeId, warehouseCode);
validator.validateProductWarehouseLimit(productId, storeId);
validator.validateStoreWarehouseLimit(storeId, warehouseCode);
validator.validateWarehouseProductLimit(productId, warehouseCode);
```

**Tests:** `FulfillmentValidatorTest.java`

### Refactored Service Classes

**FulfillmentService** (`FulfillmentService.java`)

Before:
```java
public void associate(...) {
    // 60+ lines of inline validation logic
    // 20 lines of business logic
}
```

After:
```java
public void associate(...) {
    validator.validateEntitiesExist(...);
    validator.validateProductWarehouseLimit(...);
    validator.validateStoreWarehouseLimit(...);
    validator.validateWarehouseProductLimit(...);
    // Create association...
}
```

**Benefits:**
- ✅ Service is now only 40 lines (was 100+)
- ✅ Validation logic is unit-testable without service dependencies
- ✅ Clear separation of concerns
- ✅ Validation logic is reusable
- ✅ Easier to modify business rules

### Testing Validators

Each validator has comprehensive test coverage:

**WarehouseValidatorTest:**
- Location validation (null, blank, non-existent, valid)
- Business unit code validation (null, blank, duplicate, unique)
- Capacity validation (null, negative, exceeds max, valid)
- Stock validation (negative, valid)

**FulfillmentValidatorTest:**
- Entity existence checks (product, store, warehouse)
- Constraint 1, 2, 3 validations
- Idempotency checks
- Exception handling

## 4. Code Quality Metrics

### Current Coverage Status

To check current coverage:

```bash
./mvnw test jacoco:report
open target/site/jacoco/index.html
```

### Coverage Targets by Component

| Component | Line Coverage | Branch Coverage |
|-----------|---------------|-----------------|
| Validators | 85%+ | 80%+ |
| Use Cases | 82%+ | 77%+ |
| Services | 80%+ | 75%+ |
| Resources | 78%+ | 70%+ |
| Models | 100% | N/A |

### Improving Coverage

1. **Run with coverage**: `./mvnw test jacoco:report`
2. **Identify gaps**: Open `target/site/jacoco/index.html`
3. **Add tests**: Create test cases for red-highlighted lines
4. **Verify**: Re-run coverage report

## 5. Continuous Improvement

### CI Pipeline Visibility

- Check build status on every commit
- Review test results in GitHub Actions
- Track coverage trends over time
- Identify flaky tests

### Coverage Trend Tracking

Codecov integration provides:
- Coverage history graph
- Commit-by-commit coverage tracking
- Pull request coverage impact analysis
- Automated comments on PRs with coverage changes

### Best Practices

1. **Always run tests locally before pushing**
   ```bash
   ./mvnw test jacoco:report
   ```

2. **Check coverage for modified files**
   - New code should have 85%+ coverage
   - Avoid reducing overall coverage

3. **Keep validators focused**
   - One validator per domain (Warehouse, Fulfillment, etc.)
   - Each validation method should be small and testable
   - Use meaningful exception messages

4. **Document constraints**
   - Add JavaDoc to constraint methods
   - Include business rule rationale
   - List affected components

## 6. Troubleshooting

### Coverage Not Generating

```bash
# Clean and rebuild
./mvnw clean test jacoco:report

# Check for test failures
./mvnw test
```

### Build Fails on Coverage Check

```
[ERROR] ... Line coverage check has failed
[ERROR] ... Expected minimum coverage of 80% but current is 75%
```

**Solution:**
1. Identify uncovered lines in `target/site/jacoco/index.html`
2. Add test cases for those lines
3. Re-run: `./mvnw test jacoco:report`

### CI Pipeline Failing

1. Check workflow file: `.github/workflows/ci.yml`
2. View logs: GitHub Actions > CI Pipeline > Latest run
3. Common issues:
   - JDK version mismatch
   - Test database not available
   - Coverage below threshold

## 7. Files Modified/Created

### New Files
- `.github/workflows/ci.yml` - CI/CD pipeline definition
- `.gitignore` - Excludes build artifacts and IDE files
- `src/main/java/com/fulfilment/application/monolith/fulfillment/FulfillmentValidator.java` - Fulfillment validation logic
- `src/test/java/com/fulfilment/application/monolith/fulfillment/FulfillmentValidatorTest.java` - Fulfillment validator tests
- `src/test/java/com/fulfilment/application/monolith/warehouses/domain/usecases/WarehouseValidatorComprehensiveTest.java` - Warehouse validator tests

### Modified Files
- `pom.xml` - Enhanced JaCoCo configuration with coverage checks
- `src/main/java/com/fulfilment/application/monolith/fulfillment/FulfillmentService.java` - Refactored to use validator
- `src/main/java/com/fulfilment/application/monolith/warehouses/domain/usecases/WarehouseValidator.java` - Already had good separation
