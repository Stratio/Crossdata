/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package org.apache.spark.sql.crossdata.catalyst.analysis

import org.apache.spark.sql.AnalysisException
import org.apache.spark.sql.catalyst.analysis._
import org.apache.spark.sql.catalyst.expressions.{Alias, Attribute, Expression, ExtractValue, Literal, NamedExpression}
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan

import scala.annotation.tailrec

object XDLogicalPlanFunctions{

  implicit class ExtendedLogicalPlan(logicalPlan: LogicalPlan){

    def resolveUsingChildren(
                              nameParts: Seq[String],
                              resolver: Resolver): Option[NamedExpression] =
      resolve(nameParts, logicalPlan.children.flatMap(_.output), resolver)


    /** Performs attribute resolution given a name and a sequence of possible attributes. */
    private def resolve(
                         nameParts: Seq[String],
                         input: Seq[Attribute],
                         resolver: Resolver): Option[NamedExpression] = {


      // A sequence of possible candidate matches.
      // Each candidate is a tuple. The first element is a resolved attribute, followed by a list
      // of parts that are to be resolved (subfields).
      val candidates: Seq[(Attribute, List[String])] = {
        // If the name has 2 or more parts, try to resolve it as `database.table.column` or `table.column`.
        if (nameParts.length > 1) {
          input.flatMap { option =>
            resolveAsCrossdataTable(nameParts, resolver, option)
          }
        } else {
          Seq.empty
        }
      }

      candidates.distinct match {
        // One match, no nested fields, use it.
        case Seq((a, Nil)) => Some(a)

        // One match, but we also need to extract the requested nested field.
        case Seq((a, nestedFields)) =>
          // The foldLeft adds ExtractValues for every remaining parts of the identifier,
          // and aliased it with the last part of the name.
          // For example, consider "a.b.c", where "a" is resolved to an existing attribute.
          // Then this will add ExtractValue("c", ExtractValue("b", a)), and alias the final
          // expression as "c".
          val fieldExprs = nestedFields.foldLeft(a: Expression)((expr, fieldName) =>
            ExtractValue(expr, Literal(fieldName), resolver))
          Some(Alias(fieldExprs, nestedFields.last)())

        // No matches.
        case Seq() =>
          None

        // More than one match.
        case ambiguousReferences =>
          val referenceNames = ambiguousReferences.map(_._1).mkString(", ")
          throw new AnalysisException(
            s"Reference '${UnresolvedAttribute(nameParts).name}' is ambiguous, could be: $referenceNames.")
      }
    }


    private def resolveAsCrossdataTable(
                                         nameParts: Seq[String],
                                         resolver: Resolver,
                                         attribute: Attribute): Option[(Attribute, List[String])] = {
      assert(nameParts.length > 1)

      /** Spark docs => TODO review Spark2.0 ( qualified tables will be supported)
        * All possible qualifiers for the expression.
        *
        * For now, since we do not allow using original table name to qualify a column name once the
        * table is aliased, this can only be:
        *
        * Single element: either the table name or the alias name of the table.
        */
      val attributeQualifiers = attribute.qualifiers.headOption.map(_.split("\\.").toSeq).getOrElse(Seq.empty)
      resolveUsingAttributeQualifiers(attribute, attributeQualifiers, nameParts, resolver)
    }

    /**
      * Tries to resolve nameParts: firstly, as a Seq(database,table,..), then as a Seq(table,..) and so on.
      */
    @tailrec
    private def resolveUsingAttributeQualifiers(attribute: Attribute, attrQualifiers: Seq[String], nameParts: Seq[String], resolver: Resolver): Option[(Attribute, List[String])] = {
      if (attrQualifiers.isEmpty) {
        None
      } else {
        val nameMatchesQualifiers: Boolean = attrQualifiers.zip(nameParts).forall { case (a, b) => resolver(a, b) }
        if (nameMatchesQualifiers) {
          val columnCandidate = nameParts.drop(attrQualifiers.length)
          resolveAsColumn(columnCandidate, resolver, attribute)
        } else {
          resolveUsingAttributeQualifiers(attribute, attrQualifiers.tail, nameParts, resolver)
        }
      }
    }


    private def resolveAsColumn(
                                 nameParts: Seq[String],
                                 resolver: Resolver,
                                 attribute: Attribute): Option[(Attribute, List[String])] = {
      if (resolver(attribute.name, nameParts.head)) {
        Option((attribute.withName(nameParts.head), nameParts.tail.toList))
      } else {
        None
      }
    }


  }
}

