package com.hellzzang.repository;

import com.hellzzang.entity.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * packageName    : com.hellchang.repository
 * fileName       : MailRepository
 * author         : hj
 * date           : 2023-05-03
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-05-03        hj       최초 생성
 */
public interface EmailRepository extends JpaRepository<Email, Long> {
    Optional<Email> findByCheckCode(String checkCode);

    //이메일에 등록된 id 삭제처리
    void deleteByUserId(String userId);
}
