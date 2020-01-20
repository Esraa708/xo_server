package XOServer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class XOServer {
    ServerSocket server;
    public XOServer() {
        try {
            server = new ServerSocket(5005);
            while (true) {
                Socket s = server.accept();
                new GameHandler(s);
            }
        } catch (IOException ex) {
            Logger.getLogger(XOServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static void main(String[] args) {
        new XOServer();
    }
}

class GameHandler extends Thread {
    DataInputStream dis;
    PrintStream ps;
    String request;
    PreparedStatement pst;
    Connection con;
    String checkUser;
    String listvALS="";
    static Vector<GameHandler> clientsVector = new Vector<GameHandler>();
    static ArrayList<String> onlineUsers = new ArrayList<String>();
    public GameHandler(Socket socket) {
        try {
            dis = new DataInputStream(socket.getInputStream());
            ps = new PrintStream(socket.getOutputStream());
            clientsVector.add(this);
            start();
        } catch (IOException ex) {
            Logger.getLogger(GameHandler.class.getName()).log(Level.SEVERE, null, ex);

        }
    }
    public void run() {
        while (true) {
            try {
                String str;
                str = dis.readLine();
                String[] request = str.split("[.]");
                if (request[0].equals("login")){
                    try {
                        con = DriverManager.getConnection(
                                "jdbc:mysql://127.0.0.1:3306/XO", "root", "");
                        pst = con.prepareStatement("select * from players where name = ? AND password=?");
                        pst.setString(1, request[1]);
                        pst.setString(2, request[2]);
                        ResultSet res = pst.executeQuery();
                        if (res.next()) {
                            reply("valid");
                            onlineUsers.add(request[1]);
                            System.out.println(onlineUsers.get(0));
                            
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(GameHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else if (request[0].equals("register")) {
                    try {
                        con = DriverManager.getConnection(
                                "jdbc:mysql://127.0.0.1:3306/XO", "root", "ITI@0s");
                        pst = con.prepareStatement("INSERT INTO players (name,password) VALUES(?,?)");
                        pst.setString(1, request[1]);
                        pst.setString(2, request[2]);
                        pst.executeUpdate();
                    } catch (SQLException ex) {
                        Logger.getLogger(GameHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }else if (request[0].equals("refresh")) {
                    for(int i=0;i<onlineUsers.size();i++){
                        listvALS +=onlineUsers.get(i)+".";          
                    }
                    reply("active"+"."+listvALS);
                    System.out.println(listvALS);
                }
            } catch (IOException ex) {
                clientsVector.remove(this);
                break;
            }
        }
    }

    void reply(String msg)
    {
        System.out.println("The Msg = "+msg);
        for(GameHandler ch : clientsVector) {
            ch.ps.println(msg);
        }
    }
}
