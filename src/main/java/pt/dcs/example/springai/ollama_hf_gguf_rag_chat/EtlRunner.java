package pt.dcs.example.springai.ollama_hf_gguf_rag_chat;

import org.slf4j.Logger;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;

public class EtlRunner implements ApplicationRunner {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(EtlRunner.class);

    private final VectorStore vectorStore;
    private final Resource resource;
    private final TokenTextSplitter tokenTextSplitter;

    public EtlRunner(Resource resource, VectorStore vectorStore) {
        this.resource = resource;
        this.tokenTextSplitter = TokenTextSplitter.builder().build();
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // The TikaDocumentReader uses Apache Tika to extract text from a variety of document formats, such as PDF, DOC/DOCX, PPT/PPTX, HTML, etc.
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(this.resource);
        logger.info("Loading document into vector store...");
        vectorStore.write(tokenTextSplitter.split(tikaDocumentReader.read()));
        logger.info("Documents loaded into vector store.");
    }
}
