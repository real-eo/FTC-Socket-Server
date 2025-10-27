# FTC Socket Server
Socket server written in Java to run on an FTC Robot along a client running on external laptop via local wifi connection

## Info
`robot.java` is meant to run on the FTC robot in parallell. 

## How?
First, the java server sends the image data from the usb camera mounted on the robot to a python tcp client over a local wifi socket. Then a keras image recognition model does AI recognition on the image data before it echos the response from the recognition back to the java server.    
