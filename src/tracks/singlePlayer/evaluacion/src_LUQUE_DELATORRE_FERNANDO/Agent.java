package tracks.singlePlayer.evaluacion.src_LUQUE_DELATORRE_FERNANDO;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.util.ArrayList;



public class Agent extends AbstractPlayer{

	boolean think = true;
	int pos_inicial;
	float fescala;
	
	
	/**
	 * initialize all variables for the agent
	 * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
	 */
	public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
		fescala = stateObs.getWorldDimension().height
	}
	
	private void plan() {
		
	}
	
	private int posToId (Vector2d pos, int ancho_mapa) {
		// Tenemos que dividir entre 50.0 para obtener la posición en el grid	
		int row = (int)pos.y/50;
		int column = (int)pos.x/50;
//		System.out.println(row+ ", "+column);
//		System.out.println(ancho_mapa);
		return row*ancho_mapa+column;
	}
	
	private Vector2d IdtoPos (int id, int ancho_mapa) {
		Vector2d pos = new Vector2d(id%ancho_mapa, id/ancho_mapa);
		return pos;
	}
	
	/**
	 * return ACTION_NIL on every call to simulate doNothing player
	 * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
	 * @return 	ACTION_NIL all the time
	 */
	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
//		if (think)
//			plan();
//		int row = (int)stateObs.getAvatarPosition().y/50;
//		int column = (int)stateObs.getAvatarPosition().x/50;
//		System.out.println(row+ ", "+column);
		
		return ACTIONS.ACTION_LEFT;
	}
}


