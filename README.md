# TFTP-Server
  an  extended  TFTP  (Trivial  File  Transfer  Protocol) 
server.
 The  communication  is performed  using  a  binary  communication  protocol,  which  will  support  the  upload, 
downlo
ad and lookup of files
## Introduction
  The implementation of the server will be based on the Reactor and Thread-Per-Client servers.
  The extended TFTP Specification The TFTP (Trivial File TransferProtocol) allows users to upload and download files from a
  given server. Our extendedversion will require a user to perform a passwordless server 
  login as well as enable theserver to communicate broadcast messages to all 
  users and support for directory listings. 
  ## Supported Commands
  The extended TFTP supports 10 types of packets:
  ```
  1     Read request (RRQ)
  2     Write request (WRQ)
  3     Data (DATA)
  4     Acknowledgment (ACK)
  5     Error (ERROR)
  6     Directory listing request (DIRQ)
  7     Login request (LOGRQ)
  8     Delete request (DELRQ)
  9     Broadcast file added/deleted (BCAST)
  10    Disconnect (DISC)
  ```
  ## Testing run commands
  Reactor server:
  ```
  mvn exec:java-Dexec.mainClass=”bgu.spl171.net.impl.TFTPreactor.ReactorMain”-Dexec.args=”<port>”
  ```
  Thread per client server:
  ```
  mvn exec:java-Dexec.mainClass=”bgu.spl171.net.impl.TFTPtpc.TPCMain”-Dexec.args=”<port>
  ```
  
