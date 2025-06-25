package br.ufscar.pooa.Framework___POOA;

import br.ufscar.pooa.Framework___POOA.Enum.UserGender;
import br.ufscar.pooa.Framework___POOA.persistence_framework.annotation.Column;
import br.ufscar.pooa.Framework___POOA.persistence_framework.annotation.Entity;
import br.ufscar.pooa.Framework___POOA.persistence_framework.annotation.Enumerated;
import br.ufscar.pooa.Framework___POOA.persistence_framework.annotation.Id;


@Entity(tableName = "users")
public class User {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "age")
    private Integer age;

    @Column(name = "gender")
    @Enumerated(Enumerated.EnumType.STRING)
    private UserGender gender;

    public Long getId() {
        return id;
    }

    public User setId(Long id) {
        this.id = id;
        return this;
    }

    public UserGender getGender() {
        return gender;
    }

    public void setGender(UserGender gender) {
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public Integer getAge() {
        return age;
    }

    public User setAge(Integer age) {
        this.age = age;
        return this;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", gender=" + gender +
                '}';
    }
}