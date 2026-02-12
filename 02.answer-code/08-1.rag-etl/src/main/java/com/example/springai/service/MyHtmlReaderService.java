package com.example.springai.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.jsoup.Jsoup;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MyHtmlReaderService {

	private final Resource resource;
	private final VectorStore vectorStore;

	MyHtmlReaderService(
		@Value("classpath:documents/대한민국헌법(19880225).html") Resource resource,
		VectorStore vectorStore
	) {
		this.resource = resource;
		this.vectorStore = vectorStore;
	}

	public List<Document> loadHtmlAsDocuments() throws IOException {
		try (InputStream in = resource.getInputStream()) {
			String html = new String(in.readAllBytes(), StandardCharsets.UTF_8);
			String text = Jsoup.parse(html).text();
			return List.of(new Document(text));
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
	 * ETL 파이프라인: HTML 문서를 읽고, 변환하고, PGVector에 로드합니다.
	 */
	public List<Document> executeETL() throws IOException {
		log.info("Starting HTML ETL pipeline");
		
		List<Document> documents = loadHtmlAsDocuments();
		TokenTextSplitter splitter = new TokenTextSplitter(
			800, 350, 5, 10000, true
		);
		
		// ETL: Transform -> Load
		vectorStore.write(splitter.split(documents));
		
		log.info("HTML ETL pipeline completed successfully");
		return documents;
	}
}
