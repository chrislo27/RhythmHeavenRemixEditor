package ionium.conversation;

public class Voice {

	public String voiceFile;
	public float avgLength = 0.2f;

	public Voice(String voice, float length) {
		voiceFile = voice;
		avgLength = length;
	}

}
