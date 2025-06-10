package wordageddon.model;

public class User {

    private int id;
    private String username;
    private String fname;
    private String lname;
    private String password;
    private String email;
    private Boolean isAdmin;

    // Constructor with ID (for database retrieval)
    public User(int id, String username, String fname, String lname, String password, String email, Boolean isAdmin) {
        this.id = id;
        this.username = username;
        this.fname = fname;
        this.lname = lname;
        this.password = password;
        this.email = email;
        this.isAdmin = isAdmin;
    }

    // Constructor without ID (for creation)
    public User(String username, String fname, String lname, String password, String email, Boolean isAdmin) {
        this.id = -1; // Will be set by database
        this.username = username;
        this.fname = fname;
        this.lname = lname;
        this.password = password;
        this.email = email;
        this.isAdmin = isAdmin;
    }

    // setters and getters
    public int getId() {
        return id;
    }
    
    public String getUsername() {
        return username;
    }

    public String getFname() {
        return fname;
    }

    public String getLname() {
        return lname;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(this.isAdmin ? "Admin: " : "Utente: ");

        sb.append(this.username)
            .append(" (")
            .append(this.fname + " " + this.lname)
            .append(") - Email: ")
            .append(this.email);

        return sb.toString();
    }

}
