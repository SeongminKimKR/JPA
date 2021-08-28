package jpabook.jpashop.domain;

import lombok.Data;

import javax.persistence.Entity;

@Entity
@Data
public class Movie extends Item{
    String director;
    String actor;
}
