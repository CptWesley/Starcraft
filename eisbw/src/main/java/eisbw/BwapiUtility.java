package eisbw;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import bwapi.Race;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwta.BWTA;
import bwta.Region;

/**
 * @author Danny & Harm - The Utility class of the BWAPI.
 *
 */
public class BwapiUtility {
	private static final Map<String, UnitType> unitTypeMap = new HashMap<>();
	private static final Map<String, TechType> techTypeMap = new HashMap<>();
	private static final Map<String, UpgradeType> upgradeTypeMap = new HashMap<>();
	private static final Map<Entry<Integer, Integer>, Integer> regionCache = new HashMap<>();
	private static final Map<Integer, Boolean> validCache = new HashMap<>();

	private BwapiUtility() {
		// Private constructor for static class.
	}

	public static boolean isValid(Unit unit) {
		Boolean valid = validCache.get(unit.getID());
		if (valid == null) {
			valid = unit.exists() && unit.isVisible() && !(unit.isBeingConstructed() && unit.isLoaded());
			validCache.put(unit.getID(), valid);
		}
		return valid.booleanValue();
	}

	public static void clearValidCache() {
		validCache.clear();
	}

	/**
	 * Get the name of a unit.
	 *
	 * @param unit
	 *            - the unit that has to be named.
	 * @return the name of the unit.
	 */
	public static String getName(Unit unit) {
		String name = (getName(unit.getType()) + unit.getID()).replace(" ", "");
		return name.substring(0, 1).toLowerCase() + name.substring(1);
	}

	public static String getName(Race race) {
		return race.toString().toLowerCase();
	}

	public static String getName(UnitType unittype) {
		String type = unittype.toString().replace("_", " ");
		if (type.startsWith("Terran Siege Tank")) {
			return "Terran Siege Tank";
		} else {
			return type;
		}
	}

	public static String getName(TechType type) {
		return type.toString().replace("_", " ");
	}

	public static String getName(UpgradeType type) {
		return type.toString().replace("_", " ");
	}

	public static int getRegion(TilePosition position, bwapi.Game api) {
		Entry<Integer, Integer> pos = new SimpleEntry<>(position.getX(), position.getY());
		Integer regionId = regionCache.get(pos);
		if (regionId == null) {
			Region region = BWTA.getRegion(position);
			bwapi.Region apiregion = (region == null) ? null : api.getRegionAt(region.getCenter());
			regionId = (apiregion == null) ? 0 : apiregion.getID();
			regionCache.put(pos, regionId);
		}
		return regionId.intValue();
	}

	/**
	 * Get the EIS unittype.
	 *
	 * @param unittype
	 *            the unittype
	 * @return the type of a unit.
	 */
	public static String getEisUnitType(UnitType type) {
		String result = getName(type).replace(" ", "");
		return result.substring(0, 1).toLowerCase() + result.substring(1);
	}

	/**
	 * Convert EIS type to unit.
	 *
	 * @param type
	 *            - the type to be converted.
	 * @return the unit.
	 */
	public static UnitType getUnitType(String type) {
		if (type.equals("Terran Siege Tank")) {
			type += " Tank Mode";
		}
		if (unitTypeMap.isEmpty()) {
			for (Field field : UnitType.class.getDeclaredFields()) {
				if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) {
					try {
						UnitType ut = (UnitType) field.get(null);
						unitTypeMap.put(getName(ut), ut);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace(); // TODO Auto-generated
					}
				}
			}
		}
		return unitTypeMap.get(type);
	}

	/**
	 * Convert type string to a techtype.
	 *
	 * @param type
	 *            - the string to be converted.
	 * @return a techtype.
	 */
	public static TechType getTechType(String type) {
		if (techTypeMap.isEmpty()) {
			for (Field field : TechType.class.getDeclaredFields()) {
				if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) {
					try {
						TechType tt = (TechType) field.get(null);
						techTypeMap.put(getName(tt), tt);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace(); // TODO Auto-generated
					}
				}
			}
		}
		return techTypeMap.get(type);
	}

	/**
	 * Convert a string to a upgradetype.
	 *
	 * @param type
	 *            - the string to be converted.
	 * @return a upgradetype.
	 */
	public static UpgradeType getUpgradeType(String type) {
		if (upgradeTypeMap.isEmpty()) {
			for (Field field : UpgradeType.class.getDeclaredFields()) {
				if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) {
					try {
						UpgradeType ut = (UpgradeType) field.get(null);
						upgradeTypeMap.put(getName(ut), ut);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace(); // TODO Auto-generated
					}
				}
			}
		}
		if (type.length() > 2 && Character.isDigit(type.charAt(type.length() - 1))) {
			type = type.substring(0, type.length() - 3);
		}
		return upgradeTypeMap.get(type);
	}
}
