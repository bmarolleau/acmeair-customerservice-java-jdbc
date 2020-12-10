/*******************************************************************************
* Copyright (c) 2017 IBM Corp.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/

package com.acmeair.db2.services;

import com.acmeair.db2.ConnectionManagerDb2;
import com.acmeair.db2.Db2Constants;
import com.acmeair.service.CustomerService;
import com.acmeair.web.dto.AddressInfo;
import com.acmeair.web.dto.CustomerInfo;
import org.bson.Document;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


@ApplicationScoped
public class CustomerServiceImplDb2 extends CustomerService implements Db2Constants {

  //private MongoCollection<Document> customer;
  private Boolean isPopulated = true;
  //private final int WRITE_BATCH_SIZE = ConnectionDescription.getDefaultMaxWriteBatchSize();

  @Resource(lookup = "jdbc/acmeairdb")
  private DataSource ds;

  private Connection connection;
  private ResultSet executeQuery;

  private static final Logger logger = Logger.getLogger(CustomerServiceImplDb2.class.getName());
  

  @Inject
  ConnectionManagerDb2 connectionManager;
  

  @PostConstruct
  public void initialization() {
    // MongoDatabase database = connectionManager.getDb();
    // customer = database.getCollection("customer");

      Context initCtx;
      int size=0;
     try {
      // initCtx = new InitialContext();
   
   //Context envCtx = (Context) initCtx.lookup("java:comp/env");
   // ds = (DataSource) envCtx.lookup("jdbc/db2/acmeairdb");
   //2ds = (DataSource) initCtx
    //2  .lookup("jdbc/acmeairdb");

   Connection conn = ds.getConnection();
   this.connection=conn;

   try {
     Statement stmt = conn.createStatement();
    // String sql = "SELECT * FROM CUSTOMER";
     String sql = "SELECT count(*) AS total  FROM CUSTOMER";
     ResultSet rs= stmt.executeQuery(sql);
  
 if (rs != null) 
     {
      rs.next();
        size = rs.getInt("total"); // get row id 
        System.out.println("Number of Customers:"+size);
     }
 } finally {
     conn.close();
 }
 } catch (Exception e) {
   // TODO Auto-generated catch block
   e.printStackTrace();
 } 


  }

  private Connection getConnection() {
    Connection conn=null;
    Context initCtx;
    try {
      initCtx = new InitialContext();
      //Context envCtx;
      //envCtx = (Context) initCtx.lookup("java:comp/env");
      //ds = (DataSource) envCtx.lookup("jdbc/db2/acmeairdb");
     
      //2ds = (DataSource) initCtx
      //.lookup("jdbc/acmeairdb");
   
      conn = ds.getConnection();
   return conn;

} catch (Exception e) {
  // TODO Auto-generated catch block
  e.printStackTrace();
  return conn;
}
}
  
  @Override
  public Long count() {
    //return customer.countDocuments();

    Long size =new Long(0); 

    Context initCtx;
    try {
      initCtx = new InitialContext();
  
  //1Context envCtx = (Context) initCtx.lookup("java:comp/env");
   //1ds = (DataSource) envCtx.lookup("jdbc/db2/acmeairdb");
   //2ds = (DataSource) initCtx
   //2.lookup("jdbc/acmeairdb");
   

  Connection conn = ds.getConnection();
  try {
    Statement stmt = conn.createStatement();
    String sql = "SELECT count(*) AS total  FROM CUSTOMER";
    
   ResultSet rs= stmt.executeQuery(sql);
  
 if (rs != null) 
     {
      rs.next();
        size = new Long(rs.getInt("total")); // get row id 
        System.out.println("Number of Customers:"+size);
     }

} finally {
    conn.close();
}
} catch (Exception e) {
  // TODO Auto-generated catch block
  e.printStackTrace();
} 
     return size;
  }

  @Override
  public void createCustomer(CustomerInfo customerInfo) {
    //Document customerDoc = parseCustomerInfo(customerInfo);
    //customer.insertOne(customerDoc);
    try {

    Connection conn=getConnection();
    
      Statement stmt = conn.createStatement();
      String sql = "INSERT INTO ACMEAIR.CUSTOMER ID,PASSWORD,STATUS,"+
      "TOTAL_MILES,MILES_YTD,PHONENUMBER,PHONENUMBERTYPE,STREETADDRESS1,STREETADDRESS2,"+
      "CITY, STATEPROVINCE,COUNTRY,POSTALCODE"+
      "VALUES("+customerInfo.get_id()+","+ 
      customerInfo.getPassword()+","+
      customerInfo.getStatus()+","+
      customerInfo.getTotal_miles()+","+
      customerInfo.getMiles_ytd()+","+
      customerInfo.getPhoneNumber()+","+
      customerInfo.getPhoneNumberType()+","+
      customerInfo.getAddress().getStreetAddress1()+","+
      customerInfo.getAddress().getStreetAddress2()+","+
      customerInfo.getAddress().getCity()+","+
      customerInfo.getAddress().getStateProvince()+","+
      customerInfo.getAddress().getCountry()+","+
      customerInfo.getAddress().getPostalCode()+")";
      
      //stmt.execute(sql);      
      PreparedStatement pstmt = conn.prepareStatement(sql);
      pstmt.executeUpdate(sql);

      conn.close();
    }
      catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    
  }

  @Override
  public void createCustomers(List<CustomerInfo> customers) {
    //List<Document> documents = new ArrayList<Document>(WRITE_BATCH_SIZE);
    for (int i=0; i<customers.size(); i++) {
     // documents.add(parseCustomerInfo(customers.get(i)));
      
      //if ( i % WRITE_BATCH_SIZE == 0 ) {
      //  customer.insertMany(documents);
      //  documents.clear();
      //}
    }
    //if(!documents.isEmpty()) customer.insertMany(documents);
  }

  @Override
  public String createAddress(AddressInfo addressInfo) {
    Document addressDoc = parseAddressInfo(addressInfo);
    return addressDoc.toJson();
  }

  @Override
  public void updateCustomer(String username, CustomerInfo customerInfo) {

    try {
  Connection conn=getConnection();
  //Document address = parseAddressInfo(customerInfo.getAddress());
  String SQL_UPDATE = "UPDATE ACMEAIR.CUSTOMER set status=?,"+
  "total_miles=?,"+
  "miles_ytd=?,"+
   "phonenumber=?,"+
   "phonenumbertype=?,"+
   "streetaddress1=?,"+
   "streetaddress2=?,"+
   "city=?,"+
   "stateprovince=?,"+
   "country=?,"+
 "postalcode=?"+ 
 "WHERE ID = '"+username+"'"; 
   
 PreparedStatement preparedStatement = conn.prepareStatement(SQL_UPDATE);

  preparedStatement.setString(1, customerInfo.getStatus());
  preparedStatement.setInt(2, customerInfo.getTotal_miles());
  preparedStatement.setInt(3, customerInfo.getMiles_ytd());
  
	preparedStatement.setString(4, customerInfo.getPhoneNumber());

  preparedStatement.setString(5, customerInfo.getPhoneNumberType());
  preparedStatement.setString(6, customerInfo.getAddress().getStreetAddress1());
  preparedStatement.setString(7, customerInfo.getAddress().getStreetAddress2());
  preparedStatement.setString(8, customerInfo.getAddress().getCity());
  preparedStatement.setString(9, customerInfo.getAddress().getStateProvince());
  preparedStatement.setString(10, customerInfo.getAddress().getCountry());
  preparedStatement.setString(11, customerInfo.getAddress().getPostalCode());
  //preparedStatement.setString(12, username);

  int row = preparedStatement.executeUpdate();
  System.out.println("Row Updated: "+row);
} catch (Exception e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
  /* phonenumbertype='BUSINESS', streetaddress1='124 Main St.',
 streetaddress2='-',
 city='Anytown',
 stateprovince='27617',
 country='USA',
 postalcode='27617' 
 WHERE ID like '%uid0@email.com%'"; 

   /* customer.updateOne(eq("_id", customerInfo.get_id()),
        combine(set("status", customerInfo.getStatus()), 
            set("total_miles", customerInfo.getTotal_miles()),
            set("miles_ytd", customerInfo.getMiles_ytd()), 
            set("address", address),
            set("phoneNumber", customerInfo.getPhoneNumber()),
            set("phoneNumberType", customerInfo.getPhoneNumberType())));*/

         
  }

  @Override
  protected String getCustomer(String username) {
     //customer.find(eq("_id", username)).first().toJson();
    try {

      Connection conn=getConnection();
      
        Statement stmt = conn.createStatement();
        String sql = "SELECT * FROM ACMEAIR.CUSTOMER WHERE ID like '%"+username+"%'";
        stmt.execute(sql);      
        ResultSet rs = stmt.getResultSet();
        CustomerInfo customerInfo= new CustomerInfo();
        AddressInfo addressInfo= new AddressInfo();
        while (rs.next()) {
        customerInfo.set_id(rs.getString("ID"));
        customerInfo.setPassword(rs.getString("PASSWORD"));
        customerInfo.setStatus(rs.getString("STATUS"));
        customerInfo.setTotal_miles(rs.getInt("TOTAL_MILES"));
        customerInfo.setMilesYtd(rs.getInt("MILES_YTD"));
        customerInfo.setPhoneNumber(rs.getString("PHONENUMBER"));
        customerInfo.setPhoneNumberType(rs.getString("PHONENUMBERTYPE"));
        addressInfo.setStreetAddress1(rs.getString("STREETADDRESS1"));
        addressInfo.setStreetAddress2(rs.getString("STREETADDRESS2"));
        addressInfo.setCity(rs.getString("CITY"));
        addressInfo.setStateProvince(rs.getString("STATEPROVINCE"));
        addressInfo.setCountry(rs.getString("COUNTRY"));
        addressInfo.setPostalCode(rs.getString("POSTALCODE"));
        customerInfo.setAddress(addressInfo);
        }
        Document customerDoc = parseCustomerInfo(customerInfo);

        System.out.println(customerDoc.toJson());
        logger.info(customerDoc.toJson());
        conn.close();
        return (customerDoc.toJson());
      }
        catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
          return ("");
        }
  }

  @Override
  public String getCustomerByUsername(String username) {
    //Document customerDoc = customer.find(eq("_id", username)).first();
    try {

      Connection conn=getConnection();
      
        Statement stmt = conn.createStatement();
        String sql = "SELECT * FROM ACMEAIR.CUSTOMER WHERE ID like '%"+username+"%'";
        stmt.execute(sql);      
        ResultSet rs = stmt.getResultSet();
        CustomerInfo customerInfo= new CustomerInfo();
        AddressInfo addressInfo= new AddressInfo();
        while (rs.next()) {
        customerInfo.set_id(rs.getString("ID"));
        customerInfo.setPassword(rs.getString("PASSWORD"));
        customerInfo.setStatus(rs.getString("STATUS"));
        customerInfo.setTotal_miles(rs.getInt("TOTAL_MILES"));
        customerInfo.setMilesYtd(rs.getInt("MILES_YTD"));
        customerInfo.setPhoneNumber(rs.getString("PHONENUMBER"));
        customerInfo.setPhoneNumberType(rs.getString("PHONENUMBERTYPE"));
        addressInfo.setStreetAddress1(rs.getString("STREETADDRESS1"));
        addressInfo.setStreetAddress2(rs.getString("STREETADDRESS2"));
        addressInfo.setCity(rs.getString("CITY"));
        addressInfo.setStateProvince(rs.getString("STATEPROVINCE"));
        addressInfo.setCountry(rs.getString("COUNTRY"));
        addressInfo.setPostalCode(rs.getString("POSTALCODE"));
        customerInfo.setAddress(addressInfo);
        }
        Document customerDoc = parseCustomerInfo(customerInfo);
        System.out.println(customerDoc.toJson());
        logger.info(customerDoc.toJson());

    if (customerDoc != null) {
      customerDoc.remove("password");
      customerDoc.append("password", null);
    }
       return customerDoc.toJson();
      } 
    catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return ("");
    }
  }

  @Override
  public void dropCustomers() {
    //customer.deleteMany(new Document());

  }

  @Override
  public String getServiceType() {
    return "db2 for i";
  }

  @Override
  public boolean isPopulated() {
    if (isPopulated) {
      return true;
    }
        
    /*if (customer.countDocuments() > 0) {
      isPopulated = true;
      return true;
    } else {
      return false;
    }*/
    return true;
  }

  @Override
  public boolean isConnected() {
    //return (customer.countDocuments() >= 0);
    return true;
  }

  private Document parseCustomerInfo(CustomerInfo customerInfo) {
    return new Document("_id", customerInfo.get_id())
            .append("password", customerInfo.getPassword())
            .append("status", customerInfo.getPassword())
            .append("total_miles", customerInfo.getTotal_miles()).append("miles_ytd", customerInfo.getMiles_ytd())
            .append("address", parseAddressInfo(customerInfo.getAddress()))
            .append("phoneNumber", customerInfo.getPhoneNumber())
            .append("phoneNumberType", customerInfo.getPhoneNumberType());
  }
  private Document parseAddressInfo(AddressInfo addressInfo) {
    return new Document("streetAddress1", addressInfo.getStreetAddress1())
            .append("streetAddress2", addressInfo.getStreetAddress2())
            .append("city", addressInfo.getCity())
            .append("stateProvince", addressInfo.getStateProvince())
            .append("country", addressInfo.getCountry())
            .append("postalCode", addressInfo.getPostalCode());
  }
}
