package community.mingle.app.src.post.model;

import lombok.Data;
import lombok.Getter;

@Getter
@Data
public class PatchUpdatePostRequest {
    private String title;
    private String content;
}
