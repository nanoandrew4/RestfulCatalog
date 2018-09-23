package api.catalog;

import api.Main;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.servlet.http.HttpServletResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@RunWith(SpringRunner.class)
@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = Main.class
)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext
public class CatalogIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private final int NUM_OF_CRUD_OPS = 100;

	private String[] generateCatalogItems() {
		String[] catalogEntries = new String[NUM_OF_CRUD_OPS];

		for (int i = 0; i < NUM_OF_CRUD_OPS; i++) {
			catalogEntries[i] = "{\"itemName\":\"Item" + i + "\",\"brand\":\"Brand" + i + "\"," +
								"\"starRating\":" + ((i % 5) + 1) + "," + "\"price\":" + i + "," +
								"\"quantity\":" + i +
								"}";
		}

		return catalogEntries;
	}

	private MvcResult findCatalogEntry(int itemID) throws Exception {
		return mockMvc.perform(
				MockMvcRequestBuilders.get("/api/catalog/" + itemID)
									  .accept(APPLICATION_JSON)
		).andReturn();
	}

	private void initCatalogDatabase() {
		for (int i = 0; i < NUM_OF_CRUD_OPS; i++) {
			jdbcTemplate.update(
					"insert into catalog (item_name, brand, star_rating, price, quantity) values (?,?,?,?,?)",
					"Item" + i, "Brand" + i, ((i % 5) + 1), i, i
			);
		}
	}

	private void emptyCatalogDatabase() {
		jdbcTemplate.update("truncate table catalog");
	}

	@Test
	public void crudTest() {
		initCatalogDatabase();

		final int STATUS_OK = HttpServletResponse.SC_OK;
		String[] catalogEntries = generateCatalogItems();

		// Read
		for (int i = 0; i < NUM_OF_CRUD_OPS; i++) {
			MvcResult result;
			try {
				result = findCatalogEntry(i + 1);
				TestCase.assertEquals(STATUS_OK, result.getResponse().getStatus());
				TestCase.assertEquals(catalogEntries[i], result.getResponse().getContentAsString());
			} catch (Exception e) {
				e.printStackTrace();
				TestCase.fail("Exception occurred while reading catalog table");
			}
		}

		emptyCatalogDatabase();
	}
}
