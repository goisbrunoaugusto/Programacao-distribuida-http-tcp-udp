package udp;

import java.net.DatagramPacket;

public class MessageHandler implements Runnable{
    private DatagramPacket socket;

    public MessageHandler(DatagramPacket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

    }
}
