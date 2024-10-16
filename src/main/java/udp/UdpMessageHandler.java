package udp;

import com.google.gson.Gson;
import database.DatabasePersistance;

import java.net.DatagramPacket;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

public class UdpMessageHandler implements Callable<DatagramPacket> {
    private static final Logger logger = Logger.getLogger(UdpMessageHandler.class.getName());
    private final DatagramPacket packet;
    private final DatabasePersistance db = new DatabasePersistance();
    private static final Gson gson = new Gson();

    public UdpMessageHandler(DatagramPacket packet) throws SQLException {
        this.packet = packet;
    }

    @Override
    public DatagramPacket call()   {
        logger.info("Entrou no MessageHandler");
        String receivedMessage = new String(packet.getData(), 0, packet.getLength());
        String[] lines = receivedMessage.split("\n");
//        for(String linhas : lines){
//            System.out.println(linhas);
//        }

        if (lines.length > 1) {
            String firstLine = lines[0].trim();
            String secondLine = lines[1].trim();

            if (firstLine.equalsIgnoreCase("mensagem do servidor")) {
                logger.info("Recebida uma mensagem do servidor.");
            } else if (firstLine.equalsIgnoreCase("mensagem do cliente")) {

                if(secondLine.equalsIgnoreCase("POST")){
                    StringBuilder messageBody = new StringBuilder();
                    for (int i = 2; i < lines.length; i++) {
                        messageBody.append(lines[i]).append("\n");
                    }
                    db.postMessage(String.valueOf(messageBody));

                    logger.info("#### Corpo da mensagem ##### " + messageBody.toString());

                    String response = "Mensagem enviada com sucesso";
                    return new DatagramPacket(response.getBytes(), response.getBytes().length);

                } else if (secondLine.equalsIgnoreCase("GET")) {

                    List<String> responseBody = db.getMessages();
                    String jsonResponse = gson.toJson(responseBody);
                    logger.info(jsonResponse.getBytes().toString());

                    return new DatagramPacket(jsonResponse.getBytes(), jsonResponse.length());
                }

            } else {
                logger.info("Recebida uma mensagem desconhecida.");
            }
        } else {
            logger.info("Mensagem vazia recebida.");
        }
        return null;
    };
}
