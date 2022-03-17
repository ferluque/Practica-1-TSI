package tracks.singlePlayer.evaluacion.src_LUQUE_DELATORRE_FERNANDO;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;
import java.util.Queue;
import java.util.LinkedList;

import java.util.ArrayList;

public class Agent extends AbstractPlayer{

	private ArrayList<Types.ACTIONS> current_node;
	private Queue<ArrayList<Types.ACTIONS>> nodes_queue;
	private Vector2d myPos;
	
	
	/**
	 * initialize all variables for the agent
	 * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
	 */
	public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
		myPos = stateObs.getAvatarPosition();
		nodes_queue = new LinkedList<ArrayList<Types.ACTIONS>>();
		current_node = new ArrayList<Types.ACTIONS>();
	}
	
	/**
	 * return ACTION_NIL on every call to simulate doNothing player
	 * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
	 * @return 	ACTION_NIL all the time
	 */
	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		
		// Extraemos el nodo a expandir de la cola
		current_node = nodes_queue.remove();
		
		// Vemos a qué posiciones nos podemos mover		
		ArrayList<Types.ACTIONS> available = stateObs.getAvailableActions();
		
		// Guardamos la acción a tomar ahora que será la última de las que llevemos tomadas
		Types.ACTIONS ret = current_node.get(current_node.size()-1);
		
		if (available.contains(Types.ACTIONS.ACTION_UP)) {
			// Creamos el nuevo nodo como copia del actual
			ArrayList<Types.ACTIONS> new_node = new ArrayList<>(current_node);
			// Le añadimos la siguiente decisión
			new_node.add(Types.ACTIONS.ACTION_UP);
			// Lo añadimos a la cola de nodos
			nodes_queue.add(new_node);
		}
		if (available.contains(Types.ACTIONS.ACTION_DOWN)) {
			ArrayList<Types.ACTIONS> new_node = new ArrayList<>(current_node);
			new_node.add(Types.ACTIONS.ACTION_DOWN);
			nodes_queue.add(new_node);
		}
		if (available.contains(Types.ACTIONS.ACTION_LEFT)) {
			ArrayList<Types.ACTIONS> new_node = new ArrayList<>(current_node);
			new_node.add(Types.ACTIONS.ACTION_LEFT);
			nodes_queue.add(new_node);
		}
		if (available.contains(Types.ACTIONS.ACTION_RIGHT)) {
			ArrayList<Types.ACTIONS> new_node = new ArrayList<>(current_node);
			new_node.add(Types.ACTIONS.ACTION_RIGHT);
			nodes_queue.add(new_node);
		}
		
		return ret;
		
	}
}


