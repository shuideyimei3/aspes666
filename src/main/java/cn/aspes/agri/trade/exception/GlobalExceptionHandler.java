package cn.aspes.agri.trade.exception;

import cn.aspes.agri.trade.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }
    
    /**
     * 处理事务回滚异常
     */
    @ExceptionHandler(UnexpectedRollbackException.class)
    public Result<?> handleUnexpectedRollbackException(UnexpectedRollbackException e) {
        log.error("事务回滚异常: ", e);
        return Result.error("执行操作失败，请页面刷新后重试");
    }
    
    /**
     * 处理参数校验异常
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<?> handleValidException(Exception e) {
        String message = "参数校验失败";
        if (e instanceof MethodArgumentNotValidException ex) {
            if (ex.getBindingResult().hasErrors() && ex.getBindingResult().getFieldError() != null) {
                message = ex.getBindingResult().getFieldError().getDefaultMessage();
            }
        } else if (e instanceof BindException ex) {
            if (ex.getBindingResult().hasErrors() && ex.getBindingResult().getFieldError() != null) {
                message = ex.getBindingResult().getFieldError().getDefaultMessage();
            }
        }
        log.error("参数校验异常: {}", message);
        return Result.error(400, message);
    }
    
    /**
     * 处理缺失multipart部分异常
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public Result<?> handleMissingServletRequestPartException(MissingServletRequestPartException e) {
        String message = "缺失必要的文件或表单部分: " + e.getRequestPartName();
        log.error(message);
        return Result.error(400, message);
    }
    
    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        // 过滤事务相关异常以防止无限循环
        if (e instanceof UnexpectedRollbackException) {
            log.error("事务异常: ", e);
            return Result.error("执行操作失败，请页面刷新后重试");
        }
        log.error("系统异常: ", e);
        return Result.error("系统异常，请联系管理员");
    }
}
