package it.unipi.githeritage.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class ResponseDTO<T> extends BaseDTO {
    private Boolean success;
    private String message;
    private T data;
}
