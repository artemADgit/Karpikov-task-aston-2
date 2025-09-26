package com.userservice.util;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Утилитарный класс для работы с Hibernate
 * Предоставляет централизованный доступ к SessionFactory
 */
public class HibernateUtil {

    // Логгер для записи информации о работе Hibernate
    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);
    
    // Единственный экземпляр SessionFactory для всего приложения
    private static SessionFactory sessionFactory;

    // Статический блок для инициализации SessionFactory при загрузке класса
    static {
        try {
            buildSessionFactory();
            logger.info("SessionFactory успешно создана");
        } catch (Throwable ex) {
            logger.error("Ошибка при создании SessionFactory", ex);
            throw new ExceptionInInitializerError("Не удалось создать SessionFactory: " + ex.getMessage());
        }
    }

    /**
     * Создает и настраивает SessionFactory
     * Загружает конфигурацию из hibernate.cfg.xml и устанавливает параметры подключения из переменных окружения
     */
    private static void buildSessionFactory() {
        try {
            logger.info("Начинаем создание SessionFactory");
            
            // Создаем объект конфигурации Hibernate
            Configuration configuration = new Configuration();
            
            // Загружаем конфигурацию из hibernate.cfg.xml
            configuration.configure("hibernate.cfg.xml");
            
            // Получаем параметры подключения к базе данных из переменных окружения
            String databaseUrl = System.getenv("DATABASE_URL");
            String pgUser = System.getenv("PGUSER");
            String pgPassword = System.getenv("PGPASSWORD");
            String pgHost = System.getenv("PGHOST");
            String pgPort = System.getenv("PGPORT");
            String pgDatabase = System.getenv("PGDATABASE");
            
            // Логируем информацию о подключении (без пароля для безопасности)
            logger.info("Подключение к базе данных:");
            logger.info("Host: {}", pgHost);
            logger.info("Port: {}", pgPort);
            logger.info("Database: {}", pgDatabase);
            logger.info("User: {}", pgUser);
            
            // Создаем Properties для настройки подключения
            Properties props = new Properties();
            
            // Преобразуем DATABASE_URL в формат JDBC, удаляя учетные данные из URL
            String jdbcUrl;
            String extractedUser = null;
            String extractedPassword = null;
            
            if (databaseUrl != null && !databaseUrl.isEmpty()) {
                String workingUrl = databaseUrl;
                
                // Извлекаем учетные данные из URL перед преобразованием
                if (workingUrl.contains("://") && workingUrl.contains("@")) {
                    try {
                        String protocol = workingUrl.substring(0, workingUrl.indexOf("://"));
                        String urlWithoutProtocol = workingUrl.substring(workingUrl.indexOf("://") + 3);
                        
                        if (urlWithoutProtocol.contains("@")) {
                            String[] parts = urlWithoutProtocol.split("@", 2);
                            String credentials = parts[0];
                            String hostAndRest = parts[1];
                            
                            if (credentials.contains(":")) {
                                String[] credParts = credentials.split(":", 2);
                                extractedUser = credParts[0];
                                extractedPassword = credParts[1];
                            }
                            
                            // Восстанавливаем URL без учетных данных
                            workingUrl = protocol + "://" + hostAndRest;
                        }
                    } catch (Exception e) {
                        logger.warn("Ошибка при извлечении учетных данных из URL: {}", e.getMessage());
                    }
                }
                
                // Преобразуем протокол в JDBC формат
                if (workingUrl.startsWith("postgres://")) {
                    jdbcUrl = workingUrl.replace("postgres://", "jdbc:postgresql://");
                } else if (workingUrl.startsWith("postgresql://")) {
                    jdbcUrl = "jdbc:" + workingUrl;
                } else if (workingUrl.startsWith("jdbc:")) {
                    jdbcUrl = workingUrl; // Уже в правильном формате
                } else {
                    // Если формат неизвестен, строим URL из компонентов
                    jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s?sslmode=require", pgHost, pgPort, pgDatabase);
                }
                logger.info("Преобразованный JDBC URL: {}", jdbcUrl);
            } else {
                // Собираем URL из отдельных компонентов
                jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s?sslmode=require", pgHost, pgPort, pgDatabase);
                logger.info("Собираем URL подключения из компонентов: {}", jdbcUrl);
            }
            
            props.setProperty("hibernate.connection.url", jdbcUrl);
            
            // Устанавливаем учетные данные
            // Приоритет: отдельные переменные окружения > извлеченные из URL
            String finalUser = pgUser;
            String finalPassword = pgPassword;
            
            // Если отдельные переменные пустые, используем извлеченные из URL
            if ((finalUser == null || finalUser.isEmpty()) && extractedUser != null) {
                finalUser = extractedUser;
                logger.info("Используем пользователя извлеченного из DATABASE_URL: {}", finalUser);
            } else if (finalUser != null && !finalUser.isEmpty()) {
                logger.info("Используем пользователя из PGUSER: {}", finalUser);
            }
            
            if ((finalPassword == null || finalPassword.isEmpty()) && extractedPassword != null) {
                finalPassword = extractedPassword;
                logger.info("Используем пароль извлеченный из DATABASE_URL");
            } else if (finalPassword != null && !finalPassword.isEmpty()) {
                logger.info("Используем пароль из PGPASSWORD");
            }
            
            // Устанавливаем финальные учетные данные
            if (finalUser != null && !finalUser.isEmpty()) {
                props.setProperty("hibernate.connection.username", finalUser);
            }
            if (finalPassword != null && !finalPassword.isEmpty()) {
                props.setProperty("hibernate.connection.password", finalPassword);
            }
            
            // Применяем дополнительные настройки к конфигурации
            configuration.addProperties(props);
            
            // Создаем StandardServiceRegistry с нашей конфигурацией
            StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties())
                    .build();
            
            // Создаем SessionFactory
            sessionFactory = configuration.buildSessionFactory(registry);
            
            logger.info("SessionFactory создана успешно");
            
        } catch (Exception e) {
            logger.error("Критическая ошибка при создании SessionFactory: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось создать SessionFactory", e);
        }
    }

    /**
     * Получить экземпляр SessionFactory
     * @return SessionFactory для работы с базой данных
     */
    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null || sessionFactory.isClosed()) {
            logger.warn("SessionFactory не инициализована или закрыта, пересоздаем...");
            buildSessionFactory();
        }
        return sessionFactory;
    }

    /**
     * Закрыть SessionFactory при завершении работы приложения
     * Освобождает все ресурсы, связанные с Hibernate
     */
    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            logger.info("Закрываем SessionFactory");
            try {
                sessionFactory.close();
                logger.info("SessionFactory успешно закрыта");
            } catch (Exception e) {
                logger.error("Ошибка при закрытии SessionFactory: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Проверка доступности соединения с базой данных
     * @return true если соединение доступно, false если нет
     */
    public static boolean isConnectionAvailable() {
        try {
            // Пытаемся получить сессию для проверки соединения
            getSessionFactory().getCurrentSession();
            return true;
        } catch (Exception e) {
            logger.error("Соединение с базой данных недоступно: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Приватный конструктор для предотвращения создания экземпляров класса
     * Класс должен использоваться только как утилитарный (статические методы)
     */
    private HibernateUtil() {
        // Утилитарный класс не должен создавать экземпляры
    }
}
