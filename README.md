# 학습 목적
- 자바 표준 ORM 기술인 JPA의 연관관계 매핑과 연관관계에 따른 쿼리를 실행 했을 때 내부 구조에 따른 동작방식을 이해
- 출처: [자바 ORM 표준 JPA 프로그래밍 - 기본편-](https://www.inflearn.com/course/ORM-JPA-Basic/dashboard)

## 학습 목차

- [소개](#소개)
- [영속성 관리](#영속성-관리)
- [엔티티 맵핑](#엔티티-맵핑)
- [연관관계 맵핑](#연관관계-맵핑)
- [프록시와 연관관계 관리](#프록시와-연관관계-관리)
- [값 타입](#값-타입)
- [객체지향 쿼리 언어](#객체지향-쿼리-언어)

### 소개
- JPA는 Java Persistence API의 약어로, 자바 진영의 ORM 기술 표준이다.
- 객체는 객체지향적으로 설계하고 데이터베이스는 관계지향형으로 설계할 수 있도록 ORM 프레임워크가 중간에서 매핑역할을 해준다.
- JPA는 애플리케이션과 JDBC 사이에서 동작한다.

#### JPA를 사용해야 하는 이유
- 과거 개발자는 SQL 중심적인 개발을 했기 때문에 다양한 객체들을 SQL로써 맵핑을 해주어야 하는 고충이 있었다.
- 또한 자바의 객체지향 설계의 패러다임과 관계형 데이터 베이스의 패러다임이 불일치 했기 때문에 객체의 설계 방식에 어려움이 있엇다.
- JPA는 객체 중심개발을 하도록 도와주고, 이것은 곳 생산성과 유지보수 측면에서 효율적이라고 할 수 있다.
- 직접 쿼리를 만들지 않아도 JPA 내장 함수를 사용하면 기본적인 쿼리(CRUD)를 만들어 준다.
- 영속성 컨텍스트와, 쓰기 지연, 지연 로딩등 성능 최적화를 위한 다양한 기능들을 제공한다.

-----------
### 영속성 관리

#### 영속성 컨텍스트

- '엔티티를 영구 저장하는 환경'으로 entityManager를 통해 이곳에 접근하고, 데이터를 저장하거나 조회할 때 이곳을 접근하고 엔티티를 영속성 컨텍스트 1차 캐시에 저장하게 된다.

- 영속성 컨텍스트는 entityManager를 통해 트랜잭션이 생길때마다 한 개씩 생성된다.

- 영속성 컨텍스트 안에는 1차 캐시라는 캐시가 있는데, 이곳에 있는 엔티티는 기본키 값과 엔티티를 한 쌍으로 맵 구조로 저장되어 있고,
같은 엔티티를 조회할 때는 쿼리를 호출하지 않고 이곳에서 데이터를 조회한다. 캐시에 없다면 쿼리를 호출하고 해당 엔티티를 캐시에 저장한다.

-  같은 엔티티를 호출하여 서로 다른 객체에 저장해도 동일 성을 보장한다. 
    ```
    Member a = em.find(Member.class, "member1");
    Member b = em.find(Member.class, "member1");
    System.out.println(a == b); //동일성 비교 true
    ```
- 트랜잭션을 지연하는 쓰기 지연 기능을 제공한다. 아래의 코드 처럼 영속성 컨텍스트에 해당 엔티티를 저장해두고, 쓰기 지연이 일어난 SQL 저장소에 쿼리를 저장하고,    커밋이나 플러시가 일어나는 순간 쿼리를 한번에 보냄으
     ```
    EntityManager em = emf.createEntityManager();
    EntityTransaction transaction = em.getTransaction();
    //엔티티 매니저는 데이터 변경시 트랜잭션을 시작해야 한다.
  
    transaction.begin(); // [트랜잭션] 시작
    em.persist(memberA);
    em.persist(memberB);
    //여기까지 INSERT SQL을 데이터베이스에 보내지 않는다.
    //커밋하는 순간 데이터베이스에 INSERT SQL을 보낸다.
  
    transaction.commit(); // [트랜잭션] 커밋
    ```
- JPA로 데이터를 수정하고 싶을때는 해당 엔티티를 조회해서 수정하면 된다.  
마지막으로 조회가 일어난 캐시를 스냅샷으로 저장해두었다가 트랜잭션이 커밋되거나 플러시가 될 때 비교하여 엔티티의 변경을 감지하고 
변경이 일어났다면 수정쿼리를 DB에 호출한다.

- 플러시는 flush()를 직접호출하거나 트랜잭션 커밋이 일어날때 발생하고, JPQL 쿼리를 실행하면 자동으로 일어난다. 
플러시가 일어났다고해서 영속성 컨텍스트가 비워지는 것이 아니라 1차 캐시의 엔티티의 정보를 데이터베이스와 동기화 한다.

컨텍스트를 준영속 상태로 - 영속성 만들고 싶다면 clear()를 통해 완전 초기화 할 수 있고, detach(entity)를 통해 특정 엔티티만 준영속 상태로 전환할 수 있다.

#### 엔티티의 생명주기
![context-lifecycle.PNG](./image/context-lifecycle.PNG)

- 비영속(new/transient): 영속성 컨텍스트와 전혀 관계가 없는 새로운 형태

- 영속(managed): 영속성 컨텍스트에 관리되는 상태

- 준영속(detached): 영속성 컨텍스트에 저장되었다가 분리된 상태

- 삭제(removed): 삭제된 상태

-----------

### 엔티티 맵핑

#### 방식

- 맵핑하고 싶은 객체에 각종 어노테이션을 추가함으로써 엔티티 맵핑을 지원한다.

- 객체와 테이블 맵핑
    - @Entity: JPA가 해당 객체를 엔티티로써 관리할 것이라고 명시한다는 의미이고, 테이블을 자동으로 생성한다.
      
         - 이 어노테이션을 사용하려면 기본 생성자는 필수이다.
         - final, enum, interface, inner 클래스는 사용이 불가능하다.
         - 해당 클래스 필드에 final을 선언하면 안된다.

    - @Table: 엔티티와 매핑할 테이블을 지정할 수 있고 추가적인 옵션을 제공한다.
    
- 필드와 컬럼 맵핑

    - 종류
    
    ![field-mapping.PNG](./image/field-mapping.PNG)
   
    - @Column
    
    ![column.PNG](./image/column.PNG)
    
    - @Enumerated
    
    ![enumerated.PNG](./image/enumerated.PNG)
    
    - @Temporal
        
    ![temporal.PNG](./image/temporal.PNG)
    
    - @Lob : 데이터베이스 BLOB, CLOB 타입과 매핑 된다. 매핑하는 필드 타입이 문자면 CLOB, 그외는 BLOB이다.
    
    - @Transient : 해당 어노테이션이 있으면 필드로 매핑하지 않는다. 즉 데이터베이스에 저장하지 않는다. 클래스 내부에서 임시적으로
    사용하고 싶을때 사용한다.

- 기본기 매핑

    - @Id: 기본키로 쓰고 싶은 객체의 필드에 추가
    - @GeneratedValue: 키값을 자동으로 생성해주는데 JPA에선 4가지 전략을 제공한다.
        - IDENTITY
            - 기본키 생성 책임을 데이터베이스에게 위임한다. 주로 MySQL, PostgreSQL, SQL Server에서 사용된다.
            그런데 JPA는 보통 트랜잭션 커밋 시점에 쿼리를 호출한다. 따라서 커밋이 일어나기전에 해당 식별자를 조회할 수 없다.
            하지만 이 전략을 사용하면 persist() 시점에 즉시 쿼리를 실행함으로써 DB에서 식별자를 조회할 수 있다.
        - SEQUENCE
             - 데이터베이스 시퀀스는 유일한 값을 순서대로 생성하는 특별한 데이터베이스 오브젝트(예: 오라클 시퀀스)이고 
             주로 오라클 에서 사용한다.

                 ```
               @SequenceGenerator(
                       name = “MEMBER_SEQ_GENERATOR",
                       sequenceName = “MEMBER_SEQ", //매핑할 데이터베이스 시퀀스 이름
                       initialValue = 1, allocationSize = 1)
               public class Member {
                   
                   @Id
                   @GeneratedValue(strategy = GenerationType.SEQUENCE,
                                generator = "MEMBER_SEQ_GENERATOR")
                   private Long id;
               
               ...이하 생략
                 ```

        - TABLE
            - 키 생성 전용 테이블을 하나 만들어서 데이터베이스 시퀀스를 흉내내는 전략으로 모든 
            데이터베이스에 적용이 가능하지만 성능이 저하될 수 있는 문제가 있다.
            
                ```
              @Entity
              @TableGenerator(
                      name = "MEMBER_SEQ_GENERATOR",
                      table = "MY_SEQUENCES",
                      pkColumnValue = “MEMBER_SEQ", allocationSize = 1)
              public class Member {
                  @Id
                  @GeneratedValue(strategy = GenerationType.TABLE,
                                 generator = "MEMBER_SEQ_GENERATOR")
                  private Long id;
                ```
            
        - AUTO: 방언(H2,MySQL,ORACLE, etc)에 따라 자동 지정, 기본값
        
    - 권장하는 식별자 전략은 Long형 + 대체키 + 위의 4가지 키 생성 전략중 한가지를 조합하여 사용한다.
-----------------------

### 연관관계 맵핑
- 객체를 데이터베이스 테이블에 맞추어 모델링하면 연관된 객체를 필드에 두지 않고 외래키를 필드로 설정 함으로써
객체를 한번더 찾아야된다. -> 객체간 참조거리가 멀어진다. 따라서 연관관계 맵핑을 통해 해결한다.

- 연관관계 맵핑에는 단방향, 양방향 두 가지가 존재 한다.
    
    - 단방향 맵핑은 객체의 참조와 테이블의 외래키를 맵핑하면 된다.
        ```
          @ManyToOne
          @JoinColumn(name = "TEAM_ID")
          private Team team;
        ```
    - 양방향 맵핑은 연관된 두 엔티티에 서로 단방향 맵핑을 적용하면 된다.
         ```
          @ManyToOne
          @JoinColumn(name = "TEAM_ID")
          private Team team;
            ...
         ```
      
      ```
       @OneToMany(mappedBy = "team")
       List<Member> members = new ArrayList<Member>();
        ...
      ```
        - 양방향 맵핑 관계를 적용할 때에는 연관관계의 주인을 설정해야 하는데, 이 때 주인만이 외래키를 관리(등록, 수정)할 수 있다.
        주인이 아닌 쪽은 조회(읽기)만 가능하다. 주인이 아닌 엔티티가 mappedBy 속성을 사용한다.
        
        - 양방향 맵핑시에는 toString(), Lombok, JSON 생성 라이브러리 사용 시에 발생하는 무한루프를 고려해서 적용해야 한다.
        
        - 양방향 맵핑을 항상 설정하지 말고, 필요시 에만 설정하는 것이 좋다. 양방향은 실제 테이블의 연관관계와는 아무런 상관이 없기 때문이다.
    
- 방식
    - @ManyToOne: N:1
    
        - 외래키가 있는 곳이 연관관계의 주인이다. 보통 N 쪽이 주인이다.
        
    - @OneToMany: 1:N
    
        - 일대다 단방향은 일(1)이 연관관계의 주인이다. 그러나 테이블 일대다 관계는 항상 다(N) 쪽에 외래 키가 있음
        
        - 객체와 테이블의 차이 때문에 반대편 테이블의 외래 키를 관리하는 특이한 구조이다. 
        그렇기 때문에, @JoinColumn을 꼭 사용해야 함. 그렇지 않으면 조인 테이블 방식을 사용함(중간에 테이블을 하나 추가함)
        
        - 권장하지 않는 맵핑 방식
    
    - @OneToOne: 1:1
    
        - 주 테이블이나 대상 테이블 중에 외래 키 선택 가능하다. 외래 키에 데이터베이스 유니크(UNI) 제약조건 추가해야 한다.
        
        - 대상 테이블에 외래 키 단방향 관계는 JPA가 지원하지 않는다. 하지만 양방향은 지원한다.
        
    - @ManyToMany: N:M
    
        - 사실 관계형 데이터베이스는 정규화된 테이블 2개로 다대다를 표현할 수 없다.
        
        - 두 테이블을 연결해주는 테이블을 추가적으로 만들어서 일대다, 다대일 관계로 풀어서 결과적으로 다대다인 것처럼 맵핑시켜야 한다.
        
        -@JoinTable을 통해 추가된 테이블을 연관시켜준다.