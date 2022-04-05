package tracks.singlePlayer.evaluacion.src_LUQUE_DELATORRE_FERNANDO;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;
import core.game.Observation;

import java.util.*;

public class AgentAStar extends AbstractPlayer {

	boolean think = true;
	Vector2d fescala;

	Node inicio, fin;

	PriorityQueue<Node> abiertos;
	ArrayList<Node> cerrados;
	Queue<ACTIONS> decisiones = new LinkedList<>();
	Stack<Node> camino = new Stack<>();
	boolean[][] libre;
	int consumo_memoria = 0;

	/**
	 * initialize all variables for the agent
	 * 
	 * @param stateObs     Observation of the current state.
	 * @param elapsedTimer Timer when the action returned is due.
	 */
	public AgentAStar(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
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

		// Inicializamos la cola con prioridad
		abiertos = new PriorityQueue<Node>(new NodeComparator());
		cerrados = new ArrayList<Node>();
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

	private int find(ArrayList<Node> l, Node u) {
		int pos = -1;
		for (int i=0; (i<l.size())&&(pos==-1); i++) {
			Node n = l.get(i);
			if ((n.column == u.column) && (n.row == u.row))
				pos = i;
		}
		return pos;
	}
	
	private void plan(StateObservation stateObs) {
		think = false;
		fescala = new Vector2d(stateObs.getWorldDimension().width / stateObs.getObservationGrid().length,
				stateObs.getWorldDimension().height / stateObs.getObservationGrid()[0].length);

		abiertos.add(inicio);
		Node actual = inicio;
		while (true) {
			actual = abiertos.poll();
			if ((actual.row == fin.row) && (actual.column == fin.column)) {
				break;
			}
			ArrayList<Node> sucesores = getSucesores(actual);
			for (Node sucesor : sucesores) {
				if ((actual.parent == null)||(sucesor.row != actual.parent.row) || (sucesor.column != actual.parent.column)) {
					sucesor.parent = actual;
					sucesor.g = actual.g + 1;
					sucesor.h = sucesor.heuristic(fin);
					sucesor.f = sucesor.g + sucesor.h;

					// Buscamos a sucesor en cerrados para, si está, actualizar su valor de g
					int pos = find(cerrados, sucesor);
					if (pos != -1) {
						if (sucesor.g < cerrados.get(pos).g) {
							cerrados.remove(pos);
							abiertos.add(sucesor);
						}
					} else {
						ArrayList<Node> open = new ArrayList<Node>(abiertos);
						int posabiertos = find(open, sucesor);
						if (posabiertos == -1)
							abiertos.add(sucesor);
						else if (sucesor.g < open.get(posabiertos).g){
							abiertos.remove(open.get(posabiertos));
							abiertos.add(sucesor);
						}
					}
				}
			}
			cerrados.add(actual);
			consumo_memoria = Math.max(consumo_memoria, abiertos.size()+cerrados.size());
		}

		// Recorremos el camino de vuelta
		while (actual != inicio) {
			camino.push(actual);
			actual = actual.parent;
		}
		camino.push(inicio);
		System.out.println(camino);

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
			plan(stateObs);
			System.out.println("Consumo de memoria: " + consumo_memoria + " nodos");
		}
		return decisiones.poll();
	}
}
