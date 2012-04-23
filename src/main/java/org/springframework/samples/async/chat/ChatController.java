package org.springframework.samples.async.chat;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

@Controller
@RequestMapping("/mvc/chat/{topic}/{user}")
public class ChatController implements MessageListener {

	private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

	private final Map<String, ChatParticipant> chatParticipants = new ConcurrentHashMap<String, ChatParticipant>();

	private final ChatRepository chatRepository;

	@Autowired
	public ChatController(ChatRepository chatRepository) {
		this.chatRepository = chatRepository;
	}

	/**
	 * Process new chat message notifications from Redis.
	 */
	@Override
	public void onMessage(Message message, byte[] pattern) {
		String topic = new String(message.getChannel(), DEFAULT_CHARSET);
		String messageText = new String(message.getBody(), DEFAULT_CHARSET);
		for (ChatParticipant participant : this.chatParticipants.values()) {
			participant.handleMessage(topic.substring("chat:".length()), messageText);
		}
	}

	/**
	 * Return chat messages immediately or hold the response till new chat messages
	 * become available.
	 */
	@RequestMapping(method=RequestMethod.GET, produces="application/json")
	@ResponseBody
	public Object getMessages(@PathVariable String topic, @PathVariable String user, @RequestParam int messageIndex) {
		ChatParticipant participant = getChatParticipant(topic, user);
		DeferredResult deferredResult = new DeferredResult(Collections.emptyList());
		List<String> messages = participant.getMessages(deferredResult, messageIndex);
		return messages.isEmpty() ? deferredResult : messages;
	}

	private ChatParticipant getChatParticipant(String topic, String user) {
		String key = topic + ":" + user;
		ChatParticipant participant = this.chatParticipants.get(key);
		if (participant == null) {
			participant = new ChatParticipant(topic, user, this.chatRepository);
			this.chatParticipants.put(key, participant);
		}
		return participant;
	}


	/**
	 * Post a message to participants in the chat.
	 */
	@RequestMapping(method=RequestMethod.POST)
	@ResponseBody
	public void postMessage(@PathVariable String topic, @PathVariable String user, @RequestParam String message) {
		this.chatRepository.addMessage(topic, "[" + user + "] " + message);
	}

	/**
	 * End participation in a chat.
	 */
	@RequestMapping(method=RequestMethod.DELETE)
	@ResponseBody
	public void removeParticipant(@PathVariable String topic, @PathVariable String user) {
		ChatParticipant participant = this.chatParticipants.remove(topic + ":" + user);
		participant.exitChat();
	}

}