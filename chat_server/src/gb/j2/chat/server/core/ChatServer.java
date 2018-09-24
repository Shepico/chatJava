package gb.j2.chat.server.core;

import gb.j2.chat.library.Messages;
import gb.j2.network.ServerSocketThread;
import gb.j2.network.ServerSocketThreadListener;
import gb.j2.network.SocketThread;
import gb.j2.network.SocketThreadListener;

import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Vector;

public class ChatServer implements ServerSocketThreadListener, SocketThreadListener{

    private ServerSocketThread server;
    private final DateFormat DATEFORMAT = new SimpleDateFormat("HH:mm:ss: ");
    private Vector<SocketThread> clients = new Vector<>(); //lesson6
    private ChatServerListener listener;

    public ChatServer(ChatServerListener listener){
        this.listener =listener;
    }

    public void start(int port){
        if (server !=null && server.isAlive()) {
            putLog("Server is running");
        }else{
            server = new ServerSocketThread(this,"Chat server", port, 3000);
        }

    }

    public void stop(){
        if (server == null || !server.isAlive()){
            putLog("Server is not running");
        }else {
            server.interrupt();
        }
    }

    private void putLog(String msg){
        msg = DATEFORMAT.format(System.currentTimeMillis()) + Thread.currentThread().getName() + ":" + msg;
        //System.out.println(msg);
        listener.onChatServerMessage(msg);
    }
    /**
     * Server socket thread events
     * **/
    @Override
    public void onServerThreadStart(ServerSocketThread thread) {
        putLog("server thread start");
        SqlClient.connect();
    }

    @Override
    public void onServerThreadStop(ServerSocketThread thread) {
        putLog("server thread stop");
        SqlClient.disconnect();
    }

    @Override
    public void onSocketAccepted(ServerSocketThread thread, Socket socket) {
        putLog("Socket accepted");
        String name = "SocketThread" + ":" + socket.getInetAddress() + socket.getPort();
        new ClientThread(this, name, socket);
    }

    @Override
    public void onAcceptTimeout(ServerSocketThread thread, ServerSocket server) {
        putLog("Accept timeout");
    }

    @Override
    public void onServerSockedCreated(ServerSocketThread thread, ServerSocket server) {
        putLog("Server socket created");
    }

    @Override
    public void onServerThreadException(ServerSocketThread thread, Exception e) {
        putLog("Server thread exception");
    }

    /*
     * Socket thred events
     * */

    @Override
    public synchronized void onStartSocketThread(SocketThread thread, Socket socket) {
        putLog("Start socket thread");

    }

    @Override
    public synchronized void onStopSocketThread(SocketThread thread) {
        ClientThread client = (ClientThread) thread;
        clients.remove(thread);
        if (client.isAuthorized() && !client.isReconnect()) {
            sendToAuthorizedClients(Messages.getBroadcast("Server", client.getNickname() + " disconnected"));
            sendToAuthorizedClients(Messages.getUserList(getUsers()));
        }
    }

    @Override
    public synchronized void onSocketThreadIsReady(SocketThread thread, Socket socket) {
        putLog("Socket thread is ready");
        clients.add(thread);
    }

    @Override
    public synchronized void onReceiveString(SocketThread thread, Socket socket, String msg) {
        ClientThread client = (ClientThread) thread;
        if (client.isAuthorized()) {
            handleAuthMsg(client, msg);
        } else {
            handleNonAuthMsg(client, msg);
        }
    }

    @Override
    public synchronized void onSocketThreadException(SocketThread thread, Exception e) {
        putLog("Socket thread exception");
    }

    private String getUsers() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < clients.size(); i++) {
            ClientThread client = (ClientThread) clients.get(i);
            if (!client.isAuthorized()) continue;
            sb.append(client.getNickname()).append(Messages.DELIMITER);
        }
        return sb.toString();
    }

    private void handleAuthMsg(ClientThread thread, String msg) {
        sendToAuthorizedClients(Messages.getBroadcast(thread.getName(), msg));
    }

    private void handleNonAuthMsg(ClientThread newClient, String msg) {
        String[] arr = msg.split(Messages.DELIMITER);
        if (arr.length != 3 || !arr[0].equals(Messages.AUTH_REQUEST)) {
            newClient.msgFormatError(msg);
            return;
        }
        String login = arr[1];
        String password = arr[2];
        String nickname = SqlClient.getNickname(login, password);
        if (nickname == null) {
            putLog(String.format("Invalid login/password: login='%s', password='%s'", login, password));
            newClient.authError();
        } else {
            ClientThread client = findClientThreadByNickname(nickname);
            newClient.authAccept(nickname);
            if (client == null)
                sendToAuthorizedClients(Messages.getBroadcast("Server", nickname + " connected"));
            else {
                client.reconnect();
                clients.remove(client);
            }
        }
        sendToAuthorizedClients(Messages.getUserList(getUsers()));
    }

    private void sendToAuthorizedClients(String msg) {
        for (int i = 0; i < clients.size(); i++) {
            ClientThread client = (ClientThread) clients.get(i);
            if (!client.isAuthorized()) continue;
            client.sendString(msg);
        }
    }

    private synchronized ClientThread findClientThreadByNickname(String nickname) {
        for (int i = 0; i < clients.size(); i++) {
            ClientThread client = (ClientThread) clients.get(i);
            if (!client.isAuthorized()) continue;
            if (client.getNickname().equals(nickname))
                return client;
        }
        return null;
    }

}
