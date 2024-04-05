## Tests - Light Bulb Monitor

Steps | Expected Result | Actual Result | Pass/Fail |
| --- | --------------- | ------------- | ----------|
| Run kubectl get logs light-bulb-monitor-0-and inspect the logs| Receives all light bulb messages  |    |
| | Sends a message containg the light bulb hostname as 'target' when it detects a light bulb has been on longer than the light limit  |     |
| |     |     |
