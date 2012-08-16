/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.async.chat;

import java.util.Collections;
import java.util.List;

import org.springframework.web.context.request.async.DeferredResult;

/**
 * Represents a user participating in a chat.
 *
 * Delegates the storing of chat messages to a ChatRepository.
 * Provides fine-grained locking for getting and handling new messages.
 *
 */
public class ChatParticipant {

	private final String topic;

	private final String userName;

	private DeferredResult<List<String>> deferredResult;

	private int messageIndex;

	private final ChatRepository chatRepository;

	private final Object lock = new Object();

	/**
	 * Create a new instance and post a 'User joined the chat message'.
	 */
	public ChatParticipant(String topic, String userName, ChatRepository chatRepository) {
		this.topic = topic;
		this.userName = userName;
		this.chatRepository = chatRepository;
		this.chatRepository.addMessage(this.topic, "=> " + this.userName + " joined the chat");
	}

	/**
	 * Return messages starting at the specified index if any are available.
	 * Or hold off and use the DeferredResult when new messages arrive.
	 */
	public List<String> getMessages(DeferredResult<List<String>> deferredResult, int messageIndex) {
		synchronized (this.lock) {
			List<String> messages = this.chatRepository.getMessages(this.topic, messageIndex);
			this.deferredResult = messages.isEmpty() ? deferredResult : null;
			this.messageIndex = messageIndex;
			return messages;
		}
	}

	/**
	 * Handle a new chat message.
	 */
	public void handleMessage(String topic, String message) {
		if (!matchesTopic(topic) || (this.deferredResult == null)) {
			return;
		}
		synchronized (this.lock) {
			if (this.deferredResult != null) {
				List<String> messages = this.chatRepository.getMessages(this.topic, this.messageIndex);
				this.deferredResult.setResult(messages);
				this.deferredResult = null;
			}
		}
	}

	/**
	 * End the DeferredResult and post a 'User left the chat' message.
	 */
	public void exitChat() {
		this.deferredResult.setResult(Collections.<String>emptyList());
		this.deferredResult = null;
		this.chatRepository.addMessage(this.topic, "=> " + this.userName + " left the chat");
	}

	private boolean matchesTopic(String topic) {
		return this.topic.equals(topic);
	}

}
