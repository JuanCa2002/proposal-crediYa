package co.com.pragma.api.dto;

import lombok.*;

import java.math.BigInteger;
import java.util.List;

@Getter
@Setter
public class PaginatedResponseDTO<T> {

    private List<T> data;
    private BigInteger totalElements;
    private Integer page;

}
