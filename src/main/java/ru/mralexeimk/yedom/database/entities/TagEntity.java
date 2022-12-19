package ru.mralexeimk.yedom.database.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "tags")
public class TagEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "tag")
    private String tag;

    @Column(name = "courses_count")
    private int coursesCount;

    @Column(name = "related_tags")
    private String relatedTags;


    @Override
    public String toString() {
        return "TagEntity{" +
                "id=" + id +
                ", tag='" + tag + '\'' +
                ", coursesCount=" + coursesCount +
                ", relatedTags='" + relatedTags + '\'' +
                '}';
    }
}
