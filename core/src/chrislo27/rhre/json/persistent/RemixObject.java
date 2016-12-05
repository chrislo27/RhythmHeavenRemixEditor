package chrislo27.rhre.json.persistent;

import java.util.List;

public class RemixObject {

	public String version;
	public List<EntityObject> entities;

	public static class EntityObject {

		public String id;
		public float beat;
		public int level;

		// optionals
		public boolean isPattern;
		public float width;
		public int semitone;

	}

}
