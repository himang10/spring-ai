package com.example.springai.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.jsoup.Jsoup;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.ai.document.DocumentWriter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MyUrlTxtReaderService {

	private final DocumentWriter vectorStore;

	MyUrlTxtReaderService(DocumentWriter vectorStore) {
		this.vectorStore = vectorStore;
	}

	public List<Document> loadUrlAsDocuments(String url) throws IOException {
		try {
			// Jsoup을 사용하여 URL에서 HTML을 가져오고 텍스트만 추출
			org.jsoup.nodes.Document htmlDoc = Jsoup.connect(url).get();
			String text = htmlDoc.text();
			return List.of(new Document(text));
		} catch (MalformedURLException e) {
			throw new IOException("Invalid URL: " + url, e);
		} catch (Exception e) {
			throw new IOException("Failed to parse URL content: " + url, e);
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
	 * ETL 파이프라인: URL 콘텐츠를 읽고, 변환하고, PGVector에 로드합니다.
	 * Jsoup을 사용하여 HTML에서 텍스트만 추출합니다.
	 */
	public List<Document> executeETL(String url) throws IOException {
		log.info("Starting URL ETL pipeline for: {}", url);
		
		// Jsoup을 사용하여 URL에서 HTML을 가져오고 텍스트만 추출
		org.jsoup.nodes.Document htmlDoc = Jsoup.connect(url).get();
		String text = htmlDoc.text();
		List<Document> documents = List.of(new Document(text));
		
		DocumentTransformer splitter = new TokenTextSplitter(
			800, 350, 5, 10000, true
		);
		
		// ETL: Extract -> Transform -> Load
		vectorStore.write(splitter.transform(documents));
		
		log.info("URL ETL pipeline completed successfully");
		return documents;
	}
}
