/*******************************************************************************
* Copyright (c) 2013-2015 IBM Corp.
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

package com.acmeair.service;

import com.acmeair.web.dto.AddressInfo;
import com.acmeair.web.dto.CustomerInfo;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import java.io.StringReader;
import java.util.List;

public abstract class CustomerService {
  protected static final int DAYS_TO_ALLOW_SESSION = 1;
  private static final JsonReaderFactory factory = Json.createReaderFactory(null);

  @Inject
  protected KeyGenerator keyGenerator; 
 
  public abstract void createCustomer(CustomerInfo customerInfo);

  public abstract void createCustomers(List<CustomerInfo> customers);

  public abstract String createAddress(AddressInfo addressInfo);

  public abstract void updateCustomer(String username, CustomerInfo customerJson);

  protected abstract String getCustomer(String username);

  public abstract String getCustomerByUsername(String username);

  /**
   * Validate password for customer.
   */
  public boolean validateCustomer(String username, String password) {
    boolean validatedCustomer = false;
    String customerToValidate = getCustomer(username);
    if (customerToValidate != null) {

      JsonReader jsonReader = factory.createReader(new StringReader(customerToValidate));
      JsonObject customerJson = jsonReader.readObject();
      jsonReader.close();

      validatedCustomer = password.equals((String) customerJson.getString("password"));

    }
    return validatedCustomer;
  }

  /**
   * Get customer info.
   */
  public String getCustomerByUsernameAndPassword(String username, String password) {
    String c = getCustomer(username);

    JsonReader jsonReader = factory.createReader(new StringReader(c));
    JsonObject customerJson = jsonReader.readObject();
    jsonReader.close();

    if (!customerJson.getString("password").equals(password)) {
      return null;
    }

    // Should we also set the password to null?
    return c;
  }

  public abstract Long count();

  public abstract void dropCustomers();

  public abstract String getServiceType();

  public abstract boolean isPopulated();

  public abstract boolean isConnected();
  
}
