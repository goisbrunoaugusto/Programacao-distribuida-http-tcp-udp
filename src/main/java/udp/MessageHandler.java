package udp;

import java.net.DatagramPacket;
import java.util.logging.Logger;

public class MessageHandler implements Runnable{
    private static final Logger logger = Logger.getLogger(MessageHandler.class.getName());
    private final DatagramPacket packet;

    public MessageHandler(DatagramPacket packet) {
        this.packet = packet;
    }

    @Override
    public void run() {
        logger.info("Entrou no MessageHandler");
        String receivedMessage = new String(packet.getData(), 0, packet.getLength());
        String[] lines = receivedMessage.split("\n");

        if (lines.length > 0) {
            String firstLine = lines[0].trim();
            if (firstLine.equalsIgnoreCase("mensagem do servidor")) {
                logger.info("Recebida uma mensagem do servidor.");
            } else if (firstLine.equalsIgnoreCase("mensagem do cliente")) {
                logger.info("Recebida uma mensagem do cliente.");

                StringBuilder messageBody = new StringBuilder();
                for (int i = 1; i < lines.length; i++) {
                    messageBody.append(lines[i]).append("\n");
                }
                logger.info("#### Corpo da mensagem #####" + messageBody.toString());

            } else {
                System.out.println("Recebida uma mensagem desconhecida.");
            }
        } else {
            System.out.println("Mensagem vazia recebida.");
        }

    }
}
