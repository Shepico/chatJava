package gb.j2.chat.client;

import gb.j2.chat.library.Messages;
import gb.j2.network.SocketThread;
import gb.j2.network.SocketThreadListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class ClientGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler, SocketThreadListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 500;

    private final JTextArea log = new JTextArea();
    private final JPanel panelTop = new JPanel(new GridLayout(2, 3));
    private final JTextField tfIPAddress = new JTextField("127.0.0.1");
    private final JTextField tfPort = new JTextField("8189");
    private final JCheckBox cbAlwaysOnTop = new JCheckBox("Alwayson top");
    private final JTextField tfLogin = new JTextField("Pavel");
    private final JPasswordField tfPassword = new JPasswordField("123");
    private final JButton btnLogin = new JButton("Login");

    private final JPanel panelBottom = new JPanel(new BorderLayout());
    private final JButton btnDisconnect = new JButton("<html><b>Disconnect</b></html>");
    private final JTextField tfMessage = new JTextField();
    private final JButton btnSend = new JButton("Send");

    private final JList<String> userList = new JList<>();
    private boolean shownIoErrors = false;
    private SocketThread socketThread;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientGUI();
            }
        });
    }

    private ClientGUI() {
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setSize(WIDTH, HEIGHT);
        setTitle("Chat Client");

        log.setEditable(false);
        log.setLineWrap(true);
        JScrollPane scrollLog = new JScrollPane(log);
        JScrollPane scrollUsers = new JScrollPane(userList);
        String[] users = {"user1_with_an_exceptionally_long_nickname", "user2", "user3", "user4", "user5", "user6", "user7", "user8", "user9", "user10"};
        userList.setListData(users);
        scrollUsers.setPreferredSize(new Dimension(100, 0));
        cbAlwaysOnTop.addActionListener(this);
        btnLogin.addActionListener(this);
        btnDisconnect.addActionListener(this); //lesson6
        btnSend.addActionListener(this);

        tfMessage.addActionListener(this);

        panelTop.add(tfIPAddress);
        panelTop.add(tfPort);
        panelTop.add(cbAlwaysOnTop);
        panelTop.add(tfLogin);
        panelTop.add(tfPassword);
        panelTop.add(btnLogin);
        panelBottom.add(btnDisconnect, BorderLayout.WEST);
        panelBottom.add(tfMessage, BorderLayout.CENTER);
        panelBottom.add(btnSend, BorderLayout.EAST);

        add(panelTop, BorderLayout.NORTH);
        add(scrollLog, BorderLayout.CENTER);
        add(panelBottom, BorderLayout.SOUTH);
        add(scrollUsers, BorderLayout.EAST);
        visiblePanels(false); // lesson6
        setVisible(true);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        String msg;
        StackTraceElement[] ste = e.getStackTrace();
        if (ste.length == 0)
            msg = "Empty Stacktrace";
        else {
            msg = e.getClass().getCanonicalName() + ": " + e.getMessage() +
                    "\n\t at " + ste[0];
        }
        JOptionPane.showMessageDialog(null, msg, "Exception", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == cbAlwaysOnTop) {
            setAlwaysOnTop(cbAlwaysOnTop.isSelected());
        } else if (src == btnLogin || src == tfIPAddress || src == tfLogin || src == tfPassword || src == tfPort) {
            connect();
        }else if (src == btnDisconnect) {
            disconnect();
        }else if (src == btnSend || src == tfMessage) {
            sendMessage();
        } else {
            throw new RuntimeException("Unknown source: " + src);
        }
    }

    private void connect() {
        Socket socket = null;
        try {
            socket = new Socket(tfIPAddress.getText(), Integer.parseInt(tfPort.getText()));
        } catch (IOException e) {
            log.append("Exception: " + e.getMessage());
        }
        socketThread = new SocketThread(this, "Client thread", socket);

    }

    /*
    * lesson6
    * */

    private void disconnect() {
        socketThread.close();

    }

    private void visiblePanels(boolean flagConnect){
        panelTop.setVisible(!flagConnect);
        panelBottom.setVisible(flagConnect);
    }
    /*
    * end lesson6*/


    void sendMessage() {
        String msg = tfMessage.getText();
        String username = tfLogin.getText();
        if ("".equals(msg)) return;
        tfMessage.setText(null);
        tfMessage.requestFocusInWindow();
        //putLog(String.format("%s", msg));
//        wrtMsgToLogFile(msg, username);
        //socketThread.sendString(username + ":" + msg);
        socketThread.sendString(msg);
    }

    private void wrtMsgToLogFile(String msg, String username) {
        try (FileWriter out = new FileWriter("log.txt", true)) {
            out.write(username + ": " + msg + "\n");
            out.flush();
        } catch (IOException e) {
            if (!shownIoErrors) {
                shownIoErrors = true;
                JOptionPane.showMessageDialog(this, "File write error", "Exception", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void putLog(String msg) {
        if ("".equals(msg)) return;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(msg + "\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }

    /**
     * Socket Thread Events
     * */

    @Override
    public void onStartSocketThread(SocketThread thread, Socket socket) {
        putLog("socket thread start");

    }

    @Override
    public void onStopSocketThread(SocketThread thread) {
        putLog("socket thread stop");
        visiblePanels(false);
    }

    @Override
    public void onReceiveString(SocketThread thread, Socket socket, String msg) {

        //putLog(msg);
        String[] arr = msg.split(Messages.DELIMITER);
        switch (arr[0]){
            case Messages.AUTH_ACCEPT:{
                putLog("Вы авторизованы " + arr[1]);
                break;
            }
            case Messages.BROADCAST: {
                //putLog("От:" + arr[2] + " всем: " + arr[3]);
                putLog(arr[3]);
                break;
            }

            case Messages.AUTH_ERROR: {
                putLog(String.format("Не верное имя/пароль"));
                break;
            }
            case Messages.USER_LIST: {
                String users = msg.substring(Messages.USER_LIST.length() + Messages.DELIMITER.length());
                String[] userArray = users.split(Messages.DELIMITER);
                Arrays.sort(userArray);
                userList.setListData(userArray);
                break;
            }

        }
       /* if (arr.length != 3 || !arr[0].equals(Messages.AUTH_REQUEST)) {
            newClient.msgFormatError(msg);
            return;
        }
        if ()*/
    }

    @Override
    public void onSocketThreadIsReady(SocketThread thread, Socket socket) {
        visiblePanels(true);
        String login = tfLogin.getText();
        String password = new String(tfPassword.getPassword());
        thread.sendString(Messages.getAuthRequest(login, password));
    }

    @Override
    public void onSocketThreadException(SocketThread thread, Exception e) {

        putLog("socket thread exception");
    }

}
