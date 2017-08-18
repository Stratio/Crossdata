/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.crossdata.driver.querybuilder.dslentities

import com.stratio.crossdata.driver.querybuilder.{BinaryExpression, Expression, Predicate, UnaryExpression}


case class AsteriskExpression() extends Expression {
  override private[querybuilder] def toXDQL: String = "*"
}

case class Minus(child: Expression) extends UnaryExpression {
  override val tokenStr: String = " -"

  override def childExpansion(child: Expression): String = child match {
    case _: Literal | _: Identifier => child.toXDQL
    case _ => s"(${child.toXDQL})"
  }
}

case class Not(child: Expression) extends Predicate {
  override private[querybuilder] def toXDQL: String = child match {
    case _: Predicate => s" !(${child.toXDQL})"
    case _: Expression => s" !${child.toXDQL}} "
    case _ => s" !(${child.toXDQL})} "
  }
}

case class Add(left: Expression, right: Expression) extends BinaryExpression {

  override val tokenStr = "+"

  // TODO review operator precedence
  override def childExpansion(child: Expression): String = child match {
    case _: Add => child.toXDQL
    case _: Literal | _: Identifier => child.toXDQL
    case _ => s"(${child.toXDQL})"
  }
}

case class Subtract(left: Expression, right: Expression) extends BinaryExpression {
  override val tokenStr = "-"

  override def childExpansion(child: Expression): String = child match {
    case _: Literal | _: Identifier => child.toXDQL
    case _ => s"(${child.toXDQL})"
  }
}

case class Multiply(left: Expression, right: Expression) extends BinaryExpression {
  override val tokenStr = "*"

  override def childExpansion(child: Expression): String = child match {
    case _: Literal | _: Identifier => child.toXDQL
    case _ => s"(${child.toXDQL})"
  }
}

case class Divide(left: Expression, right: Expression) extends BinaryExpression {
  override val tokenStr = "/"

  override def childExpansion(child: Expression): String = child match {
    case _: Literal | _: Identifier => child.toXDQL
    case _ => s"(${child.toXDQL})"
  }
}

case class Remainder(left: Expression, right: Expression) extends BinaryExpression {
  override val tokenStr = "%"

  override def childExpansion(child: Expression): String = child match {
    case _: Literal | _: Identifier => child.toXDQL
    case _ => s"(${child.toXDQL})"
  }
}

//Select expressions
case class Distinct(expr: Expression*) extends Expression {
  override private[querybuilder] def toXDQL: String = s" DISTINCT ${expr.map(_.toXDQL) mkString ","}"
}

case class Sum(expr: Expression) extends Expression {
  override private[querybuilder] def toXDQL: String = s" sum(${expr.toXDQL})"
}

case class SumDistinct(expr: Expression) extends Expression {
  override private[querybuilder] def toXDQL: String = s" sum( DISTINCT ${expr.toXDQL})"
}

case class Count(expr: Expression) extends Expression {
  override private[querybuilder] def toXDQL: String = s" count(${expr.toXDQL})"
}

case class CountDistinct(expr: Expression*) extends Expression {
  override private[querybuilder] def toXDQL: String = s" count( DISTINCT ${expr.map(_.toXDQL) mkString ","})"
}

case class ApproxCountDistinct(expr: Expression, rsd: Double) extends Expression {
  override private[querybuilder] def toXDQL: String = s" APPROXIMATE ($rsd) count ( DISTINCT ${expr.toXDQL})"
}

case class Avg(expr: Expression) extends Expression {
  override private[querybuilder] def toXDQL: String = s" avg(${expr.toXDQL})"
}

case class Min(expr: Expression) extends Expression {
  override private[querybuilder] def toXDQL: String = s" min(${expr.toXDQL})"
}

case class Max(expr: Expression) extends Expression {
  override private[querybuilder] def toXDQL: String = s" max(${expr.toXDQL})"
}

case class Abs(expr: Expression) extends Expression {
  override private[querybuilder] def toXDQL: String = s" abs(${expr.toXDQL})"
}