package sample;


import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    private String url = "http://music.163.com/song/media/outer/url?id=34923851.mp3";
    private String ipserverip = "127.0.0.1";
    private int serverport = 30000;
    private boolean isInit;         //是否是初始化状态
    private boolean isPlay;         //播放器状态
    private List<String> playlist;  //播放表
    private int crtpoint;           //当前播放歌曲的序号
    private float crtplaytime = 0;  //当前播放歌曲的时间
    private int crtvolumn = 50;          //当前的音量
    private int crtplaystaus = 0;   //当前的播放方式
    private String crtplaypath = "http://music.163.com/song/media/outer/url?id=34923851.mp3";     //当前歌曲的路径
    private String crtimagepath = "http://p1.music.126.net/vkoQqphGwk6TyRFai3ZBdw==/3238061743857732.jpg";       //当前歌曲图片路径
    private String crtsongname = "世末歌者";
    private JSONObject mainroot;
    private JSONArray mainlocalsonglist;
    private JSONArray mainplaylist;

    private Song[] songplaylist;
    private Song[] songlocallist;
    private Song[] songsearchlist;

    @FXML private ListView listview;
    @FXML private TableView tableview;
    @FXML private TextField search;
    @FXML private ImageView songimage;
    @FXML private Label songname;
    @FXML private Label songcrttime;
    @FXML private TableColumn songcol;
    @FXML private TableColumn songercol;
    @FXML private Media media;
    @FXML private MediaPlayer mediaplayer;
    @FXML private MediaView mediaview;
    @FXML private Slider volumnslider;
    @FXML private Rectangle volumnrectangle;
    @FXML private Slider songslider;
    @FXML private Rectangle songrectangle;
    @FXML private ImageView music_status;
    @Override
    public void initialize(URL url, ResourceBundle rb){
        //writeJsonData();
        System.out.println("initialize");
        //writeJsonData();
        profileInit();  //初始化配置
        listInit();     //初始化listview、tableview列表
        musicInit();    //初始化歌曲播放器
    }
    public static void saveData(){

        System.out.println("C_stop");
    }
    public void setSignIn()throws Exception{
        Stage signinstage = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("signin.fxml"));
        signinstage.setTitle("米瓜音乐");
        signinstage.getIcons().add(new Image("/image/icon.jpg"));
        signinstage.setResizable(false);
        signinstage.setScene(new Scene(root, 300, 150));
        signinstage.show();
    }
    /*
    * 音乐搜索功能模块
    * 先向服务器发送一个字符串
    * 然后接收服务器发送的json数据
     */
    public void search(){
        Thread clientthread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(ipserverip,serverport);
                    //发送信息给服务器
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    dataOutputStream.writeUTF(search.getText());
                    //从服务器接收数据
                    InputStream inputStream = socket.getInputStream();
                    DataInputStream dataInputStream = new DataInputStream(inputStream);
                    String s = dataInputStream.readUTF();
                    System.out.println("服务器反馈的歌曲信息："+ s );
                    //用网络传递数据过来
                    JSONObject myroot = new JSONObject(s);
                    JSONArray testjsary = myroot.getJSONArray("songsearchlist");
                    songsearchlist = new Song[testjsary.length()];
                    getSongList(songsearchlist,testjsary);
                    updateRightList(songsearchlist);

                    socket.close();
                }catch (IOException e){
                    e.printStackTrace();
                }

            }
        });
        clientthread.start();

    }
    /*
    *   获取配置文件
    *   初始化各种配置 -------------------------------------------------------------------------------------------------
     */
    public void profileInit(){
        isInit = true;
        String jsdata = readJsonData();

        //获取json数据
        mainroot = new JSONObject(jsdata);
        mainlocalsonglist = mainroot.getJSONArray("localsonglist");
        mainplaylist = mainroot.getJSONArray("playlist");

        songplaylist = new Song[mainplaylist.length()];
        getSongList(songplaylist,mainplaylist);
        songlocallist = new Song[mainlocalsonglist.length()];
        getSongList(songlocallist,mainlocalsonglist);
        //设置参数
        crtpoint = mainroot.getInt("crtpoint");
        //crtplaytime = mainroot.getFloat("crtplaytime");
        crtvolumn = mainroot.getInt("crtvolumn");
        crtplaypath = mainroot.getString("crtplaypath");
        crtplaystaus = mainroot.getInt("crtplaystatus");

        songslider.setValue(crtplaytime);   //设置播放起点
        volumnslider.setValue(crtvolumn);   //设置音量
    }

    /*
    *   初始化歌曲列表的listview\tableview  -----------------------------------------------------------------------------
     */
    public void listInit(){

        updateLeftList();
        //设置listview的双击事件

        updateRightList(songlocallist);
        //设置tableview的双击事件
        tableview.setRowFactory( tv -> {
            TableRow<Song> row = new TableRow<Song>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    Song song = row.getItem();
                    Song[] songlist = new Song[2];
                    songlist[0] = song;
                    //播放音乐
                    mediaplayer.dispose();
                    setPlayer(songlist,0);
                    mediaplayer.play();
                    System.out.println(song.getSongname());
                }
            });
            return row ;
        });
    }
    //将json数据转换为歌曲实例表
    public void getSongList(Song[] songlist,JSONArray jsary){
        JSONObject jsobj;
        for(int i = 0;i < jsary.length();i++){
            jsobj = jsary.getJSONObject(i);
            songlist[i] = new Song(jsobj.getInt("id"),jsobj.getString("songname"),jsobj.getString("songer"),
                    jsobj.getString("imagepath"),jsobj.getString("songpath"));
        }
    }
    //更新左侧列表
    public void updateLeftList(){
        ObservableList<String> l_list = FXCollections.observableArrayList();
        l_list.add("中文歌曲");
        l_list.add("欧美歌曲");
        listview.setItems(l_list);
    }
    //更新右侧列表
    public void updateRightList(Song[] songlist){
        ObservableList<Song> list = FXCollections.observableArrayList();
        for(int i=0;i<songlist.length;i++){  //长度可能有问题---------------------
            list.add(songlist[i]);
        }

        songcol.setCellValueFactory(new PropertyValueFactory("songname"));  //绑定
        songercol.setCellValueFactory(new PropertyValueFactory("songername"));
        tableview.setItems(list);
        System.out.println("更新右边list成功");
    }

    /**
     * 音乐播放器部分
     * 初始化播放列表
     * 初始化播放器及当前播放音乐
     * 初始化音乐的各种属性   ----------------------------------------------------------------------------------------
     */
    public void musicInit(){
        playlist = new ArrayList<String>();
        //playlist.add(url);
        for(int i = 0;i< songplaylist.length;i++){
            playlist.add(songplaylist[i].getPath());
        }
        setPlayer(songlocallist,crtpoint);
    }

    public void playMusic(){    //播放、暂停音乐
        if(isPlay == false) {
            mediaplayer.play();
            isPlay = true;
        }
        else {
            mediaplayer.pause();
            isPlay = false;
        }
    }
    public void setMusicPlayStatus(){ //设置播放方式（单曲循环等）
        if(isInit == false)
            crtplaystaus = (crtplaystaus + 1) % 3;
        else
            isInit = false;
        switch (crtplaystaus){
            case 0:
                music_status.setImage(new Image("image/status0.jpg"));
                break;
            case 1:
                music_status.setImage(new Image("image/status1.jpg"));
                break;
            case 2:
                music_status.setImage(new Image("image/status2.jpg"));
                break;
        }

    }
    public void lastMusic(){    //上一首歌
        mediaplayer.dispose();
        isInit = true;
        if(crtplaystaus < 2) {   //非随机播放
            if(crtpoint == 0){
                crtpoint = playlist.size()-1;
                setPlayer(songlocallist,crtpoint);
                mediaplayer.play();
            }else {
                crtpoint--;
                setPlayer(songlocallist,crtpoint);
                mediaplayer.play();
            }
        }else {     //随机播放
            crtpoint = ((int)(Math.random()*100000)) % playlist.size();
            setPlayer(songlocallist,crtpoint);
            mediaplayer.play();
        }

    }
    public void nextMusic(){    //下一首歌
        mediaplayer.dispose();
        isInit = true;
        //到达末尾
        if(crtplaystaus < 2){   //非随机播放
            if(crtpoint == playlist.size()-1){
                crtpoint = 0;
                setPlayer(songlocallist,crtpoint);
                mediaplayer.play();
            }else {
                crtpoint++;
                setPlayer(songlocallist,crtpoint);
                mediaplayer.play();
            }
        }else {     //随机播放
            crtpoint = ((int)(Math.random()*100000)) % playlist.size();
            setPlayer(songlocallist,crtpoint);
            mediaplayer.play();
        }
    }
    public void binVolumn(){    //静音
        volumnslider.setValue(0);

    }

    private void setPlayer(Song[] songlist,int point){  //设置播放歌曲的序号

        crtpoint = point;
        crtplaypath = songlist[point].getPath();
        crtsongname = songlist[point].getSongname();
        crtimagepath = songlist[point].getImagePath();


        media = new Media(crtplaypath);
        mediaplayer = new MediaPlayer(media);
        mediaview = new MediaView(mediaplayer);

        setPlayTimeRate();
        setMusicPlayStatus();
        //
        songname.setText(crtsongname);
        songimage.setImage(new Image(crtimagepath));
        //
        isInit = false;
        isPlay = false;
        mediaplayer.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                if(crtplaystaus == 0)
                    mediaplayer.seek(mediaplayer.getStartTime());
                else
                    nextMusic();
            }
        });
        //绑定时长属性
        mediaplayer.currentTimeProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                songslider.setValue(mediaplayer.getCurrentTime().toSeconds()/mediaplayer.getStopTime().toSeconds()*100.0);
                setPlayTimeRate();
            }
        });
        songrectangle.widthProperty().bind(songslider.valueProperty().multiply(10) );
        songrectangle.heightProperty().bind(songslider.heightProperty().subtract(7));
        songslider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if(songslider.isValueChanging()){
                    mediaplayer.seek(mediaplayer.getMedia().getDuration().multiply(songslider.getValue()/100.0));
                }
            }
        });
        //绑定音量属性
        mediaplayer.volumeProperty().bind(volumnslider.valueProperty().divide(100));
        volumnrectangle.widthProperty().bind(volumnslider.valueProperty());
        volumnrectangle.heightProperty().bind(volumnslider.heightProperty().subtract(7));
    }

    private void setPlayTimeRate(){
        int time = (int)mediaplayer.getCurrentTime().toSeconds();
        int htime =(int) mediaplayer.getStopTime().toSeconds();
        songcrttime.setText(new DecimalFormat("00").format(time/60) + ":" + new DecimalFormat("00").format(time%60)
                + " / " + new DecimalFormat("00").format(htime/60) + ":" + new DecimalFormat("00").format(htime%60));
    }
    /*
    *   写json数据到配置文件里   --------------------------------------------------------------------------------------
     */
    private void writeJsonData(){
        try {
            File file = new File("src/json/initfile.json");
            FileOutputStream os = new FileOutputStream(file);
            JSONObject root = new JSONObject();
            JSONArray localsongary = new JSONArray();
            JSONArray playlistary = new JSONArray();

            JSONObject song = new JSONObject();
            song.put("id","1");
            song.put("songname","世末歌者");
            song.put("songer","洛天依");
            song.put("imagepath","http://p1.music.126.net/vkoQqphGwk6TyRFai3ZBdw==/3238061743857732.jpg");
            song.put("songpath","http://music.163.com/song/media/outer/url?id=34923851.mp3");
            localsongary.put(song);
            playlistary.put(song);

            JSONObject song1 = new JSONObject();
            song1.put("id","2");
            song1.put("songname","认真的雪");
            song1.put("songer","范春飞");
            song1.put("imagepath",getClass().getResource("../image/认真的雪.jpg").toString());
            song1.put("songpath",getClass().getResource("../music/认真的雪.mp3").toString());
            localsongary.put(song1);
            playlistary.put(song1);

            JSONObject song2 = new JSONObject();
            song2.put("id","3");
            song2.put("songname","世末歌者");
            song2.put("songer","双笙");
            song2.put("imagepath",getClass().getResource("../image/世末歌者.jpg").toString());
            song2.put("songpath",getClass().getResource("../music/世末歌者.mp3").toString());
            localsongary.put(song2);
            playlistary.put(song2);

            JSONObject song3 = new JSONObject();
            song3.put("id","4");
            song3.put("songname","如果是你");
            song3.put("songer","小玉");
            song3.put("imagepath",getClass().getResource("../image/如果是你.jpg").toString());
            song3.put("songpath",getClass().getResource("../music/如果是你.mp3").toString());
            localsongary.put(song3);
            playlistary.put(song3);

            root.put("crtpoint",crtpoint);
            root.put("crtplaytime",crtplaytime);
            root.put("crtvolumn",crtvolumn);
            root.put("crtplaystatus",crtplaystaus);
            root.put("crtplaypath",crtplaypath);
            root.put("localsonglist",localsongary);
            root.put("playlist",playlistary);
            //root.put("playlist",playlistary);
            os.write(root.toString().getBytes());
            os.close();
            System.out.println("文件写入成功");
        }catch (Exception e){
            System.out.println("文件操作出现问题");
        }
    }
    /*
    *   从配置文件里读数据
     */
    public String readJsonData() {
        String encoding = "UTF-8";
        File file = new File("src/json/initfile.json");
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (Exception e) {
            System.out.println("读文件出错");
        }
        try {
            return new String(filecontent, encoding);
        }catch (Exception e){
            System.out.println("返回文件出错");
            return null;
        }
    }
}
