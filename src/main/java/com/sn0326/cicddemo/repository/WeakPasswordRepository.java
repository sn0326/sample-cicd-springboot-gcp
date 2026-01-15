package com.sn0326.cicddemo.repository;

import com.sn0326.cicddemo.model.WeakPassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 弱いパスワードのリポジトリ
 */
@Repository
public interface WeakPasswordRepository extends JpaRepository<WeakPassword, Long> {
}
