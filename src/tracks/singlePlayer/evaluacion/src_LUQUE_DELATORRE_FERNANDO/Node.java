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
	
	@Override
	public String toString() {
		return "("+row+","+column+")";
	}
}
