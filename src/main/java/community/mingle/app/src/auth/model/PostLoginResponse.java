package community.mingle.app.src.auth.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Data
public class PostLoginResponse {
//    private String email;
    private Long userIdx;
    private String jwt;
}