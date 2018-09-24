package api.catalog;

import api.DBHandler;
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

	/**
	 * Specifies number of CRUD operations that the tests will carry out. This also represents the number of entries
	 * a table will have, since it limits create operations.
	 */
	private final int NUM_OF_CRUD_OPS = 100;

	/**
	 * Generates placeholder catalog items.
	 *
	 * @return Array of catalog items represented in JSON format
	 */
	private String[] generateCatalogItems() {
		String[] catalogEntries = new String[NUM_OF_CRUD_OPS];

		for (int i = 0; i < NUM_OF_CRUD_OPS; i++) {
			catalogEntries[i] = "{\"itemName\":\"Item" + i + "\",\"brand\":\"Brand" + i + "\"," +
								"\"starRating\":" + ((i % 5) + 1) + "," + "\"price\":" + i + "}";
		}

		return catalogEntries;
	}

	/**
	 * Attempts to retrieve a catalog entry with the specified ID from the database, if it exists.
	 *
	 * @param itemID ID of the catalog entry to search for
	 * @return MvcResponse containing the server response to the GET request
	 * @throws Exception
	 */
	private MvcResult findCatalogEntry(int itemID) throws Exception {
		return mockMvc.perform(
				MockMvcRequestBuilders.get("/api/catalog/" + itemID)
									  .accept(APPLICATION_JSON)
		).andReturn();
	}

	@Test
	public void readTest() {
		// Populate catalog table, so retrieval can be tested
		DBHandler.populateCatalogTable(NUM_OF_CRUD_OPS, jdbcTemplate);

		final int STATUS_OK = HttpServletResponse.SC_OK;
		String[] catalogEntries = generateCatalogItems();

		/*
		 * Tests catalog entry retrieval, by requesting a catalog entry that was previously written to the table, and
		 * comparing the server response code to the expected value (200 OK), and the returned catalog entry to the
		 * originally inserted one.
		 */
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

		// Clear catalog table for next tests to execute on a clean database
		DBHandler.clearCatalogTable(jdbcTemplate);
	}
}
