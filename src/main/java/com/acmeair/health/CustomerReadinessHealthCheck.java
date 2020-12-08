/*******************************************************************************
 * Copyright (c) 2018 IBM Corp.
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
package com.acmeair.health;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import com.acmeair.service.CustomerService;

@Readiness
@ApplicationScoped
public class CustomerReadinessHealthCheck implements HealthCheck {
  
  @Inject
  CustomerService customerService;

  public HealthCheckResponse call() {
   
    HealthCheckResponseBuilder builder = HealthCheckResponse.named("CustomerServiceReadinessCheck");
    
    if (customerService.isConnected()) {
      builder = builder.up();
    } else {
      builder = builder.down();
    }

    return builder.build();
  }
}