package com.duanxi.es.config;

import com.alibaba.fastjson.JSON;

/**
 * @author caoduanxi
 * @Date 2021/1/8 20:34
 * @Motto Keep thinking, keep coding!
 */
public class User {
    private String name;
    private Integer age;
    private String school;

    public User() {
    }

    public User(String name, Integer age, String school) {
        this.name = name;
        this.age = age;
        this.school = school;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", school='" + school + '\'' +
                '}';
    }

    public static void main(String[] args) {
        User user = new User("caoduanxi",25,"njupt");
        System.out.println(new String(JSON.toJSONString(user,true)));
    }
}
