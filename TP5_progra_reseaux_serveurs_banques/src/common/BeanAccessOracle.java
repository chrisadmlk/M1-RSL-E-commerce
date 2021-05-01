package common;

import java.sql.*;

public class BeanAccessOracle {
    Class leDriver;
    Connection con;
    Statement instruc;

    public BeanAccessOracle(String username) throws ClassNotFoundException, SQLException, Exception, IllegalAccessException {

        leDriver = Class.forName("oracle.jdbc.driver.OracleDriver");

        String url = "jdbc:oracle:thin:@localhost:1521/orcl";
        //String username = "CC";
        String pwd = "gendarme";
        System.out.println("!/! -Connexion réussie à la db- " + url + " " + username + " " + pwd);

        con = DriverManager.getConnection(url, username, pwd);
        instruc = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    }

    public ResultSet executeQuery(String s) throws SQLException {
        return instruc.executeQuery(s);
    }

    public String executeProcedureChangeStock(int quant, String item_name) throws SQLException {
        CallableStatement cs = con.prepareCall("{CALL MERCHANT.ChangeStock(?,?,?)}");
        cs.setInt(1, quant);
        cs.setString(2, item_name);
        cs.registerOutParameter(3, Types.VARCHAR);
        cs.executeUpdate();
        String rep = cs.getString(3);
        System.out.println("Rep de la proc�dure dans bean : " + rep);
        return rep;
    }

    public String getPIN(String clientName) throws SQLException {
        PreparedStatement researchPIN = con.prepareStatement("SELECT * FROM ACS.Clients WHERE nom_client = ?");
        researchPIN.setString(1,clientName);
        ResultSet result = researchPIN.executeQuery();
        String pin = "0";
        if(result.next()){
            pin = result.getString("pin");
        }
        return pin;
    }

    public int executeUpdate(String s) throws SQLException {
        int rs = instruc.executeUpdate(s);
        return rs;
    }
}
