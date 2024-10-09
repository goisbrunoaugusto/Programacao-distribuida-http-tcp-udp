package udp;

import database.DatabasePersistance;

import java.net.DatagramPacket;
import java.sql.SQLException;
import java.util.logging.Logger;

public class MessageHandler implements Runnable{
    private static final Logger logger = Logger.getLogger(MessageHandler.class.getName());
    private final DatagramPacket packet;
    private final DatabasePersistance db = new DatabasePersistance();

    public MessageHandler(DatagramPacket packet) throws SQLException {
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
                db.postMessage(String.valueOf(messageBody));
                logger.info("#### Corpo da mensagem ##### " + messageBody.toString());

            } else {
                logger.info("Recebida uma mensagem desconhecida.");
            }
        } else {
            logger.info("Mensagem vazia recebida.");
        }

    }
}
