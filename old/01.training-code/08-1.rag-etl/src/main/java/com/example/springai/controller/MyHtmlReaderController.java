package com.example.springai.controller;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.springai.service.MyHtmlReaderService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/chat")
@Slf4j
public class MyHtmlReaderController {

	@Autowired
	private MyHtmlReaderService myHtmlReaderService;

	@PostMapping(
		value = "/html",
		consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
		produces = MediaType.TEXT_PLAIN_VALUE
	)
	public String htmlReader(@RequestParam("question") String question) {
		log.info("HTML Reader - Question: {}", question);

		try {
			List<Document> documents = myHtmlReaderService.loadHtmlAsDocuments();
			log.info("Loaded {} documents from HTML", documents.size());

			String result = myHtmlReaderService.documentsToString(documents);
			log.info("Search result: {}", result);
			
			return "문서 로딩 완료: " + documents.size() + "개의 문서를 찾았습니다.\n\n" + result;
		} catch (Exception e) {
			log.error("Error loading HTML documents", e);
			return "오류 발생: " + e.getMessage();
		}
	}

	@PostMapping(
		value = "/html-etl",
		consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
		produces = MediaType.TEXT_PLAIN_VALUE
	)
	public String htmlReaderWithETL(@RequestParam("question") String question) {
		log.info("HTML ETL Pipeline - Question: {}", question);

		try {
			myHtmlReaderService.executeETL();
			return "ETL 파이프라인 완료: HTML 문서를 읽고, 변환하고, PGVector에 저장했습니다.\n이제 벡터 저장소에서 문서를 검색할 수 있습니다.";
		} catch (Exception e) {
			log.error("Error executing ETL pipeline", e);
			return "ETL 파이프라인 오류 발생: " + e.getMessage();
		}
	}
}
