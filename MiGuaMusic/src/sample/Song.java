package sample;
/*
*   歌类
*  通过歌曲路径得到MP3文件后，获取歌曲的基本信息
*  歌名、歌手、时长、图片
 */
public class Song {
    private int id;
    private String songname;
    private String songername;
    private String imagepath;
    private String path;

    public Song(){}

    public Song(int id,String songname,String songername,String imagepath,String path){
        this.id = id;
        this.songname = songname;
        this.songername = songername;
        this.imagepath = imagepath;
        this.path = path;
    }
    public void makeImagePath(){

    }

    public void setId(int id){
        this.id = id;
    }

    public void setSongname(String songname){
        this.songname = songname;
    }

    public void setSongername(String songername){
        this.songername = songername;
    }

    public void setImagePath(String imagepath){
        this.imagepath = imagepath;
    }

    public void setPath(String path){
        this.path = path;
    }

    public int getId(){
        return this.id;
    }

    public String getSongname(){
        return this.songname;
    }

    public String getSongername(){
        return this.songername;
    }

    public String getImagePath(){
        return this.imagepath;
    }

    public String getPath(){
        return this.path;
    }
}
