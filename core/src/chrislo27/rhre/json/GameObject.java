package chrislo27.rhre.json;

public class GameObject {

	public String gameID;
	public String gameName;
	public String series;

	public SoundObject[] cues;

	public PatternObject[] patterns;

	public boolean usesGeneratorHelper = false;

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

		public Boolean loops = null;
	}

	public static class PatternObject {

		public String id;
		public String name;

		public String[] deprecatedIDs;

		public boolean isStretchable = false;

		public CueObject[] cues;

		public static class CueObject {

			public String id;
			public float beat;
			public int track;

			public Float duration = 0f;

			public Integer semitone = 0;

		}
	}

}
