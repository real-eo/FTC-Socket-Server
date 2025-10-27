# FTC Socket Server
Socket server written in Java to run on an FTC Robot along a client running on external laptop via local wifi connection

## Info
`robot.java` is meant to run on the FTC robot in parallell. 
`main.py` is meant to run on the external pc

## How
1. The java server sends the image data from the usb camera mounted on the robot to a python tcp client over a local wifi socket. 
2. A keras image recognition model does AI recognition on the image data sent to the pc running the client
3. The python client echos the response from the recognition back to the java server.

### Notes
- The image data is sent bytewise and saved to a file, before it gets recompiled into the full image.
- Test data for the program is based on identprof (chefs, docotrs, engineers). 
- Final model uses images trained based off the centerstage game scoreing elements (pixels) 

