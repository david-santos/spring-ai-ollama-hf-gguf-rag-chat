package pt.dcs.example.springai.ollama_hf_gguf_rag_chat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"app.pdf.ingest-on-startup=false",
		"spring.ai.vectorstore.pgvector.initialize-schema=false"
})
class OllamaHfGgufRagChatApplicationTests {

	@Test
	void contextLoads() {
	}
}
