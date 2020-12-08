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

package com.acmeair.loader;

import com.acmeair.service.CustomerService;
import com.acmeair.web.dto.AddressInfo;
import com.acmeair.web.dto.CustomerInfo;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class CustomerLoader {

  @Inject
  CustomerService customerService;
  
 // @Inject 
 // @ConfigProperty(name = "NUM_CUSTOMERS_TO_LOAD") 
  private Integer numCustomersToLoad=10000;
  
  private static Logger logger = Logger.getLogger(CustomerLoader.class.getName());

  /**
   * Get default number of customers.
   */
  
  public String queryLoader() {
    return numCustomersToLoad.toString();
  }

  /**
   * Load customer db.
   */
  public String loadCustomerDb(long numCustomers) {
    List<CustomerInfo> customers = new ArrayList<CustomerInfo>();
    double length = 0;
    try {
     
      logger.info("Start loading " +  numCustomers + " customers");
      long start = System.currentTimeMillis(); 
      customerService.dropCustomers();

      AddressInfo addressInfo =  new AddressInfo("123 Main St.", null, "Anytown", "NC", "USA", "27617");
      for (long ii = 0; ii < numCustomers; ii++) {
        customers.add(new CustomerInfo("uid" + ii + "@email.com", "password", "GOLD", 0, 0,
                addressInfo, "919-123-4567", "BUSINESS"));
      }
      customerService.createCustomers(customers);

      long stop = System.currentTimeMillis();
      logger.info("Finished loading in " + (stop - start) / 1000.0 + " seconds");
      length = (stop - start) / 1000.0;
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    return "Loaded "  +  numCustomers + " customers in " + length + " seconds";
  } 
}