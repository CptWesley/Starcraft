package eisbw.percepts;

import java.util.List;

import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.ParameterList;
import eis.iilang.Percept;

/**
 * @author Danny & Harm - The Enemy percept which gives information about the
 *         opponent's units.
 *
 */
public class EnemyPercept extends Percept {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor EnemyPercept.
	 *
	 * @param type
	 *            The unit type
	 * @param id
	 *            The ID of the unit
	 * @param health
	 *            The amount of health of the unit
	 * @param shields
	 *            The amount of shields of the unit
	 * @param energy
	 *            The amount of energy of the unit
	 * @param conditions
	 *            The current conditions of the unit
	 * @param x
	 *            The X coordinate of the location of the unit
	 * @param y
	 *            The Y coordinate of the location of the unit
	 */
	public EnemyPercept(String type, int id, int health, int shields, int energy, List<Parameter> conditions, int x,
			int y, int region) {
		super(Percepts.ENEMY, new Identifier(type), new Numeral(id), new Numeral(health), new Numeral(energy),
				new Numeral(shields), new ParameterList(conditions), new Numeral(x), new Numeral(y),
				new Numeral(region));
	}
}
