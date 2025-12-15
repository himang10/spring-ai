package com.example.springai.controller;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.springai.service.MyTxtReaderService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/chat")
@Slf4j
public class MyTxtReaderController {

	@Autowired
	private MyTxtReaderService myTxtReaderService;

	@PostMapping(
		value = "/txt",
		consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
		produces = MediaType.TEXT_PLAIN_VALUE
	)
	public String txtReader(@RequestParam("question") String question) {
		log.info("TXT Reader - Question: {}", question);

		try {
			List<Document> documents = myTxtReaderService.loadTxtAsDocuments();
			log.info("Loaded {} documents from TXT", documents.size());

			// Service의 documentsToString 메소드를 사용하여 Document 리스트를 문자열로 변환
			String result = myTxtReaderService.documentsToString(documents);

			log.info("Search result: {}", result);
			
			return "문서 로딩 완료: " + documents.size() + "개의 문서를 찾았습니다.\n\n" + result;
		} catch (Exception e) {
			log.error("Error loading TXT documents", e);
			return "오류 발생: " + e.getMessage();
		}
	}

	@PostMapping(
		value = "/txt-etl",
		consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
		produces = MediaType.TEXT_PLAIN_VALUE
	)
	public String txtReaderWithETL(@RequestParam("question") String question) {
		log.info("TXT ETL Pipeline - Question: {}", question);

		try {
			// ETL 파이프라인 실행: Extract -> Transform -> Load to PGVector
			List<Document> documents = myTxtReaderService.executeETL();

			log.info("Search result: {}", myTxtReaderService.documentsToString(documents));
			
			return "ETL 파이프라인 완료: TXT 문서를 읽고, 변환하고, PGVector에 저장했습니다.\n" +
					"이제 벡터 저장소에서 문서를 검색할 수 있습니다.";
		} catch (Exception e) {
			log.error("Error executing ETL pipeline", e);
			return "ETL 파이프라인 오류 발생: " + e.getMessage();
		}
	}
}
