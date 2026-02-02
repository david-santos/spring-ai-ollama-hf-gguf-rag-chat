package pt.dcs.example.springai.ollama_hf_gguf_rag_chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

/**
 * REST API: answers questions about the ingested documents content.
 * Uses ChatClient with QuestionAnswerAdvisor (RAG), MessageChatMemoryAdvisor (conversation history),
 * and SimpleLoggerAdvisor (request/response logging).
 */
@RestController
@RequestMapping("/ask")
public class AskController {

	private final ChatClient chatClient;

	public AskController(ChatClient ragChatClient) {
		this.chatClient = ragChatClient;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Answer ask(
			@RequestBody Question question,
			@RequestHeader(name="X_CONV_ID", defaultValue="defaultConversation") String conversationId) {
		return chatClient.prompt()
				.advisors(spec -> spec.param(CONVERSATION_ID, conversationId))
				.user(question.question())
				.call()
				.entity(Answer.class);
	}
}
