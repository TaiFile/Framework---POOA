package br.ufscar.pooa.Framework___POOA;

import br.ufscar.pooa.Framework___POOA.framework.annotation.Column;
import br.ufscar.pooa.Framework___POOA.framework.annotation.Entity;
import br.ufscar.pooa.Framework___POOA.framework.annotation.Id;

@Entity(tableName = "users")
public class User {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "age")
    private Integer age;

    public Long getId() {
        return id;
    }

    public User setId(Long id) {
        this.id = id;
        return this;
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
}
