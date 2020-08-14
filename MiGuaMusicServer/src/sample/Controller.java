package sample;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.sql.*;


public class Controller {

    private ServerSocket serverSocket;
    private StringBuilder alloutcome = new StringBuilder();
    private static final String DBDRIVER = "com.mysql.cj.jdbc.Driver";									//驱动程序名
    private static final String DBURL = "jdbc:mysql://localhost:3306/db_miguamusic?serverTimezone=GMT%2B8&useUnicode=true&characterEncoding=utf-8";					//URL指向要访问的数据库名mydata
    private static final String DBUSER = "root";														//MySQL配置时的用户名
    private static final String DBPASSWORD = "123456";
    private Connection con;


    @FXML private Label output;
    public void startServer(){
        System.out.println("服务器启动");
        output.setText("服务器启动");

        Thread serverthread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    linkMySql();

                    serverSocket = new ServerSocket(30000);
                    while(true){
                        Socket socket = serverSocket.accept();
                        System.out.println("客户接入");
                        InputStream inputStream = socket.getInputStream();
                        DataInputStream dataInputStream = new DataInputStream(inputStream);
                        String userkey = dataInputStream.readUTF();
                        System.out.println("客户端发送的信息：" + userkey);

                        //根据用户发来的关键字，拼接SQL搜索语句，然后查询数据库

                        String sqlss = "select * from song_table where songname like '%" + userkey +"%' or songername like '%" + userkey +"%'";
                        //String sqlss = "select * from song_table where songname like '%世末歌者%'";
                        Platform.runLater(()->{
                            output.setText(sqlss);
                        });

                        //通过客户端发来的搜索关键字，连接数据库查找歌曲，然后反馈给客户端
                        //数据库操作
                        String dbrs,jsonrs;
                        dbrs = exeSqlStatement(sqlss);

                        //反馈数据
                        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                        dataOutputStream.writeUTF(dbrs);
                    }
                }catch (Exception e){
                    //
                }
            }
        });
        serverthread.start();
    }

    public void stopServer()throws IOException {
        serverSocket.close();
        output.setText("服务器关闭");
        System.out.println("服务器关闭！");
    }
    //连接数据库
    public void linkMySql() {
        try{
            Class.forName(DBDRIVER);
            con = DriverManager.getConnection(DBURL,DBUSER,DBPASSWORD);
            if(!con.isClosed())
                System.out.println("成功连接数据库!");

        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }catch (SQLException e){
            e.printStackTrace();
        }


    }
    //关闭数据库
    public void closeMySql(){
        if(con != null) {				//如果conn连接对象不为空
            try {
                con.close();			//关闭conn连接对象对象
                System.out.println("成功断开数据库连接!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    //执行SQL语句，返回操作结果的字符串,数据以JSON格式返回
    public String exeSqlStatement(String sqls){
        try{
            Statement statement = con.createStatement(); //创建statement类对象，用来执行SQL语句！！
            //statement.executeUpdate(sqls);
            ResultSet rs = statement.executeQuery(sqls);
            //将结果处理为json格式
            int id;
            String songname,songername,imagepath,songpath;
            JSONObject myroot = new JSONObject();
            JSONArray myary = new JSONArray();

            if(rs == null){
                return "no";
            }
            while(rs.next()){
                id = rs.getInt("songid");
                songname = rs.getString("songname");
                //获取stuid这列数据
                songername = rs.getString("songername");

                imagepath = rs.getString("imagepath");
                songpath = rs.getString("songpath");
                //输出结果
                JSONObject obj = new JSONObject();
                obj.put("id",id);
                obj.put("songname",songname);
                obj.put("songer",songername);
                obj.put("imagepath",imagepath);
                obj.put("songpath",songpath);
                myary.put(obj);
            }
            myroot.put("songsearchlist",myary);
            System.out.println(myroot.toString());
            rs.close();
            return myroot.toString();
        }catch (SQLException e){
            e.printStackTrace();
        }
        return "NO";
    }

}
