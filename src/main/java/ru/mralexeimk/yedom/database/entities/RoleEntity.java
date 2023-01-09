package ru.mralexeimk.yedom.database.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import jakarta.persistence.*;
import java.util.Comparator;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "roles")
public class RoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "role", unique = true)
    private String role;

    @Column(name = "inherits", columnDefinition = "TEXT")
    private String inherits;

    @Column(name = "permissions", columnDefinition = "TEXT")
    private String permissions;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        RoleEntity roleEntity = (RoleEntity) o;
        return Comparator.comparingInt(RoleEntity::getId)
                .compare(this, roleEntity) == 0;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Role [id: "+id+", role: "+role+
                ", inherits: "+inherits+", permissions: "+permissions+"]";
    }
}
