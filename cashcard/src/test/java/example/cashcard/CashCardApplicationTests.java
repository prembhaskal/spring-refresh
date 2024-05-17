package example.cashcard;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class CashCardApplicationTests {

	@Autowired
	TestRestTemplate restTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	void shouldReturnCashCardWhenDataIsSaved() {
		ResponseEntity<String> response = restTemplate.getForEntity("/cashcards/99", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		Number id = documentContext.read("$.id");
		assertThat(id).isNotNull();
		assertThat(id).isEqualTo(99);

		Double amount = documentContext.read("$.amount");
		assertThat(amount).isEqualTo(123.45);
	}

	@Test
	void shouldNotReturnACashCardWithAnUnknownId() {
		ResponseEntity<String> response = restTemplate.getForEntity("/cashcards/1000", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isBlank();
	}

	@Test
	@DirtiesContext
	void shouldCreateNewCashCard() {
		CashCard newCashCard = new CashCard(null, 250.00);
		ResponseEntity<Void> createResponse = restTemplate.postForEntity("/cashcards", newCashCard, Void.class);
		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);


		URI newCashCardLocation = createResponse.getHeaders().getLocation();
		ResponseEntity<String> getResponse = restTemplate.getForEntity(newCashCardLocation, String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		Number newId = documentContext.read("$.id");
		Double newAmount = documentContext.read("$.amount");
		assertThat(newId).isNotNull();
		assertThat(newAmount).isEqualTo(250.00);
	}

	@Test
	void shouldReturnAllCashCardsWhenListRequested() {
		ResponseEntity<String> response = restTemplate.getForEntity("/cashcards", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		// make sure to add DirtiesContext annotation else items added in another test can reflect here based on test
//		execution order
		DocumentContext docContext = JsonPath.parse(response.getBody());
		int length = docContext.read("$.length()");
		assertThat(length).isEqualTo(3);

		JSONArray ids = docContext.read("$..id");
		JSONArray amounts = docContext.read("$..amount");
		assertThat(ids).containsExactlyInAnyOrder(99, 100, 101);
		assertThat(amounts).containsExactlyInAnyOrder(123.45, 1.0, 150.0);
	}

	@Test
	void shouldReturnAPageOfCashCards() {
		ResponseEntity<String> response  = restTemplate.getForEntity("/cashcards?page=0&size=1&sort=amount,desc", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext docContext = JsonPath.parse(response.getBody());
		JSONArray page = docContext.read("$[*]");
		assertThat(page.size()).isEqualTo(1);

		double amount = docContext.read("$[0].amount");
		assertThat(amount).isEqualTo(150.00);
	}

	@Test
	void shouldReturnsOfCashCardsWithNoParametersAndUseDefaultValues() {
		ResponseEntity<String> response = restTemplate.getForEntity("/cashcards", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext docContext = JsonPath.parse(response.getBody());
		JSONArray page = docContext.read("$[*]");
		assertThat(page.size()).isEqualTo(3);

		JSONArray amounts = docContext.read("$..amount");
		assertThat(amounts).containsExactly(1.00, 123.45, 150.00);
	}
}
