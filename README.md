# spring-ai-ollama-hf-gguf-rag-chat

This project demonstrates the simplest use Spring AI to build Retrieval Augmented Generation (RAG) pipelines and a conversational chat grounded on ingested content.

This was the second step in my quest to gain knowledge about building AI applications, with RAG, MCP and agentic workflows, using Spring AI.

This example was heavily based on one of [Spring AI Examples by Craig Walls](https://github.com/habuma/spring-ai-examples): [Spring AI RAG and Conversations Example](https://github.com/habuma/spring-ai-examples/tree/main/spring-ai-rag-chat).
It builds on top of [spring-ai-ollama-hf-gguf](https://github.com/david-santos/spring-ai-ollama-hf-gguf) therefore leveraging Ollama integration with Hugging Face GGUF models for both chat completion and embedding tasks.
Everything runs locally (application, models and vector store).

The application loads the entire content of a document at startup (`2025-nfl-rulebook-final.pdf` - loaded from classpath) into a vector store (PGVector) and then exposes an API through which questions can be asked about the document's content. It also shows how to use chat memory to enable conversational interactions.

## Prerequisites

- Java 25 (installed on your system)
- [Ollama](https://ollama.com/download) (installed and running on your system)
- Maven 3.9.12 (pulled in automatically via `./mvnw`)
- Spring AI 2.0.0-M2 (pulled in automatically)
- PostgreSQL with PGVector pg18 (running on your system - see Docker command below).

## PGVector

Start a PostgreSQL instance with the PGVector extension using Docker:

```bash
docker run -it --rm --name postgres -p 5432:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres pgvector/pgvector:pg18
```

## Run

```bash
./mvnw spring-boot:run
```

> The document ingestion happens in chunks and takes several minutes. Wait for an entry from `EtlRunner` in the logs that says `Documents loaded into vector store`.

> The Spring Boot Maven plugin is configured with `--enable-native-access=ALL-UNNAMED` so Netty’s native DNS resolver (used on macOS) can load without the Java 21+ restricted-method warning. If you run the app with `java -jar` or from an IDE, add that JVM argument so the warning does not appear.

### Troubleshooting

If you see **"expected 768 dimensions, not 4096"**, the `vector_store` table was created with the wrong embedding size. Set `spring.ai.vectorstore.pgvector.remove-existing-vector-store-table=true` once, run the app so the table is recreated with 4096 dimensions (matching `Qwen3-Embedding-8B-GGUF`), then set that property back to `false` so the store is not wiped on every restart.

## API Usage

The first time you run it, it will take a little while to load the document into the vector store. Wait for an entry from `EtlRunner` in the logs that says `Documents loaded into vector store`.

Then you can use curl to ask questions:

```bash
curl localhost:8080/ask -H"Content-type: application/json" \
  -d '{"question": "What is roughing the passer?"}'
```

You can then ask a followup question and the application will remember the context of the conversation:

```bash
curl localhost:8080/ask -H"Content-type: application/json" \
  -d '{"question": "What is the penalty for that?"}'
```

By default, the application uses a conversation ID of `defaultConversation`. To specify a different conversation ID, specify it via the `X_CONV_ID` header:

```bash
curl localhost:8080/ask -H"Content-type: application/json" \
  -H"X_CONV_ID: user1" \
  -d '{"question": "What is roughing the passer?"}'

curl localhost:8080/ask -H"Content-type: application/json" \
  -H"X_CONV_ID: user1" \
  -d '{"question": "What is the penalty for that?"}'
```

Answers are based on the ingested content and use chat memory for follow-up questions.

## Resources

- [Spring AI Docs](https://docs.spring.io/spring-ai/reference/2.0/index.html):
  - [Retrieval Augmented Generation](https://docs.spring.io/spring-ai/reference/2.0/api/retrieval-augmented-generation.html)
  - [ETL Pipeline](https://docs.spring.io/spring-ai/reference/2.0/api/etl-pipeline.html)
- [Spring AI Samples by Thomas Vitale](https://github.com/ThomasVitale/llm-apps-java-spring-ai):
  - [Question answering with documents (RAG) using LLMs via Ollama and PGVector](https://github.com/ThomasVitale/llm-apps-java-spring-ai/tree/main/use-cases/question-answering)
