# JPA 활용1

## 개발 / 학습 환경
- Project : Gradle Project
- Spring Boot : 2.3
- Language : Java
- Pajaging : Jar
- Java : 11
***
## 도메인 분석 설계
### 요구사항 분석
- 회원 기능 : 회원 등록, 회원 조회 
- 상품 기능 : 상품 등록, 상품 수정, 상품 조회 
- 주문 기능 : 상품 주문, 주문 내역 조회, 주문 취소 
- 기타 요구사항 : 
	- 상품은 재고 관리가 필요하다. 
	- 상품의 종류는 도서, 음반, 영화가 있다. 
	- 상품을 카테고리로 구분할 수 있다. 
	- 상품 주문시 배송 정보를 입력할 수 있다
*** 
### 엔티티 분석
![image](https://user-images.githubusercontent.com/48059565/152670458-6ffca20b-847f-41ed-b57e-57b23f8ffdd8.png)
- 회원(Member): 이름과 임베디드 타입인 주소( Address ), 그리고 주문( orders ) 리스트를 가진다
- 주문(Order): 한 번 주문시 여러 상품을 주문할 수 있으므로 주문과 주문상품( OrderItem )은 일대다 관계다. 주문은 상품을 주문한 회원과 배송 정보, 주문 날짜, 주문 상태( status )를 가지고 있다. 주문 상태는 열거형을 사용했는데 주문( ORDER ), 취소( CANCEL )을 표현할 수 있다
- 주문상품(OrderItem): 주문한 상품 정보와 주문 금액( orderPrice ), 주문 수량( count ) 정보를 가지고 있다.
- 상품(Item): 이름, 가격, 재고수량( stockQuantity )을 가지고 있다. 상품을 주문하면 재고수량이 줄어든다. 상품의 종류로는 도서, 음반, 영화가 있는데 각각은 사용하는 속성이 조금씩 다르다.
- 배송(Delivery): 주문시 하나의 배송 정보를 생성한다. 주문과 배송은 일대일 관계다.
- 카테고리(Category): 상품과 다대다 관계를 맺는다. parent , child 로 부모, 자식 카테고리를 연결한다.
- 주소(Address): 값 타입(임베디드 타입)이다. 회원과 배송(Delivery)에서 사용한다.
#### *참고 :  회원이 주문을 하기 때문에, 회원이 주문리스트를 가지는 것으로 설계를 잘한것 같지만  주문에서만 회원을 참조하고 있어도 관계확인이 가능하기 때문에 실무에서는 회원이 주문을 참조하지 않고, 주문이 회원을 참조한는 것으로 충분하다.*
***
### 테이블 분석
![image](https://user-images.githubusercontent.com/48059565/152670662-66f38a61-6d6e-46ed-b0ca-211147623134.png)
- MEMBER: 회원 엔티티의 Address 임베디드 타입 정보가 회원 테이블에 그대로 들어갔다. DELIVERY 테이블도 마찬가지다.
- ITEM: 앨범, 도서, 영화 타입을 통합해서 하나의 테이블로 만들었다. DTYPE 컬럼으로 타입을 구분한다.
***

### 연관관계 매핑 분석 👍
- 회원과 주문 : 
일대다 , 다대일의 양방향 관계다. 따라서 연관관계의 주인을 정해야 하는데, 
**외래 키가 있는 주문을 연관관계의 주인**으로 정하는 것이 좋다. 
그러므로 Order.member 를 ORDERS.MEMBER_ID 외래 키와 매핑한다. 
- 주문상품과 주문: 
다대일 양방향 관계다. **외래 키가 주문상품에 있으므로 주문상품이 연관관계의 주인**이다. 
그러므로 OrderItem.order 를 ORDER_ITEM.ORDER_ID 외래 키와 매핑한다. 
- 주문상품과 상품: 
다대일 단방향 관계다. **주문상품이 연관관계의 주인**이다.
OrderItem.item 을 ORDER_ITEM.ITEM_ID 외래 키와 매핑한 다. 
- 주문과 배송: 
일대일 양방향 관계다. Order.delivery 를 ORDERS.DELIVERY_ID 외래 키와 매핑한다. 
- 카테고리와 상품: 
@ManyToMany 를 사용해서 매핑한다.
**(실무에서 @ManyToMany는 사용하지 말자.** 여기 서는 다대다 관계를 예제로 보여주기 위해 추가했을 뿐이다
#### *참고 : 외래 키가 있는 곳을 연관관계의 주인으로 정해라. > 연관관계의 주인은 단순히 외래 키를 누가 관리하냐의 문제이지 비즈니스상 우위에 있다고 주인으로 정하면 안된다. 예를 들어서 자동차와 바퀴가 있으면, 일대다 관계에서 항상 다쪽에 외래 키가 있으므로 외래 키가 있는 바퀴를 연관관계의 주인으로 정하면 된다. 물론 자동차를 연관관계의 주인으로 정하는 것이 불가능 한 것은 아니지만, 자동차를 연관관계의 주인으로 정하면 자동차가 관리하지 않는 바퀴 테이블의 외래 키 값이 업데이트 되므로 관리와 유지보수가 어렵고, 추가적으로 별도의 업데이트 쿼리가 발생하는 성능 문제도 있다.*
***
## 엔티티 클래스 개발
#### - 예제에서는 설명을 쉽게하기 위해 엔티티 클래스에 Getter, Setter를 모두 열고, 최대한 단순하게 설계 
#### - 실무에서는 가급적 Getter는 열어두고, Setter는 꼭 필요한 경우에만 사용하는 것을 추천
#### 참고: 이론적으로 Getter, Setter 모두 제공하지 않고, 꼭 필요한 별도의 메서드를 제공하는게 가장 이상적 이다. 하지만 실무에서 엔티티의 데이터는 조회할 일이 너무 많으므로, Getter의 경우 모두 열어두는 것이 편리하다. Getter는 아무리 호출해도 호출 하는 것 만으로 어떤 일이 발생하지는 않는다. 하지만 Setter는 문제가 다르다. Setter를 호출하면 데이터가 변한다. Setter를 막 열어두면 가까운 미래에 엔티티에가 도대 체 왜 변경되는지 추적하기 점점 힘들어진다. 그래서 엔티티를 변경할 때는 Setter 대신에 변경 지점이 명확 하도록 변경을 위한 비즈니스 메서드를 별도로 제공해야 한다.
```
//비즈니스로직 추가
public void addStock(int quantity) { 
	this.stockQuantity += quantity; 
} 
public void removeStock(int quantity) { 
	int restStock = this.stockQuantity - quantity; 
	if (restStock < 0) { 
	throw new NotEnoughStockException("need more stock"); 
	} 
	this.stockQuantity = restStock; 
}
```
**비즈니스 로직을 entitiy에 구현하여 변경 감지로 인해 데이터가 수정되도록 한다. (도메인 모델 패턴)**

## 엔티티 설계시 주의점
1. 엔티티에는 가급적 Setter를 사용하지 말자
엔티티값을 변경하는 곳이 너무 많아져서 유지보수가 힘들어진다.
2. 모든 연관관계는 지연로딩(LAZY)으로 설정하자
- 즉시로딩(EAGER)은 예측이 어렵고 어떤 sql이 실행될지 추적하기 어렵다. JPQL을 실행할 때 1+N 문제가 자주 발생한다.
- 실무에서 모든 연관관계는 지연로딩으로 설정해야한다.
-  연관된 엔티티를 함께 DB에서 조회해야 하면, fetch join 또는 엔티티 그래프 기능을 사용한다.
- @XToOne 관계는 기본이 즉시로딩이므로 직접 지연로딩으로 설정해야 한다.
	```
	@ManyToOne(fetch = LAZY)  
	@JoinColumn(name = "item_id")  
	private Item item;
	```
3. 컬렉션은 필드에서 초기화 하자.
***
## 도메인 개발
### 리포지토리 개발
```
@Repository

@PersistenceContext private EntityManager em;
em.persist(member);
return em.createQuery("select m from Member m where m.name = :name", Member.class) 
		 .setParameter("name", name) 
         .getResultList();
```
- @Repository : 스프링 빈으로 등록, JPA 예외를 스프링 기반 예외로 예외 변환
- @PersistenceContext : 엔티티 메니저( EntityManager ) 주입
***
### 서비스 개발
```
@Service 
@Transactional(readOnly = true)

@Transactional //변경 (readonly = false)
public Long join(Member member) {...}
```
- @Transactional : 트랜잭션, 영속성 컨텍스트 		
	- readOnly=true : 데이터의 변경이 없는 읽기 전용 메서드에 사용, 영속성 컨텍스트를 플러시 하지 않으므로 약간의 성능 향상(읽기 전용에는 다 적용) 
	- 데이터베이스 드라이버가 지원하면 DB에서 성능 향상
***
### 테스트 코드
```
@RunWith(SpringRunner.class) 
@SpringBootTest 
@Transactional
@Test(expected = IllegalStateException.class)
```
- @RunWith(SpringRunner.class) : 스프링과 테스트 통합 
- @SpringBootTest : 스프링 부트 띄우고 테스트(이게 없으면 @Autowired 다 실패) 
- @Transactional : 반복 가능한 테스트 지원, 각각의 테스트를 실행할 때마다 트랜잭션을 시작하고 테스트 가 끝나면 트랜잭션을 강제로 롤백 (이 어노테이션이 테스트 케이스에서 사용될 때만 롤백)
***
## JPA 동적 쿼리
- JPQL로 처리
- JPA Criteria로 처리
**JPQL은 동적쿼리를 처리하기 위한 코드가 너무 복잡해지고 Criteria는 작성된 코드를 보고 쿼리를 떠올리기 힘들다.**
**따라서 동적쿼리 및 복작한 정적쿼리는 Querydsl로 처리한다.**
***
## 웹 계층 개발
#### 참고: 폼 객체 vs 엔티티 직접 사용
#### 참고: 요구사항이 정말 단순할 때는 폼 객체( MemberForm ) 없이 엔티티( Member )를 직접 등록과 수정 화면 에서 사용해도 된다. 하지만 화면 요구사항이 복잡해지기 시작하면, 엔티티에 화면을 처리하기 위한 기능이 점점 증가한다. 결과적으로 엔티티는 점점 화면에 종속적으로 변하고, 이렇게 화면 기능 때문에 지저분해진 엔티티는 결국 유지보수하기 어려워진다. 
#### 실무에서 엔티티는 핵심 비즈니스 로직만 가지고 있고, 화면을 위한 로직은 없어야 한다. 화면이나 API에 맞 는 폼 객체나 DTO를 사용하자. 그래서 화면이나 API 요구사항을 이것들로 처리하고, 엔티티는 최대한 순수 하게 유지하자.
***
## 변경감지와 병합(merge)
#### 준영속엔티티 : 영속성 컨텍스트가 더는 관리하지 않는 엔티티를 말한다.
#### 준영속 엔티티를 수정하는 2가지 방법 
1.변경 감지 기능
```
@Transactional 
void update(Item itemParam) { 
	//itemParam: 파리미터로 넘어온 준영속 상태의 엔티티 
	Item findItem = em.find(Item.class, itemParam.getId()); //같은 엔티티를 조회한 다. 
	findItem.setPrice(itemParam.getPrice()); //데이터를 수정한다. 
}
```
영속성 컨텍스트에서 엔티티를 다시 조회한 후에 데이터를 수정하는 방법 트랜잭션 안에서 엔티티를 다시 조회, 변경할 값 선택 트랜잭션 커밋 시점에 변경 감지(Dirty Checking) 이 동작해서 데이터베이스에 UPDATE SQL 실행
2. 병합(merge)
```
@Transactional 
void update(Item itemParam) { 
	//itemParam: 파리미터로 넘어온 준영속 상태의 엔티티 
	Item mergeItem = em.merge(item); 
}
```
파라미터로 받지 않은 속성이 있는 경우 그 컬럼은 null로 update한다 -> 따라서 변경감지 기능을 사용해야 한다.
***
