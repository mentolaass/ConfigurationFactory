package ru.mentola;

import lombok.experimental.UtilityClass;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import ru.mentola.api.ConfigurationField;
import ru.mentola.api.ConfigurationModel;
import ru.mentola.util.Util;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

/**
 * Builder for configuration models.
 */
@UtilityClass
public class ConfigurationFactory {
    /**
     *  Builds configuration model from Bukkit configuration file.
     * @param file Bukkit configuration file.
     * @param clazz Configuration model class.
     * @return Configured instance of {@code clazz} from the given {@code file}.
     * @param <T> - Type of configuration model.
     * @throws RuntimeException if failed to build configuration model.
     */
    public <T> T build(@NotNull final FileConfiguration file, @NotNull final Class<T> clazz) {
        try {
            final Field[] fields = getFieldsWithAnnotation(clazz);
            final Class<?>[] fieldTypes = getFieldTypes(fields);
            final Constructor<T> constructor = clazz.getConstructor(fieldTypes);
            return constructor.newInstance(getFileFieldValues(file, fields));
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException("Failed to build configuration model", e);
        }
    }
    /**
     * Gets fields with {@link ConfigurationField} annotation from the given class.
     * @param clazz Configuration model class.
     * @return Fields with {@link ConfigurationField} annotation.
     */
    private Field[] getFieldsWithAnnotation(final Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(ConfigurationField.class))
                .toArray(Field[]::new);
    }
    /**
     * Gets field types from the given fields.
     * @param fields Fields with {@link ConfigurationField} annotation.
     * @return Field types.
     */
    private Class<?>[] getFieldTypes(final Field[] fields) {
        return Arrays.stream(fields)
               .map(Field::getType)
               .toArray(Class<?>[]::new);
    }
    /**
     * Get file field values from the given Bukkit configuration file and fields.
     * @param file Bukkit configuration file.
     * @param fields Fields with {@link ConfigurationField} annotation.
     * @return File field values.
     */
    private Object[] getFileFieldValues(final FileConfiguration file, final Field[] fields) {
        return Arrays.stream(fields)
               .map((field) -> getFileFieldValue(file, field, field.getAnnotation(ConfigurationField.class)))
               .toArray();
    }
    /**
     * Get file field value from the given Bukkit configuration file and field path.
     * @param file Bukkit configuration file.
     * @param field Field with {@link ConfigurationField} annotation.
     * @param configurationField Configuration field annotation.
     * @return File field value.
     */
    @Nullable
    private Object getFileFieldValue(final FileConfiguration file, final Field field, final ConfigurationField configurationField) {
        final Class<?> fieldType = field.getType();
        if (file.contains(configurationField.path())) {
            if (fieldType.isPrimitive())
                return getPrimitiveFieldValue(file, fieldType, configurationField.path());
            else {
                if (fieldType.equals(String.class)) return file.getString(configurationField.path());
                if (fieldType.equals(List.class)) return file.getList(configurationField.path());
                if (fieldType.equals(ConfigurationModel.class)) return build(file, field.getType());
            }
        }
        if (!configurationField.defaultValue().isEmpty())
            return Util.parsePrimitiveObjectFromString(configurationField.defaultValue());
        return null;
    }
    /**
     * Get primitive field value from the given Bukkit configuration file and field type.
     * @param file Bukkit configuration file.
     * @param fieldType Field type.
     * @param path Field path.
     * @return Primitive field value.
     */
    @Nullable
    private Object getPrimitiveFieldValue(final FileConfiguration file, final Class<?> fieldType, final String path) {
        if (fieldType.equals(Integer.TYPE)) return file.getInt(path);
        if (fieldType.equals(Double.TYPE)) return file.getDouble(path);
        if (fieldType.equals(Long.TYPE)) return file.getLong(path);
        if (fieldType.equals(Boolean.TYPE)) return file.getBoolean(path);
        return null;
    }
}