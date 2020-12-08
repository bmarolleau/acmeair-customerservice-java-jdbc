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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.acmeair.service.CustomerService;
import com.acmeair.web.dto.CustomerInfo;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.metrics.annotation.SimplyTimed;

@Path("/")
public class CustomerServiceRest {

  @Inject
  private JsonWebToken jwt;
  
  @Inject
  CustomerService customerService;
  
  private static final Logger logger = Logger.getLogger(CustomerServiceRest.class.getName());
  
  /**
   * Get customer info.
   */
  @GET
  @Path("/byid/{custid}")
  @Produces("text/plain")
  @SimplyTimed(name="com.acmeair.web.CustomerServiceRest.getCustomer", tags = "app=acmeair-customerservice-java")
  @RolesAllowed({"user"})
  public Response getCustomer(@PathParam("custid") String customerid) {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("getCustomer : userid " + customerid);
    }

    try {
      // make sure the user isn't trying to update a customer other than the one
      // currently logged in
      if (!customerid.equals(jwt.getSubject())) {
        return Response.status(Response.Status.FORBIDDEN).build();
      }

      return Response.ok(customerService.getCustomerByUsername(customerid)).build();

    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Update customer.
   */
  @POST
  @Path("/byid/{custid}")
  @Produces("text/plain")
  @SimplyTimed(name="com.acmeair.web.CustomerServiceRest.putCustomer", tags = "app=acmemair-customerservice-java")
  @RolesAllowed({"user"})
  public Response putCustomer(CustomerInfo customer, @PathParam("custid") String customerid ) {

    // make sure the user isn't trying to update a customer other than the one
    // currently logged in
    if (!customerid.equals(jwt.getSubject())) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
    
    String username = customer.get_id();           
    String customerFromDb = customerService
        .getCustomerByUsernameAndPassword(username, customer.getPassword());

    if (logger.isLoggable(Level.FINE)) {
      logger.fine("putCustomer : " + customerFromDb);
    }

    if (customerFromDb == null) {
      // either the customer doesn't exist or the password is wrong
      return Response.status(Response.Status.FORBIDDEN).build();
    }

    customerService.updateCustomer(username, customer);
    
    // Retrieve the latest results
    customerFromDb = customerService
        .getCustomerByUsernameAndPassword(username, customer.getPassword());

    return Response.ok(customerFromDb).build();
  }

  @GET
  public Response status() {
    return Response.ok("OK").build();

  }
}
