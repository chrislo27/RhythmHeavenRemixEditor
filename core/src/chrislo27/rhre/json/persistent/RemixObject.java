package chrislo27.rhre.json.persistent;

import chrislo27.rhre.track.MusicData;
import com.badlogic.gdx.files.FileHandle;

import java.util.List;

public class RemixObject {

	public transient FileHandle fileHandle;
	public transient MusicData musicData;

	public String musicAssociation;

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
