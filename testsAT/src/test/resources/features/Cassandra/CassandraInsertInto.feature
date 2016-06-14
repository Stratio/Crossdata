Feature:[CROSSDATA-465]Cassandra InsertInto
  Scenario: [CROSSDATA-465] INSERT INTO insertIntotable (ident,name,money,new,date) VALUES (0, 'name_0', 10.2 ,true, '1999-11-30')
    When I execute 'INSERT INTO insertintotable1 (ident,name,money,new,date) VALUES (0, 'name_0', 10.2 ,true, '1999-11-30 00:00:00')'
    Then an exception 'IS NOT' thrown
    When I execute 'SELECT count(*) FROM insertintotable1'
    Then The spark result has to have '1' rows:
      |_c0-long|
      | 1     |

  Scenario: [CROSSDATA-465] INSERT INTO insertintotable2 (ident,money,new,date,name) VALUES (0, 10.2 ,true, '1999-11-30 00:00:00', 'name_0')
    When I execute 'INSERT INTO insertintotable2 (ident,money,new,date,name) VALUES (0, 10.2 ,true, '1999-11-30 00:00:00', 'name_0')'
    Then an exception 'IS NOT' thrown
    When I execute 'SELECT count(*) FROM insertintotable2'
    Then The spark result has to have '1' rows:
      |_c0-long|
      | 1     |

  Scenario: [CROSSDATA-465] INSERT INTO notexitstable (ident,money,new,date,name) VALUES (0, 10.2 ,true, '1999-11-30 00:00:00', 'name_0')
    When I execute 'INSERT INTO notexitstable (ident,money,new,date,name) VALUES (0, 10.2 ,true, '1999-11-30 00:00:00', 'name_0')'
    Then an exception 'IS' thrown

  Scenario: [CROSSDATA-465] INSERT INTO over TEMPORARY TABLE must return an exception
    When I execute 'INSERT INTO tab1(name) VALUES ('name_2')'
    Then an exception 'IS NOT' thrown


  Scenario: [CROSSDATA-465] INSERT INTO over CREATE TEMPORARY VIEW AS SELECT
    When I execute 'CREATE TEMPORARY VIEW temptable AS SELECT ident FROM insertintotable3'
    Then an exception 'IS NOT' thrown
    When I execute 'INSERT INTO temptable(ident) VALUES (1)'
    Then an exception 'IS' thrown

  Scenario: [CROSSDATA-465] INSERT INTO insertintotable4 two rows in the same sentence
    When I execute 'INSERT INTO insertintotable4 (ident,money,new,date,name) VALUES (0, 10.2 ,true, '1999-11-30 00:00:00', 'name_0'), (1, 11.2 ,true, '1999-11-30 00:00:00', 'name_1')'
    Then an exception 'IS NOT' thrown
    When I execute 'SELECT count(*) FROM insertintotable4'
    Then The spark result has to have '1' rows:
      |_c0-long|
      | 2     |

  Scenario Outline: [CROSSDATA-465] INSERT INTO insertintotable5 over Cassandra SET
    When I truncate a Cassandra table named 'insertintotable5' using keyspace 'databasetest'
    When I execute 'INSERT INTO insertintotable5 (ident,names) VALUES (0, <array>)'
    Then an exception 'IS NOT' thrown
    When I execute 'SELECT count(*) FROM insertintotable5'
    Then The spark result has to have '1' rows:
      |_c0-long       |
      | 1             |

    Examples:
     | array                |
     | ['name_0']           |
     | []                   |
     | ['name_0','name_1']  |

  Scenario Outline: [CROSSDATA-465] INSERT INTO insertintotable6 over Cassandra LIST
    When I truncate a Cassandra table named 'insertintotable6' using keyspace 'databasetest'
    When I execute 'INSERT INTO insertintotable6 (ident,names) VALUES (0, <array>)'
    Then an exception 'IS NOT' thrown
    When I execute 'SELECT count(*) FROM insertintotable6'
    Then The spark result has to have '1' rows:
      |_c0-long       |
      | 1             |

    Examples:
      | array                |
      | ['name_0']           |
      | []                   |
      | ['name_0','name_1']  |


  Scenario Outline: [CROSSDATA-465] INSERT INTO insertintotable7 over Cassandra LIST
    When I truncate a Cassandra table named 'insertinto7' using keyspace 'databasetest'
    When I execute 'INSERT INTO insertintotable7 (ident,namesphone) VALUES (0, <map>)'
    Then an exception 'IS NOT' thrown
    When I execute 'SELECT count(*) FROM insertintotable7'
    Then The spark result has to have '1' rows:
      |_c0-long       |
      | 1             |

    Examples:
      | map                |
      | ('name_0'-> 00)           |
      | ()                   |
      | ('name_0'-> 00,'name_1'-> 10)  |