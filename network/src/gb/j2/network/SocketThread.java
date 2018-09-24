package gb.j2.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SocketThread extends Thread {
    private final SocketThreadListener LISTENER;
    private Socket socket;
    private DataOutputStream out;

    public SocketThread(SocketThreadListener listener, String name, Socket socket) {
        super(name);
        this.LISTENER = listener;
        this.socket = socket;
        start();
    }

    @Override
    public void run() {
        try {
            LISTENER.onStartSocketThread(this, socket);
            DataInputStream in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            LISTENER.onSocketThreadIsReady(this, socket);
            while (!isInterrupted()) {
                String msg = in.readUTF();
                LISTENER.onReceiveString(this, socket, msg);
            }
        } catch (IOException e) {
            LISTENER.onSocketThreadException(this, e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                LISTENER.onSocketThreadException(this, e);
            }
            LISTENER.onStopSocketThread(this);
        }

    }

    public boolean sendString(String msg) {
        try {
            out.writeUTF(msg);
            out.flush(); //финализирует выходное состояние, очищая все буферы вывода
            return true;
        } catch (IOException e) {
            LISTENER.onSocketThreadException(this, e);
            close();
            return false;
        }
    }

    public void close() {
        interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            LISTENER.onSocketThreadException(this, e);
        }
    }
}
