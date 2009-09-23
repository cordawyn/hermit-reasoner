// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.debugger;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.debugger.commands.ActiveNodesCommand;
import org.semanticweb.HermiT.debugger.commands.AgainCommand;
import org.semanticweb.HermiT.debugger.commands.BreakpointTimeCommand;
import org.semanticweb.HermiT.debugger.commands.ClearCommand;
import org.semanticweb.HermiT.debugger.commands.ContinueCommand;
import org.semanticweb.HermiT.debugger.commands.DebuggerCommand;
import org.semanticweb.HermiT.debugger.commands.DerivationTreeCommand;
import org.semanticweb.HermiT.debugger.commands.ExitCommand;
import org.semanticweb.HermiT.debugger.commands.ForeverCommand;
import org.semanticweb.HermiT.debugger.commands.HelpCommand;
import org.semanticweb.HermiT.debugger.commands.HistoryCommand;
import org.semanticweb.HermiT.debugger.commands.IsAncestorOfCommand;
import org.semanticweb.HermiT.debugger.commands.ModelStatsCommand;
import org.semanticweb.HermiT.debugger.commands.NodesForCommand;
import org.semanticweb.HermiT.debugger.commands.OriginStatsCommand;
import org.semanticweb.HermiT.debugger.commands.QueryCommand;
import org.semanticweb.HermiT.debugger.commands.ReuseNodeForCommand;
import org.semanticweb.HermiT.debugger.commands.ShowDLClausesCommand;
import org.semanticweb.HermiT.debugger.commands.ShowDescriptionGraphCommand;
import org.semanticweb.HermiT.debugger.commands.ShowExistsCommand;
import org.semanticweb.HermiT.debugger.commands.ShowModelCommand;
import org.semanticweb.HermiT.debugger.commands.ShowNodeCommand;
import org.semanticweb.HermiT.debugger.commands.ShowSubtreeCommand;
import org.semanticweb.HermiT.debugger.commands.SingleStepCommand;
import org.semanticweb.HermiT.debugger.commands.UnprocessedDisjunctionsCommand;
import org.semanticweb.HermiT.debugger.commands.WaitForCommand;
import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.ExistentialConcept;
import org.semanticweb.HermiT.model.ExistsDescriptionGraph;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.monitor.TableauMonitorForwarder;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.Tableau;

public class Debugger extends TableauMonitorForwarder {
    private static final long serialVersionUID=-1061073966460686069L;

    public static final Font s_monospacedFont=new Font("Monospaced",Font.PLAIN,12);

    public static enum WaitOption {
        GRAPH_EXPANSION,EXISTENTIAL_EXPANSION,CLASH,MERGE,DATATYPE_CHECKING,BLOCKING_VALIDATION
    };

    protected final Map<String,DebuggerCommand> m_commandsByName;
    protected final Prefixes m_prefixes;
    protected final DerivationHistory m_derivationHistory;
    protected final ConsoleTextArea m_consoleTextArea;
    protected final JFrame m_mainFrame;
    protected final PrintWriter m_output;
    protected final BufferedReader m_input;
    protected final Set<WaitOption> m_waitOptions;
    protected final Map<Node,NodeCreationInfo> m_nodeCreationInfos;
    protected Node m_lastExistentialNode;
    protected ExistentialConcept m_lastExistentialConcept;
    protected Tableau m_tableau;
    protected String m_lastCommand;
    protected boolean m_forever;
    protected long m_lastStatusMark;
    protected boolean m_singlestep;
    protected boolean m_inMainLoop;
    protected int m_breakpointTime;
    protected int m_currentIteration;

    public Debugger(Prefixes prefixes,boolean historyOn) {
        super(new DerivationHistory());
        m_commandsByName=new TreeMap<String,DebuggerCommand>();
        registerCommands();
        m_prefixes=prefixes;
        m_derivationHistory=(DerivationHistory)m_forwardingTargetMonitor;
        m_consoleTextArea=new ConsoleTextArea();
        m_consoleTextArea.setFont(s_monospacedFont);
        m_output=new PrintWriter(m_consoleTextArea.getWriter());
        m_input=new BufferedReader(m_consoleTextArea.getReader());
        JScrollPane scrollPane=new JScrollPane(m_consoleTextArea);
        scrollPane.setPreferredSize(new Dimension(800,300));
        m_mainFrame=new JFrame("HermiT Debugger");
        m_mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        m_mainFrame.setContentPane(scrollPane);
        m_mainFrame.pack();
        Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
        Dimension preferredSize=m_mainFrame.getPreferredSize();
        m_mainFrame.setLocation((screenSize.width-preferredSize.width)/2,screenSize.height-100-preferredSize.height);
        m_forwardingOn=historyOn;
        m_waitOptions=new HashSet<WaitOption>();
        m_nodeCreationInfos=new HashMap<Node,NodeCreationInfo>();
        m_forever=false;
        m_singlestep=false;
        m_breakpointTime=30000;
        m_mainFrame.setVisible(true);
        m_output.println("Good morning Dr. Chandra. This is HAL. I'm ready for my first lesson.");
        m_output.println("Derivation history is "+(m_forwardingOn ? "on" : "off")+".");
    }
    protected void registerCommands() {
        registerCommand(new ActiveNodesCommand(this));
        registerCommand(new AgainCommand(this));
        registerCommand(new BreakpointTimeCommand(this));
        registerCommand(new ClearCommand(this));
        registerCommand(new ContinueCommand(this));
        registerCommand(new DerivationTreeCommand(this));
        registerCommand(new ExitCommand(this));
        registerCommand(new ForeverCommand(this));
        registerCommand(new HelpCommand(this));
        registerCommand(new HistoryCommand(this));
        registerCommand(new IsAncestorOfCommand(this));
        registerCommand(new ModelStatsCommand(this));
        registerCommand(new NodesForCommand(this));
        registerCommand(new OriginStatsCommand(this));
        registerCommand(new QueryCommand(this));
        registerCommand(new ReuseNodeForCommand(this));
        registerCommand(new ShowDescriptionGraphCommand(this));
        registerCommand(new ShowDLClausesCommand(this));
        registerCommand(new ShowExistsCommand(this));
        registerCommand(new ShowModelCommand(this));
        registerCommand(new ShowNodeCommand(this));
        registerCommand(new ShowSubtreeCommand(this));
        registerCommand(new SingleStepCommand(this));
        registerCommand(new UnprocessedDisjunctionsCommand(this));
        registerCommand(new WaitForCommand(this));
    }
    protected void registerCommand(DebuggerCommand command) {
        m_commandsByName.put(command.getCommandName().toLowerCase(),command);
    }
    public Map<String,DebuggerCommand> getDebuggerCommands() {
        return Collections.unmodifiableMap(m_commandsByName);
    }
    public void dispose() {
        m_mainFrame.dispose();
        m_tableau=null;
    }
    public Tableau getTableau() {
        return m_tableau;
    }
    public PrintWriter getOutput() {
        return m_output;
    }
    public JFrame getMainFrame() {
        return m_mainFrame;
    }
    public String getLastCommand() {
        return m_lastCommand;
    }
    public ConsoleTextArea getConsoleTextArea() {
        return m_consoleTextArea;
    }
    public Prefixes getPrefixes() {
        return m_prefixes;
    }
    public DerivationHistory getDerivationHistory() {
        return m_derivationHistory;
    }
    public NodeCreationInfo getNodeCreationInfo(Node node) {
        NodeCreationInfo nodeCreationInfo=m_nodeCreationInfos.get(node);
        return nodeCreationInfo;
    }
    public void setBreakpointTime(int time) {
        m_breakpointTime=time;
    }
    public void setInMainLoop(boolean inMainLoop) {
        m_inMainLoop=inMainLoop;
    }
    public void setForever(boolean forever) {
        m_forever=forever;
    }
    public void setSinglestep(boolean singlestep) {
        m_singlestep=singlestep;
    }
    public boolean addWaitOption(WaitOption option) {
        return m_waitOptions.add(option);
    }
    public boolean removeWaitOption(WaitOption option) {
        return m_waitOptions.remove(option);
    }
    public DebuggerCommand getCommand(String commandName) {
        return m_commandsByName.get(commandName.toLowerCase());
    }
    public void mainLoop() {
        try {
            m_inMainLoop=true;
            while (m_inMainLoop) {
                m_output.print("> ");
                String commandLine=m_input.readLine();
                if (commandLine!=null) {
                    commandLine=commandLine.trim();
                    processCommandLine(commandLine);
                }
            }
            m_output.flush();
        }
        catch (IOException e) {
        }
        m_lastStatusMark=System.currentTimeMillis();
    }
    public void processCommandLine(String commandLine) {
        String[] parsedCommand=parse(commandLine);
        String commandName=parsedCommand[0];
        DebuggerCommand command=getCommand(commandName);
        if (command==null)
            m_output.println("Unknown command '"+commandName+"'.");
        else {
            command.execute(parsedCommand);
            if (!(command instanceof AgainCommand))
                m_lastCommand=commandLine;
        }
    }
    protected String[] parse(String command) {
        command=command.trim();
        List<String> arguments=new ArrayList<String>();
        int firstChar=0;
        int nextSpace=command.indexOf(' ');
        while (nextSpace!=-1) {
            arguments.add(command.substring(firstChar,nextSpace));
            firstChar=nextSpace;
            while (firstChar<command.length() && command.charAt(firstChar)==' ')
                firstChar++;
            nextSpace=command.indexOf(' ',firstChar);
        }
        arguments.add(command.substring(firstChar));
        String[] result=new String[arguments.size()];
        arguments.toArray(result);
        return result;
    }
    protected void printState() {
        int numberOfNodes=0;
        int inactiveNodes=0;
        int blockedNodes=0;
        int nodesWithExistentials=0;
        int pendingExistentials=0;
        Node node=m_tableau.getFirstTableauNode();
        while (node!=null) {
            numberOfNodes++;
            if (!node.isActive())
                inactiveNodes++;
            else if (node.isBlocked())
                blockedNodes++;
            else {
                if (node.hasUnprocessedExistentials())
                    nodesWithExistentials++;
                pendingExistentials+=node.getUnprocessedExistentials().size();
            }
            node=node.getNextTableauNode();
        }
        m_output.println("Nodes: "+numberOfNodes+"  Inactive nodes: "+inactiveNodes+"  Blocked nodes: "+blockedNodes+"  Nodes with exists: "+nodesWithExistentials+"  Pending existentials: "+pendingExistentials);
    }

    public void setTableau(Tableau tableau) {
        super.setTableau(tableau);
        m_tableau=tableau;
    }
    public void isSatisfiableStarted(AtomicConcept atomicConcept) {
        super.isSatisfiableStarted(atomicConcept);
        m_output.println("Will check whether '"+m_prefixes.abbreviateIRI(atomicConcept.getIRI())+"' is satisfiable.");
        mainLoop();
    }
    public void isSatisfiableFinished(AtomicConcept atomicConcept,boolean result) {
        super.isSatisfiableFinished(atomicConcept,result);
        m_output.println("'"+m_prefixes.abbreviateIRI(atomicConcept.getIRI())+"' is "+(result ? "" : "not ")+"satisfiable.");
        mainLoop();
        dispose();
    }
    public void isSubsumedByStarted(AtomicConcept subconcept,AtomicConcept superconcept) {
        super.isSubsumedByStarted(subconcept,superconcept);
        m_output.println("Will check whether '"+m_prefixes.abbreviateIRI(subconcept.getIRI())+"' is subsumed by '"+m_prefixes.abbreviateIRI(superconcept.getIRI())+"'.");
        mainLoop();
    }
    public void isSubsumedByFinished(AtomicConcept subconcept,AtomicConcept superconcept,boolean result) {
        super.isSubsumedByFinished(subconcept,superconcept,result);
        m_output.println("'"+m_prefixes.abbreviateIRI(subconcept.getIRI())+"' is "+(result ? "" : "not ")+"subsumed by '"+m_prefixes.abbreviateIRI(superconcept.getIRI())+"'.");
        mainLoop();
        dispose();
    }
    public void isABoxSatisfiableStarted() {
        super.isABoxSatisfiableStarted();
        m_output.println("Will check whether ABox is satisfiable.");
        mainLoop();
    }
    public void isABoxSatisfiableFinished(boolean result) {
        super.isABoxSatisfiableFinished(result);
        m_output.println("ABox is "+(result ? "" : "not ")+"satisfiable.");
        mainLoop();
        dispose();
    }
    public void isInstanceOfStarted(AtomicConcept concept,Individual individual) {
        super.isInstanceOfStarted(concept,individual);
        m_output.println("Will check whether '"+m_prefixes.abbreviateIRI(concept.getIRI())+"' is an instance of '"+m_prefixes.abbreviateIRI(individual.getIRI())+"'.");
        mainLoop();
    }
    public void isInstanceOfFinished(AtomicConcept concept,Individual individual,boolean result) {
        super.isInstanceOfFinished(concept,individual,result);
        m_output.println("'"+m_prefixes.abbreviateIRI(concept.getIRI())+"' is "+(result ? "" : "not ")+"an instance of '"+m_prefixes.abbreviateIRI(individual.getIRI())+"'.");
        mainLoop();
        dispose();
    }
    public void tableauCleared() {
        super.tableauCleared();
        m_nodeCreationInfos.clear();
        m_lastExistentialNode=null;
        m_lastExistentialConcept=null;
    }
    public void saturateStarted() {
        super.saturateStarted();
        m_currentIteration=0;
        if (m_singlestep) {
            m_output.println("Saturation starting...");
            mainLoop();
        }
    }
    public void iterationStarted() {
        super.iterationStarted();
        m_currentIteration++;
        if (m_singlestep) {
            m_output.println("Iteration "+m_currentIteration+" starts...");
            mainLoop();
        }
    }
    public void iterationFinished() {
        super.iterationFinished();
        if (m_singlestep) {
            m_output.println("Iteration "+m_currentIteration+" finished...");
        }
        if (System.currentTimeMillis()-m_lastStatusMark>m_breakpointTime) {
            printState();
            if (!m_forever)
                mainLoop();
            m_lastStatusMark=System.currentTimeMillis();
        }
    }
    public void clashDetected() {
        super.clashDetected();
        if (m_waitOptions.contains(WaitOption.CLASH)) {
            m_forever=false;
            m_output.println("Clash detected.");
            mainLoop();
        }
    }
    public void mergeStarted(Node mergeFrom,Node mergeInto) {
        super.mergeStarted(mergeFrom,mergeInto);
        if (m_waitOptions.contains(WaitOption.MERGE)) {
            m_forever=false;
            m_output.println("Node '"+mergeFrom.getNodeID()+"' will be merged into node '"+mergeInto.getNodeID()+"'.");
            mainLoop();
        }
    }
    public void existentialExpansionStarted(ExistentialConcept existentialConcept,Node forNode) {
        super.existentialExpansionStarted(existentialConcept,forNode);
        m_lastExistentialNode=forNode;
        m_lastExistentialConcept=existentialConcept;
    }
    public void existentialExpansionFinished(ExistentialConcept existentialConcept,Node forNode) {
        super.existentialExpansionFinished(existentialConcept,forNode);
        m_lastExistentialNode=null;
        m_lastExistentialConcept=null;
        if ((existentialConcept instanceof ExistsDescriptionGraph && m_waitOptions.contains(WaitOption.GRAPH_EXPANSION)) || (existentialConcept instanceof AtLeastConcept && m_waitOptions.contains(WaitOption.EXISTENTIAL_EXPANSION))) {
            m_forever=false;
            m_output.println(existentialConcept.toString(m_prefixes)+" expanded for node "+forNode.getNodeID());
            mainLoop();
        }
    }
    public void nodeCreated(Node node) {
        super.nodeCreated(node);
        m_nodeCreationInfos.put(node,new NodeCreationInfo(node,m_lastExistentialNode,m_lastExistentialConcept));
        if (m_lastExistentialNode!=null)
            m_nodeCreationInfos.get(m_lastExistentialNode).m_children.add(node);
    }
    public void nodeDestroyed(Node node) {
        super.nodeDestroyed(node);
        NodeCreationInfo nodeCreationInfo=m_nodeCreationInfos.remove(node);
        if (nodeCreationInfo.m_createdByNode!=null)
            m_nodeCreationInfos.get(nodeCreationInfo.m_createdByNode).m_children.remove(node);
    }
    public void datatypeCheckingStarted() {
        super.datatypeCheckingStarted();
        if (m_waitOptions.contains(WaitOption.DATATYPE_CHECKING)) {
            m_forever=false;
            m_output.println("Will check whether the datatype constraints are satisfiable.");
            mainLoop();
        }
    }
    public void blockingValidationFinished() {
        super.blockingValidationFinished();
        if (m_waitOptions.contains(WaitOption.BLOCKING_VALIDATION)) {
            m_forever=false;
            m_output.println("Blocking validated.");
            mainLoop();
        }
    }

    public static class NodeCreationInfo {
        public final Node m_node;
        public final Node m_createdByNode;
        public final ExistentialConcept m_createdByExistential;
        public final List<Node> m_children;

        public NodeCreationInfo(Node node,Node createdByNode,ExistentialConcept createdByExistential) {
            m_node=node;
            m_createdByNode=createdByNode;
            m_createdByExistential=createdByExistential;
            m_children=new ArrayList<Node>(4);
        }
    }
}
