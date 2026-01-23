package com.sn0326.cicddemo.controller;

import com.sn0326.cicddemo.exception.BusinessException;
import com.sn0326.cicddemo.exception.InvalidTokenException;
import com.sn0326.cicddemo.exception.ResourceNotFoundException;
import com.sn0326.cicddemo.exception.SystemException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * グローバル例外ハンドラー
 *
 * TERASOLUNAガイドラインに準拠した例外処理を提供。
 * @ControllerAdviceにより、すべてのコントローラーで発生した例外を統一的に処理する。
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * ビジネス例外のハンドリング
     *
     * ユーザーに表示可能なエラーメッセージを持つ例外を処理。
     * リファラーまたはデフォルトページにリダイレクトする。
     *
     * @param ex 発生したBusinessException
     * @param redirectAttributes リダイレクト先にメッセージを渡すための属性
     * @param request HTTPリクエスト（リファラー取得用）
     * @return リダイレクト先のビュー名
     */
    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(
            BusinessException ex,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {

        log.warn("Business exception: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());

        // リファラーまたはホームページにリダイレクト
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        }
        return "redirect:/";
    }

    /**
     * トークン無効例外のハンドリング
     *
     * パスワードリセットやメール確認のトークンが無効な場合の専用処理。
     * 専用のエラーページを表示する。
     *
     * @param ex 発生したInvalidTokenException
     * @param model ビューに渡すモデル
     * @return エラーページのビュー名
     */
    @ExceptionHandler(InvalidTokenException.class)
    public String handleInvalidTokenException(
            InvalidTokenException ex,
            Model model) {

        log.warn("Invalid token: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/invalid-token";
    }

    /**
     * リソース未検出例外のハンドリング（404エラー）
     *
     * 要求されたリソースが見つからない場合の処理。
     *
     * @param ex 発生したResourceNotFoundException
     * @param model ビューに渡すモデル
     * @return 404エラーページのビュー名
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFoundException(
            ResourceNotFoundException ex,
            Model model) {

        log.warn("Resource not found: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    /**
     * システム例外のハンドリング
     *
     * システムレベルのエラーを処理。
     * ユーザーには例外コードのみを表示し、詳細はログに記録する。
     *
     * @param ex 発生したSystemException
     * @param model ビューに渡すモデル
     * @return システムエラーページのビュー名
     */
    @ExceptionHandler(SystemException.class)
    public String handleSystemException(
            SystemException ex,
            Model model) {

        // 例外コードに基づいてログレベルを判定
        SystemException.LogLevel logLevel = ex.getLogLevel();
        switch (logLevel) {
            case INFO:
                log.info("System exception [{}]: {}", ex.getCode(), ex.getMessage());
                break;
            case WARN:
                log.warn("System exception [{}]: {}", ex.getCode(), ex.getMessage(), ex);
                break;
            case ERROR:
            default:
                log.error("System exception [{}]: {}", ex.getCode(), ex.getMessage(), ex);
                break;
        }

        model.addAttribute("exceptionCode", ex.getCode());
        return "error/system-error";
    }

    /**
     * 予期しない例外のハンドリング
     *
     * すべての未処理例外をキャッチする最終ハンドラー。
     * システムエラーとして扱い、詳細をログに記録する。
     *
     * @param ex 発生した例外
     * @param model ビューに渡すモデル
     * @return システムエラーページのビュー名
     */
    @ExceptionHandler(Exception.class)
    public String handleException(
            Exception ex,
            Model model) {

        log.error("Unexpected exception", ex);
        model.addAttribute("exceptionCode", "e.xx.9999");
        model.addAttribute("errorMessage", "予期しないエラーが発生しました。");
        return "error/system-error";
    }
}
