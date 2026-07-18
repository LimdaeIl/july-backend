package com.backend.july.member.domain;

import com.backend.july.member.domain.vo.Address;
import com.backend.july.member.domain.vo.Email;
import com.backend.july.member.domain.vo.Name;
import com.backend.july.member.domain.vo.Password;
import com.backend.july.member.domain.vo.Phone;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.address = address;
    }

    public static Member create(Email email, Password password, Name name, Phone phone,
            Address address) {
        return new Member(email, password, name, phone, address);
    }
}

