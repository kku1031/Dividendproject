package model;  //서비스 구현을 위한 패키지.
                //데이터를 내부에서 주고 받기 위한 용도(데이터 내용 변경 등)
                //Entity와 역할 구분 지어야함.
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Company {

    private String ticker;
    private String name;

}
