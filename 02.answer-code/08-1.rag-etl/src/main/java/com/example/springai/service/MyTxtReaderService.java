package com.example.springai.service;

import java.io.IOException;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.ai.document.DocumentWriter;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
//import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MyTxtReaderService {

	private final Resource resource;
	//private final VectorStore vectorStore;
	private final DocumentWriter vectorStore;
 
	MyTxtReaderService(
		@Value("classpath:documents/대한민국헌법(19880225).txt") Resource resource,
		PgVectorStore vectorStore
	) {
		this.resource = resource;
		this.vectorStore = vectorStore;
	}

	/**
	 * TXT 문서를 Document 리스트로 로드합니다.
	 * @return
	 * @throws IOException
	 */
	public List<Document> loadTxtAsDocuments() throws IOException {
		DocumentReader textReader = new TextReader(this.resource);
		return textReader.get();
	}


	/**
	 * 함수형 스타일의 ETL 파이프라인
	 * Extract -> Transform -> Load를 함수 체이닝으로 처리합니다.
	 * 
	 * @throws IOException 문서 읽기 실패 시
	 */
	public List<Document> executeETL() throws IOException {
		log.info("Starting functional-style ETL pipeline");
		
		// 1. textReader.read() -> TXT 파일을 읽어 List<Document> 반환 (Extract)
		DocumentReader textReader = new TextReader(this.resource);

		int defaultChunkSize = 800;
		int minChunkSizeChars = 350;

		// 2. TokenTextSplitter: 큰 문서를 AI 모델의 컨텍스트 윈도우에 맞게 토큰 기반으로 분할
		// CL100K_BASE 인코딩(OpenAI 모델 호환)을 사용하여 문장 경계를 고려한 의미 있는 청크 생성
		DocumentTransformer transformer = new TokenTextSplitter(
			defaultChunkSize,    			// defaultChunkSize: 목표 토큰 수 (800 토큰)
			minChunkSizeChars,   			// minChunkSizeChars: 최소 문자 수 (350자)
			5,       // minChunkLengthToEmbed: 임베딩할 최소 청크 길이 (5자 미만은 제외)
			10000,            // maxNumChunks: 최대 청크 수 (메모리 보호)
			true             // keepSeparator: 구분자(줄바꿈 등) 유지 여부
		);
		List<Document> spliterDocuments = transformer.apply(textReader.read());
		
		// 3. vectorStore.write() -> 분할된 Document들을 임베딩하여 PGVector에 저장 (Load)
		vectorStore.write(spliterDocuments);
		
		log.info("Functional-style ETL pipeline completed successfully");

		return spliterDocuments;
	}

	/**
	 * Document 리스트를 문자열로 변환합니다.
	 * 각 Document의 텍스트를 추출하여 줄바꿈으로 결합합니다.
	 * 
	 * @param documents 변환할 Document 리스트
	 * @return 결합된 문자열
	 */
	public String documentsToString(List<Document> documents) {
		if (documents == null || documents.isEmpty()) {
			return "";
		}
		
		return documents.stream()
				.map(Document::getFormattedContent)
				.collect(java.util.stream.Collectors.joining("\n"));
	}
}
