package io.pkts.streams;

public interface TcpStream extends Stream {
    // What do I want here ?
    // Probably layer 3 information
    // Maybe some statistics
    // Maybe some indication of state

    public String getSrcAddr();

    public String getDestAddr();

    public int getSrcPort();

    public int getDestPort();

    public boolean ended();
    public boolean endedGracefully();

    public boolean endedAbruptly();


}
