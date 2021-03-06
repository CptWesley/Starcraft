package eisbw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import eis.eis2java.translation.Filter;
import eis.exceptions.ManagementException;
import eis.iilang.Percept;
import eisbw.debugger.draw.IDraw;
import eisbw.percepts.NukePercept;
import eisbw.percepts.Percepts;
import eisbw.percepts.WinnerPercept;
import eisbw.percepts.perceivers.ConstructionSitePerceiver;
import eisbw.percepts.perceivers.MapPerceiver;
import eisbw.percepts.perceivers.PerceptFilter;
import eisbw.percepts.perceivers.UnitsPerceiver;
import eisbw.units.StarcraftUnit;
import eisbw.units.Units;
import jnibwapi.JNIBWAPI;
import jnibwapi.Position;
import jnibwapi.Unit;

/**
 * @author Danny & Harm - The game class where the percepts are updated.
 *
 */
public class Game {
	protected final StarcraftEnvironmentImpl env;
	protected Units units; // overriden in test
	protected final Map<String, IDraw> draws;
	protected volatile Map<String, Map<PerceptFilter, List<Percept>>> percepts;
	protected volatile Map<PerceptFilter, List<Percept>> mapPercepts;
	protected volatile Map<PerceptFilter, List<Percept>> constructionPercepts;
	protected volatile Map<PerceptFilter, List<Percept>> nukePercepts;
	protected volatile Map<PerceptFilter, List<Percept>> endGamePercepts;
	private final Map<String, Map<String, List<Percept>>> previous;

	/**
	 * Constructor.
	 *
	 * @param environment
	 *            - the environment
	 */
	public Game(StarcraftEnvironmentImpl environment) {
		this.env = environment;
		this.units = new Units(environment);
		this.draws = new ConcurrentHashMap<>();
		this.percepts = new ConcurrentHashMap<>();
		this.previous = new ConcurrentHashMap<>();
	}

	public void mapAgent() {
		if (this.env.mapAgent()) {
			this.env.addToEnvironment("mapAgent", "mapAgent");
		}
	}

	public StarcraftEnvironmentImpl getEnvironment() {
		return this.env;
	}

	public int getAgentCount() {
		return this.env.getAgents().size();
	}

	public Units getUnits() {
		return this.units;
	}

	public List<IDraw> getDraws() {
		return new ArrayList<>(this.draws.values());
	}

	/**
	 * Updates the percepts.
	 *
	 * @param bwapi
	 *            - the game bridge
	 */
	public void update(JNIBWAPI bwapi) {
		processUninitializedUnits();
		Map<String, Map<PerceptFilter, List<Percept>>> unitPerceptHolder = new HashMap<>();
		Map<PerceptFilter, List<Percept>> globalPercepts = getGlobalPercepts(bwapi);
		for (Unit unit : bwapi.getMyUnits()) {
			StarcraftUnit scUnit = this.units.getStarcraftUnit(unit);
			if (scUnit == null) {
				continue;
			}
			Map<PerceptFilter, List<Percept>> thisUnitPercepts = new HashMap<>(scUnit.perceive());
			if (!this.env.mapAgent()) {
				thisUnitPercepts.putAll(globalPercepts); // UnitsPerceiver
				if (scUnit.isWorker() && this.constructionPercepts != null) {
					thisUnitPercepts.putAll(this.constructionPercepts);
				}
				if (this.mapPercepts != null) {
					thisUnitPercepts.putAll(this.mapPercepts);
				}
				if (this.nukePercepts != null) {
					thisUnitPercepts.putAll(this.nukePercepts);
				}
				if (this.endGamePercepts != null) {
					thisUnitPercepts.putAll(this.endGamePercepts);
				}
			}
			unitPerceptHolder.put(this.units.getUnitName(unit.getID()), thisUnitPercepts);
		}
		if (this.env.mapAgent()) {
			Map<PerceptFilter, List<Percept>> thisUnitPercepts = new HashMap<>(globalPercepts);
			if (this.constructionPercepts != null) {
				thisUnitPercepts.putAll(this.constructionPercepts);
			}
			if (this.mapPercepts != null) {
				thisUnitPercepts.putAll(this.mapPercepts);
			}
			if (this.nukePercepts != null) {
				thisUnitPercepts.putAll(this.nukePercepts);
			}
			if (this.endGamePercepts != null) {
				thisUnitPercepts.putAll(this.endGamePercepts);
			}
			unitPerceptHolder.put("mapAgent", thisUnitPercepts);
		}
		this.percepts = unitPerceptHolder;
	}

	private void processUninitializedUnits() {
		if (this.units.getUninitializedUnits() != null) {
			List<Unit> toAdd = new LinkedList<>();
			Unit unit;
			while ((unit = this.units.getUninitializedUnits().poll()) != null) {
				String unitName = BwapiUtility.getName(unit);
				if (unit.isCompleted() && isInitialized(unitName)) {
					this.env.addToEnvironment(unitName, BwapiUtility.getEisUnitType(unit));
				} else {
					toAdd.add(unit);
				}
			}
			this.units.getUninitializedUnits().addAll(toAdd);
		}
	}

	private LinkedList<Percept> translatePercepts(String unitName, Map<PerceptFilter, List<Percept>> map) {
		LinkedList<Percept> percepts = new LinkedList<>();
		Map<String, List<Percept>> previousPercepts = this.previous.get(unitName);
		if (previousPercepts == null) {
			previousPercepts = new HashMap<>();
			this.previous.put(unitName, previousPercepts);
		}
		Iterator<Entry<PerceptFilter, List<Percept>>> entries = map.entrySet().iterator();
		while (entries.hasNext()) {
			Entry<PerceptFilter, List<Percept>> entry = entries.next();
			PerceptFilter key = entry.getKey();
			List<Percept> values = entry.getValue();
			switch (key.getType()) {
			case ALWAYS:
				percepts.addAll(values);
				break;
			case ONCE:
				if (!previousPercepts.containsKey(key.getName())) {
					percepts.addAll(values);
				}
				break;
			case ON_CHANGE:
				List<Percept> previous = previousPercepts.get(key.getName());
				List<Percept> changed = new ArrayList<>(values);
				if (previous != null) {
					changed.removeAll(previous);
				}
				previousPercepts.put(key.getName(), values);
				percepts.addAll(changed);
				break;
			default:
				Logger.getLogger("StarCraft Logger").warning("Unknown percept type " + key);
				break;
			}
		}
		return percepts;
	}

	/**
	 * Get the global percepts (resources, mineralfield, vespenegeyser, friendly,
	 * enemy, attacking, underconstruction)
	 *
	 * @param api
	 *            - the API.
	 */
	private Map<PerceptFilter, List<Percept>> getGlobalPercepts(JNIBWAPI bwapi) {
		Map<PerceptFilter, List<Percept>> toReturn = new HashMap<>(7);
		new UnitsPerceiver(bwapi).perceive(toReturn);
		return toReturn;
	}

	/**
	 * Update the map percepts (map, enemy, base, choke, region)
	 *
	 * @param api
	 *            - the API.
	 */
	public void updateMap(JNIBWAPI api) {
		Map<PerceptFilter, List<Percept>> toReturn = new HashMap<>(5);
		new MapPerceiver(api).perceive(toReturn);
		this.mapPercepts = toReturn;
	}

	/**
	 * updates the constructionsites in the game.
	 *
	 * @param bwapi
	 *            - the JNIBWAPI
	 */
	public void updateConstructionSites(JNIBWAPI bwapi) {
		Map<PerceptFilter, List<Percept>> toReturn = new HashMap<>(1);
		new ConstructionSitePerceiver(bwapi).perceive(toReturn);
		this.constructionPercepts = toReturn;
	}

	/**
	 * Updates the endGame percept.
	 *
	 * @param bwapi
	 *            - the JNIBWAPI
	 */
	public void updateNukePerceiver(Position pos) {
		if (pos == null) {
			this.nukePercepts = null;
		} else {
			Map<PerceptFilter, List<Percept>> toReturn = new HashMap<>(1);
			List<Percept> nukepercept = new ArrayList<>(1);
			nukepercept.add(new NukePercept(pos.getBX(), pos.getBY()));
			toReturn.put(new PerceptFilter(Percepts.NUKE, Filter.Type.ON_CHANGE), nukepercept);
			if (this.nukePercepts == null) {
				this.nukePercepts = toReturn;
			} else {
				this.nukePercepts.values().iterator().next().addAll(toReturn.values().iterator().next());
			}
		}
	}

	/**
	 * Updates the endGame percept.
	 *
	 * @param bwapi
	 *            - the JNIBWAPI
	 */
	public void updateEndGamePerceiver(boolean winner) {
		Map<PerceptFilter, List<Percept>> toReturn = new HashMap<>(1);
		List<Percept> endgamepercept = new ArrayList<>(1);
		endgamepercept.add(new WinnerPercept(winner));
		toReturn.put(new PerceptFilter(Percepts.WINNER, Filter.Type.ALWAYS), endgamepercept);
		this.endGamePercepts = toReturn;
	}

	public void addDraw(String draw, IDraw idraw) {
		this.draws.put(draw, idraw);
	}

	public void removeDraw(String draw) {
		this.draws.remove(draw);
	}

	public void toggleDraw(String draw) {
		this.draws.get(draw).toggle();
	}

	/**
	 * Get the percepts of this unit.
	 *
	 * @param entity
	 *            - the name of the unit
	 * @return the percepts
	 */
	public LinkedList<Percept> getPercepts(String entity) {
		Map<PerceptFilter, List<Percept>> percepts = this.percepts.get(entity);
		if (percepts == null) {
			return new LinkedList<>();
		} else {
			return translatePercepts(entity, this.percepts.get(entity));
		}
	}

	/**
	 * get the constructionSites.
	 *
	 * @return the constructionsites.
	 */
	public List<Percept> getConstructionSites() {
		List<Percept> perceptHolder = new LinkedList<>();
		for (List<Percept> percept : this.constructionPercepts.values()) {
			perceptHolder.addAll(percept);
		}
		return perceptHolder;
	}

	/**
	 * Clean the game data.
	 */
	public void clean() {
		this.units.clean();
		if (this.env.mapAgent()) {
			this.env.deleteFromEnvironment("mapAgent");
		}
		this.percepts = null;
		this.constructionPercepts = null;
		this.endGamePercepts = null;
		this.nukePercepts = null;
		this.mapPercepts = null;
		this.previous.clear();
		try {
			this.env.kill();
		} catch (ManagementException ignore) {
		}
	}

	/**
	 * @param entity
	 *            The evaluated entity
	 * @return boolean indicating whether the unit is initialized or not.
	 */
	public boolean isInitialized(String entity) {
		return this.percepts != null && this.percepts.containsKey(entity);
	}
}
