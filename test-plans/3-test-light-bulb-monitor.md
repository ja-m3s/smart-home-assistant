## Tests - Light Bulb Monitor

Steps | Expected Result | Actual Result | Pass/Fail |
| --- | --------------- | ------------- | ----------|
| Run microk8s kubectl get logs light-bulb-monitor-0-and inspect the logs| Meaningful log output is displayed   |    |
| | Establishes connection to event bus |     |
| | Receives messages from the light bulb |     |
| | Disregards messages not from the light-bulb |     |
| | Sends trigger message to the light-bulb after the light-bulb has been on for a set amount of time |     |
| | Doesn't send a trigger message when light-bulb is off |     
  

