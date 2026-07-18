package com.backend.july.member.domain;

import com.backend.july.member.domain.vo.*;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "v1_members")
@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Email email;

    @Embedded
    private Password password;

    @Embedded
    private Name name;

    @Embedded
    private Phone phone;

    @Embedded
    private Address address;

    private Member(Email email, Password password, Name name, Phone phone, Address address) {
        this.email = Objects.requireNonNull(email, "이메일은 필수입니다.");
        this.password = Objects.requireNonNull(password, "비밀번호는 필수입니다.");
        this.name = Objects.requireNonNull(name, "이름은 필수입니다.");
        this.phone = Objects.requireNonNull(phone, "전화번호는 필수입니다.");
        this.address = Objects.requireNonNull(address, "주소는 필수입니다.");
    }

    public static Member create(Email email, Password password, Name name, Phone phone, Address address) {
        return new Member(email, password, name, phone, address);
    }
}

