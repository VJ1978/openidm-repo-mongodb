/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 2011 ForgeRock AS.
 * Portions Copyrighted 2013 Takao Sekiguchi.
 * All rights reserved.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 */
package org.forgerock.openidm.repo.mongodb.impl.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.forgerock.json.fluent.JsonValue;
import org.forgerock.openidm.objset.BadRequestException;
import org.forgerock.openidm.repo.QueryConstants;
import org.forgerock.openidm.repo.mongodb.impl.DBHelper;
import org.forgerock.openidm.repo.mongodb.impl.MongoDBRepoService;
import org.forgerock.openidm.repo.mongodb.util.JsonReader;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

/**
 * Queries test
 * 
 * @author takao-s
 */
public class QueriesTest {

    private static ObjectMapper mapper = new ObjectMapper();
    private static String jsonConfig = new JsonReader().getJsonConfig();
    private static String jsonUsers = new JsonReader().getJsonUsers();
    private static JsonValue config = null;
    private static String collectionName = "managed_user";
    
    @BeforeClass
    public static void setUpClassl() {
        Map<String, Object> parsedConfig = null;
        try {
            parsedConfig = mapper.readValue(jsonConfig, Map.class);
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        config = new JsonValue(parsedConfig);
        DBCollection collection = DBHelper.getDB(config, true).getCollection(collectionName);
        
        List<DBObject> l = (List<DBObject>)JSON.parse(jsonUsers);
        for (DBObject user : l) {
            collection.insert(user);
        }
    }
    @AfterClass
    public static void tearDownClass() {
        DB db = DBHelper.getDB(config, false);
        db.dropDatabase();
    }

    @Test
    public void testSetQueriesConfig() {
        try {
            Queries queries = getQueries();
        } catch (JsonParseException e) {
            Assert.fail(e.getMessage());
        } catch (JsonMappingException e) {
            Assert.fail(e.getMessage());
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testSetFieldsConfig() {
        try {
            Queries queries = getQueriesWithFields();
        } catch (JsonParseException e) {
            Assert.fail(e.getMessage());
        } catch (JsonMappingException e) {
            Assert.fail(e.getMessage());
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testSetSortConfig() {
        try {
            Queries queries = getQueriesWithSort();
        } catch (JsonParseException e) {
            Assert.fail(e.getMessage());
        } catch (JsonMappingException e) {
            Assert.fail(e.getMessage());
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testSetAggregationConfig() throws JsonParseException, JsonMappingException, IOException {
        try {
            Queries queries = getQueriesOfAggregate();
        } catch (JsonParseException e) {
            Assert.fail(e.getMessage());
        } catch (JsonMappingException e) {
            Assert.fail(e.getMessage());
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }
    
    @Test
    public void testResolveQuery() {
        Queries queries = new Queries();
        String qStr = "{ \"${field}\" : \"${value}\", \"$max\" : 345 }";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("field", "userName");
        map.put("value", "Smith");
        map.put("max", "123");
        
        DBObject obj = null;
        try {
            obj = queries.resolveQuery(qStr, map);
        } catch (BadRequestException e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertEquals(obj.containsField("userName"), true);
        Assert.assertEquals(obj.get("userName").toString(), "Smith");
        Assert.assertEquals(obj.get("$max").toString(), "345");
        Assert.assertNull(obj.get("max"));
    }
    
    @Test
    public void testExecuteQuery_test_01() {
        Queries queries = null;
        try {
            queries = getQueriesWithSort();
        } catch (Exception e) {
        }
        
        String queryName = "unit-test-01";
        QueryInfo queryInfo = queries.getQueryInfo(queryName);
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put("gender", "female");
        params.put("age", "58");
        DBCollection collection = DBHelper.getDB(config, true).getCollection(collectionName);
        
        try {
            List<DBObject> list = queries.executeQuery(queryInfo, params, collection);
            Assert.assertEquals(list.size(), 1);
            Assert.assertEquals(list.get(0).get("_openidm_id").toString(), "user049");
            Assert.assertEquals(list.get(0).get("userName").toString(), "user049");
            Assert.assertEquals(list.get(0).get("age"), 59);
            Assert.assertEquals(list.get(0).get("gender").toString(), "female");
            Assert.assertEquals(list.get(0).get("mail").toString(), "user049@mail.test");
            Assert.assertEquals(list.get(0).get("birthday").toString(), "2001-02-19");
            Assert.assertEquals(list.get(0).get("job").toString(), "doctor");
            return;
        } catch (BadRequestException e) {
            Assert.fail(e.getMessage());
        }
        Assert.fail("Not yet implemented");
    }
    
    @Test
    public void testExecuteQuery_test_02() {
        Queries queries = null;
        try {
            queries = getQueriesWithSort();
        } catch (Exception e) {
        }
        
        String queryName = "unit-test-02";
        QueryInfo queryInfo = queries.getQueryInfo(queryName);
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put("fieldnames", new ArrayList<String>(){{add("_openidm_id");add("age");add("job");}} );
        DBCollection collection = DBHelper.getDB(config, true).getCollection(collectionName);
        
        try {
            List<DBObject> list = queries.executeQuery(queryInfo, params, collection);
            Assert.assertEquals(list.size(), 50);
            Assert.assertEquals(list.get(0).containsField("_openidm_id"), true);
            Assert.assertEquals(list.get(0).containsField("age"), true);
            Assert.assertEquals(list.get(0).containsField("job"), true);

            Assert.assertEquals(list.get(0).containsField("gender"), false);
            Assert.assertEquals(list.get(0).containsField("birthday"), false);
            Assert.assertEquals(list.get(0).containsField("mail"), false);

            return;
        } catch (BadRequestException e) {
            Assert.fail(e.getMessage());
        }
        Assert.fail("Not yet implemented");
    }
    
    @Test
    public void testExecuteQuery_test_03() {
        Queries queries = null;
        try {
            queries = getQueriesWithSort();
        } catch (Exception e) {
        }
        
        String queryName = "unit-test-03";
        QueryInfo queryInfo = queries.getQueryInfo(queryName);
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        DBCollection collection = DBHelper.getDB(config, true).getCollection(collectionName);
        
        try {
            List<DBObject> list = queries.executeQuery(queryInfo, params, collection);
            Assert.assertEquals(list.size(), 50);
            Assert.assertEquals(list.get(0).get("_openidm_id").toString(), "user049");
            Assert.assertEquals(list.get(1).get("_openidm_id").toString(), "user048");
            Assert.assertEquals(list.get(2).get("_openidm_id").toString(), "user047");

            return;
        } catch (BadRequestException e) {
            Assert.fail(e.getMessage());
        }
        Assert.fail("Not yet implemented");
    }
    
    @Test
    public void testExecuteQuery_test_04() {
        Queries queries = null;
        try {
            queries = getQueriesOfAggregate();
        } catch (Exception e) {
        }
        
        String queryName = "unit-test-04";
        QueryInfo queryInfo = queries.getQueryInfo(queryName);
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        DBCollection collection = DBHelper.getDB(config, true).getCollection(collectionName);
        
        try {
            List<DBObject> list = queries.executeQuery(queryInfo, params, collection);
            Assert.assertEquals(list.size(), 6);
            Assert.assertEquals(list.get(0).get("avg_age").toString(), "39.0");
            Assert.assertEquals(list.get(1).get("min_birthday").toString(), "2000-01-14");
            Assert.assertEquals(list.get(2).get("_id").toString(), "{ \"job\" : \"doctor\" , \"gender\" : \"male\"}");
            return;
        } catch (BadRequestException e) {
            Assert.fail(e.getMessage());
        }
        Assert.fail("Not yet implemented");
    }
    
    @Test
    public void testQuery() {
        Queries queries = null;
        try {
            queries = getQueriesWithSort();
        } catch (Exception e) {
        }
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put(QueryConstants.QUERY_ID, "unit-test-03");
        DBCollection collection = DBHelper.getDB(config, true).getCollection(collectionName);
        
        try {
            List<DBObject> list = queries.query(params, collection);
            Assert.assertEquals(list.size(), 50);
            Assert.assertEquals(list.get(0).get("_openidm_id").toString(), "user049");
            Assert.assertEquals(list.get(1).get("_openidm_id").toString(), "user048");
            Assert.assertEquals(list.get(2).get("_openidm_id").toString(), "user047");
            return;
        } catch (BadRequestException e) {
            Assert.fail(e.getMessage());
        }
        
        Assert.fail("Not yet implemented");
    }
    
    
    
    /*** PRIVATE METHODS ***/
    
    private Queries getQueries() throws JsonParseException, JsonMappingException, IOException {
        Queries queries = new Queries();
        queries.setQueriesConfig(config.get(MongoDBRepoService.CONFIG_QUERIES).toString());
        return queries;
    }
    
    private Queries getQueriesWithFields() throws JsonParseException, JsonMappingException, IOException {
        Queries queries = new Queries();
        queries.setQueriesConfig(config.get(MongoDBRepoService.CONFIG_QUERIES).toString());
        
        queries.setFieldsConfig(config.get(MongoDBRepoService.CONFIG_QUERY_FIELDS).toString());
        return queries;
    }
    
    private Queries getQueriesWithSort() throws JsonParseException, JsonMappingException, IOException {
        Queries queries = new Queries();
        queries.setQueriesConfig(config.get(MongoDBRepoService.CONFIG_QUERIES).toString());
        
        queries.setFieldsConfig(config.get(MongoDBRepoService.CONFIG_QUERY_FIELDS).toString());
        
        queries.setSortConfig(config.get(MongoDBRepoService.CONFIG_QUERY_SORT).toString());

        return queries;
    }

    private Queries getQueriesOfAggregate() throws JsonParseException, JsonMappingException, IOException {
        Queries queries = new Queries();
        queries.setAggregationConfig(config.get(MongoDBRepoService.CONFIG_QUERY_AGGREGATE).toString());
        return queries;
    }
}
