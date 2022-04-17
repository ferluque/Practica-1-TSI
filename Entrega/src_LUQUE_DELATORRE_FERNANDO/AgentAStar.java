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
//	ArrayList<Node> cerrados;
	Queue<ACTIONS> decisiones = new LinkedList<>();
	Stack<Node> camino = new Stack<>();
	boolean[][] libre;
	Node[][] open; 
	Node[][] cerrados;
	int nodos_cerrados = 0;
	int nodos_expandidos = 0;
	int num_maximo_memoria = 0;

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
		// Inicializamos la matriz de abiertos y de cerrados todos a nulo
		open = new Node[r][c];
		cerrados = new Node[r][c];
		for (int i=0; i<r; i++)
			for (int j=0; j<c; j++)
				open[i][j] = cerrados[i][j]= null;
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
	
	private void plan(StateObservation stateObs) {
		think = false;
		fescala = new Vector2d(stateObs.getWorldDimension().width / stateObs.getObservationGrid().length,
				stateObs.getWorldDimension().height / stateObs.getObservationGrid()[0].length);

		// Añadimos el inicial a la cola y a la matriz
		abiertos.add(inicio);
		open[inicio.row][inicio.column] = inicio;
		Node actual = inicio;
		while (true) {
			// Mientras no lleguemos al final, extraemos de la cola, que ordenará los nodos
			// Según el comparador implementado, que compara, primero f, después g y si coinciden,
			// se visitan en orden fifo (arriba,abajo,izq,derecha)
			actual = abiertos.poll();
			if ((actual.row == fin.row) && (actual.column == fin.column)) {
				break;
			}
			ArrayList<Node> sucesores = getSucesores(actual);
			nodos_expandidos++;
			for (Node sucesor : sucesores) {
				if ((actual.parent == null)||(sucesor.row != actual.parent.row) || (sucesor.column != actual.parent.column)) {
					// Calculamos los valores de la heurística
					sucesor.parent = actual;
					sucesor.g = actual.g + 1;
					sucesor.h = sucesor.heuristic(fin);
					sucesor.f = sucesor.g + sucesor.h;

					// Buscamos a sucesor en cerrados para, si está, actualizar su valor de g
										
					if (cerrados[sucesor.row][sucesor.column] != null) { // Si está en cerrados
						// Actualizamos el valor de c si procede, lo sacamos de cerrados y lo metemos
						// en abiertos
						if (sucesor.g < cerrados[sucesor.row][sucesor.column].g) {
							cerrados[sucesor.row][sucesor.column] = null;
							nodos_cerrados--;
							abiertos.add(sucesor);
							open[sucesor.row][sucesor.column] = sucesor;
						}
					} else {
						// Si no esta en cerrados
						// ni en abiertos
						// Lo metemos en abiertos
						if (open[sucesor.row][sucesor.column] == null) {
							abiertos.add(sucesor);
							open[sucesor.row][sucesor.column] = sucesor;
						}
						// Si está en abiertos y podemos actualizar su valor de g lo hacemos
						else if (sucesor.g < open[sucesor.row][sucesor.column].g) {
							abiertos.remove(open[sucesor.row][sucesor.column]);
							abiertos.add(sucesor);
							open[sucesor.row][sucesor.column] = sucesor;
						}
					}
				}
			}
			// Lo añadimos a cerrados 
			cerrados[actual.row][actual.column] = actual;
			nodos_cerrados++;
			num_maximo_memoria = Math.max(num_maximo_memoria, abiertos.size()+nodos_cerrados);
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
		System.out.println("Tamaño de la ruta: "+camino.size());
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
			System.out.println("Tiempo acumulado: "+(tFin-tInicio)/1e6);
			System.out.println("Número de nodos expamdidos: "+nodos_expandidos);
			System.out.println("Consumo máximo de memoria: "+num_maximo_memoria);
		}
		return decisiones.poll();
	}
}
