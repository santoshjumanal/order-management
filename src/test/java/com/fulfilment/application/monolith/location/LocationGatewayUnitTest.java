package com.fulfilment.application.monolith.location;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

/**
 * Unit tests for LocationGateway.
 * Tests location resolution and lookup functionality.
 */
@DisplayName("Location Gateway Tests")
public class LocationGatewayUnitTest {

    private LocationGateway locationGateway;

    @BeforeEach
    public void setUp() {
        locationGateway = new LocationGateway();
    }

    @Test
    @DisplayName("Should resolve location by valid identifier")
    public void testResolveLocation_Valid() {
        Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");
        assertNotNull(location);
        assertEquals("ZWOLLE-001", location.identification);
    }

    @Test
    @DisplayName("Should return null for non-existent location")
    public void testResolveLocation_NotFound() {
        Location location = locationGateway.resolveByIdentifier("NONEXISTENT");
        assertNull(location);
    }

    @Test
    @DisplayName("Should resolve AMSTERDAM-001")
    public void testResolveLocation_Amsterdam() {
        Location location = locationGateway.resolveByIdentifier("AMSTERDAM-001");
        assertNotNull(location);
        assertEquals("AMSTERDAM-001", location.identification);
        assertEquals(100, location.maxCapacity);
    }

    @Test
    @DisplayName("Should resolve AMSTERDAM-002")
    public void testResolveLocation_Amsterdam2() {
        Location location = locationGateway.resolveByIdentifier("AMSTERDAM-002");
        assertNotNull(location);
        assertEquals("AMSTERDAM-002", location.identification);
        assertEquals(75, location.maxCapacity);
    }

    @Test
    @DisplayName("Should resolve TILBURG-001")
    public void testResolveLocation_Tilburg() {
        Location location = locationGateway.resolveByIdentifier("TILBURG-001");
        assertNotNull(location);
        assertEquals("TILBURG-001", location.identification);
    }

    @Test
    @DisplayName("Should resolve EINDHOVEN-001")
    public void testResolveLocation_Eindhoven() {
        Location location = locationGateway.resolveByIdentifier("EINDHOVEN-001");
        assertNotNull(location);
        assertEquals("EINDHOVEN-001", location.identification);
        assertEquals(70, location.maxCapacity);
    }

    @Test
    @DisplayName("Should resolve VETSBY-001")
    public void testResolveLocation_Vetsby() {
        Location location = locationGateway.resolveByIdentifier("VETSBY-001");
        assertNotNull(location);
        assertEquals("VETSBY-001", location.identification);
    }

    @Test
    @DisplayName("Should resolve HELMOND-001")
    public void testResolveLocation_Helmond() {
        Location location = locationGateway.resolveByIdentifier("HELMOND-001");
        assertNotNull(location);
        assertEquals("HELMOND-001", location.identification);
    }

    @Test
    @DisplayName("Should return null for null identifier")
    public void testResolveLocation_NullId() {
        Location location = locationGateway.resolveByIdentifier(null);
        assertNull(location);
    }

    @Test
    @DisplayName("Should return null for empty identifier")
    public void testResolveLocation_EmptyId() {
        Location location = locationGateway.resolveByIdentifier("");
        assertNull(location);
    }

    @Test
    @DisplayName("Should return null for blank identifier")
    public void testResolveLocation_BlankId() {
        Location location = locationGateway.resolveByIdentifier("   ");
        assertNull(location);
    }

    @Test
    @DisplayName("Location capacity should be positive")
    public void testLocationCapacity_Positive() {
        Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");
        assertNotNull(location);
        assertTrue(location.maxCapacity > 0);
    }

    @Test
    @DisplayName("Location should have maximum warehouses count")
    public void testLocationMaxWarehouses() {
        Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");
        assertNotNull(location);
        assertTrue(location.maxNumberOfWarehouses > 0);
    }
}
