package tracks.singlePlayer.evaluacion.src_LUQUE_DELATORRE_FERNANDO;

import java.util.*; 

class Node {
	int row, column;
	Node parent;
	int f;
	int g;
	int h;
	
	Node() {
		parent = null;
		row = -1;
		column = -1;
	}
	
	Node(int r, int c) {
		row = r;
		column = c;
		parent = null;
	}
	
	Node(Node another) {
		this.row = another.row;
		this.column = another.column;
		this.parent = another.parent;
	}
	
	@Override
	public String toString() {
		return "("+row+","+column+")";
	}
	
	int heuristic(Node another) {
		return Math.abs(another.column-this.column)+Math.abs(another.row-this.row);
	}
}

class NodeComparator implements Comparator<Node> {
	public int compare(Node n1, Node n2) {
		if (n1.f>n2.f)
			return 1;
		else if (n1.f<n2.f)
			return -1;
		else {
			if (n1.g>n2.g)
				return 1;
			else if (n1.g<n2.g)
				return -1;
		}
		return 0;
	}
}
