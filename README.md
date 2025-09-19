# REST Adapter Service

## What does this do?
This service fetches theatre data from partners like INOX and PVR every day, normalizes it using field mappings, and stores both raw and normalized data in our database.

## How it works
1. **Every day at 2 AM**, it wakes up and calls partner APIs
2. **Gets different types of data** from each partner:
   - Theatre locations and details
   - Hall configurations and seating layouts
   - Show schedules and timings
   - Pricing information and offers
   - (Any other data the partner provides)
3. **Validates and normalizes** the data using field mappings from Config Service
4. **Saves both raw and normalized JSON** to the database
5. **Tracks what worked** and what failed with detailed error tracking

## Data Processing
```
Partner API → Raw JSON → Field Mappings → Normalized JSON → Database
```

- **Raw data**: Exact partner response (for debugging)
- **Normalized data**: Transformed using field mappings to standard format
- **Validation**: Catches malformed JSON and missing required fields

## Partner APIs it calls
- INOX: `/theatres`, `/halls`, `/shows`, etc.
- PVR: `/locations`, `/halls`, `/shows`, etc.
- (More partners and endpoints can be added easily)

## Field Mapping
- Partner-specific field transformations via Config Service
- Example: INOX `theater_id` → TMS `externalId`
- Required field validation ensures data quality

## How to check if it's working
- **Latest job status**: `GET /api/job-status/latest`
- **Specific day**: `GET /api/job-status/2024-01-15`
- **Only failures**: `GET /api/job-status/failed/2024-01-15`

## Database tables
- **staging_records**: Raw and normalized JSON data from partners
- **job_executions**: Track which jobs passed/failed with error details

## Configuration
- Partner details and API endpoints from Config Service (port 8086)
- Field mappings for data transformation from Config Service
- No hardcoded values!

## Future plans
⚠️ **Note**: The job status monitoring will move to a separate service later when we have multiple adapters (file, email, etc.). This keeps things organized and gives a single place to check all adapter statuses.

## Running it
```bash
mvn spring-boot:run
```
Runs on port 8081.