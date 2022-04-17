package tracks.singlePlayer.evaluacion.src_LUQUE_DELATORRE_FERNANDO;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;
import core.game.Observation;

import java.util.*;

public class AgentBFS extends AbstractPlayer {

	boolean think = true;
	Vector2d fescala;

	Node inicio, fin;

	Queue<Node> toExpand = new LinkedList<Node>();
	Queue<ACTIONS> decisiones = new LinkedList<>();
	Stack<Node> camino = new Stack<>();
	boolean[][] libre;
	boolean[][] visited;
	int nodos_expandidos = 0;
	int nodos_visitados = 0;
	/**
	 * initialize all variables for the agent
	 * 
	 * @param stateObs     Observation of the current state.
	 * @param elapsedTimer Timer when the action returned is due.
	 */
	public AgentBFS(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		fescala = new Vector2d(stateObs.getWorldDimension().width / stateObs.getObservationGrid().length,
				stateObs.getWorldDimension().height / stateObs.getObservationGrid()[0].length);

		// Almacenamos la posición de los portales convirtiendo de coordenadas píxel a
		// casilla
		ArrayList<Observation>[] posiciones = stateObs.getPortalsPositions();
		Vector2d portal = posiciones[0].get(0).position;

		fin = new Node();
		fin.row = (int) Math.floor(portal.y / fescala.y);
		fin.column = (int) Math.floor(portal.x / fescala.x);

		// Hacemos lo mismo con la posición inicial
		inicio = new Node();
		Vector2d posJugador = stateObs.getAvatarPosition();
		inicio.row = (int) Math.floor(posJugador.y / fescala.y);
		inicio.column = (int) Math.floor(posJugador.x / fescala.x);

		// Obtenemos la lista de posiciones inmóviles (muros y trampas)
		ArrayList<Observation> inmoviles = stateObs.getImmovablePositions()[0];
		inmoviles.addAll(stateObs.getImmovablePositions()[1]);

		ArrayList<Node> objetosInmoviles = new ArrayList<>();
		for (Observation i : inmoviles) {
			Vector2d pos = i.position;
			objetosInmoviles.add(new Node((int) (pos.y / fescala.y), (int) (pos.x / fescala.x)));
		}
		// Y las almacenamos en una matriz booleana (true si podemos movernos a la posición i,j)
		int r = (int) (stateObs.getWorldDimension().height / fescala.y);
		int c = (int) (stateObs.getWorldDimension().width / fescala.x);
		libre = new boolean[r][c];
		for (int i = 0; i < r; i++)
			for (int j = 0; j < c; j++)
				libre[i][j] = true;
		for (Node n : objetosInmoviles) {
			libre[n.row][n.column] = false;
		}
		// La lista de nodos visitados será unamatriz booleana
		visited = new boolean[r][c];
		for (int i = 0; i < r; i++)
			for (int j = 0; j < c; j++)
				visited[i][j] = false;

	}

	private ArrayList<Node> getSucesores(StateObservation stateObs, Node u) {
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

	private void plan(StateObservation stateObs) {
		think = false;
		fescala = new Vector2d(stateObs.getWorldDimension().width / stateObs.getObservationGrid().length,
				stateObs.getWorldDimension().height / stateObs.getObservationGrid()[0].length);

		// Inicializamos la cola con el nodo inicial
		inicio.parent = null;
		visited[inicio.row][inicio.column] = true;
		nodos_visitados++;
		toExpand.offer(inicio);
		Node u = inicio;
		// Siempre que queden hijos 
		while (!toExpand.isEmpty()) {
			u = toExpand.poll();
			nodos_expandidos++;
			// SI no hemos llegado al final
			if ((u.row == fin.row) && (u.column == fin.column))
				break;
			else {
				// Añadimos los sucesores a la cola, almacenando su padre
				ArrayList<Node> sucesores = getSucesores(stateObs, u); 
				for (Node v : sucesores) {
					// Siempre que sean una posicion que se pueda visitar (ni muro ni trampa)
					if (!visited[v.row][v.column]) {
						visited[v.row][v.column] = true;
						nodos_visitados++;
						v.parent = u;
						toExpand.offer(v);
					}
				}
			}
		}
		// Recorremos el camino de vuelta almacenando los nodos
		Node actual = u;
		while (actual != inicio) {
			camino.push(actual);
			actual = actual.parent;
		}
		camino.push(inicio);
		
		System.out.println("Tamaño del camino: "+ camino.size());

		Node siguiente;
		// Una vez conocidos los nodos averiguamos la secuencia de acciones
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
			System.out.println("Tiempo en milisegundos: " + (tFin-tInicio)/1e6);
			System.out.println("Nodos expandidos: " + nodos_expandidos);
			System.out.println("Nodos visitados: " + nodos_visitados);
		}
		return decisiones.poll();
	}
}
