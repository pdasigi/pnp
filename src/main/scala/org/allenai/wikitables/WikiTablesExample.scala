package org.allenai.wikitables

import com.jayantkrish.jklol.ccg.lambda2.Expression2
import com.jayantkrish.jklol.nlpannotation.AnnotatedSentence
import edu.stanford.nlp.sempre.ContextValue
import edu.stanford.nlp.sempre.Formula
import edu.stanford.nlp.sempre.Value
import fig.basic.LispTree

/**
 * Similar to CcgExample, except that this supports multiple logical forms per example, and also stores the
 * context and target values from Sempre to execute and evaluate formulas.
 * This currently does not hold a syntactic parse or dependencies. But we may need them in the future.
 */
case class WikiTablesExample(
  sentence: AnnotatedSentence,
  goldLogicalForm: Option[Expression2],
  possibleLogicalForms: Set[Expression2],
  tableString: String,
  targetValue: Value
) {

  val logicalForms = goldLogicalForm match {
    case None => possibleLogicalForms
    case Some(lf) => Set(lf)
  }

  /**
   * Loads the knowledge graph into memory, from the `tableString` constructor argument.  We do not
   * keep a reference to this object, so it can get garbage collected when the caller is done with
   * the object.  TODO(matt): we might want to reconsider this, as it's slower than I thought it
   * was.
   */
  def getContext(): ContextValue = {
    val lispTree = LispTree.proto.parseFromString(tableString)
     new ContextValue(LispTree.proto.parseFromString(tableString))
  }

  def isFormulaCorrect(pnpFormula: Expression2): Boolean = {
    // Sempre represents lambda expressions differently. We changed them when reading the examples. Changing
    // them back for execution.
    val expressionString = WikiTablesUtil.toSempreLogicalForm(pnpFormula);
    try {
      val sempreFormula = Formula.fromString(expressionString);
      WikiTablesDataProcessor.isFormulaCorrect(sempreFormula, getContext(), targetValue, null);
    } catch {
      case e: Exception => {
        System.err.println("Bad formula: " + expressionString);
        false;
      }
    }
  }

  override def toString() = {
    sentence + " [\n" + logicalForms.map(_.toString).mkString(" ") + "\n]"
  }
}