package tracks.singlePlayer.evaluacion.src_LUQUE_DELATORRE_FERNANDO;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;
import core.game.Observation;

import java.util.*;

public class AgentIDAStar extends AbstractPlayer {

	boolean think = true;
	Vector2d fescala;

	Node inicio, fin;

	Queue<ACTIONS> decisiones = new LinkedList<>();
	Stack<Node> camino = new Stack<>();
	boolean[][] libre;
	boolean[][] inRuta;
	ArrayList<Node> ruta;
	
	int nodos_expandidos = 0;
	int consumo_maximo = 0;
	int prof = 0;

	/**
	 * initialize all variables for the agent
	 * 
	 * @param stateObs     Observation of the current state.
	 * @param elapsedTimer Timer when the action returned is due.
	 */
	public AgentIDAStar(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		fescala = new Vector2d(stateObs.getWorldDimension().width / stateObs.getObservationGrid().length,
				stateObs.getWorldDimension().height / stateObs.getObservationGrid()[0].length);

		// Almacenamos la posición de los portales convirtiendo de coordenadas píxel a
		// casilla
		ArrayList<Observation>[] posiciones = stateObs.getPortalsPositions();
		Vector2d portal = posiciones[0].get(0).position;

		fin = new Node();
		fin.row = (int) Math.floor(portal.y / fescala.y);
		fin.column = (int) Math.floor(portal.x / fescala.x);
		fin.f = fin.g = fin.h = 0;

		// Hacemos lo mismo con la posición inicial
		inicio = new Node();
		Vector2d posJugador = stateObs.getAvatarPosition();
		inicio.row = (int) Math.floor(posJugador.y / fescala.y);
		inicio.column = (int) Math.floor(posJugador.x / fescala.x);

		inicio.g = 0;
		inicio.h = inicio.heuristic(fin);
		inicio.f = inicio.h;

		ArrayList<Observation> inmoviles = stateObs.getImmovablePositions()[0];
		inmoviles.addAll(stateObs.getImmovablePositions()[1]);

		ArrayList<Node> objetosInmoviles = new ArrayList<>();
		for (Observation i : inmoviles) {
			Vector2d pos = i.position;
			objetosInmoviles.add(new Node((int) (pos.y / fescala.y), (int) (pos.x / fescala.x)));
		}
		int r = (int) (stateObs.getWorldDimension().height / fescala.y);
		int c = (int) (stateObs.getWorldDimension().width / fescala.x);
		libre = new boolean[r][c];
		for (int i = 0; i < r; i++)
			for (int j = 0; j < c; j++)
				libre[i][j] = true;
		for (Node n : objetosInmoviles) {
			libre[n.row][n.column] = false;
		}
		inRuta = new boolean[r][c];
		for (int i=0; i<r; i++)
			for (int j=0; j<c; j++)
				inRuta[i][j] = false;
		ruta = new ArrayList<>();
	}

	private ArrayList<Node> getSucesores(Node u) {
		ArrayList<Node> sucesores = new ArrayList<>();
		if (libre[u.row - 1][u.column])
			sucesores.add(new Node(u.row - 1, u.column));
		if (libre[u.row + 1][u.column])
			sucesores.add(new Node(u.row + 1, u.column));
		if (libre[u.row][u.column - 1])
			sucesores.add(new Node(u.row, u.column - 1));
		if (libre[u.row][u.column + 1])
			sucesores.add(new Node(u.row, u.column + 1));

		return sucesores;
	}

//	private int find(ArrayList<Node> l, Node u) {
//		int pos = -1;
//		for (int i=0; (i<l.size())&&(pos==-1); i++) {
//			Node n = l.get(i);
//			if ((n.column == u.column) && (n.row == u.row))
//				pos = i;
//		}
//		return pos;
//	}
	
	private int search(int g, int cota) {
		// expandimos el último nodo de la ruta
		Node nodo = ruta.get(ruta.size()-1);
		int f = g+nodo.heuristic(fin);
		// Si es mayor que la cota paramos
		if (f>cota) return f;
		// Si no, si es el final
		if ((nodo.row == fin.row) && (nodo.column == fin.column)) {
			// Nos quedamos con la ruta y la almacenamos
			while (!ruta.isEmpty()) {
				camino.push(ruta.remove(ruta.size()-1));
			}
			System.out.println(camino);
			return -1;
		}
		int min = (int)Double.POSITIVE_INFINITY;
		ArrayList<Node> sucesores = getSucesores(nodo);
		nodos_expandidos++;
		// Obtenemos los vecinos del nodo actual
		for (int i=0; (i<sucesores.size())&&!ruta.isEmpty();++i) {
			Node v = sucesores.get(i);
			if (!inRuta[v.row][v.column]) {
				// Buscamos en las rutas con cada uno de los sucesores
				ruta.add(v);
				inRuta[v.row][v.column] = true;
				prof++;
				consumo_maximo = Math.max(prof, consumo_maximo);
				int t = search(g+1,cota);
				prof--;
				if (t<min) min = t;
				// Añadimos esta condición para cuando hayamos alcanzado el final y esté la ruta vacía
				if (!ruta.isEmpty()) {
					ruta.remove(ruta.size()-1);
				}
				inRuta[v.row][v.column] = false;
			}
		}
		return min;
	}
	
	private void plan(StateObservation stateObs) {
		think = false;
		fescala = new Vector2d(stateObs.getWorldDimension().width / stateObs.getObservationGrid().length,
				stateObs.getWorldDimension().height / stateObs.getObservationGrid()[0].length);

		Node actual = inicio;
		int cota = actual.heuristic(fin);
		ruta.add(actual);
		inRuta[actual.row][actual.column] = true;
		int t=0;
		while (t != -1) {
			t = search(0, cota);
			cota = t;
		}
				
		Node siguiente;
		// Una vez conocidos los nodos averiguamos la secuencia de acciones
		System.out.println("Tamaño de la ruta: " + camino.size());
		actual = camino.pop();
		while (!camino.isEmpty()) {
			siguiente = camino.pop();

			// Separamos movimientos horizontales de verticales
			// Horizontales (no se mueve en la columna
			if (actual.row == siguiente.row) {
				// Ha avanzado a la derecha
				if (actual.column < siguiente.column)
					decisiones.offer(ACTIONS.ACTION_RIGHT);
				else
					decisiones.offer(ACTIONS.ACTION_LEFT);
			} else {
				if (actual.row > siguiente.row)
					decisiones.offer(ACTIONS.ACTION_UP);
				else
					decisiones.offer(ACTIONS.ACTION_DOWN);
			}
			actual = siguiente;
		}
	}

	/**
	 * return ACTION_NIL on every call to simulate doNothing player
	 * 
	 * @param stateObs     Observation of the current state.
	 * @param elapsedTimer Timer when the action returned is due.
	 * @return ACTION_NIL all the time
	 */
	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		if (think) {
			long tInicio = System.nanoTime();
			plan(stateObs);
			long tFin = System.nanoTime();
			System.out.println("Tiempo empleado: "+(tFin-tInicio)/1e6);
			System.out.println("Nodos expandidos: "+nodos_expandidos);
			System.out.println("Consumo máximo: " + consumo_maximo);
		}
		return decisiones.poll();
	}
}
