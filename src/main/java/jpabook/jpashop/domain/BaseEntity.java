package jpabook.jpashop.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@MappedSuperclass
@Data
public abstract class BaseEntity {

    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
