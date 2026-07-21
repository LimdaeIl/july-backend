package com.backend.july.member.infrastructure;

import com.backend.july.member.domain.Member;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Optional<Member> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT m
            FROM Member m
            WHERE m.id = :memberId
            """)
    Optional<Member> findByIdForUpdate(
            @Param("memberId") Long memberId
    );

    boolean existsByPhoneAndIdNot(String phone, Long memberId);

}
