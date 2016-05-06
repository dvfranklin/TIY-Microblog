import java.util.ArrayList;

public class User {

    String userName;
    private String password;
    ArrayList<Message> messages = new ArrayList<>();

    public User(){

    }

    public User(String username) {
        this.userName = username;
    }

    public User(String username, String password){
        this.userName = username;
        this.password = password;
    }

    public String getUsername() {
        return userName;
    }

    public void setUsername(String username) {
        this.userName = username;
    }

    public String getPassword(){
        return this.password;
    }

    public void setPassword(String password){
        this.password = password;
    }
}
