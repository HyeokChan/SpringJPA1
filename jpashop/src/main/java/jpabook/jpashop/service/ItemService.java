package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    @Transactional
    public void updateItem(Long itemId, String name, int price, int stockQuantity) {
        //JPA가 관리하고 있는 영속성엔티티 (변경감지), 변경하고자하는 속성만 수정할 수 있다.
        // findItem <- 영속성엔티티
        Item findItem = itemRepository.findOne(itemId);
        /*findItem.setName(name);
        findItem.setPrice(price);
        findItem.setStockQuantity(stockQuantity);*/
        // 엔티티에서 변경하는것이 낫다.
        findItem.change(name, price, stockQuantity);
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }
}
