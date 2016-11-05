package chrislo27.rhre.json;

public class GameObject {

	public String gameID;
	public String gameName;
	public String series;

	public SoundObject[] cues;

	public PatternObject[] patterns;

	public static class SoundObject {

		public String id;
		public String fileExtension = "ogg";

		public String[] deprecatedIDs;

		public String name;

		public float duration = 0.5f;

		public boolean canAlterPitch = false;
		public boolean canAlterDuration = false;

		public String introSound = null;

		public float baseBpm = 0;

		public boolean loops = false;
	}

	public static class PatternObject {

		public String id;
		public String name;

		public boolean isStretchable = false;

		public CueObject[] cues;

		public static class CueObject {

			public String id;
			public float beat;

			public float duration;

			public int semitone;

		}
	}

}
