package tracks.singlePlayer.evaluacion.src_LUQUE_DELATORRE_FERNANDO;

class Node {
	int row, column;
	Node parent;
	
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
}
