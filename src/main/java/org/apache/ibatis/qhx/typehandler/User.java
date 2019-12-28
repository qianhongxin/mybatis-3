package org.apache.ibatis.qhx.typehandler;

/**
 * @Description:
 * @Author: red
 * @Time: 2019/12/26 22:00
 */
public class User {

    private Long id;

    private String name;

    private String password;

    public User(String userJson) {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String toJson() {
        return null;
    }
}
