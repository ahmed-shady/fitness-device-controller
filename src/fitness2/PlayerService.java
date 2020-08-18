package fitness2;

/**
 * @author aboshady
 */
import java.sql.*;
import java.util.ArrayList;

public class PlayerService {

    private Connection c = null;


    public void initDB() {
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:fitness_db.db");
            c.setAutoCommit(true);
            createPlayerTable();
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Opened database successfully");

    }

    private void createPlayerTable() {

        try {

            try (Statement stmt = c.createStatement()) {
                String sql = "CREATE TABLE IF NOT EXISTS Player"
                        + "(ID          INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + " CODE        TEXT    NOT NULL UNIQUE, "
                        + " NAME        TEXT, "
                        + "TIMESTAMP    DATETIME DEFAULT CURRENT_TIMESTAMP, "
                        + " STAGE       TEXT    NOT NULL, "
                        + " HEIGHT      INTEGER, "
                        + " FLEXIBLE    REAL, "
                        + " PUSHUP      INTEGER, "
                        + " SITUP       INTEGER, "
                        + " RUN         INTEGER,"
                        + " JUMP        INTEGER)";
                stmt.executeUpdate(sql);
            }
            
 

        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Table created successfully");

    }

    public synchronized boolean save(Player player) {
        try {
            /*make sure no other player has the same code*/
            String code = player.getCode();
            String stage = player.getStage();
            String name = player.getName();
            /*INSERT QUERY MAKE SURE NO OTHER CODE IS HERE*/
            String query = "INSERT INTO Player(CODE, STAGE, name) VALUES(?,?,?)";
            PreparedStatement stmt = c.prepareStatement(query);
            stmt.setString(1, code);
            stmt.setString(2, stage);
            stmt.setString(3, name);
            
            int row_count = stmt.executeUpdate();
            if (row_count == 0) {
                return false;
            }
            return update(player);

        } catch (SQLException ex) {
            System.err.println(ex.getClass().getName() + ": " + ex.getMessage());
            return false;
        }

    }

    public synchronized boolean update(Player player) {
        try {
            String code = player.getCode();
            String stage = player.getStage();
            int height = player.getHeight();
            double flexible = player.getFlexible();
            int pushup = player.getPushup();
            int situp = player.getSitup();
            int run = player.getRun();
            int jump = player.getJump();

            String query = "UPDATE Player SET STAGE=?, HEIGHT=?, FLEXIBLE=?, "
                    + "PUSHUP=?, SITUP=?, RUN=?, "
                    + "JUMP=? "
                    + "WHERE CODE=?";
            PreparedStatement stmt = c.prepareStatement(query);
            stmt.setString(1, stage);
            stmt.setInt(2, height);
            stmt.setDouble(3, flexible);
            stmt.setInt(4, pushup);
            stmt.setInt(5, situp);
            stmt.setInt(6, run);
            stmt.setInt(7, jump);
            stmt.setString(8, code);
            
            int row_count = stmt.executeUpdate();
            return row_count != 0;
        } catch (SQLException ex) {
            System.err.println(ex.getClass().getName() + ": " + ex.getMessage());
            return false;
        }
    }

    public ArrayList<Player> getAll() {

        ArrayList<Player> players = new ArrayList<>();
        try (PreparedStatement stmt = c.prepareStatement("Select * from Player order by TIMESTAMP;")) {
            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String code = rs.getString("code");
                    String stage = rs.getString("stage");
                    String name = rs.getString("name");
                    
                    Player p = new Player(code, stage, name);

                    p.setFlexible(rs.getDouble("flexible"));
                    p.setHeight(rs.getInt("height"));
                    p.setPushup(rs.getInt("pushup"));
                    p.setSitup(rs.getInt("situp"));
                    p.setRun(rs.getInt("run"));
                    p.setJump(rs.getInt("jump"));
                    players.add(p);
                }
            }

        } catch (SQLException ex) {
            System.err.println(ex.getClass().getName() + ": " + ex.getMessage());

        }
        return players;
    }

    public Player get(String code) {

        try (PreparedStatement stmt = c.prepareStatement("Select * from Player WHERE CODE=?;")) {
            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    String stage = rs.getString("stage");
                    String name = rs.getString("name");
                    
                    Player player = new Player(code, stage, name);

                    player.setFlexible(rs.getDouble("flexible"));
                    player.setHeight(rs.getInt("height"));
                    player.setPushup(rs.getInt("pushup"));
                    player.setSitup(rs.getInt("situp"));
                    player.setRun(rs.getInt("run"));
                    player.setJump(rs.getInt("jump"));
                    return player;
                }

            }

        } catch (SQLException ex) {
            System.err.println(ex.getClass().getName() + ": " + ex.getMessage());

        }
        return null;

    }

    public ArrayList<Player> getByStage(String stage) {

        ArrayList<Player> players = new ArrayList<>();
        try (PreparedStatement stmt = c.prepareStatement("Select * from Player WHERE stage=? order by TIMESTAMP;")) {
            stmt.setString(1, stage);
            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String code = rs.getString("code");
                    String name = rs.getString("name");
                    
                    Player p = new Player(code, stage, name);

                    p.setFlexible(rs.getDouble("flexible"));
                    p.setHeight(rs.getInt("height"));
                    p.setPushup(rs.getInt("pushup"));
                    p.setSitup(rs.getInt("situp"));
                    p.setRun(rs.getInt("run"));
                    p.setJump(rs.getInt("jump"));
                    players.add(p);
                }
            }

        } catch (SQLException ex) {
            System.err.println(ex.getClass().getName() + ": " + ex.getMessage());

        }
        return players;
    }

    public ArrayList<Player> getActive() {
        String query = "SELECT * FROM Player "
                + "WHERE HEIGHT = -1 "
                + "OR    FLEXIBLE = -1 "
                + "OR    PUSHUP = -1 "
                + "OR    SITUP = -1 "
                + "OR    RUN = -1 "
                + "OR    JUMP = -1 "
                + "ORDER BY TIMESTAMP;";
        ArrayList<Player> players = new ArrayList<>();
        try (PreparedStatement stmt = c.prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    String code = rs.getString("code");
                    String stage = rs.getString("stage");
                    String name = rs.getString("name");
                    
                    Player p = new Player(code, stage, name);

                    p.setFlexible(rs.getDouble("flexible"));
                    p.setHeight(rs.getInt("height"));
                    p.setPushup(rs.getInt("pushup"));
                    p.setSitup(rs.getInt("situp"));
                    p.setRun(rs.getInt("run"));
                    p.setJump(rs.getInt("jump"));
                    players.add(p);
                }
            }

        } catch (SQLException ex) {
            System.err.println(ex.getClass().getName() + ": " + ex.getMessage());

        }
        return players;
    }

    public ArrayList<Player> getActiveByStage(String stage) {
        return null;
    }

    public boolean delete(Player player) {
        String code = player.getCode();
        /*delete*/
        String query = "DELETE FROM Player WHERE code=?;";

        try (PreparedStatement stmt = c.prepareStatement(query)) {
            stmt.setString(1, code);
            return stmt.executeUpdate() > 0;

        } catch (SQLException ex) {
            System.err.println(ex.getClass().getName() + ": " + ex.getMessage());
            return false;
        }

    }
}
