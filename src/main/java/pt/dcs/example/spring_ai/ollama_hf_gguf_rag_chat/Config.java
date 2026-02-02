package pt.dcs.example.spring_ai.ollama_hf_gguf_rag_chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * Configures ChatClient with RAG and chat memory advisors.
 * - QuestionAnswerAdvisor: retrieves relevant document chunks from the vector store and augments the user question.
 * - MessageChatMemoryAdvisor: keeps conversation history so follow-up questions have context.
 * - SimpleLoggerAdvisor: logs request/response for debugging and observability.
 * Configures the ETL pipeline for ingesting documents.
 *  * Based on Spring AI ETL: DocumentReader -> DocumentTransformer -> DocumentWriter (VectorStore - PGVector).
 */
@Configuration
public class Config {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, VectorStore vectorStore) {
        return builder
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(
                                MessageWindowChatMemory.builder().build()
                        ).build(),
                        QuestionAnswerAdvisor.builder(vectorStore).build(),
                        SimpleLoggerAdvisor.builder().build()
                )
                .build();
    }

    /**
     * Loads the document into the vector store (on startup).
     * Uses ETL: TikaDocumentReader -> TokenTextSplitter -> VectorStore.
     */
    @Bean
    public EtlRunner etlRunner(@Value("classpath:/2025-nfl-rulebook-final.pdf") Resource resource, VectorStore vectorStore) {
        return new EtlRunner(resource, vectorStore);
    }

}
