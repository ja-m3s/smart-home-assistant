## Tests Grafana

Steps | Expected Result | Actual Result | Pass/Fail |
| --- | --------------- | ------------- | ----------|
| microk8s kubectl get pods -n monitoring | Grafana is running | | |
| port forward and login to frontend with login admin/admin |  Prometheus is added as a datasource | | |
