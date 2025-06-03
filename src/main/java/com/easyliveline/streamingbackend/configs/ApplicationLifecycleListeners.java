package com.easyliveline.streamingbackend.configs;

import org.springframework.boot.context.event.*;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class ApplicationLifecycleListeners {

//    @Component
//    public static class StartingListener implements ApplicationListener<ApplicationStartingEvent> {
//        @Override
//        public void onApplicationEvent(ApplicationStartingEvent event) {
//            System.out.println("🔧 [1] ApplicationStartingEvent: Application is starting.");
//        }
//    }
//
//    @Component
//    public static class EnvironmentPreparedListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
//        @Override
//        public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
//            System.out.println("📄 [2] ApplicationEnvironmentPreparedEvent: Environment prepared.");
//        }
//    }

//    @Component
//    public static class ContextInitializedListener implements ApplicationListener<ApplicationContextInitializedEvent> {
//        @Override
//        public void onApplicationEvent(ApplicationContextInitializedEvent event) {
//            System.out.println("📦 [3] ApplicationContextInitializedEvent: Context initialized.");
//        }
//    }

//    @Component
//    public static class PreparedListener implements ApplicationListener<ApplicationPreparedEvent> {
//        @Override
//        public void onApplicationEvent(ApplicationPreparedEvent event) {
//            System.out.println("🧰 [4] ApplicationPreparedEvent: Context prepared, ready to refresh.");
//        }
//    }

    @Component
    public static class ContextRefreshedListener implements ApplicationListener<ContextRefreshedEvent> {
        @Override
        public void onApplicationEvent(ContextRefreshedEvent event) {
            System.out.println("🔁 [5] ContextRefreshedEvent: Context has been refreshed.");
        }
    }

    @Component
    public static class StartedListener implements ApplicationListener<ApplicationStartedEvent> {
        @Override
        public void onApplicationEvent(ApplicationStartedEvent event) {
            System.out.println("🚀 [6] ApplicationStartedEvent: Application started before runners.");
        }
    }

    @Component
    public static class ReadyListener implements ApplicationListener<ApplicationReadyEvent> {
        @Override
        public void onApplicationEvent(ApplicationReadyEvent event) {
            System.out.println("✅ [7] ApplicationReadyEvent: Application is fully ready.");
        }
    }

    @Component
    public static class FailedListener implements ApplicationListener<ApplicationFailedEvent> {
        @Override
        public void onApplicationEvent(ApplicationFailedEvent event) {
            System.out.println("❌ [8] ApplicationFailedEvent: Application startup failed.");
            if (event.getException() != null) {
                event.getException().printStackTrace();
            }
        }
    }
}