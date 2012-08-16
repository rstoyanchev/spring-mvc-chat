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

import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.DeferredResult.DeferredResultHandler;

public class ChatParticipantTests {

	private static final String TOPIC = "tennis";

	private ChatRepository chatRepository;

	private ChatParticipant participant;

	@Before
	public void setup() {
		this.chatRepository = createStrictMock(ChatRepository.class);
		this.chatRepository.addMessage(TOPIC, "=> Johny joined the chat");
		replay(this.chatRepository);

		this.participant = new ChatParticipant(TOPIC, "Johny", chatRepository);

		verify(this.chatRepository);
		reset(this.chatRepository);
	}

	@Test
	public void handleMessage() {
		DeferredResultHandler resultHandler = createStrictMock(DeferredResultHandler.class);

		DeferredResult<List<String>> deferredResult = new DeferredResult<List<String>>();
		deferredResult.setResultHandler(resultHandler);

		expect(this.chatRepository.getMessages(TOPIC, 0)).andReturn(new ArrayList<String>());
		replay(this.chatRepository);

		this.participant.getMessages(deferredResult, 0);

		verify(this.chatRepository);
		reset(this.chatRepository);

		List<String> messages = Arrays.asList("Andy Murray got the gold!");

		expect(this.chatRepository.getMessages(TOPIC, 0)).andReturn(messages);
		replay(this.chatRepository);

		resultHandler.handleResult(messages);
		replay(resultHandler);

		this.participant.handleMessage(TOPIC, "Andy Murray got the gold!");

		verify(resultHandler, this.chatRepository);
	}


}
