package FlowGraph;

import Assem.Instr;
import Assem.InstrList;
import Assem.LABEL;
import Assem.MOVE;
import Assem.Targets;
import Graph.Node;
import Graph.NodeList;
import Temp.LabelList;
import Temp.TempList;

public class AssemFlowGraph extends FlowGraph{
	java.util.HashMap<Node,Instr> ht;

	public Instr instr(Node n) {
		return (Instr) ht.get(n);
	}

	public TempList def(Node node) {
		return instr(node).def();
	}

	public TempList use(Node node) {
		return instr(node).use();
	}

	public boolean isMove(Node node) {
		return instr(node) instanceof MOVE;
	}

	public AssemFlowGraph(InstrList instrs) {
		ht = new java.util.HashMap<Node,Instr>();
		java.util.HashMap<Temp.Label,Node> l2n = new java.util.HashMap<Temp.Label,Node>();

		// Add all instructions as nodes
		for (InstrList p = instrs; p != null; p = p.tail) {
			Node n = new Node(this);
            ht.put(n, p.head);
			if (p.head instanceof LABEL)
				l2n.put(((LABEL) p.head).label, n);
		}
		// Add edges from each branch instruction node to target node(s)
		for (NodeList p = nodes(); p != null; p = p.tail) {
			Node n = p.head;
			Targets jumps = (Targets) instr(n).jumps();
			if (jumps == null && p.tail != null) {
				addEdge(n, p.tail.head); // Fall through with edge to next
				// instruction
			} else if (jumps != null) { // Jumps - Edge to target label node
				LabelList l = jumps.labels;
				while (l != null) {
					addEdge(n, (Node) l2n.get(l.head));
					l = l.tail;
				}
			}
		}
               
                
	}
}
