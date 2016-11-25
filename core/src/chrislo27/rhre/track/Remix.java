package chrislo27.rhre.track;

import chrislo27.rhre.entity.Entity;
import chrislo27.rhre.entity.SoundEntity;
import chrislo27.rhre.registry.GameRegistry;

import java.util.ArrayList;
import java.util.List;

public class Remix {

	public final List<Entity> entities = new ArrayList<>();
	public final List<Entity> selection = new ArrayList<>();

	public Remix() {
		entities.add(new SoundEntity(this, GameRegistry.instance().get("tapTrial").getCue("ook"), 0, 0));
		entities.add(new SoundEntity(this, GameRegistry.instance().get("tapTrial").getCue("tap"), 1, 0));
	}
}
