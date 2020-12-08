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

package com.acmeair.db2;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class ConnectionManagerDb2 implements Db2Constants {
 
  private static final JsonReaderFactory factory = Json.createReaderFactory(null);

  private static final Logger logger = Logger.getLogger(ConnectionManagerDb2.class.getName());

  protected MongoClient mongoClient;
  protected MongoDatabase db;
    
 /*  @Inject 
  @ConfigProperty(name = "DB_HOST", defaultValue = "localhost") 
  private String Host;
  
  @Inject 
  @ConfigProperty(name = "DB_PORT", defaultValue = "27017") 
  private Integer Port;
  
  @Inject 
  @ConfigProperty(name = "DBNAME", defaultValue = "acmeair") 
  private String DbName;
  
  @Inject 
  @ConfigProperty(name = "DB2_USERNAME" , defaultValue = "user") 
  private Optional<String> Username;
  
  @Inject 
  @ConfigProperty(name = "DB2_PASSWORD" , defaultValue = "password") 
  private Optional<String> Password;
     */
  /* @Inject 
  @ConfigProperty(name = "MONGO_SSL_ENABLED", defaultValue = "false") 
  private Optional<Boolean> mongoSslEnabled;
  
  @Inject 
  @ConfigProperty(name = "MONGO_MIN_CONNECTIONS_PER_HOST") 
  private Optional<Integer> mongoMinConnectionsPerHost;
  
  @Inject 
  @ConfigProperty(name = "MONGO_CONNECTIONS_PER_HOST") 
  private Optional<Integer> mongoConnectionsPerHost;
  
  @Inject 
  @ConfigProperty(name = "MONGO_MAX_WAIT_TIME") 
  private Optional<Integer> mongoMaxWaitTime;
  
  @Inject 
  @ConfigProperty(name = "MONGO_CONNECT_TIME_OUT") 
  private Optional<Integer> mongoConnectTimeOut;
  
  @Inject 
  @ConfigProperty(name = "MONGO_SOCKET_TIME_OUT") 
  private Optional<Integer> mongoSocketTimeOut;
  
  @Inject 
  @ConfigProperty(name = "MONGO_THREADS_ALLOWED_TO_BLOCK_FOR_CONNECTION_MULTIPLIER") 
  private Optional<Integer> mongoThreadsAllowedToBlockForConnectionMultiplier;
  
  @Inject 
  @ConfigProperty(name = "MONGO_MAX_CONNECTION_IDLE_TIME") 
  private Optional<Integer> mongoMaxConnectionIdleTime;
  
  @Inject 
  @ConfigProperty(name = "MONGO_SOCKET_KEEPALIVE") 
  private Optional<Boolean> mongoSocketKeepalive; */
  
  //@Inject 
  //@ConfigProperty(name = "VCAP_SERVICES") 
  //Optional<String> vcapJsonString;

  @PostConstruct
  private void initialize() {     

   /*  ServerAddress dbAddress = null;
    MongoClientOptions.Builder options = new MongoClientOptions.Builder();

    if (mongoConnectionsPerHost.isPresent()) {
      options.connectionsPerHost(mongoConnectionsPerHost.get());
    }
    if (mongoMinConnectionsPerHost.isPresent()) {
      options.minConnectionsPerHost(mongoMinConnectionsPerHost.get());
    }
    if (mongoMaxWaitTime.isPresent()) {
      options.maxWaitTime(mongoMaxWaitTime.get());
    }
    if (mongoConnectTimeOut.isPresent()) {
      options.connectTimeout(mongoConnectTimeOut.get());
    }
    if (mongoSocketTimeOut.isPresent()) {
      options.socketTimeout(mongoSocketTimeOut.get());
    }
    if (mongoSocketKeepalive.isPresent()) {
      options.socketKeepAlive(mongoSocketKeepalive.get());
    }
    if (mongoSslEnabled.isPresent()) {
      options.sslEnabled(mongoSslEnabled.get());
    }
    if (mongoThreadsAllowedToBlockForConnectionMultiplier.isPresent()) {
      options.threadsAllowedToBlockForConnectionMultiplier(
          mongoThreadsAllowedToBlockForConnectionMultiplier.get());
    }
    if (mongoMaxConnectionIdleTime.isPresent()) {
      options.maxConnectionIdleTime(mongoMaxConnectionIdleTime.get());
    }

    MongoClientOptions builtOptions = options.build(); */
    
   // try {
      // Check if VCAP_SERVICES exist, and if it does, look up the url from the
      // credentials.
  /*     if (vcapJsonString.isPresent()) {
        logger.info("Reading VCAP_SERVICES");

        JsonReader jsonReader = factory.createReader(new StringReader(vcapJsonString.get()));
        JsonObject vcapServices = jsonReader.readObject();
        jsonReader.close();

        JsonArray mongoServiceArray = null;
        for (Object key : vcapServices.keySet()) {
          if (key.toString().startsWith("mongo")) {
            mongoServiceArray = (JsonArray) vcapServices.get(key);
            logger.info("Service Type : MongoLAB - " + key.toString());
            break;
          }
          if (key.toString().startsWith("user-provided")) {
            mongoServiceArray = (JsonArray) vcapServices.get(key);
            logger.info("Service Type : MongoDB by Compost - " + key.toString());
            break;
          }
        }

        if (mongoServiceArray == null) {
          logger.info(
              "VCAP_SERVICES existed, but a MongoLAB or MongoDB by COMPOST service was "
              + "not definied. Trying DB resource");
          // VCAP_SERVICES don't exist, so use the DB resource
          dbAddress = new ServerAddress(mongoHost, mongoPort);

          // If username & password exists, connect DB with username & password
          if ((!mongoUsername.isPresent()) || (!mongoPassword.isPresent())) {
            mongoClient = new MongoClient(dbAddress, builtOptions);
          } else {
            List<MongoCredential> credentials = new ArrayList<>();
            credentials.add(MongoCredential
                .createCredential(mongoUsername.get(), mongoDbName, 
                    mongoPassword.get().toCharArray()));
            mongoClient = new MongoClient(dbAddress, credentials, builtOptions);
          }
        } else {
          JsonObject mongoService = (JsonObject) mongoServiceArray.get(0);
          JsonObject credentials = (JsonObject) mongoService.get("credentials");
          String url = (String) credentials.getString("url");
          logger.fine("service url = " + url);
          MongoClientURI mongoUri = new MongoClientURI(url, options);
          mongoClient = new MongoClient(mongoUri);
          mongoDbName = mongoUri.getDatabase();

        }
      } else { */

        // VCAP_SERVICES don't exist, so use the DB resource
       /*  dbAddress = new ServerAddress(mongoHost, mongoPort);

        // If username & password exists, connect DB with username & password
        if ((!mongoUsername.isPresent()) || (!mongoPassword.isPresent())) {
          mongoClient = new MongoClient(dbAddress, builtOptions);
        } else {
          List<MongoCredential> credentials = new ArrayList<>();
          credentials.add(MongoCredential
              .createCredential(mongoUsername.get(), mongoDbName, 
                  mongoPassword.get().toCharArray()));
          mongoClient = new MongoClient(dbAddress, credentials, builtOptions);
        }
      //}

      db = mongoClient.getDatabase(mongoDbName);
      logger.info("#### Mongo DB Server " + mongoClient.getAddress().getHost() + " ####");
      logger.info("#### Mongo DB Port " + mongoClient.getAddress().getPort() + " ####");
      logger.info("#### Mongo DB is created with DB name " + mongoDbName + " ####");
      logger.info("#### MongoClient Options ####");
      logger.info("maxConnectionsPerHost : " + builtOptions.getConnectionsPerHost());
      logger.info("minConnectionsPerHost : " + builtOptions.getMinConnectionsPerHost());
      logger.info("maxWaitTime : " + builtOptions.getMaxWaitTime());
      logger.info("connectTimeout : " + builtOptions.getConnectTimeout());
      logger.info("socketTimeout : " + builtOptions.getSocketTimeout());
      logger.info("socketKeepAlive : " + builtOptions.isSocketKeepAlive());
      logger.info("sslEnabled : " + builtOptions.isSslEnabled());
      logger.info("threadsAllowedToBlockForConnectionMultiplier : "
          + builtOptions.getThreadsAllowedToBlockForConnectionMultiplier());
      logger.info("Complete List : " + builtOptions.toString());

    } catch (Exception e) {
      logger.severe("Caught Exception : " + e.getMessage());
    } */

  }

  public MongoDatabase getDb() {
    return db;
  }
}
