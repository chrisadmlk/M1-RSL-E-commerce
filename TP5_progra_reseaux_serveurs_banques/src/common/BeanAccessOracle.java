package common;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

public class BeanAccessOracle {
    Class leDriver;
    Connection con;
    Statement instruc;

    public BeanAccessOracle(String username) throws ClassNotFoundException, SQLException, Exception, IllegalAccessException {

        leDriver = Class.forName("oracle.jdbc.driver.OracleDriver");

        String url = "jdbc:oracle:thin:@localhost:1521/orcl";
        //String username = "CC";
        String pwd = "gendarme";
        System.out.println("!!!!!!!!!!!!!! " + url + " " + username + " " + pwd);

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

    public int executeUpdate(String s) throws SQLException {
        int rs = instruc.executeUpdate(s);
        return rs;
    }


}
