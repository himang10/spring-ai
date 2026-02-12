package com.example.springai.music;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 음악 서비스 Tool을 제공합니다.
 */
@Component
@Slf4j
public class MusicTools {

    /**
     * 노래 정보를 찾아주는 Tool
     *
     * @param songName 노래 이름
     * @param artist 아티스트 이름
     * @return 찾은 노래 정보
     */
    @Tool(description = "노래 이름과 아티스트로 노래 정보를 찾습니다.")
    public String findSong(
            @ToolParam(description = "노래 제목", required = true) 
            String songName,
            @ToolParam(description = "아티스트 이름", required = true) 
            String artist) {
        
        log.info("노래 검색 - 제목: {}, 아티스트: {}", songName, artist);
        
        // 실제로는 음악 API를 호출하지만 여기서는 시뮬레이션
        StringBuilder result = new StringBuilder();
        result.append("🎵 노래 정보:\n");
        result.append("- 제목: ").append(songName).append("\n");
        result.append("- 아티스트: ").append(artist).append("\n");
        result.append("- 앨범: Best Hits\n");
        result.append("- 재생 시간: 3:45\n");
        result.append("- 장르: Pop\n");
        
        return result.toString();
    }

    /**
     * 재생목록에 노래를 추가하는 Tool
     *
     * @param playlistName 재생목록 이름
     * @param songName 추가할 노래 이름
     * @return 추가 결과 메시지
     */
    @Tool(description = "지정한 재생목록에 노래를 추가합니다.")
    public String addToPlaylist(
            @ToolParam(description = "재생목록 이름", required = true) 
            String playlistName,
            @ToolParam(description = "추가할 노래 제목", required = true) 
            String songName) {
        
        log.info("재생목록 추가 - 목록: {}, 노래: {}", playlistName, songName);
        
        StringBuilder result = new StringBuilder();
        result.append("✅ '").append(songName).append("'을(를) ");
        result.append("'").append(playlistName).append("' 재생목록에 추가했습니다.");
        
        return result.toString();
    }
}
