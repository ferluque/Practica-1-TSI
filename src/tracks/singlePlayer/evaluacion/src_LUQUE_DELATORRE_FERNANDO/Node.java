package tracks.singlePlayer.evaluacion.src_LUQUE_DELATORRE_FERNANDO;

class Node {
	int row, column;
	Node parent;
	boolean visited;
	
	Node() {
		visited = false;
		parent = null;
		row = -1;
		column = -1;
	}
	
	Node(int r, int c) {
		row = r;
		column = c;
		visited = false;
		parent = null;
	}
}
