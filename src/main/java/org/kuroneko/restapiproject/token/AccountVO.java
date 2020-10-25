package org.kuroneko.restapiproject.token;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.kuroneko.restapiproject.account.domain.UserAuthority;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/*
    Seuciry의 Principal로써만 사용하기 위해 등록한 Object
    기본적인 값은 Account와 동일
 */

@Entity
@Table(name = "USER")
@Getter
@NoArgsConstructor
public class AccountVO implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @Setter
    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Setter
    @Column(nullable = false)
    private String password;

    @Setter
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private UserAuthority authority;

//    @CreationTimestamp
    @Setter
    @Column(nullable = false, length = 20, updatable = false)
    private LocalDateTime createdAt;                        // 등록 일자

    @UpdateTimestamp
    @Column(length = 20)
    private LocalDateTime updatedAt;                        // 수정 일자

    @Setter
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean isEnable = true;                        // 사용 여부

    public AccountVO(String email, String password, UserAuthority authority) {
        this.email = email;
        this.password = password;
        this.authority = authority;
    }
}
