# Logging Configuration

This application now includes comprehensive logging for all essential operations.

## Log Files

The application creates the following log files in the `logs` directory:

1. **application.log** - Contains all application logs (INFO, WARN, ERROR, DEBUG)
2. **error.log** - Contains only ERROR level logs for easy troubleshooting

## Log File Features

- **Rolling Strategy**: Logs are rolled over daily
- **Size-based Rolling**: When a log file reaches 10MB, it creates a new file
- **Retention**: Logs are kept for 30 days
- **Format**: `timestamp [thread] level logger - message`

## What is Logged

### Controller Layer
- **All HTTP requests** with endpoint, method, and parameters
- **Request results** with response data size or counts
- **File uploads/downloads** with filenames and sizes

### Service Layer
- **CRUD operations** (Create, Read, Update, Delete)
- **Business logic operations** like CSV import/export
- **Data validation** and error conditions
- **Bulk operations** with counts

### Exception Handler
- **Validation errors** with field-level details
- **Business exceptions** (not found, conflicts, etc.)
- **Data integrity violations**
- **Unexpected errors** with full stack traces

## Log Levels

- **INFO**: Normal operational events (requests, successful operations)
- **WARN**: Warning conditions (validation failures, conflicts)
- **ERROR**: Error conditions (not found, exceptions)
- **DEBUG**: Detailed information for debugging (database queries, data retrieval)

## Configuration Files

1. **logback-spring.xml** - Main logging configuration
2. **application.properties** - Additional logging settings

## Sample Log Entries

### Successful Operation
```
2026-06-22 18:30:15.123 [http-nio-8080-exec-1] INFO  c.a.a.controller.AuthorController - POST /authors - Creating new author: John Doe
2026-06-22 18:30:15.234 [http-nio-8080-exec-1] INFO  c.a.a.service.AuthorService - Saving Author: John Doe with email: john@example.com
2026-06-22 18:30:15.345 [http-nio-8080-exec-1] INFO  c.a.a.service.AuthorService - Successfully saved author with ID: 1
2026-06-22 18:30:15.456 [http-nio-8080-exec-1] INFO  c.a.a.controller.AuthorController - POST /authors - Successfully created author with ID: 1
```

### Error Condition
```
2026-06-22 18:31:20.123 [http-nio-8080-exec-2] INFO  c.a.a.controller.AuthorController - GET /authors/99 - Fetching author by ID
2026-06-22 18:31:20.234 [http-nio-8080-exec-2] ERROR c.a.a.service.AuthorService - Author not found with id: 99
2026-06-22 18:31:20.345 [http-nio-8080-exec-2] WARN  c.a.a.e.GlobalExceptionHandler - Not found error: Author not found with id: 99
```

### CSV Import
```
2026-06-22 18:32:10.123 [http-nio-8080-exec-3] INFO  c.a.a.controller.AuthorController - POST /authors/import - Importing authors from CSV file: authors.csv
2026-06-22 18:32:10.234 [http-nio-8080-exec-3] INFO  c.a.a.service.AuthorService - Starting CSV import for authors from file: authors.csv
2026-06-22 18:32:10.567 [http-nio-8080-exec-3] INFO  c.a.a.service.AuthorService - Successfully imported 25 authors from CSV
2026-06-22 18:32:10.678 [http-nio-8080-exec-3] INFO  c.a.a.controller.AuthorController - POST /authors/import - Successfully imported 25 authors
```

## Viewing Logs

### During Development
Logs are also printed to the console for easy viewing during development.

### In Production
Monitor the log files in the `logs` directory:
```powershell
# View the latest logs
Get-Content logs\application.log -Tail 50 -Wait

# View only errors
Get-Content logs\error.log -Tail 50 -Wait

# Search for specific logs
Select-String -Path logs\application.log -Pattern "ERROR"
```

## Customizing Log Levels

To change the log level for specific packages, edit `application.properties`:

```properties
# Set application logging to DEBUG
logging.level.com.aryan.authorbook=DEBUG

# Set Spring Web logging to DEBUG
logging.level.org.springframework.web=DEBUG

# Set Hibernate logging to DEBUG
logging.level.org.hibernate=DEBUG
```

## Troubleshooting

### Logs Directory Not Created
The `logs` directory is automatically created by Logback when the application starts.

### Log Files Growing Too Large
Adjust the rolling policy in `logback-spring.xml`:
- Change `maxFileSize` (currently 10MB)
- Change `maxHistory` (currently 30 days)

### Too Much Logging
Reduce the log level from INFO to WARN or ERROR in `application.properties`.

### Not Enough Logging
Increase the log level from INFO to DEBUG in `application.properties`.

