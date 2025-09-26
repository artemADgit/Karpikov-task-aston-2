package com.userservice.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Сущность User представляет пользователя в системе
 * Использует аннотации Hibernate для маппинга с базой данных PostgreSQL
 */
@Entity
@Table(name = "users") // Таблица в базе данных будет называться 'users'
public class User {

    /**
     * Уникальный идентификатор пользователя
     * Генерируется автоматически при создании записи
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Имя пользователя
     * Обязательное поле, максимальная длина 100 символов
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Email пользователя
     * Обязательное поле, должно быть уникальным, максимальная длина 150 символов
     */
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    /**
     * Возраст пользователя
     * Может быть null, если пользователь не указал возраст
     */
    @Column(name = "age")
    private Integer age;

    /**
     * Дата и время создания записи пользователя
     * Устанавливается автоматически при создании записи
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Конструктор по умолчанию
     * Необходим для работы Hibernate
     */
    public User() {
        // Hibernate требует конструктор по умолчанию
    }

    /**
     * Конструктор для создания пользователя с базовыми данными
     * @param name имя пользователя
     * @param email email пользователя
     * @param age возраст пользователя
     */
    public User(String name, String email, Integer age) {
        this.name = name;
        this.email = email;
        this.age = age;
        this.createdAt = LocalDateTime.now(); // Устанавливаем текущую дату создания
    }

    /**
     * Метод вызывается автоматически перед сохранением в базу данных
     * Устанавливает дату создания если она не была установлена
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Геттеры и сеттеры

    /**
     * Получить ID пользователя
     * @return ID пользователя
     */
    public Long getId() {
        return id;
    }

    /**
     * Установить ID пользователя (обычно не используется, так как ID генерируется автоматически)
     * @param id ID пользователя
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Получить имя пользователя
     * @return имя пользователя
     */
    public String getName() {
        return name;
    }

    /**
     * Установить имя пользователя
     * @param name имя пользователя
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Получить email пользователя
     * @return email пользователя
     */
    public String getEmail() {
        return email;
    }

    /**
     * Установить email пользователя
     * @param email email пользователя
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Получить возраст пользователя
     * @return возраст пользователя
     */
    public Integer getAge() {
        return age;
    }

    /**
     * Установить возраст пользователя
     * @param age возраст пользователя
     */
    public void setAge(Integer age) {
        this.age = age;
    }

    /**
     * Получить дату создания записи
     * @return дата создания записи
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Установить дату создания записи
     * @param createdAt дата создания записи
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Переопределенный метод toString для удобного вывода информации о пользователе
     * @return строковое представление объекта User
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                ", createdAt=" + createdAt +
                '}';
    }

    /**
     * Переопределенный метод equals для сравнения объектов User
     * @param obj объект для сравнения
     * @return true если объекты равны, false если нет
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return id != null ? id.equals(user.id) : user.id == null;
    }

    /**
     * Переопределенный метод hashCode
     * @return хэш-код объекта
     */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
