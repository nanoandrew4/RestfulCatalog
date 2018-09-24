package api.order;

import api.DBHandler;
import api.Main;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Random;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@RunWith(SpringRunner.class)
@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = Main.class
)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext
public class OrderIntegrationTest {
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
	 * Represents maximum number of items any given order will have. Limit exists to save time in testing, but can
	 * be changed as needed.
	 */
	private final int MAX_ITEMS_PER_ORDER = 100;

	private Random rand = new Random();

	/**
	 * Generates an order with random item IDs (which exist in the database) and random quantities for each item. This
	 * method also has the option to mismatch the number of itemIDs and item quantities, which is useful when testing
	 * error codes, to malform the requests in order to trigger a 422 response by the server.
	 *
	 * @param orderID       ID of the order to be created (server will ignore it and assign its own, but for checking
	 *                      against server responses having the id in the JSON message can be useful. Lowest possible
	 *                      value is 1
	 * @param causeMismatch True to cause a different number of itemIDs and item quantities to be generated, in order
	 *                      to trigger an erroneous response from the server, false to generate an equal number of
	 *                      itemIDs and item quantities
	 * @return Randomly generated order, in JSON format, for submitting to and checking against the server
	 */
	private String genRandJSONOrder(int orderID, boolean causeMismatch) {
		int itemsInOrder = rand.nextInt(MAX_ITEMS_PER_ORDER - 1) + 1;

		// Initialize static JSON data
		StringBuilder strBuilder = new StringBuilder(
				"{\"id\":" + orderID + ",\"purchaserName\":\"TestBuyer\",\"itemIDs\":["
		);
		long[] quantities = new long[NUM_OF_CRUD_OPS];

		// Randomly generates and writes itemIDs to the JSON string
		boolean insertCommaSeparator = false;
		for (int i = 0; i < itemsInOrder; i++) {
			int itemID = rand.nextInt(NUM_OF_CRUD_OPS - 1) + 1;
			if (quantities[itemID - 1] == 0)
				strBuilder.append(insertCommaSeparator ? "," : "").append(itemID);
			quantities[itemID - 1]++;
			insertCommaSeparator = true;
		}

		/*
		 * Writes item quantities to the JSON string, unless a mismatch should be triggered, in which case the first
		 * quantity will be skipped, so that there is one less quantity value than itemIDs.
		 */
		strBuilder.append("],\"itemQuantities\":[");
		insertCommaSeparator = false;
		for (long quantity : quantities) {
			if (quantity > 0) {
				if (causeMismatch) { // Skip quantity, which will cause a mismatch
					causeMismatch = false;
					continue;
				}

				strBuilder.append(insertCommaSeparator ? "," : "").append(quantity);
				insertCommaSeparator = true;
			}
		}

		strBuilder.append("]}");
		return strBuilder.toString();
	}

	/**
	 * Generates as many random orders as CRUD operations are to be carried out, and returns them as an array of JSON
	 * strings.
	 *
	 * @return Array of orders represented as JSON strings, with as many orders as CRUD ops are to be carried out
	 */
	private String[] generateRandomOrders() {
		String[] orders = new String[NUM_OF_CRUD_OPS];

		for (int i = 0; i < NUM_OF_CRUD_OPS; i++)
			orders[i] = genRandJSONOrder(i + 1, false);

		return orders;
	}

	/**
	 * Removes the itemID field from an order JSON string. Useful for comparing server values versus original values
	 * when order updates are carried out, since IDs are not updated, so in order to check that the update was
	 * carried out successfully, the IDs need to be stripped.
	 *
	 * @param orderJSON Order JSON string
	 * @return Order JSON string without the 'id' field
	 */
	private String stripItemID(String orderJSON) {
		int pos = 0;
		for (; pos < orderJSON.length(); pos++)
			if (orderJSON.charAt(pos) == ',')
				break;
		return new StringBuilder(orderJSON).delete(1, pos).toString();
	}

	/**
	 * Submits an order to the server, for storage in the database.
	 *
	 * @param orderJSON Order to be stored in the database, in JSON format
	 * @return MvcResult containing the server response to the POST request
	 * @throws Exception
	 */
	private MvcResult createOrder(String orderJSON) throws Exception {
		return mockMvc.perform(
				MockMvcRequestBuilders.post("/api/orders/")
									  .content(orderJSON)
									  .contentType(APPLICATION_JSON)
									  .accept(APPLICATION_JSON)
		).andReturn();
	}

	/**
	 * Attempts to retrieve an order with the specified ID from the server.
	 *
	 * @param orderID ID of the order to be retrieved
	 * @return MvcResult containing the server response to the GET request
	 * @throws Exception
	 */
	private MvcResult findOrderByID(int orderID) throws Exception {
		return mockMvc.perform(
				MockMvcRequestBuilders.get("/api/orders/" + orderID)
									  .accept(APPLICATION_JSON)
		).andReturn();
	}

	/**
	 * Attempts to update (replace) an order with the specified ID, with a new order.
	 *
	 * @param orderID      ID of the order to be updated
	 * @param newOrderJSON Order to replace the old order, in JSON format
	 * @return MvcResult containing the server response to the PUT request
	 * @throws Exception
	 */
	private MvcResult updateOrder(int orderID, String newOrderJSON) throws Exception {
		return mockMvc.perform(
				MockMvcRequestBuilders.put("/api/orders/" + orderID)
									  .content(newOrderJSON)
									  .contentType(APPLICATION_JSON)
									  .accept(APPLICATION_JSON)
		).andReturn();
	}

	/**
	 * Attempts to delete an order with the specified ID.
	 *
	 * @param orderID ID of the order to tbe deleted.
	 * @return MvcResult containing the server response to the DELETE request
	 * @throws Exception
	 */
	private MvcResult deleteOrder(int orderID) throws Exception {
		return mockMvc.perform(
				MockMvcRequestBuilders.delete("/api/orders/" + orderID)
		).andReturn();
	}

	@Test
	@DirtiesContext
	public void crudTest() {
		DBHandler.populateCatalogTable(NUM_OF_CRUD_OPS, jdbcTemplate);

		String[] orders = generateRandomOrders();

		// Tests order creation by comparing server response code to the expected code (200 OK)
		for (int i = 0; i < NUM_OF_CRUD_OPS; i++) {
			MvcResult result;
			try {
				result = createOrder(orders[i]);
				TestCase.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
			} catch (Exception e) {
				e.printStackTrace();
				TestCase.fail("Exception occurred while creating order with ID: " + (i + 1));
			}
		}

		// Tests order retrieval by comparing server response code to the expected code (200 OK)
		for (int i = 0; i < NUM_OF_CRUD_OPS; i++) {
			MvcResult result;
			int orderID = i + 1;
			try {
				result = findOrderByID(orderID);
				TestCase.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
				TestCase.assertEquals(orders[i], result.getResponse().getContentAsString());
			} catch (Exception e) {
				e.printStackTrace();
				TestCase.fail("Exception occurred while reading order with ID: " + orderID);
			}
		}

		/*
		 * Tests order update by comparing server response code to the expected code (200 OK). Update reverses the
		 * orders in the database, so order with ID 1 will contain what the order with ID 100 previously contained.
		 *
		 * Once the orders are updated, they are retrieved and compared to the original orders that were submitted,
		 * in reverse order, to check that the update worked correctly and the table was successfully reversed. IDs are
		 * stripped for this check, since the original orders have the IDs in ascending order, while the updated orders
		 * will return IDs in descending order.
		 */
		for (int i = 0; i < NUM_OF_CRUD_OPS; i++) {
			MvcResult result;
			int orderID = i + 1;
			try { // Reverses order of entries in the 'orders' table
				result = updateOrder(orderID, orders[NUM_OF_CRUD_OPS - orderID]);
				TestCase.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
			} catch (Exception e) {
				e.printStackTrace();
				TestCase.fail("Exception occurred while updating order with ID: " + orderID);
			}

			// Verify that updates were fully carried out
			MvcResult getUpdatedResult;
			try {
				getUpdatedResult = findOrderByID(orderID);
				TestCase.assertEquals(HttpStatus.OK.value(), getUpdatedResult.getResponse().getStatus());
				TestCase.assertEquals(
						stripItemID(orders[NUM_OF_CRUD_OPS - orderID]),
						stripItemID(getUpdatedResult.getResponse().getContentAsString())
				);
			} catch (Exception e) {
				e.printStackTrace();
				TestCase.fail("Exception occurred while reading updated order with ID: " + orderID);
			}
		}

		// Tests order deletion by comparing server response code to the expected code (200 OK)
		for (int i = 0; i < NUM_OF_CRUD_OPS; i++) {
			MvcResult result;
			int orderID = i + 1;
			try {
				result = deleteOrder(orderID);
				TestCase.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
			} catch (Exception e) {
				e.printStackTrace();
				TestCase.fail("Exception occurred while deleting order with ID: " + orderID);
			}
		}

		// Clear catalog table for next tests to execute on a clean database
		DBHandler.clearCatalogTable(jdbcTemplate);
	}

	@Test
	@DirtiesContext
	public void outOfBoundsTest() {

		/*
		 * Tests server response to creating order with itemIDs that do not exist in the catalog, which should be
		 * (422 Unprocessable Entity). No orders should be created successfully.
		 */
		for (int i = 0; i < NUM_OF_CRUD_OPS; i++) {
			try {
				MvcResult result = createOrder(genRandJSONOrder(i + 1, false));
				TestCase.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
			} catch (Exception e) {
				e.printStackTrace();
				TestCase.fail("Exception occurred while creating an order");
			}
		}

		// Init catalog in order to create orders, for the next part of the test
		DBHandler.populateCatalogTable(NUM_OF_CRUD_OPS, jdbcTemplate);

		// Create orders to be updated
		for (int i = 0; i < NUM_OF_CRUD_OPS; i++) {
			try {
				createOrder(genRandJSONOrder(i + 1, false));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Clear catalog table, so no update is valid, for the next part of the test
		DBHandler.clearCatalogTable(jdbcTemplate);

		/*
		 * Attempts to update existing orders with itemIDs that do not exist (since the catalog is empty), which tests
		 * the server response to such a scenario, which should be (422 Unprocessable entity). No orders should be
		 * updated successfully.
		 */
		for (int i = 0; i < NUM_OF_CRUD_OPS; i++) {
			try {
				int itemID = i + 1;
				MvcResult result = updateOrder(itemID, genRandJSONOrder(itemID, false));
				TestCase.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
			} catch (Exception e) {
				e.printStackTrace();
				TestCase.fail("Exception occurred while updating an order");
			}
		}

		// Clear orders table for next tests to execute on a clean database
		DBHandler.clearOrdersTable(jdbcTemplate);
	}

	@Test
	@DirtiesContext
	public void itemIDAndQuantityMismatchTest() {
		// Populate catalog table so that orders have valid itemIDs
		DBHandler.populateCatalogTable(NUM_OF_CRUD_OPS, jdbcTemplate);

		/*
		 * Test server response to creating an order with an unequal number of itemIDs and item quantities, which
		 * should be (422 Unprocessable Entity). No orders should be created.
		 */
		for (int i = 0; i < NUM_OF_CRUD_OPS; i++) {
			try {
				MvcResult result = createOrder(genRandJSONOrder(i + 1, true));
				TestCase.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
			} catch (Exception e) {
				e.printStackTrace();
				TestCase.fail("Exception occurred while creating an order");
			}
		}

		// Create random orders so that the next section of the test can attempt to update them
		for (int i = 0; i < NUM_OF_CRUD_OPS; i++) {
			try {
				createOrder(genRandJSONOrder(i + 1, false));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/*
		 * Test server response to updating an order, where the new order has an unequal number of itemIDs and item
		 * quantities. Response code should be (422 Unprocessable Entity)
		 */
		for (int i = 0; i < NUM_OF_CRUD_OPS; i++) {
			try {
				int itemID = i + 1;
				MvcResult result = updateOrder(itemID, genRandJSONOrder(itemID, true));
				TestCase.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
			} catch (Exception e) {
				e.printStackTrace();
				TestCase.fail("Exception occurred while updating an order");
			}
		}

		// Clear catalog table for next tests to execute on a clean database
		DBHandler.clearCatalogTable(jdbcTemplate);
	}
}
