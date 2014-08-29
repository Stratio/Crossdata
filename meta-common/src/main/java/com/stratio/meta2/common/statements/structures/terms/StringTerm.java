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

package com.stratio.meta2.common.statements.structures.terms;

import com.stratio.meta.common.statements.structures.relationships.Operator;

import java.util.Iterator;

public class StringTerm extends Term<String> {

  private static final long serialVersionUID = 4470491967411363431L;

  private boolean quotedLiteral = false;

  private char quotationMark = '\0';

  public StringTerm(String term) {
    super(String.class, term);
    if(term.startsWith("'") && term.endsWith("'")){
      this.quotedLiteral = true;
      this.quotationMark = '\'';
      this.value = term.substring(1, term.length()-1);
    } else if(term.startsWith("\"") && term.endsWith("\"")){
      this.quotedLiteral = true;
      this.quotationMark = '"';
      this.value = term.substring(1, term.length()-1);
    }
  }

  public boolean isQuotedLiteral() {
    return quotedLiteral;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (this.isQuotedLiteral()) {
      sb.append(String.valueOf(quotationMark) + value + String.valueOf(quotationMark));
    } else {
      sb.append(value);
    }
    if(hasCompoundTerms()){
      Iterator<GenericTerm> termsIter = getCompoundTerms().iterator();
      for(Operator operator: getValueOperators()){
        GenericTerm gTerm = termsIter.next();
        sb.append(" ").append(operator).append(" ").append(gTerm.toString());
        if(termsIter.hasNext()){
          sb.append(", ");
        }
      }
    }
    return sb.toString();
  }
}
