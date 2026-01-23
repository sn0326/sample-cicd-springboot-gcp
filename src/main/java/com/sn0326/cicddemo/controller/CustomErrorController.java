package com.sn0326.cicddemo.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * カスタムエラーコントローラー
 *
 * Spring Bootのデフォルトエラーハンドリングをカスタマイズし、
 * HTTPステータスコードに応じた適切なエラーページを表示する。
 * TERASOLUNAガイドラインに準拠。
 */
@Controller
@Slf4j
public class CustomErrorController implements ErrorController {

    /**
     * エラーハンドリングのエンドポイント
     *
     * HTTPステータスコードに応じて適切なエラーページを表示する。
     * - 404: リソースが見つからない
     * - 403: アクセス拒否
     * - 500: サーバー内部エラー
     * - その他: 汎用エラーページ
     *
     * @param request HTTPリクエスト
     * @param model ビューに渡すモデル
     * @return エラーページのビュー名
     */
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // HTTPステータスコードを取得
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            // 404 Not Found
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                String requestUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
                log.warn("404 Not Found: {}", requestUri);
                model.addAttribute("requestUri", requestUri);
                model.addAttribute("errorMessage", "お探しのページが見つかりません。");
                return "error/404";
            }

            // 403 Forbidden
            if (statusCode == HttpStatus.FORBIDDEN.value()) {
                log.warn("403 Forbidden: {}", request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));
                model.addAttribute("errorMessage", "このページへのアクセスは拒否されました。");
                return "error/403";
            }

            // 500 Internal Server Error
            if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                Throwable throwable = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
                log.error("500 Internal Server Error", throwable);
                model.addAttribute("exceptionCode", "e.xx.5000");
                model.addAttribute("errorMessage", "サーバー内部エラーが発生しました。");
                return "error/system-error";
            }

            // その他のエラー
            log.warn("HTTP Error {}: {}", statusCode, request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));
            model.addAttribute("statusCode", statusCode);
            model.addAttribute("errorMessage", "エラーが発生しました。");
            return "error/system-error";
        }

        // ステータスコードが取得できない場合
        log.error("Unknown error occurred");
        model.addAttribute("exceptionCode", "e.xx.9999");
        model.addAttribute("errorMessage", "予期しないエラーが発生しました。");
        return "error/system-error";
    }
}
