package io.dsub.discogsdata.batch.argument.formatter;

import java.util.regex.Pattern;

/**
 * Implementation of {@link ArgumentFormatter} that formats url entry into Jdbc connection string.
 */
public class JdbcUrlFormatter implements ArgumentFormatter {

    /*
     * Static patterns to be used!
     */
    private static final Pattern URL_ENTRY = Pattern.compile("^[-]{0,3}url=.*");
    private static final Pattern STARTS_WITH_MYSQL = Pattern.compile("^mysql://.*");
    private static final Pattern STARTS_WITH_JDBC = Pattern.compile("^jdbc:.*");
    private static final Pattern STARTS_WITH_JDBC_MYSQL = Pattern.compile("^jdbc:mysql://.*");
    private static final Pattern PORT_PATTERN = Pattern.compile(".*:[1-9][0-9]{0,4}.*");
    private static final Pattern SCHEMA_PATTERN = Pattern.compile(".*[^/]/[\\w!@#$%^&*-]+.*");

    /**
     * Formats argument to be proper jdbc connection string if marked as url entry.
     *
     * @param arg argument to be evaluated.
     * @return as a jdbc connection string if arg is marked as a url entry. If not,
     * it will simply return the input argument as-is.
     */
    @Override
    public String format(String arg) {
        if (!URL_ENTRY.matcher(arg).matches()) {
            return arg;
        }

        String[] parts = arg.split("=");
        if (parts.length <= 1) {
            return arg;
        }

        String urlValue = arg.substring(arg.indexOf('=') + 1);

        boolean startsWithMysql = STARTS_WITH_MYSQL.matcher(urlValue).matches();
        boolean startsWithJdbc = STARTS_WITH_JDBC.matcher(urlValue).matches();
        boolean startsProperly = STARTS_WITH_JDBC_MYSQL.matcher(urlValue).matches();

        if (startsWithMysql) {
            urlValue = "jdbc:" + urlValue;
        } else if (startsWithJdbc && !startsProperly) {
            urlValue = urlValue.replaceFirst("jdbc:", "jdbc:mysql://");
        } else if (!startsProperly) {
            urlValue = "jdbc:mysql://" + urlValue;
        }

        boolean portExists = PORT_PATTERN.matcher(urlValue).matches();
        boolean schemaExists = SCHEMA_PATTERN.matcher(urlValue).matches();

        if (portExists && schemaExists) {
            return String.join("=", parts[0], urlValue);
        }

        if (!portExists && !schemaExists) {
            return parts[0] + "=" + urlValue + ":3306/discogs_data";
        }

        if (!portExists) {
            int idx = urlValue.lastIndexOf('/');
            return parts[0] + "=" + urlValue.substring(0, idx) + ":3306" + urlValue.substring(idx);
        }

        // missing schema, so we add the default value.
        return parts[0] + "=" + urlValue + "/discogs_data";
    }
}
