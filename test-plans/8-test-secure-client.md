## Tests Secure Client Cockroach

Steps | Expected Result | Actual Result | Pass/Fail |
| --- | --------------- | ------------- | ----------|
| microk8s kubectl get pods| The secure client is in 'Running' state | | |
| microk8s kubectl get logs cockroachdb-client-secure | The schema has been successfully applied to the database | | |
| microk8s kubectl exec -it cockroachdb-client-secure -- ./cockroach sql --certs-dir=/cockroach/cockroach-certs --host=cockroachdb-public| Login to database is successful | | |

