package eisbw.percepts.perceivers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import bwapi.Player;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import eis.iilang.Percept;

public class GenericUnitPerceiverTest {
	private GenericUnitPerceiver perciever;

	@Mock
	private Unit unit;
	@Mock
	private UnitType unitType;
	@Mock
	private Player self;
	@Mock
	private bwapi.Game api;
	@Mock
	private Race race;

	/**
	 * Initialize mocks.
	 */
	@Before
	public void start() {
		MockitoAnnotations.initMocks(this);
		when(this.api.enemy()).thenReturn(this.self);
		when(this.api.self()).thenReturn(this.self);
		when(this.self.getRace()).thenReturn(Race.None);

		when(this.self.minerals()).thenReturn(50);
		when(this.self.gas()).thenReturn(90);
		when(this.self.supplyUsed()).thenReturn(10);
		when(this.self.supplyTotal()).thenReturn(20);

		when(this.unit.getID()).thenReturn(1);
		when(this.unit.getType()).thenReturn(this.unitType);
		when(this.unitType.toString()).thenReturn("type");

		when(this.unit.getHitPoints()).thenReturn(25);
		when(this.unit.getShields()).thenReturn(30);
		when(this.unit.getTilePosition()).thenReturn(new TilePosition(2, 1));

		when(this.unit.getEnergy()).thenReturn(100);
		when(this.unitType.maxEnergy()).thenReturn(110);
		this.perciever = new GenericUnitPerceiver(this.api, this.unit);
	}

	@Test
	public void size_test() {
		Map<PerceptFilter, Set<Percept>> ret = new HashMap<>();
		when(this.race.toString()).thenReturn("race");
		when(this.self.getRace()).thenReturn(this.race);
		assertEquals(4, this.perciever.perceive(ret).size());
		when(this.api.enemy()).thenReturn(null);
		ret = new HashMap<>();
		assertEquals(4, this.perciever.perceive(ret).size());
		when(this.unitType.maxEnergy()).thenReturn(0);
		when(this.unit.isDefenseMatrixed()).thenReturn(true);
		ret = new HashMap<>();
		assertEquals(5, this.perciever.perceive(ret).size());
	}
}
