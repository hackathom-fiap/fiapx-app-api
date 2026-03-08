package Mocks;

import org.springframework.mock.web.MockMultipartFile;

public class MultiPartFileTestMock {


    static public MockMultipartFile createFile(String name, String filename, String contentType) {
        return new MockMultipartFile(
                name,
                filename,
                contentType,
                "video content".getBytes()
        );
    }
}
