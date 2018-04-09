/*
 * Copyright 2000-2018 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.v7.data.util.sqlcontainer.query.generator;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * StatementHelper is a simple helper class that assists TableQuery and the
 * query generators in filling a PreparedStatement. The actual statement is
 * generated by the query generator methods, but the resulting statement and all
 * the parameter values are stored in an instance of StatementHelper.
 *
 * This class will also fill the values with correct setters into the
 * PreparedStatement on request.
 *
 * @deprecated As of 8.0, no replacement available.
 */
@Deprecated
public class StatementHelper implements Serializable {

    private String queryString;

    private List<Object> parameters = new ArrayList<Object>();
    private Map<Integer, Class<?>> dataTypes = new HashMap<Integer, Class<?>>();

    public StatementHelper() {
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String getQueryString() {
        return queryString;
    }

    public void addParameterValue(Object parameter) {
        if (parameter != null) {
            parameters.add(parameter);
            dataTypes.put(parameters.size() - 1, parameter.getClass());
        } else {
            throw new IllegalArgumentException(
                    "You cannot add null parameters using addParameters(Object). "
                            + "Use addParameters(Object,Class) instead");
        }
    }

    public void addParameterValue(Object parameter, Class<?> type) {
        parameters.add(parameter);
        dataTypes.put(parameters.size() - 1, type);
    }

    public void setParameterValuesToStatement(PreparedStatement pstmt)
            throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            if (parameters.get(i) == null) {
                handleNullValue(i, pstmt);
            } else {
                pstmt.setObject(i + 1, parameters.get(i));
            }
        }

        /*
         * The following list contains the data types supported by
         * PreparedStatement but not supported by SQLContainer:
         *
         * [The list is provided as PreparedStatement method signatures]
         *
         * setNCharacterStream(int parameterIndex, Reader value)
         *
         * setNClob(int parameterIndex, NClob value)
         *
         * setNString(int parameterIndex, String value)
         *
         * setRef(int parameterIndex, Ref x)
         *
         * setRowId(int parameterIndex, RowId x)
         *
         * setSQLXML(int parameterIndex, SQLXML xmlObject)
         *
         * setBytes(int parameterIndex, byte[] x)
         *
         * setCharacterStream(int parameterIndex, Reader reader)
         *
         * setClob(int parameterIndex, Clob x)
         *
         * setURL(int parameterIndex, URL x)
         *
         * setArray(int parameterIndex, Array x)
         *
         * setAsciiStream(int parameterIndex, InputStream x)
         *
         * setBinaryStream(int parameterIndex, InputStream x)
         *
         * setBlob(int parameterIndex, Blob x)
         */
    }

    private void handleNullValue(int i, PreparedStatement pstmt)
            throws SQLException {
        Class<?> dataType = dataTypes.get(i);
        int index = i + 1;
        if (BigDecimal.class.equals(dataType)) {
            pstmt.setBigDecimal(index, null);
        } else if (Boolean.class.equals(dataType)) {
            pstmt.setNull(index, Types.BOOLEAN);
        } else if (Byte.class.equals(dataType)) {
            pstmt.setNull(index, Types.SMALLINT);
        } else if (Date.class.equals(dataType)) {
            pstmt.setDate(index, null);
        } else if (Double.class.equals(dataType)) {
            pstmt.setNull(index, Types.DOUBLE);
        } else if (Float.class.equals(dataType)) {
            pstmt.setNull(index, Types.FLOAT);
        } else if (Integer.class.equals(dataType)) {
            pstmt.setNull(index, Types.INTEGER);
        } else if (Long.class.equals(dataType)) {
            pstmt.setNull(index, Types.BIGINT);
        } else if (Short.class.equals(dataType)) {
            pstmt.setNull(index, Types.SMALLINT);
        } else if (String.class.equals(dataType)) {
            pstmt.setString(index, null);
        } else if (Time.class.equals(dataType)) {
            pstmt.setTime(index, null);
        } else if (Timestamp.class.equals(dataType)) {
            pstmt.setTimestamp(index, null);
        } else if (byte[].class.equals(dataType)) {
            pstmt.setBytes(index, null);
        } else {

            if (handleUnrecognizedTypeNullValue(i, pstmt, dataTypes)) {
                return;
            }

            throw new SQLException("Data type for parameter " + i
                    + " not supported by SQLContainer: " + dataType.getName());
        }
    }

    /**
     * Handle unrecognized null values. Override this to handle null values for
     * platform specific data types that are not handled by the default
     * implementation of the {@link StatementHelper}.
     *
     * @param i
     * @param pstmt
     * @param dataTypes2
     *
     * @return true if handled, false otherwise
     *
     * @see {@link http://dev.vaadin.com/ticket/9148}
     */
    protected boolean handleUnrecognizedTypeNullValue(int i,
            PreparedStatement pstmt, Map<Integer, Class<?>> dataTypes)
            throws SQLException {
        return false;
    }
}
