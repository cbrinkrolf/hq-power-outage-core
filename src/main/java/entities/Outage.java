package entities;

import java.time.LocalDateTime;

public record Outage(int customersAffected, LocalDateTime start, double longitude, double latitude) {

}
