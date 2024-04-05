## Tests - Light Bulb

Steps | Expected Result   | Actual Result | Pass/Fail |
| --- | --- | --- | ---|
| 1. Run kubectl get logs light-bulb-0
2. Inspect the logs| Sends Messages containing details of the light bulb  |    |
| | Receives messages from the light bulb monitor |     |
| | Turns the light off when it receives a message from the light bulb monitor    |     |

