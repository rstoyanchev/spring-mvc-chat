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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

/**
 * Stores chat messages in-memory.
 */
@Repository
public class InMemoryChatRepository implements ChatRepository {

	private final ConcurrentHashMap<String, List<String>> messages = new ConcurrentHashMap<String, List<String>>();

	public List<String> getMessages(String topic, int messageIndex) {
		List<String> chatMessages = this.messages.get(topic);
		if (chatMessages == null) {
			return Collections.<String> emptyList();
		}
		else {
			Assert.isTrue((messageIndex >= 0) && (messageIndex <= chatMessages.size()), "Invalid messageIndex");
			return chatMessages.subList(messageIndex, chatMessages.size());
		}
	}

	public void addMessage(String topic, String message) {
		initChat(topic);
		this.messages.get(topic).add(message);
	}

	private void initChat(String topic) {
		if (!this.messages.containsKey(topic)) {
			this.messages.putIfAbsent(topic, new CopyOnWriteArrayList<String>());
		}
	}

}
