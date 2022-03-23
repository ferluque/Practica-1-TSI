package tracks.singlePlayer.evaluacion.src_LUQUE_DELATORRE_FERNANDO;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;
import core.game.Observation;

import java.util.*;



public class Agent extends AbstractPlayer{

	boolean think = true;
	Vector2d fescala;
	
	Node inicio, fin;
	
	Queue<Node> toExpand = new LinkedList<Node>();
	Queue<ACTIONS> decisiones = new LinkedList<>();
	Stack<Node> camino = new Stack<>();
	
	
	/**
	 * initialize all variables for the agent
	 * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
	 */
	public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
		fescala = new Vector2d(stateObs.getWorldDimension().width / stateObs.getObservationGrid().length,
				stateObs.getWorldDimension().height / stateObs.getObservationGrid()[0].length);
		
		// Almacenamos la posición de los portales convirtiendo de coordenadas píxel a casilla
		ArrayList<Observation>[] posiciones = stateObs.getPortalsPositions();
		Vector2d portal = posiciones[0].get(0).position;
		
		fin = new Node();
		fin.row = (int)Math.floor(portal.y / fescala.y);
		fin.column = (int)Math.floor(portal.x / fescala.x);
		
		// Hacemos lo mismo con la posición inicial
		inicio = new Node();
		Vector2d posJugador = stateObs.getAvatarPosition();
		inicio.row = (int)Math.floor(posJugador.y/fescala.y);
		inicio.column = (int)Math.floor(posJugador.x/fescala.x);
		
		System.out.println("Estoy creado");
	}
	
	private ArrayList<Node> getSucesores(StateObservation stateObs, Node u) {
		ArrayList<Node> sucesores = new ArrayList<>();
		// Me estaba devolviendo todas las direcciones no solo las posibles
		
		ArrayList<Observation>[] moviles = stateObs.getMovablePositions();
		
		
		
		for (ACTIONS a: posiblesAcciones) {
			switch (a) {
				case ACTION_UP:sucesores.add(new Node(u.row+1, u.column));break;
				case ACTION_DOWN:sucesores.add(new Node(u.row-1, u.column));break;
				case ACTION_LEFT:sucesores.add(new Node(u.row, u.column-1));break;
				case ACTION_RIGHT:sucesores.add(new Node(u.row, u.column+1));break;
				default:break;
			}		
		}
		return sucesores;		
	}
	
	private void plan(StateObservation stateObs) {
		think = false;
		fescala = new Vector2d(stateObs.getWorldDimension().width / stateObs.getObservationGrid().length,
				stateObs.getWorldDimension().height / stateObs.getObservationGrid()[0].length);
		
		// Implementamos la búsqueda en profundidad
		inicio.visited = true;
		inicio.parent = null;
		toExpand.offer(inicio);
		Node u=inicio;
		while (!toExpand.isEmpty()) {
			u = toExpand.poll();
			if (u==fin) 
				break;
			else {
				ArrayList<Node> sucesores = getSucesores(stateObs, u);
				for (Node v: sucesores) {
					if (!v.visited) {
						v.visited = true;
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
			actual = u.parent;
		}
		camino.push(inicio);
		
		Node siguiente = new Node();
		// Una vez conocidos los nodos averiguamos la secuencia de acciones
		while (!camino.isEmpty()) {
			actual = camino.pop();
			siguiente = camino.pop();
			
			// Separamos movimientos horizontales de verticales
			// Horizontales (no se mueve en la columna
			if (actual.row == siguiente.row) {
				// Ha avanzado a la derecha
				if (actual.column < siguiente.column)
					decisiones.offer(ACTIONS.ACTION_RIGHT);
				else
					decisiones.offer(ACTIONS.ACTION_LEFT);
			}
			else {
				if (actual.row < siguiente.row)
					decisiones.offer(ACTIONS.ACTION_UP);
				else
					decisiones.offer(ACTIONS.ACTION_DOWN);
			}
		}
	}
	
	/**
	 * return ACTION_NIL on every call to simulate doNothing player
	 * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
	 * @return 	ACTION_NIL all the time
	 */
	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		System.out.println("Estoy pensando");
		if (think)
			plan(stateObs);
//		int row = (int)stateObs.getAvatarPosition().y/50;
//		int column = (int)stateObs.getAvatarPosition().x/50;
//		System.out.println(row+ ", "+column);
		
		System.out.println("Hola");
		
		return decisiones.poll();
	}
}


