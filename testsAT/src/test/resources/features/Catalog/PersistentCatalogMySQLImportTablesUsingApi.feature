Feature: [CROSSDATA-174]Import tables using api from persistence

  Background:
    Given I execute a jdbc select 'TRUNCATE TABLE crossdataTables'
    And Drop the spark tables

  Scenario: Import a simple mongo table in catalog
    When I import tables using api for 'com.stratio.crossdata.connector.mongodb'
      |host                  | ${MONGO_HOST}:${MONGO_PORT} |
      |schema_samplingRatio  | 0.1             |
    When I execute a jdbc select 'SELECT count(*) FROM crossdataTables WHERE db = 'databasetest' AND tableName='tabletest' AND datasource='com.stratio.crossdata.connector.mongodb''
    Then an exception 'IS NOT' thrown
    Then The result has to be '1'
    Then I execute a jdbc select 'TRUNCATE TABLE crossdataTables'
    And Drop the spark tables

  Scenario: Import a simple cassandra table in MYSQL using api
    When I import tables using api for 'com.stratio.crossdata.connector.cassandra'
      |cluster                          | ${CASSANDRA_CLUSTER}          |
      |spark_cassandra_connection_host  | ${CASSANDRA_HOST}           |
    When I execute a jdbc select 'SELECT count(*) FROM crossdataTables WHERE db = 'databasetest' AND tableName='tabletest' AND datasource='com.stratio.crossdata.connector.cassandra''
    Then an exception 'IS NOT' thrown
    Then The result has to be '1'
    When I execute a jdbc select 'SELECT count(*) FROM crossdataTables WHERE db = 'databasetest' AND datasource='com.stratio.crossdata.connector.cassandra''
    Then an exception 'IS NOT' thrown
    Then The result has to be '6'
    Then I execute a jdbc select 'TRUNCATE TABLE crossdataTables'
    And Drop the spark tables

  Scenario: Import a simple ES table using api
    When I import tables using api for 'com.stratio.crossdata.connector.elasticsearch'
      |resource                           | databasetest/tabletest  |
      | es.nodes                          | ${ES_NODES}             |
      |es.port                            | ${ES_PORT}              |
      |es.nativePort                      | ${ES_NATIVE_PORT}       |
      |es.cluster                         | ${ES_CLUSTER}           |
    When I execute a jdbc select 'SELECT count(*) FROM crossdataTables WHERE db = 'databasetest' AND tableName='tabletest' AND datasource='com.stratio.crossdata.connector.elasticsearch''
    Then an exception 'IS NOT' thrown
    Then The result has to be '1'
    Then I execute a jdbc select 'TRUNCATE TABLE crossdataTables'
    And Drop the spark tables