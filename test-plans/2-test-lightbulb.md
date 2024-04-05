## Tests - Light Bulb

Steps | Expected Result   | Actual Result | Pass/Fail |
| --- | --- | --- | ---|
| Run kubectl get logs light-bulb-0 and inspect the logs| Meaningful log output is displayed  |    |
| | Receives messages from the light bulb monitor |     |
| | Disregards messages not from the light-bulb-monitor |     |
| | Sends messages containing details of the light bulb state |     |
| | Turns the light off when it receives a message from the light bulb monitor addressed to this light-bulb   |     
| | Sends updated status of light-bulb when off  |     
