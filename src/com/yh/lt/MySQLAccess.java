package com.yh.lt;

import org.apache.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by @yhankovich on 9/11/17.
 */
public class MySQLAccess {

    final static Logger logger = Logger.getLogger(MySQLAccess.class);

    public List<Offer> readDataBase() throws SQLException {

        Connection connect = null;
        List<Offer> result = new ArrayList<>();
        ResultSet resultSet = null;
        try {
            // This will load the MySQL driver, each DB has its own driver
//            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            connect = connect();

            // Statements allow to issue SQL queries to the database
            Statement statement = connect.createStatement();
            // Result set get the result of the SQL query
            resultSet = statement
                    //.executeQuery("select htmlText, id, sent from offer where sent is not null;");
                    .executeQuery("select htmlText, id, sent from offer;");

            while (resultSet.next()) {
                result.add(new Offer(resultSet.getString("htmlText"), resultSet.getString("id"), resultSet.getDate("sent")));
            }

        } catch (Exception e) {
            logger.error("Cant read from database.", e);
            throw e;
        } finally {
            close(null, connect, resultSet);
        }
        return result;
    }


    public void writeDataBase(Offer offer) throws SQLException {
        Connection connect = null;
        PreparedStatement preparedStatement = null;
        try {
            connect = connect();
            // Statements allow to issue SQL queries to the database
            preparedStatement = connect
                    .prepareStatement("insert ignore into  larditransparser.offer (id, htmlText, sent) values (?, ?, ?)");
            preparedStatement.setString(1, offer.getId());
            preparedStatement.setString(2, offer.getRowHtml());

            if (offer.getSent() == null){
                preparedStatement.setNull(3, Types.DATE);
            } else {
                preparedStatement.setDate(3, new java.sql.Date(offer.getSent().getTime()));
            }

            preparedStatement.executeUpdate();


        } catch (SQLException e) {
            logger.error("Cant write to database.", e);
            throw e;
        }

        finally {
            close(preparedStatement, connect, null);
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager
                .getConnection("jdbc:mysql://localhost/larditransparser?"
                        + "user=larditransparser&password=larditransparser&characterEncoding=UTF-8");
    }

    private static void close(PreparedStatement statement, Connection connect, ResultSet resultSet) throws SQLException {
        try {
            if (statement != null) {
                statement.close();
            }

            if (connect != null) {
                connect.close();
            }

            if (resultSet != null) {
                resultSet.close();
            }
        } catch (Exception e) {
            logger.error("Cant close database connection.", e);
            throw e;
        }
    }

}
