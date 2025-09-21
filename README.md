# TMS REST Adapter Service

A Spring Boot service that connects with theatre partners like INOX and PVR to fetch show data automatically.

## What it does

This service polls theatre partner APIs daily to collect:
- Theatre locations and details
- Hall configurations and seating
- Show schedules and timings
- Pricing information

The data gets normalized and stored in a staging database for the main TMS system to process.

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- TMS Config Service running on port 8086

### Running the Service

1. **Start the application:**
   ```bash
   mvn spring-boot:run
   ```

2. **Access the service:**
   - Main service: http://localhost:8081
   - H2 Database console: http://localhost:8081/h2-console
   - Health check: http://localhost:8081/actuator/health

3. **Database connection (H2 Console):**
   - JDBC URL: `jdbc:h2:mem:testdb`
   - Username: `sa`
   - Password: `password`

## Demo Mode

The service includes WireMock to simulate partner APIs for testing:

### Mock Partner APIs
- **INOX Theatres:** http://localhost:8089/v1/theatres
- **INOX Halls:** http://localhost:8089/v1/halls
- **INOX Shows:** http://localhost:8089/v1/shows
- **INOX Prices:** http://localhost:8089/v1/prices

## How it Works

1. **Scheduled Polling:** Runs daily at 2 AM (configurable)
2. **Data Fetching:** Gets partner configurations from TMS Config Service
3. **API Calls:** Fetches data from partner REST APIs
4. **Data Transformation:** Normalizes data using field mappings
5. **Storage:** Saves both raw and processed data to staging database

## Configuration

### Scheduling
Update the cron expression in `PartnerPollingService.java`:
```java
@Scheduled(cron = "0 0 2 * * *")  // Daily at 2 AM
```

### Database
Switch from H2 to PostgreSQL in `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tms_staging
    username: tms_user
    password: tms_password
```

### WireMock
Disable mock APIs:
```yaml
wiremock:
  enabled: false
```

### Health Checks
- http://localhost:8081/actuator/health

### Database Tables
- `staging_records` - Raw and normalized partner data
- `job_execution` - Polling job history and status

## Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Config        │    │   REST Adapter   │    │   Partner APIs  │
│   Service       │◄───┤   Service        ├───►│   (INOX/PVR)    │
│   (Port 8086)   │    │   (Port 8081)    │    │   (Port 8089)   │
└─────────────────┘    └──────────┬───────┘    └─────────────────┘
                                  │
                                  ▼
                       ┌─────────────────┐
                       │   Staging       │
                       │   Database      │
                       │   (H2/PostgreSQL)│
                       └─────────────────┘
```

## Development

### Adding New Partners

1. **Add partner config** in TMS Config Service
2. **Add field mappings** for data transformation
3. **Update WireMock** with partner's API format (for testing)

### Changing Polling Schedule

Edit the `@Scheduled` annotation:
```java
@Scheduled(cron = "0 */30 * * * *")  // Every 30 minutes
```

### Error Handling

Failed records are saved with:
- Status: `FAILED`
- Raw data preserved
- Error logged for investigation