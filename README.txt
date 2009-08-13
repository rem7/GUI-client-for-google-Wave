A very simple GUI based client to be used with the reference server

This is a small experiement I investigated and thus unfortunately I cannot keep the project updated or fix bugs etc at the moment, especially as google keep updating the reference code. This may or may not work with the current distribution of the google wave reference server again I haven't tested it.

To run...
get a copy of the Google Wave reference client and server from...
	http://code.google.com/p/wave-protocol/source/checkout
...install this and ensure it is working.

Go to the location where the reference client/server was installed and navigate to...
	/wave-protocol/src/org/waveprotocol/wave/examples/fedone/waveclient/
delete the console folder. Place the console folder found in this repository there.

Recompile and run the server as described on the Google Wave reference client and server site. Run the client in the same way you would normally.

You should have a basic user interface providing the same functionality as the text based client.




Editing the client should be easy with the included WaveConnector.java class. This is fully documented and abstracts the underlaying complexity of communicating with google wave. You could build your own text based client using this or your own graphical interface. For example use see the ConsoleClient.java class.