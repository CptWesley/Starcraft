package eisbw.percepts.perceivers;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eis.eis2java.translation.Filter;
import eis.iilang.Percept;
import eisbw.BwapiUtility;
import eisbw.percepts.DefensiveMatrixPercept;
import eisbw.percepts.Percepts;
import eisbw.percepts.ResourcesPercept;
import eisbw.percepts.SelfPercept;
import eisbw.percepts.SpaceProvidedPercept;
import eisbw.percepts.StatusPercept;
import eisbw.percepts.UnitLoadedPercept;
import eisbw.units.ConditionHandler;
import jnibwapi.JNIBWAPI;
import jnibwapi.Player;
import jnibwapi.Position;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;

/**
 * @author Danny & Harm - The perceiver which handles all the generic percepts.
 *
 */
public class GenericUnitPerceiver extends UnitPerceiver {
	/**
	 * @param api
	 *            The BWAPI.
	 * @param unit
	 *            The perceiving unit.
	 */
	public GenericUnitPerceiver(JNIBWAPI api, Unit unit) {
		super(api, unit);
	}

	@Override
	public Map<PerceptFilter, Set<Percept>> perceive(Map<PerceptFilter, Set<Percept>> toReturn) {
		defensiveMatrixPercept(toReturn);
		resourcesPercept(toReturn);
		selfPercept(toReturn);
		statusPercept(toReturn);

		if (unit.getType().getSpaceProvided() > 0) {
			List<Unit> loadedUnits = unit.getLoadedUnits();
			spaceProvidedPercept(toReturn, loadedUnits);
			unitLoadedPercept(toReturn, loadedUnits);
		}

		return toReturn;
	}

	/**
	 * @param toReturn
	 *            The percept and reference of which kind of percept it is.
	 */
	private void statusPercept(Map<PerceptFilter, Set<Percept>> toReturn) {
		Set<Percept> statusPercept = new HashSet<>(1);
		Position pos = unit.getPosition();
		statusPercept.add(new StatusPercept(unit.getHitPoints(), unit.getShields(), unit.getEnergy(),
				new ConditionHandler(api, unit).getConditions(), pos.getBX(), pos.getBY()));
		toReturn.put(new PerceptFilter(Percepts.STATUS, Filter.Type.ON_CHANGE), statusPercept);
	}

	/**
	 * @param toReturn
	 *            The percept and reference of which kind of percept it is.
	 */
	private void selfPercept(Map<PerceptFilter, Set<Percept>> toReturn) {
		Set<Percept> selfPercept = new HashSet<>(1);
		UnitType type = unit.getType();
		selfPercept.add(new SelfPercept(unit.getID(), BwapiUtility.getUnitType(unit), type.getMaxHitPoints(),
				type.getMaxShields(), type.getMaxEnergy()));
		toReturn.put(new PerceptFilter(Percepts.SELF, Filter.Type.ONCE), selfPercept);
	}

	/**
	 * @param toReturn
	 *            The percept and reference of which kind of percept it is.
	 */
	private void resourcesPercept(Map<PerceptFilter, Set<Percept>> toReturn) {
		Set<Percept> resourcePercept = new HashSet<>(1);
		Player self = api.getSelf();
		resourcePercept.add(
				new ResourcesPercept(self.getMinerals(), self.getGas(), self.getSupplyUsed(), self.getSupplyTotal()));
		toReturn.put(new PerceptFilter(Percepts.RESOURCES, Filter.Type.ON_CHANGE), resourcePercept);
	}

	/**
	 * @param toReturn
	 *            The percept and reference of which kind of percept it is.
	 */
	private void defensiveMatrixPercept(Map<PerceptFilter, Set<Percept>> toReturn) {
		Set<Percept> defensiveMatrixPercept = new HashSet<>(1);
		if (unit.isDefenseMatrixed()) {
			defensiveMatrixPercept.add(new DefensiveMatrixPercept(unit.getDefenseMatrixPoints()));
			toReturn.put(new PerceptFilter(Percepts.DEFENSIVEMATRIX, Filter.Type.ALWAYS), defensiveMatrixPercept);
		}
	}

	/**
	 * @param toReturn
	 *            The percept and reference of which kind of percept it is.
	 * @param loadedUnits
	 *            The loaded units
	 */
	private void unitLoadedPercept(Map<PerceptFilter, Set<Percept>> toReturn, List<Unit> loadedUnits) {
		Set<Percept> unitLoadedPercept = new HashSet<>(loadedUnits.size());
		for (Unit u : loadedUnits) {
			if (u != null) {
				unitLoadedPercept.add(new UnitLoadedPercept(u.getID(), u.getType().getName()));
			}
		}
		if (!unitLoadedPercept.isEmpty()) {
			toReturn.put(new PerceptFilter(Percepts.UNITLOADED, Filter.Type.ALWAYS), unitLoadedPercept);
		}
	}

	/**
	 * @param toReturn
	 *            The percept and reference of which kind of percept it is.
	 * @param loadedUnits
	 *            The loaded units
	 */
	private void spaceProvidedPercept(Map<PerceptFilter, Set<Percept>> toReturn, List<Unit> loadedUnits) {
		Set<Percept> spaceProvidedPercept = new HashSet<>(1);
		spaceProvidedPercept.add(new SpaceProvidedPercept(loadedUnits.size(), unit.getType().getSpaceProvided()));
		toReturn.put(new PerceptFilter(Percepts.SPACEPROVIDED, Filter.Type.ON_CHANGE), spaceProvidedPercept);
	}
}
