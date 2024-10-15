package tcp;

import database.DatabasePersistance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpServerThread extends Thread{
    private final Socket socket;
    private final DatabasePersistance db = new DatabasePersistance();
    public TcpServerThread(Socket socket) throws SQLException {
        this.socket = socket;
    }

    public void run(){
        System.out.println(Thread.currentThread().getName());
        try(BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))){
            String firstLine;
            if ((firstLine = in.readLine()) != null){

                if(firstLine.equalsIgnoreCase("Mensagem do cliente")) {
                    System.out.println("Mensagem do cliente recebida");

                    String secondLine = in.readLine();

                    if(secondLine.equalsIgnoreCase("POST")){
                        System.out.println("Mensagem post identificada");
                        //Lendo a terceira linha
                        String message = in.readLine();
                            try {
                                postMessage(message);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                    }else if (secondLine.equalsIgnoreCase("GET")){
                        System.out.println("Mensagem get identificada");
                            try {
                                getMessages();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                    }else{
                        socket.getOutputStream().write("Método não permitido".getBytes());
                    }
                }
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        } finally {
            try {
                socket.close();
            } catch (IOException ex) {
                System.out.println("Erro ao fechar o socket: " + ex.getMessage());
            }
        }
    }

    private void getMessages() throws IOException {
        try{
            System.out.println("Buscando mensagens");
            List<String> messages = db.getMessages();
            socket.getOutputStream().write(messages.toString().getBytes());
        }catch (IOException e){
            socket.getOutputStream().write("Erro ao buscar mensagens".getBytes());
            System.out.println("Erro ao buscar mensagens: " + e.getMessage());
            throw new RuntimeException(e);
        }finally {
            socket.close();
        }
    }

    private void postMessage(String message) throws IOException {
        try{
            db.postMessage(message);
            socket.getOutputStream().write("Mensagem postada".getBytes());
        }catch (IOException e){
            socket.getOutputStream().write("Erro ao postar mensagem".getBytes());
            System.out.println("Erro ao postar mensagem: " + e.getMessage());
            throw new RuntimeException(e);
        }finally {
            socket.close();
        }
    }
}
