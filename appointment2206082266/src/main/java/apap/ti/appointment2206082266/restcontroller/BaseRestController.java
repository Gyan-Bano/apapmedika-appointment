package apap.ti.appointment2206082266.restcontroller;

import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import apap.ti.appointment2206082266.restdto.response.BaseResponseDTO;
@RestControllerAdvice
public class BaseRestController {
    
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResourceFoundException(final NoResourceFoundException ex) {
        var baseResponseDTO = new BaseResponseDTO<NoResourceFoundException>();
        baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
        baseResponseDTO.setMessage("Path URI tidak ditemukan");
        baseResponseDTO.setTimestamp(new Date());

        ex.printStackTrace();

        return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadableException(final HttpMessageNotReadableException ex) {
        var baseResponseDTO = new BaseResponseDTO<HttpMessageNotReadableException>();
        baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
        baseResponseDTO.setMessage(String.format("Terjadi error saat membaca request body. Error: %s", ex.getMessage()));
        baseResponseDTO.setTimestamp(new Date());

        ex.printStackTrace();

        return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(final Exception ex) {
        var baseResponseDTO = new BaseResponseDTO<Exception>();
        baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        baseResponseDTO.setMessage(String.format("Terjadi error pada server. Error: %s", ex.getMessage()));
        baseResponseDTO.setTimestamp(new Date());

        ex.printStackTrace();

        return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
