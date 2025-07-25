package com.example.demo.error;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@Controller
public class CustomErrorController implements ErrorController {

    private final ErrorAttributes errorAttributes;

    public CustomErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        WebRequest webRequest = new ServletWebRequest(request);
        Map<String, Object> errorPropertiesMap = errorAttributes.getErrorAttributes(webRequest, ErrorAttributeOptions.defaults());

        if (statusCode == null) {
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        }

        String message = (String) errorPropertiesMap.get("message");
        if (message == null) message = "No message available";

        String path = (String) errorPropertiesMap.get("path");
        if (path == null) path = "N/A";

        String error = (String) errorPropertiesMap.get("error");
        if (error == null) error = "Error";

        // 404 처리
        if (statusCode == HttpStatus.NOT_FOUND.value()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "status", statusCode,
                            "error", "Not Found",
                            "message", "요청하신 URL이 존재하지 않습니다.",
                            "path", path
                    ));
        }

        // 400 처리 (Bad Request)
        if (statusCode == HttpStatus.BAD_REQUEST.value()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "status", statusCode,
                            "error", "Bad Request",
                            "message", message,
                            "path", path
                    ));
        }

        // 기타 에러
        return ResponseEntity.status(statusCode)
                .body(Map.of(
                        "status", statusCode,
                        "error", error,
                        "message", message,
                        "path", path
                ));
    }
}
