# Networked-Chat
Networked chat with RSA encryption/decryption

We implemented two programs. A ChatServer.java and a ChatClient.java program.
You must run only one instance of ChatServer but you can run multiple instances of ChatClient.java.

Using Chat Server
	Run the program
	Click on the button to start listening
	The port number and ip address should appear

Using Chat Client
	Run the program 
	Enter the IP address
	Enter the port 
	Then when you click connect you will be prompted with a joption panel 
	You will then enter your username and a valid P and Q
	NOTE: we did not implement a file to provide the prime numbers for you

To send to a individual you must click on their name on the client list on the left side 
Otherwise the program will default to sending a message to everyone in the client list

The server provides output on the messages that are encrypted.

NOTE: 
We did not implement a client removal system
So when you close out the program the client names remain on the list.

NOTE:
We used this resource to check for prime numbers
//https://www.mkyong.com/java/how-to-determine-a-prime-number-in-java/
