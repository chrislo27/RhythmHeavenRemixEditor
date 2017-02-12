package chrislo27.rhre.json.persistent;

import java.util.List;

public class RemixObject {

	public String version;
	public List<EntityObject> entities;

	public float playbackStart;
	public float musicVolume = 1f;
	public float musicStartTime;
	public List<BpmTrackerObject> bpmChanges;

	public MetadataObject metadata = new MetadataObject();

	public static class EntityObject {

		public String id;
		public float beat;
		public int level;

		// optionals
		public boolean isPattern;
		public float width;
		public int semitone;

	}

	public static class BpmTrackerObject {

		public float beat;
		public float tempo;

	}

	public static class MetadataObject {

		public String author;
		public String description;
		public String gamesUsed;

	}

}
