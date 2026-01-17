package com.sn0326.cicddemo.repository;

import com.sn0326.cicddemo.model.UserPasskeyBinding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ユーザーとパスキーの紐付け情報のリポジトリ
 */
@Repository
public interface UserPasskeyBindingRepository
        extends JpaRepository<UserPasskeyBinding, UserPasskeyBinding.UserPasskeyBindingId> {

    /**
     * ユーザー名で紐付け情報を検索
     *
     * @param username ユーザー名
     * @return 紐付け情報のリスト
     */
    List<UserPasskeyBinding> findByUsername(String username);

    /**
     * ユーザーエンティティIDで紐付け情報を検索
     *
     * @param userEntityId ユーザーエンティティID
     * @return 紐付け情報
     */
    Optional<UserPasskeyBinding> findByUserEntityId(String userEntityId);

    /**
     * ユーザー名とユーザーエンティティIDで紐付け情報が存在するか確認
     *
     * @param username     ユーザー名
     * @param userEntityId ユーザーエンティティID
     * @return 存在する場合true
     */
    boolean existsByUsernameAndUserEntityId(String username, String userEntityId);

    /**
     * ユーザー名で紐付け情報が存在するか確認
     *
     * @param username ユーザー名
     * @return 存在する場合true
     */
    boolean existsByUsername(String username);

    /**
     * ユーザー名で全ての紐付け情報を削除
     *
     * @param username ユーザー名
     */
    void deleteByUsername(String username);
}
