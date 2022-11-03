package kitchenpos.application.product;

import static kitchenpos.fixture.ProductRequestBuilder.aProductRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import kitchenpos.domain.product.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ProductServiceTest {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductService sut;

    @Test
    @DisplayName("가격은 null일 수 없다")
    void throwException_WhenPriceNull() {
        // given
        var request = aProductRequest()
                .withPrice(null)
                .build();

        // when && then
        assertThatThrownBy(() -> sut.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("가격이 유효하지 않습니다");
    }

    @Test
    @DisplayName("가격은 음수일 수 없다")
    void throwException_WhenPriceNegative() {
        // given
        var request = aProductRequest()
                .withPrice(BigDecimal.valueOf(-1L))
                .build();

        // when && then
        assertThatThrownBy(() -> sut.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("가격이 유효하지 않습니다");
    }

    @Test
    @DisplayName("Product를 생성한다")
    void delegateSaveAndReturnSavedEntity() {
        // given
        var request = aProductRequest().build();
        // when
        var response = sut.create(request);

        // then
        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo(request.getName());
        assertThat(response.getPrice()).isEqualTo(request.getPrice());
    }

    @Test
    @DisplayName("Product 목록을 조회한다")
    void returnAllSavedEntities() {
        var expected = productRepository.findAll();

        var actual = sut.list();

        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }
}