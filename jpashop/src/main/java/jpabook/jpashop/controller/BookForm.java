package jpabook.jpashop.controller;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter @Setter
public class BookForm {

    private Long id;

    @NotEmpty(message = "이름을 입력해 주세요.")
    private String name;
    @NotNull(message = "가격을 입력해 주세요.")
    private int price;
    @NotNull(message = "재고수량을 입력해 주세요.")
    private int stockQuantity;

    private String author;
    private String isbn;


}
