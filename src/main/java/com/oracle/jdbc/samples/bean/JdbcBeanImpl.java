package com.oracle.jdbc.samples.bean;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.oracle.jdbc.samples.entity.Employee;

import oracle.jdbc.OracleTypes;

/**
 *
 * @author nirmala.sundarappa@oracle.com
 */
@SuppressWarnings("all")
public class JdbcBeanImpl implements JdbcBean {

  public static Connection getConnection() throws SQLException {
    //DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
    //Connection connection = DriverManager.getConnection("jdbc:oracle:thin:@//140.238.19.195:1521/WLSDB_icn149.subnet.vcn.oraclevcn.com", "hr", "WelCome123##");
    
    Context ctx = null;
        DataSource ds = null;
        try {
            ctx = new InitialContext();
            ds = (DataSource) ctx.lookup("jdbc/wlsdbDataSource");
        } catch (NamingException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (ctx != null)
                    ctx.close();
            } catch (NamingException ne) {
                System.err.println("comm.util.conPoool : can't close context resource !!");
            }
        }
        return (Connection) ds.getConnection();
  }

  @Override
  public List<Employee> getEmployees() {
    List<Employee> returnValue = new ArrayList<>();
    try (Connection connection = getConnection()) {
      try (Statement statement = connection.createStatement()) {
        try (ResultSet resultSet = statement.executeQuery("SELECT Employee_Id, First_Name, Last_Name, Email, Phone_Number, Job_Id, Salary FROM EMPLOYEES")) {
          while(resultSet.next()) {
            returnValue.add(new Employee(resultSet));
          }
        }
      }
    } catch (SQLException ex) {
      logger.log(Level.SEVERE, null, ex);
      ex.printStackTrace();
    }
    
    return returnValue;
  }

  /**
   * Returns the employee object for the given empId.   Returns
   * @param empId
   * @return
   */
  @Override
  public List<Employee> getEmployee(int empId) {
    List<Employee> returnValue = new ArrayList<>();

    try (Connection connection = getConnection()) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(
          "SELECT Employee_Id, First_Name, Last_Name, Email, Phone_Number, Job_Id, Salary FROM EMPLOYEES WHERE Employee_Id = ?")) {
        preparedStatement.setInt(1, empId);
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          if(resultSet.next()) {
            returnValue.add(new Employee(resultSet));
          }
        }
      }
    } catch (SQLException ex) {
      logger.log(Level.SEVERE, null, ex);
      ex.printStackTrace();
    }

    return returnValue;
  }

  @Override
  public Employee updateEmployee(int empId) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<Employee> getEmployeeByFn(String fn) {
    List<Employee> returnValue = new ArrayList<>();

    try (Connection connection = getConnection()) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(
          "SELECT Employee_Id, First_Name, Last_Name, Email, Phone_Number, Job_Id, Salary FROM EMPLOYEES WHERE First_Name LIKE ?")) {
        preparedStatement.setString(1, fn + '%');
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          while(resultSet.next()) {
            returnValue.add(new Employee(resultSet));
          }
        }
      }
    } catch (SQLException ex) {
      logger.log(Level.SEVERE, null, ex);
      ex.printStackTrace();
    }

    return returnValue;
  }

   @Override
   public List<Employee> incrementSalary (int incrementPct) {
     List<Employee> returnValue = new ArrayList<>();

     try (Connection connection = getConnection()) {
       try (CallableStatement callableStatement = connection.prepareCall("begin ? :=  refcur_pkg.incrementsalary(?); end;")) {
         callableStatement.registerOutParameter(1, OracleTypes.CURSOR);
         callableStatement.setInt(2, incrementPct);
         callableStatement.execute();
         try (ResultSet resultSet = (ResultSet) callableStatement.getObject(1)) {
           while (resultSet.next()) {
             returnValue.add(new Employee(resultSet));
           }
         }
       }
     } catch (SQLException ex) {
       logger.log(Level.SEVERE, null, ex);
       ex.printStackTrace();
     }

     return returnValue;
   }

  static final Logger logger = Logger.getLogger("com.oracle.jdbc.samples.bean.JdbcBeanImpl");
}
