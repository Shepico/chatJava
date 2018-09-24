package gb.j2.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ServerSocketThread extends Thread {
    private final int PORT;
    private final int TIMEOUT;
    private final ServerSocketThreadListener LISTENER;

    public ServerSocketThread(ServerSocketThreadListener listener, String name, int port, int timeout) {
        super(name);
        this.PORT = port;
        this.TIMEOUT = timeout;
        this.LISTENER = listener;
        start();
    }

    @Override
    public void run() {
        //System.out.println("Thread started");
        LISTENER.onServerThreadStart(this);
       try (ServerSocket server = new ServerSocket(PORT)){
           //System.out.println("Server started");
           LISTENER.onServerSockedCreated(this, server);
           server.setSoTimeout(TIMEOUT);
           Socket socket;
            while (!isInterrupted()){
                try {
                    socket = server.accept();
                }catch (SocketTimeoutException e){
                    //e.printStackTrace();
                    //LISTENER.onServerThreadException(this, e); //Почему не этот
                    LISTENER.onAcceptTimeout(this, server);

                    continue;
                }
                //System.out.println("Client connected");
                LISTENER.onSocketAccepted(this, socket);
            }
       }catch (IOException e) {
           //e.printStackTrace();
           LISTENER.onServerThreadException(this, e);
       }finally {
           //System.out.println("Server stopped");
           LISTENER.onServerThreadStop(this);
       }
    }
}