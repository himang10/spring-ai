package com.example.springai.service;

import java.io.IOException;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MyDocxReaderService {

	private final Resource resource;
	private final VectorStore vectorStore;

	MyDocxReaderService(
		@Value("classpath:documents/대한민국헌법(19880225).docx") Resource resource,
		VectorStore vectorStore
	) {
		this.resource = resource;
		this.vectorStore = vectorStore;
	}

	public List<Document> loadDocxAsDocuments() throws IOException {
		try {
			DocumentReader reader = new TikaDocumentReader(this.resource);
			return reader.get();
		} catch (Exception e) {
			throw new IOException("Failed to parse DOCX resource: " + resource.getDescription(), e);
		}
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
	 * ETL 파이프라인: DOCX 문서를 읽고, 변환하고, PGVector에 로드합니다.
	 */
	public List<Document> executeETL() throws IOException {
		log.info("Starting DOCX ETL pipeline");
		
		DocumentReader docxReader = new TikaDocumentReader(this.resource);
		TokenTextSplitter splitter = new TokenTextSplitter(
			800, 350, 5, 10000, true
		);
		
		// ETL: Extract -> Transform -> Load
		vectorStore.write(splitter.split(docxReader.read()));
		
		log.info("DOCX ETL pipeline completed successfully");
		return docxReader.get();
	}
}
