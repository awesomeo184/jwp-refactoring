package kitchenpos.application;

import static kitchenpos.fixture.MenuGroupFactory.createMenuGroup;
import static kitchenpos.fixture.ProductBuilder.aProduct;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import kitchenpos.dao.MenuDao;
import kitchenpos.dao.MenuGroupDao;
import kitchenpos.dao.MenuProductDao;
import kitchenpos.dao.ProductDao;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuProduct;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MenuServiceTest {

    @Autowired
    MenuDao menuDao;

    @Autowired
    MenuGroupDao menuGroupDao;

    @Autowired
    MenuProductDao menuProductDao;

    @Autowired
    ProductDao productDao;

    @Autowired
    MenuService sut;

    @Test
    @DisplayName("Menu의 가격은 null일 수 없다")
    void throwException_WhenPriceNull() {
        // given
        Menu menu = new Menu();
        menu.setName("강정치킨");
        menu.setMenuGroupId(1L);
        menu.setMenuProducts(new ArrayList<>());

        // when && then
        assertThatThrownBy(() -> sut.create(menu))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Menu의 가격은 음수일 수 없다")
    void throwException_WhenPriceNegative() {
        // given
        Menu menu = new Menu();
        menu.setName("강정치킨");
        menu.setPrice(BigDecimal.valueOf(-1L));
        menu.setMenuGroupId(1L);
        menu.setMenuProducts(new ArrayList<>());

        // when && then
        assertThatThrownBy(() -> sut.create(menu))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Menu의 MenuGroupId가 존재하지 않으면 Menu를 생성할 수 없다")
    void throwException_WhenGivenNonExistMenuGroupId() {
        // given
        Menu menu = new Menu();
        menu.setName("강정치킨");
        menu.setMenuGroupId(0L);
        menu.setPrice(BigDecimal.valueOf(1000L));
        menu.setMenuProducts(new ArrayList<>());

        // when && when
        assertThatThrownBy(() -> sut.create(menu))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Menu에 포함된 Product가 존재하지 않으면 Menu를 생성할 수 없다")
    void throwException_WhenGivenNonExistMenuProductId() {
        // given
        Long menuGroupId = menuGroupDao.save(createMenuGroup()).getId();

        Menu menu = new Menu();
        menu.setName("강정치킨");
        menu.setMenuGroupId(menuGroupId);
        menu.setPrice(BigDecimal.valueOf(1000L));

        List<MenuProduct> menuProducts = new ArrayList<>();
        MenuProduct menuProduct = new MenuProduct();
        menuProduct.setProductId(0L);
        menuProducts.add(menuProduct);
        menu.setMenuProducts(menuProducts);

        // when && when
        assertThatThrownBy(() -> sut.create(menu))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Menu의 가격과 Menu에 포함된 Product 가격의 합이 달라서는 안된다")
    void throwException_WhenMenuPriceAndSumOfMenuProductPrice_NotMatch() {
        // given
        final BigDecimal PRODUCT_PRICE = BigDecimal.valueOf(1000L);
        final long QUANTITY = 2;
        final BigDecimal WRONG_MENU_PRICE = PRODUCT_PRICE.multiply(BigDecimal.valueOf(QUANTITY))
                .add(BigDecimal.valueOf(1000L));

        Long productId = productDao.save(
                aProduct()
                        .withPrice(PRODUCT_PRICE)
                        .build()
        ).getId();

        MenuProduct menuProduct = new MenuProduct();
        menuProduct.setProductId(productId);
        menuProduct.setQuantity(QUANTITY);
        List<MenuProduct> menuProducts = List.of(menuProduct);

        Menu menu = new Menu();
        menu.setMenuGroupId(1L);
        menu.setPrice(WRONG_MENU_PRICE);
        menu.setMenuProducts(menuProducts);

        // when && then
        assertThatThrownBy(() -> sut.create(menu))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("메뉴를 생성한다")
    void saveMenu() {
        // given
        Long productId = productDao.save(aProduct().build()).getId();

        Long menuGroupId = menuGroupDao.save(createMenuGroup()).getId();

        MenuProduct menuProduct = new MenuProduct();
        menuProduct.setProductId(productId);
        menuProduct.setQuantity(2);
        List<MenuProduct> menuProducts = List.of(menuProduct);

        Menu menu = new Menu();
        menu.setName("강정치킨");
        menu.setMenuGroupId(menuGroupId);
        menu.setPrice(BigDecimal.valueOf(2000L));
        menu.setMenuProducts(menuProducts);

        // when
        Menu savedMenu = sut.create(menu);

        // then
        assertThat(savedMenu).isNotNull();
        assertThatMenuIdIsSet(savedMenu.getMenuProducts(), savedMenu.getId());
    }

    @Test
    @DisplayName("Menu 목록을 조회한다")
    void listMenus() {
        List<Menu> expected = menuDao.findAll();

        List<Menu> menus = sut.list();

        assertThat(menus).isEqualTo(expected);
        for (Menu menu : menus) {
            assertThatMenuIdIsSet(menu.getMenuProducts(), menu.getId());
        }
    }

    private void assertThatMenuIdIsSet(List<MenuProduct> menuProducts, Long menuId) {
        for (MenuProduct menuProduct : menuProducts) {
            assertThat(menuProduct.getMenuId()).isEqualTo(menuId);
        }
    }
}
