package com.example.springai.service;

import java.io.IOException;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.document.DocumentWriter;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MyJsonReaderService {

	private final Resource resource;
	private final DocumentWriter vectorStore;

    MyJsonReaderService(
		@Value("classpath:documents/대한민국헌법(19880225).json") Resource resource,
		DocumentWriter vectorStore
	) {
        this.resource = resource;
		this.vectorStore = vectorStore;
    }

	public List<Document> loadJsonAsDocuments() {
        DocumentReader jsonReader = new JsonReader(this.resource, "source", "content");
        return jsonReader.get();
	}

	/**
	 * Document 리스트를 문자열로 변환합니다.
	 */
	public String documentsToString(List<Document> documents) {
		if (documents == null || documents.isEmpty()) {
			return "";
		}
		return documents.stream()
				.map(Document::getFormattedContent)
				.collect(java.util.stream.Collectors.joining("\n"));
	}

	/**
	 * ETL 파이프라인: JSON 문서를 읽고, 변환하고, PGVector에 로드합니다.
	 */
	public List<Document> executeETL() throws IOException {
		log.info("Starting JSON ETL pipeline");
		
		/**
		 * 여기에 JSON ETL 파이프라인 코드를 작성하세요.
		 */

		return null;
	}
}