package tracks.singlePlayer.evaluacion.src_LUQUE_DELATORRE_FERNANDO;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;
import core.game.Observation;

import java.util.*;

public class AgentRTAStar extends AbstractPlayer {

	boolean think = true;
	Vector2d fescala;

	Node inicio, fin;
	Node actual;

	boolean[][] libre;
	int[][] heuristics;
	int consumo_memoria = 0;
	
	long tAcumulado = (long)0.0;

	/**
	 * initialize all variables for the agent
	 * 
	 * @param stateObs     Observation of the current state.
	 * @param elapsedTimer Timer when the action returned is due.
	 */
	public AgentRTAStar(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
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

		actual = inicio;

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
		
		heuristics = new int[r][c];
		for (int i=0; i<r; i++) {
			for (int j=0; j<c; j++) {
				Node n = new Node(i,j);
				heuristics[i][j] = n.heuristic(fin);
			}
		}
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
	
	private Types.ACTIONS think() {
		ArrayList<Node> sucesores = getSucesores(actual);
		for (int i = 0; i < sucesores.size(); i++) {
			sucesores.get(i).h = heuristics[sucesores.get(i).row][sucesores.get(i).column];
			sucesores.get(i).g = actual.g+1;
			sucesores.get(i).f = sucesores.get(i).g+sucesores.get(i).h;
		}
		// Escogemos el mejor para que sea el siguiente
		Node better_option = sucesores.get(0);
		for (int i=0; i<sucesores.size(); i++) {
			if (better_option.f>sucesores.get(i).f)
				better_option = sucesores.get(i);
		}
		// Sacamos la mejor opción para quedarnos ahora con la segunda para actualizar la heurística del nodo actual
		sucesores.remove(better_option);
		// Si está vacío, el segundo mínimo es igual que el primero
		Node second_minimum;
		if (!sucesores.isEmpty()) {
			second_minimum=sucesores.get(0);
			for (int i=1; i<sucesores.size(); i++)
				if (second_minimum.f > sucesores.get(i).f)
					second_minimum = sucesores.get(i);
		}
		else
			second_minimum = better_option;
		// Actualizamos la heurística del nodo actual
		if (heuristics[actual.row][actual.column] < second_minimum.f) {
			heuristics[actual.row][actual.column] = second_minimum.f;
		}
		// Separamos movimientos horizontales de verticales
		// Horizontales (no se mueve en la columna
		Types.ACTIONS accion;
		if (actual.row == better_option.row) {
			// Ha avanzado a la derecha
			if (actual.column < better_option.column)
				accion = ACTIONS.ACTION_RIGHT;
			else
				accion = ACTIONS.ACTION_LEFT;
		} else {
			if (actual.row > better_option.row)
				accion = ACTIONS.ACTION_UP;
			else
				accion = ACTIONS.ACTION_DOWN;
		}
		actual = better_option;
		
		return accion;
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
		long tInicio = System.nanoTime();
		Types.ACTIONS accion = think();
		long tFin = System.nanoTime();
		tAcumulado += (tFin-tInicio)/(long)1e3;
		
		System.out.println("Tiempo acumulado: " + tAcumulado);
		
		
		return accion;
	}
}
