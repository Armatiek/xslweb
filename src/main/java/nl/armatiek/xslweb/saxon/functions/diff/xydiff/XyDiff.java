/**
 * JXyDiff: An XML Diff Written in Java
 *
 * Contact: pascal.molli@loria.fr
 *
 * This software is free software; you can redistribute it and/or
 * modify it under the terms of QPL/CeCill
 *
 * See licences details in QPL.txt and CeCill.txt
 *
 * Initial developer: Raphael Tani
 * Initial Developer: Gregory Cobena
 * Initial Developer: Gerald Oster
 * Initial Developer: Pascal Molli
 * Initial Developer: Serge Abiteboul
 * 
 * Adaptions for XSLWeb by Maarten Kroon (maarten.kroon@armatiek.nl)
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package nl.armatiek.xslweb.saxon.functions.diff.xydiff;

import nl.armatiek.xslweb.saxon.functions.diff.node.TreeNode;

public class XyDiff {
  
  TreeNode v0XML;
  TreeNode v1XML;
  NodesManager xyMappingEngine;

  public XyDiff(TreeNode v0XML, TreeNode v1XML) {
    this.v0XML = v0XML;
    this.v1XML = v1XML;
  }

  public DeltaConstructor diff() throws Exception {
    // ---- [[Phase 1: ]] Compute signature and weight for subtrees on both
    // documents ----
    xyMappingEngine = new NodesManager();

    xyMappingEngine.registerSourceDocument(this.v0XML);
    xyMappingEngine.registerResultDocument(this.v1XML);

    int v0rootID = xyMappingEngine.sourceNumberOfNodes;
    int v1rootID = xyMappingEngine.resultNumberOfNodes;

    // ---- [[Phase 2: ]] Apply Bottom-Up Lazy-Down Algorithm ----

    // xyMappingEngine.matchById(v1rootID);
    // xyMappingEngine.fullBottomUp(v1rootID);
    xyMappingEngine.topDownMatch(v0rootID, v1rootID);

    // ---- [[Phase 3: ]] Peephole Optimization to Propagate Matchings ----
    xyMappingEngine.optimize(v0rootID);

    // ---- [[Phase 4: ]] Construct the Delta ----

    DeltaConstructor myDeltaConstructor = new DeltaConstructor(xyMappingEngine, v0XML, v1XML);
    myDeltaConstructor.constructDeltaDocument();

    return myDeltaConstructor;
  }
}