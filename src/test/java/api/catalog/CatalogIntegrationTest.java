package api.catalog;

import api.Main;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

	private MvcResult generateCatalogEntry(String catalogEntryJSON) throws Exception {
		return mockMvc.perform(
				MockMvcRequestBuilders.post("/api/catalog/")
									  .content(catalogEntryJSON)
									  .contentType(APPLICATION_JSON)
									  .accept(APPLICATION_JSON)
		).andReturn();
	}

	private MvcResult findCatalogEntry(int itemID) throws Exception {
		return mockMvc.perform(
				MockMvcRequestBuilders.get("/api/catalog/" + itemID)
									  .accept(APPLICATION_JSON)
		).andReturn();
	}

	private MvcResult updateCatalogEntry(int itemID, String newCatalogEntryJSON) throws Exception {
		return mockMvc.perform(
				MockMvcRequestBuilders.put("/api/catalog/" + itemID)
									  .content(newCatalogEntryJSON)
									  .contentType(APPLICATION_JSON)
									  .accept(APPLICATION_JSON)
		).andReturn();
	}

	private MvcResult deleteCatalogEntry(int itemID) throws Exception {
		return mockMvc.perform(
				MockMvcRequestBuilders.delete("/api/catalog/" + itemID)
		).andReturn();
	}

	@Test
	public void crudTest() throws Exception {
		final int STATUS_OK = HttpServletResponse.SC_OK;
		String[] catalogEntries = generateCatalogItems();

		// Create
		for (int i = 0; i < NUM_OF_CRUD_OPS; i++) {
			MvcResult result = generateCatalogEntry(catalogEntries[i]);
			TestCase.assertEquals(STATUS_OK, result.getResponse().getStatus());
		}

		// Read
		for (int i = 0; i < NUM_OF_CRUD_OPS; i++) {
			MvcResult result = findCatalogEntry(i + 1);
			TestCase.assertEquals(STATUS_OK, result.getResponse().getStatus());
			TestCase.assertEquals(catalogEntries[i], result.getResponse().getContentAsString());
		}

		// Update
		for (int i = 0; i < NUM_OF_CRUD_OPS; i++) { // Reverses order of entries in the database
			MvcResult result = updateCatalogEntry(i + 1, catalogEntries[NUM_OF_CRUD_OPS - 1 - i]);
			TestCase.assertEquals(STATUS_OK, result.getResponse().getStatus());

			MvcResult getUpdatedResult = findCatalogEntry(i + 1);
			TestCase.assertEquals(STATUS_OK, getUpdatedResult.getResponse().getStatus());
			TestCase.assertEquals(catalogEntries[NUM_OF_CRUD_OPS - 1 - i], getUpdatedResult.getResponse().getContentAsString());
		}

		// Delete
		for (int i = 0; i < NUM_OF_CRUD_OPS; i++) {
			MvcResult result = deleteCatalogEntry(i + 1);
			TestCase.assertEquals(STATUS_OK, result.getResponse().getStatus());
		}
	}
}
