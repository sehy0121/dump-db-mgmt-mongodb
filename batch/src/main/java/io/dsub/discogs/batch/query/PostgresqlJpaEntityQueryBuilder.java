package io.dsub.discogs.batch.query;

import io.dsub.discogs.common.entity.base.BaseEntity;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PostgresqlJpaEntityQueryBuilder extends SqlJpaEntityQueryBuilder<BaseEntity> {

  public static final String ON_CONFLICT_DO_NOTHING = "ON CONFLICT DO NOTHING";

  public static final String UPSERT_QUERY_FORMAT = "ON CONFLICT (%s) DO UPDATE SET %s WHERE %s";

  @Override
  public String getUpsertQuery(Class<? extends BaseEntity> targetClass) {
    String insertClause = getInsertQuery(targetClass) + SPACE;
    boolean idAutoGenerated = isIdAutoGenerated(targetClass);
    boolean hasUniqueConstraints = hasUniqueConstraints(targetClass);
    if (idAutoGenerated && !hasUniqueConstraints) {
      return insertClause;
    }

    String upsertClause =
        String.format(
            UPSERT_QUERY_FORMAT,
            getOnConflictCols(targetClass),
            getUpdateClause(targetClass),
            getWhereClause(targetClass));

    if (!hasJoinColumns(targetClass)) {
      return String.join(SPACE, insertClause, upsertClause);
    }
    return String.join(
        SPACE, insertClause, getRelationExistCountingWhereClause(targetClass), upsertClause);
  }

  @Override
  public String getIdOnlyInsertQuery(Class<? extends BaseEntity> targetClass) {
    String tblName = getTableName(targetClass);
    Map<String, String> idMap = getIdMappings(targetClass);
    List<String> idColumns = new ArrayList<>(idMap.keySet());
    List<String> idFields = new ArrayList<>(idMap.values());
    Field createdAt = getCreatedAtField(targetClass);
    Field lastModified = getLastModifiedField(targetClass);
    String mappedIdFields = getFormattedValueFields(createdAt, lastModified, idFields);
    String query =
        String.format(
            DEFAULT_SQL_INSERT_QUERY_FORMAT, tblName, String.join(",", idColumns), mappedIdFields);
    return String.join(SPACE, query, ON_CONFLICT_DO_NOTHING);
  }

  private String getWhereClause(Class<? extends BaseEntity> targetClass) {
    String tblName = getTableName(targetClass);
    boolean idAutoGenerated = isIdAutoGenerated(targetClass);
    boolean hasUniqueConstraints = hasUniqueConstraints(targetClass);
    if (idAutoGenerated && hasUniqueConstraints) {
      return getUniqueConstraintColumns(targetClass).entrySet().stream()
          .map(this::wrapColumnFieldByEquals)
          .map(wrapped -> tblName + PERIOD + wrapped)
          .collect(Collectors.joining(SPACE + AND + SPACE));
    }

    if (!idAutoGenerated && hasUniqueConstraints) {
      Map<String, String> colFieldMap = getUniqueConstraintColumns(targetClass);
      colFieldMap.putAll(getIdMappings(targetClass));
      return colFieldMap.entrySet().stream()
          .map(this::wrapColumnFieldByEquals)
          .map(wrapped -> tblName + PERIOD + wrapped)
          .collect(Collectors.joining(SPACE + AND + SPACE));
    }

    return getIdMappings(targetClass).entrySet().stream()
        .map(this::wrapColumnFieldByEquals)
        .map(wrapped -> tblName + PERIOD + wrapped)
        .collect(Collectors.joining(SPACE + AND + SPACE));
  }

  private String getOnConflictCols(Class<? extends BaseEntity> targetClass) {
    boolean idAutoGenerated = isIdAutoGenerated(targetClass);
    boolean hasUniqueConstraints = hasUniqueConstraints(targetClass);
    if (idAutoGenerated && hasUniqueConstraints) {
      return String.join(",", getUniqueConstraintColumns(targetClass).keySet());
    }
    if (!idAutoGenerated && hasUniqueConstraints) {
      Set<String> cols = new HashSet<>(getUniqueConstraintColumns(targetClass).keySet());
      cols.addAll(getIdMappings(targetClass).keySet());
      return String.join(",", cols);
    }
    return String.join(",", getIdMappings(targetClass).keySet());
  }

  private String wrapColumnFieldByEquals(Entry<String, String> entry) {
    return entry.getKey() + EQUALS + COLON + entry.getValue();
  }

  private String getUpdateClause(Class<? extends BaseEntity> targetClass) {
    boolean idAutoGenerated = isIdAutoGenerated(targetClass);
    boolean hasUniqueConstraints = hasUniqueConstraints(targetClass);

    Field lastModified = getLastModifiedField(targetClass);
    Field createdAt = getCreatedAtField(targetClass);

    Stream<Entry<String, String>> stream;
    if (idAutoGenerated && hasUniqueConstraints) {
      stream = getMappingsOutsideUniqueConstraints(targetClass, false).entrySet().stream();
    } else {
      stream = getMappings(targetClass, false).entrySet().stream();
    }

    return stream
        .filter(entry -> createdAt == null || !entry.getValue().contains(createdAt.getName()))
        .map(entry -> makeUpdateSetMapping(entry.getKey(), entry.getValue(), lastModified))
        .collect(Collectors.joining(COMMA));
  }

  private String makeUpdateSetMapping(String key, String value, Field lastUpdateAt) {
    if (lastUpdateAt != null && value.equals(lastUpdateAt.getName())) {
      return key + EQUALS + "NOW()";
    }
    return key + EQUALS + COLON + value;
  }
}