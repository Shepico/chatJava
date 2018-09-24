package gb.j2.network;

import java.net.ServerSocket;
import java.net.Socket;

public interface ServerSocketThreadListener {

    void onServerThreadStart(ServerSocketThread thread);
    void onServerThreadStop(ServerSocketThread thread);
    //
    void onSocketAccepted(ServerSocketThread thread, Socket socket);
    void onAcceptTimeout(ServerSocketThread thread, ServerSocket server);
    //
    void onServerSockedCreated(ServerSocketThread thread, ServerSocket server);
    void onServerThreadException(ServerSocketThread thread, Exception e);

}
