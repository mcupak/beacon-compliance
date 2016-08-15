# Beacon Compliance Suite
This compliance suite checks your Beacon server implementation is compliant with the **0.3.0** specifications.

## Requirements
- Java 8;
- Maven 3;
- Beacon Protobuf DTOs: generated Java classes of the currently in development [Protobuf Schema](https://github.com/david4096/beacon-team/tree/proto) should be in the classpath.

## How To

To run the compliance suite against your server, type
```
mvn test -DserverToTest.url=http://your.beacon.server/
```