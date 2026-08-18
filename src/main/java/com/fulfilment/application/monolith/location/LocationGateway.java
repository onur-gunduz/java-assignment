package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class LocationGateway implements LocationResolver {

  private static final Map<String, Location> locations = new HashMap<>();

  static {
    locations.put("ZWOLLE-001", new Location("ZWOLLE-001", 1, 40));
    locations.put("ZWOLLE-002", new Location("ZWOLLE-002", 2, 50));
    locations.put("AMSTERDAM-001", new Location("AMSTERDAM-001", 5, 100));
    locations.put("AMSTERDAM-002", new Location("AMSTERDAM-002", 3, 75));
    locations.put("TILBURG-001", new Location("TILBURG-001", 1, 40));
    locations.put("HELMOND-001", new Location("HELMOND-001", 1, 45));
    locations.put("EINDHOVEN-001", new Location("EINDHOVEN-001", 2, 70));
    locations.put("VETSBY-001", new Location("VETSBY-001", 1, 90));
  }

  @Override
  public Location resolveByIdentifier(String identifier) {
    if (identifier == null || identifier.isBlank() || !locations.containsKey(identifier.toUpperCase())) {
      throw new IllegalArgumentException("Invalid or non-existent Location identifier: " + identifier);
    }
    return locations.get(identifier.toUpperCase());
  }
}
