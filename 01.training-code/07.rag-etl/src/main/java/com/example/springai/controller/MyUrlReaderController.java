package com.example.springai.controller;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.springai.service.MyUrlReaderService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/chat")
@Slf4j
public class MyUrlReaderController {

	@Autowired
	private MyUrlReaderService myUrlReaderService;

	@PostMapping(
		value = "/url",
		consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
		produces = MediaType.TEXT_PLAIN_VALUE
	)
	public String urlReader(@RequestParam("question") String question) {
		log.info("URL Reader - question: {}", question);

		try {
			List<Document> documents = myUrlReaderService.loadUrlAsDocuments(question);
			log.info("Loaded {} documents from URL", documents.size());

			String result = myUrlReaderService.documentsToString(documents);
			log.info("Search result: {}", result);

			return "문서 로딩 완료: " + documents.size() + "개의 문서를 찾았습니다.\n\n" + result;
		} catch (Exception e) {
			log.error("Error loading URL documents", e);
			return "오류 발생: " + e.getMessage();
		}
	}

	@PostMapping(
		value = "/url-etl",
		consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
		produces = MediaType.TEXT_PLAIN_VALUE
	)
	public String urlReaderWithETL(@RequestParam("question") String question) {
		log.info("URL ETL Pipeline - URL: {}", question);

		try {
			myUrlReaderService.executeETL(question);
			return "ETL 파이프라인 완료: URL 콘텐츠를 읽고, 변환하고, PGVector에 저장했습니다.\n이제 벡터 저장소에서 문서를 검색할 수 있습니다.";
		} catch (Exception e) {
			log.error("Error executing ETL pipeline", e);
			return "ETL 파이프라인 오류 발생: " + e.getMessage();
		}
	}
}
