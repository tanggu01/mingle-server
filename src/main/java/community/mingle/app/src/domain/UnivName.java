package community.mingle.app.src.domain;

import community.mingle.app.src.domain.Univ.UnivPost;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="univ_name")

public class UnivName {

    @Id
    @Column(name ="univ_id")
    private int id;

    @Column(name = "univ_name")
    private String univName;

    @OneToMany(mappedBy = "univName")
    private List<UnivEmail> univEmailList = new ArrayList<>();

    /*private List<User> members */
    //단방향?

    /** 2.3 단방향 아님*/
//    @OneToMany(mappedBy = "univName")
//    private List<UnivPost> univPosts = new ArrayList<>();


    @OneToMany(mappedBy = "univName")
    private List<UnivEmail> univEmailList = new ArrayList<>();

    /** 추가 */


}
