package com.bloggingit.odata.storage;

import com.bloggingit.odata.model.BaseEntity;
import com.bloggingit.odata.model.Book;
import com.bloggingit.odata.exception.EntityDataException;
import com.bloggingit.odata.olingo.v4.util.ReflectionUtils;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This class provides a simple in-memory data storage with some example data.
 */
public class InMemoryDataStorage {

    private static final ConcurrentMap<String, Book> DATA_MAT = new ConcurrentHashMap<>();

    static {
        createMaterialsList();
    }
    private static void createMaterialsList() {
        LocalDateTime lDate1 = LocalDateTime.of(2011, Month.JULY, 21, 0, 0);
        LocalDateTime lDate2 = LocalDateTime.of(2015, Month.AUGUST, 6, 13, 15);
        LocalDateTime lDate3 = LocalDateTime.of(2013, Month.MAY, 12, 0, 0);

        Date date1 = Date.from(lDate1.atZone(ZoneId.systemDefault()).toInstant());
        Date date2 = Date.from(lDate2.atZone(ZoneId.systemDefault()).toInstant());
        Date date3 = Date.from(lDate3.atZone(ZoneId.systemDefault()).toInstant());

        Book mat1 = new Book("EN", "This is the description of book 1", "A", true, true, "G1", "kg", 9.95, date1);
        //Materials book2 = new Materials("N", "This is the description of book 2", date2, DATA_AUTHOR.get("2"), 5.99, true);
        //Materials book3 = new Materials("O", "This is the description of book 3", date3, DATA_AUTHOR.get("3"), 14.50, false);

        mat1.setId("A1");
        //book2.setId("B2");
        //book3.setId("C3");

        DATA_MAT.put(mat1.getId(), mat1);
        //DATA_MAT.put(book2.getId(), book2);
        //DATA_MAT.put(book3.getId(), book3);
    }

    @SuppressWarnings("unchecked")
    private static <T> ConcurrentMap<String, T> getDataMapByEntityClass(Class<T> entityClazz) {
        ConcurrentMap<String, T> entities = null;

        if (Book.class.equals(entityClazz)) {
            entities = (ConcurrentMap<String, T>) DATA_MAT;
//        } else if (Author.class.equals(entityClazz)) {
//            entities = (ConcurrentMap<String, T>) DATA_AUTHOR;
        }

        return entities;
    }

    public static <T> List<T> getDataListByBaseEntityClass(Class<T> entityClazz) {
        final ConcurrentMap<String, T> entityMap = getDataMapByEntityClass(entityClazz);

        return new ArrayList<>(entityMap.values());
    }

    public static <T> T getDataByClassAndId(Class<T> entityClazz, String id) {
        final ConcurrentMap<String, T> entityMap = getDataMapByEntityClass(entityClazz);

        return entityMap.get(id);
    }

    public static <T> void deleteDataByClassAndId(Class<T> entityClazz, String id) {
        final ConcurrentMap<String, T> entityMap = getDataMapByEntityClass(entityClazz);
        entityMap.remove(id);
    }

    public static <T> T createEntity(T newEntity) throws EntityDataException {

        if (newEntity == null) {
            throw new EntityDataException("Unable to create entity, because no entity given");
        }

        if (newEntity instanceof BaseEntity) {
            if (newEntity instanceof Book) {
//                Author author = ((Materials) newEntity).getAuthor();
//                if (author != null) {
//                    if ("".equals(author.getId())) {
//                        author = (Author) getDataByClassAndId(newEntity.getClass(), author.getId());
//                    } else {
//                        author = createEntity(author);
//                    }
//                    ((Materials) newEntity).setAuthor(author);
//                }
            }

            @SuppressWarnings("unchecked")
            final ConcurrentMap<String, T> entityMap = getDataMapByEntityClass((Class<T>) newEntity.getClass());

            BaseEntity baseEntity = (BaseEntity) newEntity;

            if ("".equals(baseEntity.getId()) || entityMap.putIfAbsent(baseEntity.getId(), newEntity) == null) {
                baseEntity.setId("" + entityMap.size() + 1);
                entityMap.put(baseEntity.getId(), newEntity);
            } else {
                throw new EntityDataException("Could not create entity, because it already exists");
            }
        } else {
            throw new EntityDataException("Unable to create unsupported entity class" + newEntity);
        }

        return newEntity;
    }

    @SuppressWarnings("unchecked")
    public static <T> T updateEntity(Class<T> entityClazz, String id, Map<String, Object> newPropertyValues, boolean nullableUnkownProperties) throws EntityDataException {
        T updatedEntity = null;

        if (BaseEntity.class.isAssignableFrom(entityClazz)) {
            final ConcurrentMap<String, BaseEntity> entityMap = (ConcurrentMap<String, BaseEntity>) getDataMapByEntityClass(entityClazz);

            BaseEntity baseEntity = entityMap.get(id);

            if (baseEntity == null) {
                throw new EntityDataException("Unable to update entity, because the entity does not exist");
            }

            newPropertyValues.entrySet().forEach((propEntry) -> {
                String fieldname = propEntry.getKey();
                Object value = propEntry.getValue();
                if (!("id".equalsIgnoreCase(fieldname))) {
                    ReflectionUtils.invokePropertySetter(fieldname, baseEntity, value);
                }
            });

            entityMap.put(baseEntity.getId(), baseEntity);

            updatedEntity = (T) baseEntity;
        } else {
            throw new EntityDataException("Unable to update unsupported entity class" + entityClazz);
        }

        return updatedEntity;
    }
}
