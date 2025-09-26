package com.userservice;

import com.userservice.dao.UserDAO;
import com.userservice.entity.User;
import com.userservice.util.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Главный класс консольного приложения для управления пользователями
 * Предоставляет интерактивный интерфейс для выполнения CRUD операций
 */
public class Main {

    // Логгер для записи информации о работе приложения
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    // DAO для работы с пользователями
    private static UserDAO userDAO = new UserDAO();
    
    // Scanner для чтения пользовательского ввода
    private static Scanner scanner = new Scanner(System.in);

    /**
     * Точка входа в приложение
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        logger.info("Запуск приложения User Service");
        
        try {
            // Проверяем подключение к базе данных при запуске
            logger.info("Проверка подключения к базе данных...");
            
            // Выводим приветствие
            printWelcomeMessage();
            
            // Запускаем основной цикл приложения
            runMainLoop();
            
        } catch (Exception e) {
            logger.error("Критическая ошибка в приложении: {}", e.getMessage(), e);
            System.err.println("Произошла критическая ошибка: " + e.getMessage());
        } finally {
            // Закрываем ресурсы при завершении работы
            cleanup();
        }
    }

    /**
     * Вывод приветственного сообщения
     */
    private static void printWelcomeMessage() {
        System.out.println("=====================================");
        System.out.println("   СИСТЕМА УПРАВЛЕНИЯ ПОЛЬЗОВАТЕЛЯМИ");
        System.out.println("=====================================");
        System.out.println("Добро пожаловать в систему управления пользователями!");
        System.out.println("Используйте Hibernate ORM для работы с PostgreSQL");
        System.out.println("=====================================");
    }

    /**
     * Основной цикл работы приложения
     * Отображает меню и обрабатывает пользовательский выбор
     */
    private static void runMainLoop() {
        boolean running = true;
        
        while (running) {
            try {
                displayMenu();
                int choice = getUserChoice();
                
                switch (choice) {
                    case 1:
                        createNewUser();
                        break;
                    case 2:
                        viewUserById();
                        break;
                    case 3:
                        viewAllUsers();
                        break;
                    case 4:
                        updateUser();
                        break;
                    case 5:
                        deleteUser();
                        break;
                    case 6:
                        searchUserByEmail();
                        break;
                    case 7:
                        showUserStatistics();
                        break;
                    case 0:
                        System.out.println("Завершение работы приложения...");
                        running = false;
                        break;
                    case -2:
                        System.out.println("Обнаружен конец ввода. Завершение работы...");
                        running = false;
                        break;
                    default:
                        System.out.println("Неверный выбор. Пожалуйста, попробуйте снова.");
                }
                
                // Пауза перед следующей операцией
                if (running) {
                    System.out.println("\nНажмите Enter для продолжения...");
                    if (scanner.hasNextLine()) {
                        scanner.nextLine();
                    } else {
                        // EOF достигнут, завершаем приложение
                        running = false;
                    }
                }
                
            } catch (Exception e) {
                logger.error("Ошибка в главном цикле: {}", e.getMessage(), e);
                System.err.println("Произошла ошибка: " + e.getMessage());
                System.out.println("Попробуйте еще раз.");
            }
        }
    }

    /**
     * Отображение главного меню
     */
    private static void displayMenu() {
        System.out.println("\n=====================================");
        System.out.println("             ГЛАВНОЕ МЕНЮ");
        System.out.println("=====================================");
        System.out.println("1. Создать нового пользователя");
        System.out.println("2. Найти пользователя по ID");
        System.out.println("3. Показать всех пользователей");
        System.out.println("4. Обновить данные пользователя");
        System.out.println("5. Удалить пользователя");
        System.out.println("6. Найти пользователя по Email");
        System.out.println("7. Статистика пользователей");
        System.out.println("0. Выход");
        System.out.println("=====================================");
        System.out.print("Выберите действие (0-7): ");
    }

    /**
     * Получить выбор пользователя из меню
     * @return номер выбранного пункта меню, -2 если достигнут EOF
     */
    private static int getUserChoice() {
        try {
            if (scanner.hasNextLine()) {
                return Integer.parseInt(scanner.nextLine().trim());
            } else {
                // EOF достигнут - завершаем приложение
                logger.info("Достигнут конец ввода (EOF), завершаем приложение");
                return -2;
            }
        } catch (NumberFormatException e) {
            return -1; // Неверный ввод
        }
    }

    /**
     * Создание нового пользователя
     */
    private static void createNewUser() {
        System.out.println("\n--- СОЗДАНИЕ НОВОГО ПОЛЬЗОВАТЕЛЯ ---");
        
        try {
            System.out.print("Введите имя пользователя: ");
            if (!scanner.hasNextLine()) {
                System.out.println("Ввод завершен.");
                return;
            }
            String name = scanner.nextLine().trim();
            
            if (name.isEmpty()) {
                System.out.println("Имя не может быть пустым!");
                return;
            }
            
            System.out.print("Введите email: ");
            if (!scanner.hasNextLine()) {
                System.out.println("Ввод завершен.");
                return;
            }
            String email = scanner.nextLine().trim();
            
            if (email.isEmpty()) {
                System.out.println("Email не может быть пустым!");
                return;
            }
            
            // Проверяем, не существует ли пользователь с таким email
            if (userDAO.existsByEmail(email)) {
                System.out.println("Пользователь с таким email уже существует!");
                return;
            }
            
            System.out.print("Введите возраст (или нажмите Enter, чтобы пропустить): ");
            if (!scanner.hasNextLine()) {
                System.out.println("Ввод завершен.");
                return;
            }
            String ageInput = scanner.nextLine().trim();
            
            Integer age = null;
            if (!ageInput.isEmpty()) {
                try {
                    age = Integer.parseInt(ageInput);
                    if (age < 0 || age > 150) {
                        System.out.println("Некорректный возраст. Устанавливается значение null.");
                        age = null;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Некорректный формат возраста. Устанавливается значение null.");
                    age = null;
                }
            }
            
            // Создаем нового пользователя
            User newUser = new User(name, email, age);
            User savedUser = userDAO.createUser(newUser);
            
            System.out.println("✓ Пользователь успешно создан!");
            System.out.println("ID: " + savedUser.getId());
            System.out.println("Имя: " + savedUser.getName());
            System.out.println("Email: " + savedUser.getEmail());
            System.out.println("Возраст: " + (savedUser.getAge() != null ? savedUser.getAge() : "не указан"));
            System.out.println("Дата создания: " + savedUser.getCreatedAt());
            
        } catch (Exception e) {
            logger.error("Ошибка при создании пользователя: {}", e.getMessage(), e);
            System.err.println("Ошибка при создании пользователя: " + e.getMessage());
        }
    }

    /**
     * Просмотр пользователя по ID
     */
    private static void viewUserById() {
        System.out.println("\n--- ПОИСК ПОЛЬЗОВАТЕЛЯ ПО ID ---");
        
        try {
            System.out.print("Введите ID пользователя: ");
            String idInput = scanner.nextLine().trim();
            
            Long id;
            try {
                id = Long.parseLong(idInput);
            } catch (NumberFormatException e) {
                System.out.println("Некорректный формат ID!");
                return;
            }
            
            Optional<User> userOptional = userDAO.findUserById(id);
            
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                System.out.println("✓ Пользователь найден:");
                printUserDetails(user);
            } else {
                System.out.println("✗ Пользователь с ID " + id + " не найден.");
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при поиске пользователя: {}", e.getMessage(), e);
            System.err.println("Ошибка при поиске пользователя: " + e.getMessage());
        }
    }

    /**
     * Просмотр всех пользователей
     */
    private static void viewAllUsers() {
        System.out.println("\n--- СПИСОК ВСЕХ ПОЛЬЗОВАТЕЛЕЙ ---");
        
        try {
            List<User> users = userDAO.findAllUsers();
            
            if (users.isEmpty()) {
                System.out.println("В системе нет пользователей.");
                return;
            }
            
            System.out.println("Найдено пользователей: " + users.size());
            System.out.println("=====================================");
            
            for (User user : users) {
                printUserDetails(user);
                System.out.println("-------------------------------------");
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при получении списка пользователей: {}", e.getMessage(), e);
            System.err.println("Ошибка при получении списка пользователей: " + e.getMessage());
        }
    }

    /**
     * Обновление данных пользователя
     */
    private static void updateUser() {
        System.out.println("\n--- ОБНОВЛЕНИЕ ДАННЫХ ПОЛЬЗОВАТЕЛЯ ---");
        
        try {
            System.out.print("Введите ID пользователя для обновления: ");
            String idInput = scanner.nextLine().trim();
            
            Long id;
            try {
                id = Long.parseLong(idInput);
            } catch (NumberFormatException e) {
                System.out.println("Некорректный формат ID!");
                return;
            }
            
            Optional<User> userOptional = userDAO.findUserById(id);
            
            if (!userOptional.isPresent()) {
                System.out.println("✗ Пользователь с ID " + id + " не найден.");
                return;
            }
            
            User user = userOptional.get();
            System.out.println("Текущие данные пользователя:");
            printUserDetails(user);
            
            System.out.println("\nВведите новые данные (нажмите Enter, чтобы оставить текущее значение):");
            
            System.out.print("Новое имя [" + user.getName() + "]: ");
            String newName = scanner.nextLine().trim();
            if (!newName.isEmpty()) {
                user.setName(newName);
            }
            
            System.out.print("Новый email [" + user.getEmail() + "]: ");
            String newEmail = scanner.nextLine().trim();
            if (!newEmail.isEmpty()) {
                // Проверяем, не занят ли новый email другим пользователем
                if (!newEmail.equals(user.getEmail()) && userDAO.existsByEmail(newEmail)) {
                    System.out.println("Email уже используется другим пользователем!");
                    return;
                }
                user.setEmail(newEmail);
            }
            
            System.out.print("Новый возраст [" + (user.getAge() != null ? user.getAge() : "не указан") + "]: ");
            String newAgeInput = scanner.nextLine().trim();
            if (!newAgeInput.isEmpty()) {
                try {
                    Integer newAge = Integer.parseInt(newAgeInput);
                    if (newAge >= 0 && newAge <= 150) {
                        user.setAge(newAge);
                    } else {
                        System.out.println("Некорректный возраст, значение не изменено.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Некорректный формат возраста, значение не изменено.");
                }
            }
            
            // Обновляем пользователя
            User updatedUser = userDAO.updateUser(user);
            
            System.out.println("✓ Данные пользователя успешно обновлены!");
            printUserDetails(updatedUser);
            
        } catch (Exception e) {
            logger.error("Ошибка при обновлении пользователя: {}", e.getMessage(), e);
            System.err.println("Ошибка при обновлении пользователя: " + e.getMessage());
        }
    }

    /**
     * Удаление пользователя
     */
    private static void deleteUser() {
        System.out.println("\n--- УДАЛЕНИЕ ПОЛЬЗОВАТЕЛЯ ---");
        
        try {
            System.out.print("Введите ID пользователя для удаления: ");
            String idInput = scanner.nextLine().trim();
            
            Long id;
            try {
                id = Long.parseLong(idInput);
            } catch (NumberFormatException e) {
                System.out.println("Некорректный формат ID!");
                return;
            }
            
            // Сначала показываем информацию о пользователе
            Optional<User> userOptional = userDAO.findUserById(id);
            
            if (!userOptional.isPresent()) {
                System.out.println("✗ Пользователь с ID " + id + " не найден.");
                return;
            }
            
            User user = userOptional.get();
            System.out.println("Данные пользователя для удаления:");
            printUserDetails(user);
            
            System.out.print("Вы уверены, что хотите удалить этого пользователя? (y/N): ");
            String confirmation = scanner.nextLine().trim().toLowerCase();
            
            if (confirmation.equals("y") || confirmation.equals("да")) {
                boolean deleted = userDAO.deleteUser(id);
                
                if (deleted) {
                    System.out.println("✓ Пользователь успешно удален!");
                } else {
                    System.out.println("✗ Не удалось удалить пользователя.");
                }
            } else {
                System.out.println("Удаление отменено.");
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при удалении пользователя: {}", e.getMessage(), e);
            System.err.println("Ошибка при удалении пользователя: " + e.getMessage());
        }
    }

    /**
     * Поиск пользователя по email
     */
    private static void searchUserByEmail() {
        System.out.println("\n--- ПОИСК ПОЛЬЗОВАТЕЛЯ ПО EMAIL ---");
        
        try {
            System.out.print("Введите email пользователя: ");
            String email = scanner.nextLine().trim();
            
            if (email.isEmpty()) {
                System.out.println("Email не может быть пустым!");
                return;
            }
            
            Optional<User> userOptional = userDAO.findUserByEmail(email);
            
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                System.out.println("✓ Пользователь найден:");
                printUserDetails(user);
            } else {
                System.out.println("✗ Пользователь с email '" + email + "' не найден.");
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при поиске пользователя по email: {}", e.getMessage(), e);
            System.err.println("Ошибка при поиске пользователя: " + e.getMessage());
        }
    }

    /**
     * Показать статистику пользователей
     */
    private static void showUserStatistics() {
        System.out.println("\n--- СТАТИСТИКА ПОЛЬЗОВАТЕЛЕЙ ---");
        
        try {
            long totalUsers = userDAO.getUserCount();
            System.out.println("Общее количество пользователей: " + totalUsers);
            
            if (totalUsers > 0) {
                List<User> allUsers = userDAO.findAllUsers();
                
                // Подсчитываем статистику по возрасту
                long usersWithAge = allUsers.stream().filter(u -> u.getAge() != null).count();
                long usersWithoutAge = totalUsers - usersWithAge;
                
                System.out.println("Пользователи с указанным возрастом: " + usersWithAge);
                System.out.println("Пользователи без указанного возраста: " + usersWithoutAge);
                
                if (usersWithAge > 0) {
                    double averageAge = allUsers.stream()
                            .filter(u -> u.getAge() != null)
                            .mapToInt(User::getAge)
                            .average()
                            .orElse(0.0);
                    
                    System.out.printf("Средний возраст: %.1f лет%n", averageAge);
                }
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при получении статистики: {}", e.getMessage(), e);
            System.err.println("Ошибка при получении статистики: " + e.getMessage());
        }
    }

    /**
     * Вывод детальной информации о пользователе
     * @param user пользователь для отображения
     */
    private static void printUserDetails(User user) {
        System.out.println("ID: " + user.getId());
        System.out.println("Имя: " + user.getName());
        System.out.println("Email: " + user.getEmail());
        System.out.println("Возраст: " + (user.getAge() != null ? user.getAge() + " лет" : "не указан"));
        System.out.println("Дата создания: " + user.getCreatedAt());
    }

    /**
     * Очистка ресурсов при завершении работы приложения
     */
    private static void cleanup() {
        try {
            if (scanner != null) {
                scanner.close();
            }
            
            // Закрываем SessionFactory
            HibernateUtil.shutdown();
            
            logger.info("Приложение завершено успешно");
            System.out.println("До свидания!");
            
        } catch (Exception e) {
            logger.error("Ошибка при закрытии ресурсов: {}", e.getMessage(), e);
        }
    }
}
