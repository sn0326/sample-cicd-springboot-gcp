package com.sn0326.cicddemo.service;

import com.sn0326.cicddemo.model.WeakPassword;
import com.sn0326.cicddemo.repository.WeakPasswordRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 弱いパスワードのキャッシュサービス
 *
 * アプリケーション起動時にDBから全件読み込み、メモリにキャッシュします。
 * 定期的（1時間ごと）にキャッシュをリフレッシュします。
 */
@Service
public class WeakPasswordCacheService {

    private static final Logger logger = LoggerFactory.getLogger(WeakPasswordCacheService.class);

    private final WeakPasswordRepository repository;

    /**
     * 弱いパスワードのキャッシュ（小文字で保存）
     * ConcurrentHashMapのkeySetを使用してスレッドセーフを確保
     */
    private Set<String> cachedPasswords;

    public WeakPasswordCacheService(WeakPasswordRepository repository) {
        this.repository = repository;
        this.cachedPasswords = ConcurrentHashMap.newKeySet();
    }

    /**
     * アプリケーション起動時にキャッシュを初期化
     */
    @PostConstruct
    public void init() {
        refreshCache();
        logger.info("弱いパスワードキャッシュを初期化しました: {} 件", cachedPasswords.size());
    }

    /**
     * 定期的にキャッシュをリフレッシュ（1時間ごと）
     */
    @Scheduled(fixedRate = 3600000)
    public void refreshCache() {
        try {
            Set<String> newCache = repository.findAll()
                .stream()
                .map(WeakPassword::getPassword)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

            cachedPasswords = ConcurrentHashMap.newKeySet();
            cachedPasswords.addAll(newCache);

            logger.debug("弱いパスワードキャッシュをリフレッシュしました: {} 件", cachedPasswords.size());
        } catch (Exception e) {
            logger.error("弱いパスワードキャッシュのリフレッシュに失敗しました", e);
        }
    }

    /**
     * パスワードが弱いパスワードリストに含まれているかチェック
     *
     * @param password チェック対象のパスワード
     * @return 弱いパスワードの場合true
     */
    public boolean isWeakPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return cachedPasswords.contains(password.toLowerCase());
    }

    /**
     * キャッシュされているパスワード数を取得（デバッグ用）
     */
    public int getCacheSize() {
        return cachedPasswords.size();
    }
}
