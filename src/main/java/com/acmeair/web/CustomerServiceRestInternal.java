/*******************************************************************************
 * Copyright (c) 2013 IBM Corp.
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

package com.acmeair.web;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.eclipse.microprofile.metrics.annotation.SimplyTimed;

import com.acmeair.service.CustomerService;

import com.acmeair.web.dto.AddressInfo;
import com.acmeair.web.dto.CustomerInfo;



@Path("/internal")
public class CustomerServiceRestInternal {

  // This class contains endpoints that are called by other services.
  // In the real world, these should be secured somehow, but for simplicity and to avoid too much overhead, they are not.
  // the other endpoints generate enough JWT/security work for this benchmark.
  @Resource(lookup = "jdbc/acmeairdb")
  private DataSource ds;
  
  @Inject
  CustomerService customerService;
   
  private static final Logger logger = Logger.getLogger(CustomerServiceRestInternal.class.getName());
  private static final JsonReaderFactory rfactory = Json.createReaderFactory(null);
  
  /**
   * Validate user/password.
   */
  @POST
  @Path("/validateid")
  @Consumes({ "application/x-www-form-urlencoded" })
  @Produces("application/json")
  @SimplyTimed(name="com.acmeair.web.CustomerServiceRestInternal.validateCustomer", tags= {"app=acmeair-customerservice-java"})
  public LoginResponse validateCustomer( 
      @FormParam("login") String login,
      @FormParam("password") String password) {

    if (logger.isLoggable(Level.FINE)) {
      logger.fine("validateid : login " + login + " password " + password);
    }
    
    if (!customerService.isPopulated()) {
      throw new RuntimeException("Customer DB has not been populated");
    }

    Boolean validCustomer = customerService.validateCustomer(login, password);

    return new LoginResponse(validCustomer); 
  }

  @GET
  @Path("/validatesetup")
  @Consumes({ "application/x-www-form-urlencoded" })
  @Produces("application/json")
  @SimplyTimed(name="com.acmeair.web.CustomerServiceRestInternal.validateSetup", tags= {"app=acmeair-customerservice-java"})
  public int validateSetup() {
    int size =0;
    try {

    Connection conn = ds.getConnection();
   

   try {
     Statement stmt = conn.createStatement();
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
     return size; 
 }
 } catch (Exception e) {
   // TODO Auto-generated catch block
   e.printStackTrace();
   return size; 
 } 

   
  }
  /**
   * Update reward miles.
   */
  @POST
  @Path("/updateCustomerTotalMiles/{custid}")
  @Consumes({ "application/x-www-form-urlencoded" })
  @Produces("application/json")
  @SimplyTimed(name="com.acmeair.web.CustomerServiceRestInternal.updateCustomerTotalMiles", tags={"app=acmeair-customerservice-java"})
  public MilesResponse updateCustomerTotalMiles(
      @PathParam("custid") String customerid,
      @FormParam("miles") Long miles) {
    
    JsonReader jsonReader = rfactory.createReader(new StringReader(customerService
        .getCustomerByUsername(customerid)));

    JsonObject customerJson = jsonReader.readObject();
    jsonReader.close();

    JsonObject addressJson = customerJson.getJsonObject("address");

    String streetAddress2 = null;

    if (addressJson.get("streetAddress2") != null 
        && !addressJson.get("streetAddress2").toString().equals("null")) {
      streetAddress2 = addressJson.getString("streetAddress2");
    }

    AddressInfo addressInfo = new AddressInfo(addressJson.getString("streetAddress1"), 
        streetAddress2,
        addressJson.getString("city"), 
        addressJson.getString("stateProvince"),
        addressJson.getString("country"),
        addressJson.getString("postalCode"));

    Long milesUpdate = customerJson.getInt("total_miles") + miles;
    CustomerInfo customerInfo = new CustomerInfo(customerid, 
        null, 
        customerJson.getString("status"),
        milesUpdate.intValue(), 
        customerJson.getInt("miles_ytd"), 
        addressInfo, 
        customerJson.getString("phoneNumber"),
        customerJson.getString("phoneNumberType"));

    customerService.updateCustomer(customerid, customerInfo);

    return new MilesResponse(milesUpdate);
  }
}
