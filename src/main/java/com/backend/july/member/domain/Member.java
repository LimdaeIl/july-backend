package com.backend.july.member.domain;

import com.backend.july.common.audit.BaseAuditEntity;
import com.backend.july.member.exception.MemberErrorCode;
import com.backend.july.member.exception.MemberException;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "v1_members",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_v1_members_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_v1_members_phone", columnNames = "phone")
        }
)
@Entity
public class Member extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "password", nullable = false, length = 512)
    private String password;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private MemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MemberStatus status;

    private Member(String email, String password, String name, String phone, Address address) {
        this.email = validate(email, "이메일");
        this.password = validate(password, "비밀번호");
        this.name = validate(name, "이름");
        this.phone = validate(phone, "전화번호");
        this.address = Objects.requireNonNull(address, "주소는 null일 수 없습니다.");
        this.role = MemberRole.MEMBER;
        this.status = MemberStatus.ACTIVATE;
    }

    public static Member create(String email, String password, String name, String phone,
            Address address) {
        return new Member(email, password, name, phone, address);
    }

    public void updateProfile(String name, String phone, Address address) {
        validateUpdatable();

        this.name = validate(name, "이름");
        this.phone = validate(phone, "전화번호");
        this.address = Objects.requireNonNull(address, "주소는 null일 수 없습니다.");
    }

    public void withdraw() {
        if (status == MemberStatus.WITHDRAWAL) {
            throw new MemberException(MemberErrorCode.ALREADY_WITHDRAWN_MEMBER);
        }

        this.status = MemberStatus.WITHDRAWAL;
    }

    public boolean isWithdrawal() {
        return status == MemberStatus.WITHDRAWAL;
    }

    public boolean hasSamePhone(String phone) {
        return this.phone.equals(phone);
    }

    private void validateUpdatable() {
        if (status != MemberStatus.ACTIVATE) {
            throw new MemberException(MemberErrorCode.MEMBER_NOT_ACTIVE);
        }
    }

    private String validate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new MemberException(MemberErrorCode.VALIDATE_FIELD, fieldName);
        }

        return value;
    }

    public boolean isSignInAllowed() {
        return status == MemberStatus.ACTIVATE;
    }
}

