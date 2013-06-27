Framers take raw data and only examines just enough of it in order to find the boundaries of the message and frame it. The result of this operation is a Frame.

A Frame contains raw data but has examined some of the information in the frame in order to be able to make decisions such as what payload it contains etc. A Frame can be asked to fully parse its data in order to produce a Packet.

A Packet is a parsed frame and has usually partially examined all the data within the frame to find all the necessary information about that particular protocol packet. However, everything in yajpcap is done lazily so even though the first pass of the data didn't find any obvious errors, there still can be some. If you really need to find out if all the data within the package is correct, you can call Packet.verify() which forces the packet to check everything and complain if it finds any errors.
