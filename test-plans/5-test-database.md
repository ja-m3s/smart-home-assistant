## Tests - Database

Steps | Expected Result | Actual Result | Pass/Fail |
| --- | --------------- | ------------- | ----------|
|Run command microk8s kubectl exec -it cockroachdb-client-secure -- ./cockroach sql --certs-dir=/cockroach/cockroach-certs --host=cockroachdb-public | There is a database called db_smart_home | | |
| | There is a messages table, under the schema s_smart_home | | |
| | There are records from the db import program in the messages table | | |
| | There is a user called event_bus_connector that has been created | | |
