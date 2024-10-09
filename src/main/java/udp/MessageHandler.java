package udp;

import java.net.DatagramPacket;

public class MessageHandler implements Runnable{
    private DatagramPacket packet;

    public MessageHandler(DatagramPacket socket) {
        this.packet = socket;
    }

    @Override
    public void run() {
        String receivedMessage = new String(packet.getData(), 0, packet.getLength());
        String[] lines = receivedMessage.split("\n");

        if (lines.length > 0) {
            String firstLine = lines[0].trim();
            if (firstLine.equalsIgnoreCase("mensagem do servidor")) {
                System.out.println("Recebida uma mensagem do servidor.");
            } else if (firstLine.equalsIgnoreCase("mensagem do cliente")) {
                StringBuilder messageBody = new StringBuilder();
                for (int i = 1; i < lines.length; i++) {
                    messageBody.append(lines[i]).append("\n");
                }

            } else {
                System.out.println("Recebida uma mensagem desconhecida.");
            }
        } else {
            System.out.println("Mensagem vazia recebida.");
        }

    }
}
