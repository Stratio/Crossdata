/*
 * Licensed to STRATIO (C) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  The STRATIO (C) licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.stratio.meta.common.statements.structures.relationships;

/**
 * Operators supported in an {@link com.stratio.meta.common.statements.structures.assignations.Assignation}.
 */
public enum Operator {
  ADD{
    @Override
    public String toString() {
      return "+";
    }
  },
  SUBTRACT{
    @Override
    public String toString() {
      return "-";
    }
  },
  /**
   * Constant to define inclusion relationships.
   */
  IN{
    @Override
    public String toString() {
      return "IN";
    }
  },
  /**
   * Constant to define range comparisons.
   */
  BETWEEN{
    @Override
    public String toString() {
      return "BETWEEN";
    }
  },
  /**
   * Constant to define compare relationships (e.g., >, <, =, etc.).
   */
  COMPARE{
    @Override
    public String toString() {
      return "=";
    }
  },
  /**
   * Assign relationship for update-like statements.
   */
  ASSIGN{
    public String toString() {
      return "=";
    }
  },
  MATCH{
    public String toString() {
      return "MATCH";
    }
  },
  GREATER_THAN{
    public String toString() {
      return ">";
    }
  },
  LOWER_THAN{
    public String toString() {
      return "<";
    }
  },
  GREATER_EQUAL_THAN{
    public String toString() {
      return ">=";
    }
  },
  LOWER_EQUAL_THAN{
    public String toString() {
      return "<=";
    }
  },
  NOT_EQUAL{
    public String toString() {
      return "<>";
    }
  },
  LIKE{
    public String toString() {
      return "LIKE";
    }
  };

}
