package org.springframework.samples.async.chat;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

@Controller
@RequestMapping("/mvc/chat")
public class ChatController {

	private final ChatRepository chatRepository;

	private final Map<DeferredResult<List<String>>, Integer> chatRequests =
			new ConcurrentHashMap<DeferredResult<List<String>>, Integer>();


	@Autowired
	public ChatController(ChatRepository chatRepository) {
		this.chatRepository = chatRepository;
	}

	@RequestMapping(method=RequestMethod.GET)
	@ResponseBody
	public DeferredResult<List<String>> getMessages(@RequestParam int messageIndex) {

		DeferredResult<List<String>> result = new DeferredResult<List<String>>(null, Collections.emptyList());
		this.chatRequests.put(result, messageIndex);

		List<String> messages = this.chatRepository.getMessages(messageIndex);
		if (!messages.isEmpty()) {
			this.chatRequests.remove(result);
			result.setResult(messages);
		}

		return result;
	}

	@RequestMapping(method=RequestMethod.POST)
	@ResponseBody
	public void postMessage(@RequestParam String message) {
		this.chatRepository.addMessage(message);
		updateChatRequests();
	}

	private void updateChatRequests() {
		for (Entry<DeferredResult<List<String>>, Integer> entry : this.chatRequests.entrySet()) {
			this.chatRequests.remove(entry.getKey());
			List<String> messages = this.chatRepository.getMessages(entry.getValue());
			entry.getKey().setResult(messages);
		}
	}

}
