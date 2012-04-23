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
import org.springframework.web.context.request.async.StaleAsyncWebRequestException;

/**
 * Represents a user participating in a chat.
 *
 * Delegates the storing of chat messages to a ChatRepository.
 * Provides fine-grained locking for getting and handling new messages.
 *
 */
public class ChatParticipant {

	private final String topic;

	private DeferredResult deferredResult;

	private int messageIndex;

	private final ChatRepository chatRepository;

	private final Object lock = new Object();

	/**
	 * Create a new instance.
	 */
	public ChatParticipant(String topic, ChatRepository chatRepository) {
		this.topic = topic;
		this.chatRepository = chatRepository;
	}

	/**
	 * Return messages starting at the specified index if any are available.
	 * Or hold off and use the DeferredResult when new messages arrive.
	 */
	public List<String> getMessages(DeferredResult deferredResult, int messageIndex) {
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
				setResult(messages);
			}
		}
	}

	private void setResult(List<String> messages) {
		try {
			this.deferredResult.set(messages);
		}
		catch (StaleAsyncWebRequestException e) {
			// ignore
		}
		finally {
			this.deferredResult = null;
		}
	}

	/**
	 * End the DeferredResult.
	 */
	public void exitChat() {
		setResult(Collections.<String>emptyList());
	}

	private boolean matchesTopic(String topic) {
		return this.topic.equals(topic);
	}

}
