package ionium.conversation;


public class Character {

	/**
	 * the localized name prefix
	 * <br>
	 * appended to the beginning of the character's name in the locale files
	 */
	public static String LocalizedNamePrefix = "character.name.";
	
	public String name;
	public Voice voice;
	public String face;
	
	public Character(String name, Voice voice, String face){
		this.name = name;
		this.voice = voice;
		this.face = face;
	}
	
	@Override
	/**
	 * Returns true everything matches
	 */
	public boolean equals(Object obj) {
		if(obj instanceof Character){
			if(((Character) obj).name.equals(name) &&
					((Character) obj).voice.equals(voice) &&
					((Character) obj).face.equals(face)) return true;
		}
		
		return super.equals(obj);
	}
	
}
