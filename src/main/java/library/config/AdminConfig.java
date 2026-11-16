
package library.config;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

import library.exception.StorageException;

public class AdminConfig {
    private final Properties properties;

    public AdminConfig() {
        properties = new Properties();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("admin.properties")) {

            if (input == null) {
                throw new StorageException("admin.properties not found.");
            }

            properties.load(input);

            String adminId = properties.getProperty("admin.id");
            String adminPasswordHash = properties.getProperty("admin.passwordHash");

            if (adminId == null || adminId.isBlank()
                    || adminPasswordHash == null || adminPasswordHash.isBlank()) {

                throw new StorageException("Invalid admin configuration.");
            }

        } catch (Exception e) {
            throw new StorageException("Unable to load admin configuration.", e);
        }
    }

    public String getAdminId() {
        return properties.getProperty("admin.id");
    }

    public String getAdminPasswordHash() {
        return properties.getProperty("admin.passwordHash");
    }
}
