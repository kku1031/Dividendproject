package model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Dividend {
    //배당금 정보.
    private LocalDateTime date;
    private String dividend;

}
