package com.userservice.dao;

import com.userservice.entity.User;
import com.userservice.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.NoResultException;
import java.util.List;
import java.util.Optional;

/**
 * DAO (Data Access Object) класс для работы с сущностью User
 * Реализует паттерн DAO для разделения бизнес-логики и логики доступа к данным
 * Содержит все CRUD операции для сущности User
 */
public class UserDAO {

    // Логгер для записи информации о операциях с базой данных
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    /**
     * Создать нового пользователя в базе данных
     * @param user объект пользователя для сохранения
     * @return сохраненный пользователь с установленным ID
     * @throws RuntimeException если произошла ошибка при сохранении
     */
    public User createUser(User user) {
        logger.info("Создание нового пользователя: {}", user.getEmail());
        
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Начинаем транзакцию
            transaction = session.beginTransaction();
            
            // Сохраняем пользователя в базе данных
            session.save(user);
            
            // Подтверждаем транзакцию
            transaction.commit();
            
            logger.info("Пользователь успешно создан с ID: {}", user.getId());
            return user;
            
        } catch (Exception e) {
            // В случае ошибки откатываем транзакцию
            if (transaction != null) {
                try {
                    transaction.rollback();
                    logger.warn("Транзакция отменена из-за ошибки при создании пользователя");
                } catch (Exception rollbackEx) {
                    logger.error("Ошибка при откате транзакции: {}", rollbackEx.getMessage());
                }
            }
            
            logger.error("Ошибка при создании пользователя: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось создать пользователя: " + e.getMessage(), e);
        }
    }

    /**
     * Найти пользователя по ID
     * @param id идентификатор пользователя
     * @return Optional с пользователем если найден, пустой Optional если не найден
     */
    public Optional<User> findUserById(Long id) {
        logger.info("Поиск пользователя по ID: {}", id);
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, id);
            
            if (user != null) {
                logger.info("Пользователь найден: {}", user.getEmail());
                return Optional.of(user);
            } else {
                logger.info("Пользователь с ID {} не найден", id);
                return Optional.empty();
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при поиске пользователя по ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Ошибка при поиске пользователя: " + e.getMessage(), e);
        }
    }

    /**
     * Найти пользователя по email адресу
     * @param email email адрес пользователя
     * @return Optional с пользователем если найден, пустой Optional если не найден
     */
    public Optional<User> findUserByEmail(String email) {
        logger.info("Поиск пользователя по email: {}", email);
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Создаем HQL запрос для поиска по email
            Query<User> query = session.createQuery(
                "FROM User u WHERE u.email = :email", User.class);
            query.setParameter("email", email);
            
            try {
                User user = query.getSingleResult();
                logger.info("Пользователь с email {} найден", email);
                return Optional.of(user);
            } catch (NoResultException e) {
                logger.info("Пользователь с email {} не найден", email);
                return Optional.empty();
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при поиске пользователя по email {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Ошибка при поиске пользователя: " + e.getMessage(), e);
        }
    }

    /**
     * Получить всех пользователей из базы данных
     * @return список всех пользователей
     */
    public List<User> findAllUsers() {
        logger.info("Получение списка всех пользователей");
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Создаем HQL запрос для получения всех пользователей
            Query<User> query = session.createQuery("FROM User ORDER BY createdAt DESC", User.class);
            List<User> users = query.getResultList();
            
            logger.info("Найдено {} пользователей", users.size());
            return users;
            
        } catch (Exception e) {
            logger.error("Ошибка при получении списка пользователей: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка при получении пользователей: " + e.getMessage(), e);
        }
    }

    /**
     * Обновить данные пользователя
     * @param user пользователь с обновленными данными
     * @return обновленный пользователь
     * @throws RuntimeException если произошла ошибка при обновлении
     */
    public User updateUser(User user) {
        logger.info("Обновление пользователя с ID: {}", user.getId());
        
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Начинаем транзакцию
            transaction = session.beginTransaction();
            
            // Обновляем пользователя в базе данных
            session.update(user);
            
            // Подтверждаем транзакцию
            transaction.commit();
            
            logger.info("Пользователь с ID {} успешно обновлен", user.getId());
            return user;
            
        } catch (Exception e) {
            // В случае ошибки откатываем транзакцию
            if (transaction != null) {
                try {
                    transaction.rollback();
                    logger.warn("Транзакция отменена из-за ошибки при обновлении пользователя");
                } catch (Exception rollbackEx) {
                    logger.error("Ошибка при откате транзакции: {}", rollbackEx.getMessage());
                }
            }
            
            logger.error("Ошибка при обновлении пользователя: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось обновить пользователя: " + e.getMessage(), e);
        }
    }

    /**
     * Удалить пользователя по ID
     * @param id идентификатор пользователя для удаления
     * @return true если пользователь был удален, false если пользователь не найден
     * @throws RuntimeException если произошла ошибка при удалении
     */
    public boolean deleteUser(Long id) {
        logger.info("Удаление пользователя с ID: {}", id);
        
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Начинаем транзакцию
            transaction = session.beginTransaction();
            
            // Находим пользователя для удаления
            User user = session.get(User.class, id);
            
            if (user != null) {
                // Удаляем пользователя из базы данных
                session.delete(user);
                
                // Подтверждаем транзакцию
                transaction.commit();
                
                logger.info("Пользователь с ID {} успешно удален", id);
                return true;
            } else {
                // Пользователь не найден, откатываем транзакцию
                transaction.rollback();
                logger.info("Пользователь с ID {} не найден для удаления", id);
                return false;
            }
            
        } catch (Exception e) {
            // В случае ошибки откатываем транзакцию
            if (transaction != null) {
                try {
                    transaction.rollback();
                    logger.warn("Транзакция отменена из-за ошибки при удалении пользователя");
                } catch (Exception rollbackEx) {
                    logger.error("Ошибка при откате транзакции: {}", rollbackEx.getMessage());
                }
            }
            
            logger.error("Ошибка при удалении пользователя с ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Не удалось удалить пользователя: " + e.getMessage(), e);
        }
    }

    /**
     * Проверить существование пользователя с указанным email
     * @param email email для проверки
     * @return true если пользователь с таким email существует, false если нет
     */
    public boolean existsByEmail(String email) {
        logger.info("Проверка существования пользователя с email: {}", email);
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Создаем запрос для подсчета пользователей с указанным email
            Query<Long> query = session.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class);
            query.setParameter("email", email);
            
            Long count = query.getSingleResult();
            boolean exists = count > 0;
            
            logger.info("Пользователь с email {} {}", email, exists ? "существует" : "не существует");
            return exists;
            
        } catch (Exception e) {
            logger.error("Ошибка при проверке существования пользователя с email {}: {}", 
                        email, e.getMessage(), e);
            throw new RuntimeException("Ошибка при проверке пользователя: " + e.getMessage(), e);
        }
    }

    /**
     * Получить количество всех пользователей в системе
     * @return общее количество пользователей
     */
    public long getUserCount() {
        logger.info("Получение количества пользователей");
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(u) FROM User u", Long.class);
            Long count = query.getSingleResult();
            
            logger.info("Общее количество пользователей: {}", count);
            return count;
            
        } catch (Exception e) {
            logger.error("Ошибка при получении количества пользователей: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка при подсчете пользователей: " + e.getMessage(), e);
        }
    }
}
