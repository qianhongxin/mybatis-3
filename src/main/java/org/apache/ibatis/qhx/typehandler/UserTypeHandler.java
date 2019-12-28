package org.apache.ibatis.qhx.typehandler;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @Description:
 * @Author: red
 * @Time: 2019/12/26 22:01
 */
public class UserTypeHandler implements TypeHandler<User> {

    @Override
    public void setParameter(PreparedStatement ps, int i, User user, JdbcType jdbcType) throws SQLException {
        ps.setString(i, user.toJson());
    }

    @Override
    public User getResult(ResultSet rs, String columnName) throws SQLException {
        String userJson = rs.getString(columnName);
        return new User(userJson);
    }

    @Override
    public User getResult(ResultSet rs, int columnIndex) throws SQLException {
        String userJson = rs.getString(columnIndex);
        return new User(userJson);
    }

    @Override
    public User getResult(CallableStatement cs, int columnIndex) throws SQLException {
        String userJson = cs.getString(columnIndex);
        return new User(userJson);
    }

}
