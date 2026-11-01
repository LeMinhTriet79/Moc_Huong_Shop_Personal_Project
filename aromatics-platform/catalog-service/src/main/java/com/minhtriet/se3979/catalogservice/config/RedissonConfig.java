package com.minhtriet.se3979.catalogservice.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private String redisPort;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        try {
            Config config = new Config();
            String redisAddress = String.format("redis://%s:%s", redisHost, redisPort);

            config.useSingleServer()
                    .setAddress(redisAddress)
                    .setConnectTimeout(2000)
                    .setTimeout(3000)
                    .setRetryAttempts(1)
                    .setRetryInterval(1000)
                    .setConnectionMinimumIdleSize(2)
                    .setConnectionPoolSize(10);

            RedissonClient client = Redisson.create(config);
            // Thử kiểm tra kết nối thực tế tới Redis
            client.getKeys().count();
            log.info("🌿 [REDISSON] Kết nối Redis Distributed Lock thành công tại {}:{}", redisHost, redisPort);
            return client;
        } catch (Exception e) {
            log.warn("⚠️ [REDISSON] Không thể kết nối tới Redis tại {}:{} ({}). Tự động kích hoạt In-Memory Concurrent Lock Fallback cho Local Development!",
                    redisHost, redisPort, e.getMessage());
            return createFallbackRedissonClient();
        }
    }

    private RedissonClient createFallbackRedissonClient() {
        final ConcurrentHashMap<String, ReentrantLock> localLocks = new ConcurrentHashMap<>();

        return (RedissonClient) Proxy.newProxyInstance(
                RedissonClient.class.getClassLoader(),
                new Class<?>[]{RedissonClient.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String methodName = method.getName();
                        if ("getLock".equals(methodName) && args != null && args.length >= 1) {
                            String lockName = String.valueOf(args[0]);
                            ReentrantLock reentrantLock = localLocks.computeIfAbsent(lockName, k -> new ReentrantLock());
                            return createFallbackRLock(lockName, reentrantLock);
                        }
                        if ("shutdown".equals(methodName)) {
                            localLocks.clear();
                            return null;
                        }
                        if ("isShutdown".equals(methodName)) {
                            return false;
                        }
                        if ("isShuttingDown".equals(methodName)) {
                            return false;
                        }
                        if ("toString".equals(methodName)) {
                            return "FallbackLocalRedissonClient";
                        }
                        if ("hashCode".equals(methodName)) {
                            return System.identityHashCode(proxy);
                        }
                        if ("equals".equals(methodName) && args != null && args.length == 1) {
                            return proxy == args[0];
                        }
                        return null;
                    }
                }
        );
    }

    private RLock createFallbackRLock(String lockName, ReentrantLock reentrantLock) {
        return (RLock) Proxy.newProxyInstance(
                RLock.class.getClassLoader(),
                new Class<?>[]{RLock.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String name = method.getName();
                        switch (name) {
                            case "tryLock":
                                if (args == null || args.length == 0) {
                                    return reentrantLock.tryLock();
                                } else if (args.length == 2 && args[0] instanceof Long waitTime && args[1] instanceof TimeUnit unit) {
                                    return reentrantLock.tryLock(waitTime, unit);
                                } else if (args.length >= 3 && args[0] instanceof Long waitTime && args[2] instanceof TimeUnit unit) {
                                    // args: waitTime, leaseTime, unit
                                    return reentrantLock.tryLock(waitTime, unit);
                                }
                                return true;
                            case "lock":
                                reentrantLock.lock();
                                return null;
                            case "unlock":
                                if (reentrantLock.isHeldByCurrentThread()) {
                                    reentrantLock.unlock();
                                }
                                return null;
                            case "isHeldByCurrentThread":
                                return reentrantLock.isHeldByCurrentThread();
                            case "isLocked":
                                return reentrantLock.isLocked();
                            case "getName":
                                return lockName;
                            case "getHoldCount":
                                return reentrantLock.getHoldCount();
                            case "forceUnlock":
                                while (reentrantLock.isHeldByCurrentThread()) {
                                    reentrantLock.unlock();
                                }
                                return true;
                            case "toString":
                                return "FallbackLocalRLock[" + lockName + "]";
                            case "hashCode":
                                return reentrantLock.hashCode();
                            case "equals":
                                return args != null && args.length == 1 && proxy == args[0];
                            default:
                                return null;
                        }
                    }
                }
        );
    }
}
