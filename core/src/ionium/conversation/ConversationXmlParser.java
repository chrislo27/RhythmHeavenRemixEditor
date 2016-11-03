package ionium.conversation;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import ionium.conversation.Conversation.Choice;

import java.text.MessageFormat;

/**
 * Creates a conversation object from an XML file.
 * 
 *
 */
public abstract class ConversationXmlParser {

	private final XmlReader reader = new XmlReader();

	public abstract Character getCharacterFromId(String id);

	public ParsedConversation getConversationFromXml(String xml)
			throws MalformedConversationXmlException {
		ParsedConversation parsedConv = new ParsedConversation(new Conversation(null, null), null);

		// example
		/*
		 * <conv id="aConversation" next="theNextConvOrNull" finish="onConvFinish event">
		 *    <lines>
		 *         <line order='0'/>
		 *             <speaker>speakerName</speaker>
		 *             <text>conv.something.0</text>
		 *         </line>
		 *         
		 *         <line order='1'/>
		 *             <speaker>otherSpeakerName</speaker>
		 *             <text>conv.something.1</text>
		 *         </line>
		 *    </lines>
		 * 	   
		 *     <choices>
		 *         <choice order='0' text="conv.something.choice0" next="nextConv" />
		 *         <choice order='1' text="conv.something.choice1" next="nextConv" />
		 *         <choice order='2' text="conv.yes" next="nextConv" />
		 *         <choice order='3' text="conv.no" next="nextConv" isCancel="true/1/YeS" choose="eventOnChoose"/>
		 *     </choices>
		 * 
		 * </conv>
		 */

		// <conv> or whatever
		Element root = reader.parse(xml);

		// conv attributes (id, gotoNext)
		String convId, gotoNext, finish;

		{
			convId = root.getAttribute("id", null);
			gotoNext = root.getAttribute("next", null);
			finish = root.getAttribute("finish", null);

			if (convId == null) {
				throw new MalformedConversationXmlException(
						"Error while parsing conversation: conversation ID attribute was null");
			}

			parsedConv.id = convId;
			parsedConv.conv.gotoNext = gotoNext;
			parsedConv.conv.finishEvent = finish;
		}

		// getting the lines
		{
			Element linesRoot = root.getChildByName("lines");

			if (linesRoot == null) {
				throw new MalformedConversationXmlException(
						"Error while parsing conversation: missing <lines> element");
			}

			Array<Element> lines = linesRoot.getChildrenByName("line");
			parsedConv.conv.lines = new DialogueLine[lines.size];

			if (lines.size <= 0) {
				throw new MalformedConversationXmlException(
						"Error while parsing conversation: number of lines cannot be 0 or lower ("
								+ lines.size + ")");
			}

			for (Element e : lines) {
				int order = e.getIntAttribute("order", -1);
				Element speaker = e.getChildByName("speaker");
				Element text = e.getChildByName("text");

				if (order <= -1 || speaker == null || text == null) {
					throw new MalformedConversationXmlException(MessageFormat.format(
							"Error while parsing conv. lines: (order: {0}), (speaker: {1}), (text: {2})",
							order, speaker, text));
				}

				parsedConv.conv.lines[order] = new DialogueLine(
						getCharacterFromId(speaker.getText()), text.getText());
			}
		}

		// getting the choices if any
		{
			Element choicesRoot = root.getChildByName("choices");

			if (choicesRoot != null) {
				Array<Element> choices = choicesRoot.getChildrenByName("choice");
				parsedConv.conv.choices = new Choice[choices.size];

				for (Element e : choices) {
					int order = e.getIntAttribute("order", -1);
					String next = e.getAttribute("next", null);
					String text = e.getAttribute("text", null);
					String isCancel = e.getAttribute("isCancel", null);
					String chooseEvent = e.getAttribute("choose", null);

					if (order <= -1 || text == null) {
						parsedConv.conv.choices = null;

						throw new MalformedConversationXmlException(MessageFormat.format(
								"Error while parsing conv. choices: (order: {0}), (next: {1}), (text: {2})",
								order, next, text));
					}

					parsedConv.conv.choices[order] = new Choice(text, next, chooseEvent);

					if (isCancel != null) {
						if (isCancel.equalsIgnoreCase("1") || isCancel.equalsIgnoreCase("true")
								|| isCancel.equalsIgnoreCase("yes")) {
							parsedConv.conv.cancelChoice = order;
						}
					}
				}
			}
		}

		return parsedConv;
	}

	public static class ParsedConversation {

		public Conversation conv;
		public String id;

		public ParsedConversation(Conversation c, String id) {
			conv = c;
			this.id = id;
		}

	}

	public static class MalformedConversationXmlException extends RuntimeException {

		public MalformedConversationXmlException(String string) {
			super(string);
		}

	}

}
