## Tests - DBImporter

Steps | Expected Result | Actual Result | Pass/Fail |
| --- | --------------- | ------------- | ----------|
| Run microk8s kubectl logs db-importer-0 | Meaningful log output is displayed | | |
| Run microk8s kubectl logs db-importer-0 | Inserts messages on the RabbitMQ exchange queue DBIMPORTER into the database | | |
| Run microk8s kubectl logs db-importer-0 | Database connection message stating the connection is established | | |
| Run microk8s kubectl logs db-importer-0 | RabbitMQ connection message stating the connection is established | | |
| Set replica count to two in db-importer.yaml. Redeploy. Run microk8s kubectl logs db-importer-0. Run microk8s kubectl logs db-importer-1| When two importer replicas are running, only one copy of each message in the queue is inserted into the database| | |
