package ionium.conversation;

public class Conversation {

	public DialogueLine[] lines;
	public String gotoNext = null;
	public Choice[] choices = null;
	public String finishEvent = null;
	public String startEvent = null;
	public int cancelChoice = -1;

	public Conversation(DialogueLine[] lines, String next) {
		this(lines, next, null, null);
	}

	public Conversation(DialogueLine[] lines, String next, String startEvent, String finishEvent) {
		this(lines, next, null, startEvent, finishEvent, -1);
	}

	public Conversation(DialogueLine[] lines, String next, Choice[] choices, String startEvent, String finishEvent, int cancel) {
		this.lines = lines;
		this.gotoNext = next;
		this.choices = choices;
		this.finishEvent = finishEvent;
		this.startEvent = startEvent;
		cancelChoice = cancel;
	}

	public static class Choice {

		public String question;
		public String gotoNext = null;
		public String event = null;

		public Choice(String question, String gotoNext, String event) {
			this.question = question;
			this.gotoNext = gotoNext;
			this.event = event;
		}
	}

}
