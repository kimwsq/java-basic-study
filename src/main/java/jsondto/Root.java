package jsondto;

// Root.java
import java.util.List;

public class Root {
    private Meta meta;
    private User user;
    private List<Task> tasks;

    public Meta getMeta() { return meta; }
    public User getUser() { return user; }
    public List<Task> getTasks() { return tasks; }
}