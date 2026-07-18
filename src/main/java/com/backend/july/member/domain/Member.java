package com.backend.july.member.domain;

import com.backend.july.member.exception.MemberErrorCode;
import com.backend.july.member.exception.MemberException;
import jakarta.persistence.*;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "v1_members")
@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password", nullable = false, length = 512)
    private String password;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Embedded
    private Address address;

    private Member(String email, String password, String name, String phone, Address address) {
        this.email = validate(email, "이메일");
        this.password = validate(password, "비밀번호");
        this.name = validate(name, "이름");
        this.phone = validate(phone, "전화번호");
        this.address = Objects.requireNonNull(address, "주소는 null일 수 없습니다.");
    }

    public static Member create(String email, String password, String name, String phone, Address address) {
        return new Member(email, password, name, phone, address);
    }

    private String validate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new MemberException(MemberErrorCode.VALIDATE_FIELD, fieldName);
        }
        return value;
    }
}
