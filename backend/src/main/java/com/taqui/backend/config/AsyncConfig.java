package com.taqui.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Liga o suporte a @Async. Os listeners de notificação usam @Async +
 * @TransactionalEventListener(AFTER_COMMIT) para não travar a request nem
 * notificar em cima de uma transação que deu rollback. O Spring Boot já
 * autoconfigura um ThreadPoolTaskExecutor, que o @Async usa por padrão.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
