@echo off
REM Usage:  client <hostname> <port> <numberOfConnections>
java -cp classes client.MultiplexingClient %1 %2 %3 %4