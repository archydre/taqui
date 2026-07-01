package com.taqui.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taqui.backend.modules.order.entity.Address;
import com.taqui.backend.modules.order.entity.Order;
import com.taqui.backend.modules.order.entity.OrderStatus;
import com.taqui.backend.modules.order.repository.OrderRepository;
import com.taqui.backend.modules.post.repository.PostRepository;
import com.taqui.backend.modules.product.entity.Product;
import com.taqui.backend.modules.product.repository.ProductRepository;
import com.taqui.backend.modules.storage.service.StorageService;
import com.taqui.backend.modules.user.entity.User;
import com.taqui.backend.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

/**
 * Base dos testes de integração. Sobe UM Postgres real (Testcontainers) compartilhado por
 * todas as classes de teste do JVM (padrão singleton: start() no bloco estático, o Ryuk do
 * Testcontainers derruba no fim) — evita o tropeço de um @Container estático ser parado pela
 * primeira classe e quebrar as seguintes. O @DynamicPropertySource aponta o datasource pro
 * container. MockMvc + spring-security-test (jwt()) batem na stack inteira com auth forjada.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected UserRepository userRepository;
    @Autowired protected ProductRepository productRepository;
    @Autowired protected PostRepository postRepository;
    @Autowired protected OrderRepository orderRepository;
    @Autowired protected PasswordEncoder passwordEncoder;

    // Sem broker nos testes: mockar evita bater no RabbitMQ (e mandar email real) no fluxo de registro.
    @MockitoBean protected RabbitTemplate rabbitTemplate;

    // Mockado pra não bater no R2 real; os testes verificam as chamadas de limpeza.
    @MockitoBean protected StorageService storageService;

    @BeforeEach
    void cleanDatabase() {
        orderRepository.deleteAll();
        postRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    /** Persiste um usuário direto no banco (senha "senha12345" já encodada), com chave Pix default. */
    protected User givenUser(String username, String whatsapp) {
        return givenUser(username, whatsapp, "chave-pix-teste");
    }

    /** Persiste um usuário direto no banco com whatsapp e chave Pix explícitos. */
    protected User givenUser(String username, String whatsapp, String pixKey) {
        User user = new User();
        user.setEmail(username + "@test.com");
        user.setUsername(username);
        user.setDisplayName(username);
        user.setPassword(passwordEncoder.encode("senha12345"));
        user.setWhatsapp(whatsapp);
        user.setPixKey(pixKey);
        return userRepository.save(user);
    }

    /** Persiste um produto de um dono. */
    protected Product givenProduct(User owner, String name, String description) {
        Product product = new Product();
        product.setProductName(name);
        product.setProductDescription(description);
        product.setPrice(BigDecimal.valueOf(20));
        product.setOwner(owner);
        return productRepository.save(product);
    }

    /** Persiste um pedido de um comprador pra um produto, com os snapshots e o status dados. */
    protected Order givenOrder(User buyer, Product product, OrderStatus status) {
        Address address = new Address();
        address.setRecipientName("Maria Compradora");
        address.setPostalCode("59600000");
        address.setStreet("Rua das Flores");
        address.setNumber("100");
        address.setDistrict("Centro");
        address.setCity("Mossoró");
        address.setState("RN");

        Order order = new Order();
        order.setBuyer(buyer);
        order.setProduct(product);
        order.setQuantity(1);
        order.setUnitPrice(product.getPrice());
        order.setFreightService("SEDEX");
        order.setFreightPrice(BigDecimal.valueOf(15));
        order.setTotal(product.getPrice().add(BigDecimal.valueOf(15)));
        order.setSellerPixKey(product.getOwner().getPixKey());
        order.setAddress(address);
        order.setStatus(status);
        return orderRepository.save(order);
    }

    /** Forja um request autenticado como o usuário (só o claim sub = userId, que os controllers leem). */
    protected RequestPostProcessor authAs(User user) {
        return jwt().jwt(token -> token.subject(user.getUserId().toString()));
    }
}
