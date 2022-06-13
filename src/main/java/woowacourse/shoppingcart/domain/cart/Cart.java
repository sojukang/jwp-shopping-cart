package woowacourse.shoppingcart.domain.cart;

import java.util.ArrayList;
import java.util.List;

import woowacourse.shoppingcart.exception.InvalidCartItemException;

public class Cart {

    private final List<CartItem> cartItems;

    public Cart(List<CartItem> cartItems) {
        this.cartItems = new ArrayList<>(cartItems);
    }

    public void addCartItem(Product product, int count) {
        checkAlreadyInCart(product);
        checkInStock(product, count);
        cartItems.add(new CartItem(count, product));
    }

    private void checkAlreadyInCart(Product product) {
        if (isInCart(product)) {
            throw new InvalidCartItemException("이미 담겨있는 상품입니다.");
        }
    }

    private boolean isInCart(Product product) {
        return cartItems.stream().
            anyMatch(cartItem -> cartItem.isProductOf(product));
    }

    private void checkInStock(Product product, int count) {
        if (count > product.getQuantity()) {
            throw new InvalidCartItemException("재고가 부족합니다.");
        }
    }

    public void update(Product product, int count) {
        checkInStock(product, count);
        CartItem cartItem = getItemOf(product);
        CartItem cartItemToUpdate = new CartItem(count, product);
        int index = cartItems.indexOf(cartItem);
        cartItems.set(index, cartItemToUpdate);
    }

    public CartItem getItemOf(Product product) {
        return cartItems.stream()
            .filter(cartItem -> cartItem.isProductOf(product))
            .findFirst()
            .orElseThrow(() -> new InvalidCartItemException("카트에 담겨있지 않은 상품입니다."));
    }

    public boolean isEmpty() {
        return cartItems.isEmpty();
    }

    public List<CartItem> getCartItems() {
        return List.copyOf(cartItems);
    }
}
