package eisbw.actions;

import java.util.List;

import eis.iilang.Action;
import eis.iilang.Identifier;
import eis.iilang.Parameter;
import jnibwapi.JNIBWAPI;
import jnibwapi.Unit;
import jnibwapi.types.TechType;

/**
 * @author Danny & Harm - Use a researched TechType.
 *
 */
public class Use extends StarcraftTechAction {
	/**
	 * The Use constructor.
	 * 
	 * @param api
	 *            The BWAPI.
	 */
	public Use(JNIBWAPI api) {
		super(api);
	}

	@Override
	public void execute(Unit unit, Action action) {
		List<Parameter> parameters = action.getParameters();
		TechType techType = getTechType(((Identifier) parameters.get(0)).getValue());

		unit.useTech(techType);
	}

	@Override
	public String toString() {
		return "ability(Type)";
	}
}
