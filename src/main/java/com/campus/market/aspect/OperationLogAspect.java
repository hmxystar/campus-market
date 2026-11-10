package com.campus.market.aspect;

import com.campus.market.annotation.OperationLog;
import com.campus.market.common.UserContext;
import com.campus.market.entity.OperationLogEntity;
import com.campus.market.mapper.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.databind.ObjectMapper;

/**
 * 操作日志切面：环绕通知计时 + 异步落库。
 * 注意 Boot 4 注入的 ObjectMapper 是 Jackson 3（tools.jackson），不要再引 fastjson。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogMapper operationLogMapper;
    private final ObjectMapper objectMapper;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint pjp, OperationLog operationLog) throws Throwable {
        long start = System.currentTimeMillis();
        boolean success = true;
        try {
            return pjp.proceed();
        } catch (Throwable e) {
            success = false;
            throw e;
        } finally {
            // TODO 讲义写法：同类内调用 @Async 不会真正异步（自调用不经过代理）。
            //      正式实现应把 saveLog 抽到独立 Bean（如 OperationLogRecorder）再调用。
            saveLog(pjp, operationLog, System.currentTimeMillis() - start, success);
        }
    }

    @Async("taskExecutor")
    public void saveLog(ProceedingJoinPoint pjp, OperationLog op, long cost, boolean success) {
        try {
            MethodSignature sig = (MethodSignature) pjp.getSignature();
            OperationLogEntity entity = new OperationLogEntity();
            entity.setUserId(UserContext.getUserId());
            entity.setModule(op.module());
            entity.setAction(op.action());
            entity.setMethod(sig.getDeclaringTypeName() + "#" + sig.getName());
            entity.setParams(objectMapper.writeValueAsString(pjp.getArgs()));
            entity.setCostMs(cost);
            entity.setSuccess(success ? 1 : 0);

            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                entity.setIp(req.getRemoteAddr());
            }
            operationLogMapper.insert(entity);
        } catch (Exception e) {
            log.warn("操作日志写入失败", e);      // 日志失败不能影响主业务
        }
    }
}
