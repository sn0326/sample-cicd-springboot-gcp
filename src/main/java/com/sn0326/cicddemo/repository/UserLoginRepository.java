package com.sn0326.cicddemo.repository;

import com.sn0326.cicddemo.model.UserLogin;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ユーザーログインのリポジトリ
 */
@Repository
public interface UserLoginRepository extends JpaRepository<UserLogin, Long> {

    /**
     * ユーザー名で成功したログインを新しい順に取得
     *
     * @param username ユーザー名
     * @param pageable ページング情報
     * @return ログインリスト
     */
    List<UserLogin> findByUsernameAndSuccessTrueOrderByLoggedInAtDesc(String username, Pageable pageable);

    /**
     * ユーザー名で前回のログイン日時を取得（現在のログインを除く）
     * 最新のログインをスキップして、2番目のログイン日時を取得
     *
     * @param username ユーザー名
     * @return 前回ログイン日時
     */
    @Query(value = "SELECT logged_in_at FROM user_logins " +
                   "WHERE username = :username AND success = true " +
                   "ORDER BY logged_in_at DESC " +
                   "LIMIT 1 OFFSET 1",
           nativeQuery = true)
    LocalDateTime findLastLoginTime(@Param("username") String username);
}
