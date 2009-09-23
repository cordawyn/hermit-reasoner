// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.monitor;

import org.semanticweb.HermiT.model.AnnotatedEquality;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.ExistentialConcept;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.tableau.BranchingPoint;
import org.semanticweb.HermiT.tableau.DLClauseEvaluator;
import org.semanticweb.HermiT.tableau.DatatypeManager;
import org.semanticweb.HermiT.tableau.GroundDisjunction;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.Tableau;

public interface TableauMonitor {
    void setTableau(Tableau tableau);
    void isSatisfiableStarted(AtomicConcept atomicConcept);
    void isSatisfiableFinished(AtomicConcept atomicConcept,boolean result);
    void isSubsumedByStarted(AtomicConcept subconcept,AtomicConcept superconcept);
    void isSubsumedByFinished(AtomicConcept subconcept,AtomicConcept superconcept,boolean result);
    void isABoxSatisfiableStarted();
    void isABoxSatisfiableFinished(boolean result);
    void isInstanceOfStarted(AtomicConcept concept,Individual individual);
    void isInstanceOfFinished(AtomicConcept concept,Individual individual,boolean result);
    void tableauCleared();
    void saturateStarted();
    void saturateFinished();
    void iterationStarted();
    void iterationFinished();
    void dlClauseMatchedStarted(DLClauseEvaluator dlClauseEvaluator,int dlClauseIndex);
    void dlClauseMatchedFinished(DLClauseEvaluator dlClauseEvaluator,int dlClauseIndex);
    void addFactStarted(Object[] tuple,boolean isCore);
    void addFactFinished(Object[] tuple,boolean isCore,boolean factAdded);
    void mergeStarted(Node mergeFrom,Node mergeInto);
    void nodePruned(Node node);
    void mergeFactStarted(Node mergeFrom,Node mergeInto,Object[] sourceTuple,Object[] targetTuple);
    void mergeFactFinished(Node mergeFrom,Node mergeInto,Object[] sourceTuple,Object[] targetTuple);
    void mergeFinished(Node mergeFrom,Node mergeInto);
    void clashDetectionStarted(Object[]... tuples);
    void clashDetectionFinished(Object[]... tuples);
    void clashDetected();
    void backtrackToStarted(BranchingPoint newCurrentBrancingPoint);
    void tupleRemoved(Object[] tuple);
    void backtrackToFinished(BranchingPoint newCurrentBrancingPoint);
    void groundDisjunctionDerived(GroundDisjunction groundDisjunction);
    void processGroundDisjunctionStarted(GroundDisjunction groundDisjunction);
    void groundDisjunctionSatisfied(GroundDisjunction groundDisjunction);
    void processGroundDisjunctionFinished(GroundDisjunction groundDisjunction);
    void disjunctProcessingStarted(GroundDisjunction groundDisjunction,int disjunct);
    void disjunctProcessingFinished(GroundDisjunction groundDisjunction,int disjunct);
    void pushBranchingPointStarted(BranchingPoint branchingPoint);
    void pushBranchingPointFinished(BranchingPoint branchingPoint);
    void startNextBranchingPointStarted(BranchingPoint branchingPoint);
    void startNextBranchingPointFinished(BranchingPoint branchingPoint);
    void existentialExpansionStarted(ExistentialConcept existentialConcept,Node forNode);
    void existentialExpansionFinished(ExistentialConcept existentialConcept,Node forNode);
    void existentialSatisfied(ExistentialConcept existentialConcept,Node forNode);
    void nominalIntorductionStarted(Node rootNode,Node treeNode,AnnotatedEquality annotatedEquality,Node argument1,Node argument2);
    void nominalIntorductionFinished(Node rootNode,Node treeNode,AnnotatedEquality annotatedEquality,Node argument1,Node argument2);
    void descriptionGraphCheckingStarted(int graphIndex1,int tupleIndex1,int position1,int graphIndex2,int tupleIndex2,int position2);
    void descriptionGraphCheckingFinished(int graphIndex1,int tupleIndex1,int position1,int graphIndex2,int tupleIndex2,int position2);
    void nodeCreated(Node node);
    void nodeDestroyed(Node node);
    void datatypeCheckingStarted();
    void datatypeCheckingFinished(boolean result);
    void datatypeConjunctionCheckingStarted(DatatypeManager.DConjunction conjunction);
    void datatypeConjunctionCheckingFinished(DatatypeManager.DConjunction conjunction,boolean result);
    void blockingValidationStarted();
    void blockingValidationFinished();
}
