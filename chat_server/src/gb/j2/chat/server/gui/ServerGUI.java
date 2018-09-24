package gb.j2.chat.server.gui;

import gb.j2.chat.server.core.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ServerGUI extends JFrame implements ActionListener, ChatServerListener, Thread.UncaughtExceptionHandler {
    private final int POS_X = 500;
    private final int POS_Y = 300;
    private final int WIDTH = 600;
    private final int HEIGHT = 400;

    private final ChatServer CHATSERVER = new ChatServer(this);
    private final JButton BTNSTART = new JButton("Start");
    private final JButton BTNSTOP = new JButton("Stop");
    private final JPanel PANEL = new JPanel();
    private final JPanel PANEL_BTN = new JPanel();
    private final JTextArea LOG = new JTextArea();

    public static void main (String[] args){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ServerGUI();
            }
        });
    }

    private ServerGUI() {
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(POS_X, POS_Y, WIDTH, HEIGHT);
        setResizable(false);
        setTitle("Chat server");
        setAlwaysOnTop(true);
        BTNSTART.addActionListener(this);
        BTNSTOP.addActionListener(this);
        //
        //setLayout(new GridLayout(1,2));
        PANEL_BTN.setLayout(new GridLayout(1,2));
        PANEL_BTN.add(BTNSTART);
        PANEL_BTN.add(BTNSTOP);
        PANEL.setLayout(new BorderLayout());
        PANEL.add(PANEL_BTN,BorderLayout.NORTH);
        LOG.setLineWrap(true);
        LOG.setCaretPosition(LOG.getDocument().getLength());
        PANEL.add(LOG,BorderLayout.SOUTH);
        add(PANEL);

        //add(BTNSTART);
        //add(BTNSTOP);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e){
        Object src = e.getSource();
        if (src == BTNSTART) {
            CHATSERVER.start(8189);
        //    throw new ArithmeticException();
        }else if (src == BTNSTOP) {
            CHATSERVER.stop();
            //System.exit(0);
        }

    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        String msg = "Что то пошло не так";
        LOG.append(msg + ": " + e.toString());
        //e.printStackTrace();
        //JOptionPane.showMessageDialog(this, msg, "Alert", JOptionPane.ERROR_MESSAGE);
        //System.exit(1);
    }

    @Override
    public void onChatServerMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            LOG.append(msg + "\n");
            LOG.setCaretPosition(LOG.getDocument().getLength());
        });
    }
}
