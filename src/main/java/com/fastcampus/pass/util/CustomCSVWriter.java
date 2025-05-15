package com.fastcampus.pass.util;

// OpenCSV 라이브러리의 CSVWriter 클래스를 import
import com.opencsv.CSVWriter;
// lombok의 Slf4j 어노테이션을 사용하여 로그를 쉽게 남길 수 있게 함
import lombok.extern.slf4j.Slf4j;

import java.io.FileWriter;
import java.util.List;

// @Slf4j 어노테이션을 통해 log 객체를 자동으로 생성해줌
@Slf4j
public class CustomCSVWriter {

    // write 메서드는 주어진 파일명(fileName)으로 CSV 파일을 생성하고, 데이터를 기록함
    // fileName: 생성할 CSV 파일의 경로 및 이름
    // data: CSV에 기록할 데이터(각 행은 String 배열로 표현)
    public static int write(final String fileName, List<String[]> data) {
        int rows = 0; // 실제로 기록한 행(row)의 수를 저장할 변수
        // try-with-resources 구문: FileWriter와 CSVWriter를 자동으로 닫아줌(자원 누수 방지)
        try (CSVWriter writer = new CSVWriter(new FileWriter(fileName))) {
            // writeAll 메서드: 전달받은 data(List<String[]>)를 한 번에 모두 파일에 기록
            writer.writeAll(data);
            rows = data.size(); // 기록한 행의 수를 저장
        } catch (Exception e) {
            // 예외 발생 시, 에러 로그를 남김
            // log.error: 에러 메시지와 함께 파일명을 출력
            log.error("CustomCSVWriter - write: CSV 파일 생성 실패, fileName: {}", fileName);
        }
        // 기록한 행의 수를 반환
        return rows;
    }

}
