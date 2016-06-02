/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.crossdata.driver.querybuilder.dslentities

import com.stratio.crossdata.driver.querybuilder.{BinaryExpression, Expression, Predicate}

// Logical predicates
case class And(left: Expression, right: Expression) extends BinaryExpression
with Predicate {

  override val tokenStr = "AND"

  override def childExpansion(child: Expression): String = child match {
    case _: And => child.toXDQL
    case _: Predicate => s"(${child.toXDQL})"
    case _: Expression => child.toXDQL
    case _ => s"(${child.toXDQL})"
  }
}

case class Or(left: Expression, right: Expression) extends BinaryExpression
with Predicate {

  override val tokenStr = "OR"

  override def childExpansion(child: Expression): String = child match {
    case _: Or => child.toXDQL
    case _: Predicate => s"(${child.toXDQL})"
    case _: Expression => child.toXDQL
    case _ => s"(${child.toXDQL})"
  }

}

private[dslentities] trait EqualityCheckers extends BinaryExpression {
  //TODO: Improve management of cases as `x === y === z`
  override def childExpansion(child: Expression): String = child.toXDQL
}

// Comparison predicates
case class Equal(left: Expression, right: Expression) extends EqualityCheckers
with Predicate {
  override val tokenStr: String = "="
}

case class Different(left: Expression, right: Expression) extends EqualityCheckers
with Predicate {
  override val tokenStr: String = "<>"
}

case class LessThan(left: Expression, right: Expression) extends BinaryExpression //TODO: Review
with Predicate {

  override val tokenStr: String = "<"

  override def childExpansion(child: Expression): String = child match {
    case _: Expression => child.toXDQL
    case _ => s"(${child.toXDQL})"
  }
}

case class LessThanOrEqual(left: Expression, right: Expression) extends BinaryExpression //TODO: Review
with Predicate {

  override val tokenStr: String = "<="

  override def childExpansion(child: Expression): String = child match {
    case _: Expression => child.toXDQL
    case _ => s"(${child.toXDQL})"
  }
}

case class GreaterThan(left: Expression, right: Expression) extends BinaryExpression //TODO: Review
with Predicate {

  override val tokenStr: String = ">"

  override def childExpansion(child: Expression): String = child match {
    case _: Expression => child.toXDQL
    case _ => s"(${child.toXDQL})"
  }
}

case class GreaterThanOrEqual(left: Expression, right: Expression) extends BinaryExpression //TODO: Review
with Predicate {

  override val tokenStr: String = ">="

  override def childExpansion(child: Expression): String = child match {
    case _: Expression => child.toXDQL
    case _ => s"(${child.toXDQL})"
  }
}

case class IsNull(expr: Expression) extends Predicate {
  override private[querybuilder] def toXDQL: String = s" ${expr.toXDQL} IS NULL"
}

case class IsNotNull(expr: Expression) extends Predicate {
  override private[querybuilder] def toXDQL: String = s" ${expr.toXDQL} IS NOT NULL"
}

case class In(left: Expression, right: Expression*) extends Expression with Predicate {
  override private[querybuilder] def toXDQL: String = s" ${left.toXDQL} IN ${right map (_.toXDQL) mkString("(", ",", ")")}"
}

case class Like(left: Expression, right: Expression) extends BinaryExpression with Predicate {
  override val tokenStr = "LIKE"
}