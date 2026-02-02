# ollama-hf-gguf-rag-chat

This project demonstrates the simplest use Spring AI to build Retrieval Augmented Generation (RAG) pipelines and a conversational chat grounded on ingested content.

Builds on top of `ollama-hf-gguf` therefore leveraging Ollama integration, Hugging Face GGUF models for both chat completion and embedding tasks.

This is the second step in my quest to gain knowledge about building AI applications, with RAG, MCP and agentic workflows, using Spring AI.

## Overview

Loads the entire content of a document into a vector store (PGVector) and then exposes an API through which questions can be asked about the document's content. It also shows how to use chat memory to enable conversational interactions.

## Prerequisites

- **Ollama** running locally with models:
  - `hf.co/Qwen/Qwen3-8B-GGUF` (chat)
  - `hf.co/Qwen/Qwen3-Embedding-8B-GGUF` (embeddings)
- **PostgreSQL with PGVector** for the vector store.

## PGVector

Start a PostgreSQL instance with the PGVector extension using Docker:

```bash
docker run -it --rm --name postgres -p 5432:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres pgvector/pgvector:pg18
```

Then configure `spring.datasource.url`, `username`, and `password` in `application.properties` if needed (defaults point to `localhost:5432/postgres` with user `postgres` / password `postgres`).

If you see **"expected 768 dimensions, not 4096"**, the `vector_store` table was created with the wrong embedding size. Set `spring.ai.vectorstore.pgvector.remove-existing-vector-store-table=true` once, run the app so the table is recreated with 4096 dimensions (matching Qwen3-Embedding-8B-GGUF), then set that property back to `false` so the store is not wiped on every restart.

## Document Content

The app ingests `2025-nfl-rulebook-final.pdf`, loaded from classpath. This ingestion happens in chunks and takes several minutes. Wait for an entry from `EtlRunner` in the logs that says `Documents loaded into vector store`.

## Run

```bash
./mvnw spring-boot:run
```

The Spring Boot Maven plugin is configured with `--enable-native-access=ALL-UNNAMED` so Netty’s native DNS resolver (used on macOS) can load without the Java 21+ restricted-method warning. If you run the app with `java -jar` or from an IDE, add that JVM argument so the warning does not appear.

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

## Stack

- Spring Boot 4, Java 25, Spring AI 2.0.0-M2
- Ollama with `hf.co/Qwen/Qwen3-8B-GGUF` (chat) and `hf.co/Qwen/Qwen3-Embedding-8B-GGUF` (embeddings)
- PGVector (vector store)
- ETL: `PagePdfDocumentReader` → `TokenTextSplitter` → PGVector
- Advisors: `QuestionAnswerAdvisor` (RAG), `MessageChatMemoryAdvisor` (conversation history), `SimpleLoggerAdvisor` (logging)

## Resources

- [Spring AI Docs](https://docs.spring.io/spring-ai/reference/2.0/index.html):
  - [Retrieval Augmented Generation](https://docs.spring.io/spring-ai/reference/2.0/api/retrieval-augmented-generation.html)
  - [ETL Pipeline](https://docs.spring.io/spring-ai/reference/2.0/api/etl-pipeline.html)
- [Spring AI Samples by Thomas Vitale](https://github.com/ThomasVitale/llm-apps-java-spring-ai):
  - [Question answering with documents (RAG) using LLMs via Ollama and PGVector](https://github.com/ThomasVitale/llm-apps-java-spring-ai/tree/main/use-cases/question-answering)
- [Spring AI Examples by Craig Walls](https://github.com/habuma/spring-ai-examples)
  - [Spring AI RAG and Conversations Example](https://github.com/habuma/spring-ai-examples/tree/main/spring-ai-rag-chat)